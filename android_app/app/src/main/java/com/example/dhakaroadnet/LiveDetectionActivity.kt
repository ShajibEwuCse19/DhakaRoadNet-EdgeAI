package com.example.dhakaroadnet

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.util.Size
import android.view.Surface
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.example.dhakaroadnet.databinding.ActivityLiveDetectionBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.roundToInt

class LiveDetectionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLiveDetectionBinding
    private lateinit var detector: DhakaRoadNetDetector

    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private val inferenceRunning = AtomicBoolean(false)
    private val analysisEnabled = AtomicBoolean(false)
    private var cameraProvider: ProcessCameraProvider? = null
    private var analysisUseCase: ImageAnalysis? = null
    private var isPaused = false
    private var isRequestingCameraPermission = false
    private var isStartingCamera = false
    private var permissionDialogShowing = false
    private var hasRequestedCameraPermission = false
    private var cameraStartRequestId = 0
    private var lastResultAtMs = 0L
    private var lastAnalyzedAtMs = 0L
    private var confidenceThreshold = DhakaRoadNetDetector.DEFAULT_CONFIDENCE

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        isRequestingCameraPermission = false
        if (granted) {
            startCamera()
        } else {
            showCameraPermissionDialog()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLiveDetectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        detector = DhakaRoadNetDetector(this, DhakaRoadNetDetector.LIVE_THREAD_COUNT)
        binding.previewView.scaleType = PreviewView.ScaleType.FIT_CENTER
        updateConfidenceText()

        bindActions()
        setupBackPressHandling()
        requestCameraIfNeeded()
    }

    private fun bindActions() {
        binding.backButton.setOnClickListener { finish() }
        binding.pauseButton.setOnClickListener { toggleAnalysisPaused() }
        binding.decreaseConfidenceButton.setOnClickListener { adjustConfidence(-CONFIDENCE_STEP) }
        binding.increaseConfidenceButton.setOnClickListener { adjustConfidence(CONFIDENCE_STEP) }
    }

    private fun setupBackPressHandling() {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finish()
                }
            }
        )
    }

    private fun requestCameraIfNeeded() {
        if (hasCameraPermission()) {
            startCamera()
        } else {
            binding.liveStatusText.text = "Camera permission is off"
            binding.liveHintText.text = "Allow Camera permission to start live road-object detection."
            if (!hasRequestedCameraPermission) {
                hasRequestedCameraPermission = true
                isRequestingCameraPermission = true
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            } else {
                showCameraPermissionDialog()
            }
        }
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun showCameraPermissionDialog() {
        if (permissionDialogShowing || isFinishing || isDestroyed) return

        binding.liveStatusText.text = "Camera permission is off"
        binding.liveHintText.text = "Open app settings and allow Camera permission to use live detection."
        permissionDialogShowing = true

        MaterialAlertDialogBuilder(this)
            .setTitle("Camera permission required")
            .setMessage("Live detection cannot start because Android has blocked camera access for this app. Open Settings, choose Permissions, and allow Camera.")
            .setNegativeButton("Back") { _, _ -> finish() }
            .setNeutralButton("Try again") { _, _ -> requestCameraIfNeeded() }
            .setPositiveButton("Open Settings") { _, _ ->
                openAppSettings()
            }
            .show()
            .setOnDismissListener { permissionDialogShowing = false }
    }

    private fun openAppSettings() {
        val settingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(settingsIntent)
    }

    private fun startCamera() {
        if (isStartingCamera) return
        isStartingCamera = true
        val requestId = ++cameraStartRequestId
        binding.liveStatusText.text = "Starting camera..."
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(
            {
                try {
                    if (
                        requestId != cameraStartRequestId ||
                        !hasCameraPermission() ||
                        isFinishing ||
                        isDestroyed
                    ) {
                        return@addListener
                    }
                    val provider = cameraProviderFuture.get()
                    cameraProvider = provider
                    bindCameraUseCases(provider)
                } catch (exception: Exception) {
                    showCameraError(exception)
                } finally {
                    isStartingCamera = false
                }
            },
            ContextCompat.getMainExecutor(this)
        )
    }

    private fun bindCameraUseCases(provider: ProcessCameraProvider) {
        val preview = Preview.Builder()
            .setTargetRotation(binding.previewView.display?.rotation ?: Surface.ROTATION_0)
            .build()
            .also { preview ->
                preview.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

        val analysisResolutionSelector = ResolutionSelector.Builder()
            .setResolutionStrategy(
                ResolutionStrategy(
                    Size(640, 480),
                    ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                )
            )
            .build()

        val analysis = ImageAnalysis.Builder()
            .setResolutionSelector(analysisResolutionSelector)
            .setTargetRotation(binding.previewView.display?.rotation ?: Surface.ROTATION_0)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { imageAnalysis ->
                imageAnalysis.setAnalyzer(cameraExecutor) { image ->
                    analyzeFrame(image)
                }
            }

        provider.unbindAll()
        provider.bindToLifecycle(
            this,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            analysis
        )

        analysisUseCase = analysis
        analysisEnabled.set(true)
        lastAnalyzedAtMs = 0L
        lastResultAtMs = 0L

        binding.liveStatusText.text = "Live detection ready"
    }

    private fun analyzeFrame(image: ImageProxy) {
        if (
            !analysisEnabled.get() ||
            isPaused ||
            shouldSkipFrame() ||
            !inferenceRunning.compareAndSet(false, true)
        ) {
            image.close()
            return
        }

        var imageClosed = false
        var bitmap: Bitmap? = null

        try {
            bitmap = LiveFrameConverter.toBitmap(image)
            image.close()
            imageClosed = true

            if (!analysisEnabled.get() || isPaused) return

            val output = detector.detect(bitmap, confidenceThreshold)
            val fps = calculateFps()
            runOnUiThread {
                if (!analysisEnabled.get() || isFinishing || isDestroyed) return@runOnUiThread
                binding.detectionOverlay.setOutput(output)
                binding.liveStatusText.text = buildLiveStatus(output, fps)
            }
        } catch (exception: Exception) {
            runOnUiThread {
                if (!analysisEnabled.get() || isFinishing || isDestroyed) return@runOnUiThread
                binding.liveStatusText.text = "Live detection problem: ${exception.message}"
            }
        } finally {
            bitmap?.recycle()
            if (!imageClosed) {
                image.close()
            }
            inferenceRunning.set(false)
        }
    }

    private fun stopCameraUseCases() {
        analysisEnabled.set(false)
        analysisUseCase?.clearAnalyzer()
        analysisUseCase = null
        cameraProvider?.unbindAll()
        cameraProvider = null
        isStartingCamera = false
        cameraStartRequestId++
        lastResultAtMs = 0L
        lastAnalyzedAtMs = 0L
        if (::binding.isInitialized) {
            binding.detectionOverlay.setOutput(null)
        }
    }

    private fun closeDetectorOnAnalyzerThread() {
        if (!::detector.isInitialized) return

        try {
            cameraExecutor.execute { detector.close() }
        } catch (_: RejectedExecutionException) {
            detector.close()
        } finally {
            cameraExecutor.shutdown()
        }
    }

    private fun shouldSkipFrame(): Boolean {
        val now = SystemClock.elapsedRealtime()
        if (now - lastAnalyzedAtMs < MIN_ANALYSIS_INTERVAL_MS) return true
        lastAnalyzedAtMs = now
        return false
    }

    private fun calculateFps(): Float {
        val now = SystemClock.elapsedRealtime()
        val previous = lastResultAtMs
        lastResultAtMs = now
        return if (previous == 0L) {
            0f
        } else {
            1000f / (now - previous).coerceAtLeast(1L)
        }
    }

    private fun buildLiveStatus(output: DetectionOutput, fps: Float): String {
        return String.format(
            Locale.US,
            "%d object(s) • %d ms • %.1f FPS",
            output.detections.size,
            output.inferenceTimeMs,
            fps
        )
    }

    private fun adjustConfidence(delta: Float) {
        confidenceThreshold = (confidenceThreshold + delta)
            .coerceIn(MIN_CONFIDENCE, MAX_CONFIDENCE)
        updateConfidenceText()
        lastResultAtMs = 0L
    }

    private fun updateConfidenceText() {
        val percent = (confidenceThreshold * 100).roundToInt()
        binding.confidenceText.text = "Confidence $percent%"
        binding.liveHintText.text = "Back camera - TFLite FP16 - ${DhakaRoadNetDetector.LIVE_THREAD_COUNT} thread - threshold $percent%"
    }

    private fun toggleAnalysisPaused() {
        isPaused = !isPaused
        binding.pauseButton.text = if (isPaused) "Resume" else "Pause"
        if (isPaused) {
            binding.liveStatusText.text = "Live detection paused"
        } else {
            binding.liveStatusText.text = "Live detection running"
            lastResultAtMs = 0L
        }
    }

    private fun showCameraError(exception: Exception) {
        binding.liveStatusText.text = "Could not start camera: ${exception.message}"
        MaterialAlertDialogBuilder(this)
            .setTitle("Camera unavailable")
            .setMessage(exception.message ?: "The camera could not be started on this device.")
            .setPositiveButton("Back") { _, _ -> finish() }
            .show()
    }

    override fun onResume() {
        super.onResume()
        if (!::binding.isInitialized) return

        if (hasCameraPermission()) {
            if (cameraProvider == null && !isStartingCamera) {
                startCamera()
            }
        } else {
            stopCameraUseCases()
            binding.liveStatusText.text = "Camera permission is off"
            binding.liveHintText.text = "Open app settings and allow Camera permission to use live detection."
            if (!isRequestingCameraPermission) {
                showCameraPermissionDialog()
            }
        }
    }

    override fun onPause() {
        stopCameraUseCases()
        super.onPause()
    }

    override fun onDestroy() {
        stopCameraUseCases()
        closeDetectorOnAnalyzerThread()
        super.onDestroy()
    }

    companion object {
        private const val CONFIDENCE_STEP = 0.05f
        private const val MIN_CONFIDENCE = 0.10f
        private const val MAX_CONFIDENCE = 0.80f
        private const val MIN_ANALYSIS_INTERVAL_MS = 120L
    }
}
