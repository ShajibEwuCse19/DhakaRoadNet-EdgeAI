package com.example.dhakaroadnet

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import com.example.dhakaroadnet.databinding.ActivityMainBinding
import com.example.dhakaroadnet.databinding.BottomSheetDetectionDetailsBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.util.Locale

class MainActivity : AppCompatActivity(), DetectionContract.View {
    private lateinit var binding: ActivityMainBinding
    private lateinit var presenter: DetectionPresenter

    private var selectedBitmap: Bitmap? = null
    private var latestOutput: DetectionOutput? = null
    private var latestAnnotatedBitmap: Bitmap? = null
    private var pendingCameraUri: Uri? = null
    private var detectionRunning = false

    private val galleryPicker = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            loadSelectedImage(uri)
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingCameraUri
        if (success && uri != null) {
            loadSelectedImage(uri)
        }
        pendingCameraUri = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        presenter = DetectionPresenter(this, this)
        bindActions()
        resetScreen()
    }

    private fun bindActions() {
        binding.selectButton.setOnClickListener { showImageSourceDialog() }
        binding.detectButton.setOnClickListener { runDetection() }
        binding.detailsButton.setOnClickListener { showDetailsBottomSheet() }
        binding.clearButton.setOnClickListener { resetScreen() }
    }

    private fun showImageSourceDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Select road image")
            .setItems(arrayOf("Upload from gallery", "Take photo with camera")) { _, which ->
                if (which == 0) {
                    openGallery()
                } else {
                    openCamera()
                }
            }
            .show()
    }

    private fun openGallery() {
        galleryPicker.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    private fun openCamera() {
        try {
            val cameraUri = createCameraImageUri()
            pendingCameraUri = cameraUri
            cameraLauncher.launch(cameraUri)
        } catch (exception: Exception) {
            showError("Could not open camera: ${exception.message}")
        }
    }

    private fun createCameraImageUri(): Uri {
        val imageDirectory = File(cacheDir, "camera_images").apply { mkdirs() }
        val imageFile = File.createTempFile("dhakaroadnet_", ".jpg", imageDirectory)
        return FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            imageFile
        )
    }

    private fun loadSelectedImage(uri: Uri) {
        try {
            val bitmap = decodeBitmap(uri)
            selectedBitmap = bitmap
            latestOutput = null
            latestAnnotatedBitmap = null

            binding.inputImageView.setImageBitmap(bitmap)
            binding.inputPlaceholderText.isVisible = false
            binding.resultImageView.setImageDrawable(null)
            binding.resultPlaceholderText.isVisible = true
            binding.summaryText.text = "Image selected. Tap Detect to run DhakaRoadNet on-device."
            updateButtonState()
        } catch (exception: Exception) {
            showError("Could not read image: ${exception.message}")
        }
    }

    private fun decodeBitmap(uri: Uri): Bitmap {
        val decoded = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            contentResolver.openInputStream(uri).use { stream ->
                BitmapFactory.decodeStream(stream)
            } ?: error("Image file is empty.")
        }
        return decoded.copy(Bitmap.Config.ARGB_8888, false) ?: decoded
    }

    private fun runDetection() {
        val bitmap = selectedBitmap
        if (bitmap == null) {
            showError("Select an image first.")
            return
        }
        presenter.runDetection(bitmap)
    }

    override fun setLoading(isLoading: Boolean) {
        detectionRunning = isLoading
        binding.progressBar.isVisible = isLoading
        if (isLoading) {
            binding.summaryText.text = "Running detection locally with the TFLite FP16 model..."
        }
        updateButtonState()
    }

    override fun showDetectionResult(output: DetectionOutput, annotatedBitmap: Bitmap) {
        latestOutput = output
        latestAnnotatedBitmap = annotatedBitmap

        binding.resultImageView.setImageBitmap(annotatedBitmap)
        binding.resultPlaceholderText.isVisible = false
        binding.summaryText.text = buildDetectionSummary(output)
        updateButtonState()
    }

    override fun showError(message: String) {
        binding.summaryText.text = "Problem: $message"
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        updateButtonState()
    }

    private fun resetScreen() {
        selectedBitmap = null
        latestOutput = null
        latestAnnotatedBitmap = null
        pendingCameraUri = null
        detectionRunning = false

        binding.inputImageView.setImageDrawable(null)
        binding.resultImageView.setImageDrawable(null)
        binding.inputPlaceholderText.isVisible = true
        binding.resultPlaceholderText.isVisible = true
        binding.progressBar.isVisible = false
        binding.summaryText.text = "Ready. Select an image to begin."
        updateButtonState()
    }

    private fun updateButtonState() {
        binding.selectButton.isEnabled = !detectionRunning
        binding.detectButton.isEnabled = !detectionRunning && selectedBitmap != null
        binding.detailsButton.isEnabled = !detectionRunning && latestOutput != null
        binding.clearButton.isEnabled = !detectionRunning &&
            (selectedBitmap != null || latestOutput != null || latestAnnotatedBitmap != null)
    }

    private fun buildDetectionSummary(output: DetectionOutput): String {
        if (output.detections.isEmpty()) {
            return "No objects were detected above ${formatPercent(output.confidenceThreshold)} confidence. Inference: ${output.inferenceTimeMs} ms."
        }

        val objectCounts = output.detections
            .groupingBy { readableLabel(it.label) }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .joinToString(", ") { "${it.key} x${it.value}" }

        return "${output.detections.size} object(s) detected: $objectCounts. Inference: ${output.inferenceTimeMs} ms. Tap Details for annotation data."
    }

    private fun showDetailsBottomSheet() {
        val output = latestOutput
        if (output == null) {
            showError("Run detection first.")
            return
        }

        val sheetBinding = BottomSheetDetectionDetailsBinding.inflate(layoutInflater)
        sheetBinding.modelInfoText.text = buildModelInfo(output)
        addDetectionRows(sheetBinding.detectionListContainer, output)

        BottomSheetDialog(this).apply {
            setContentView(sheetBinding.root)
            show()
        }
    }

    private fun buildModelInfo(output: DetectionOutput): String {
        return buildString {
            appendLine("Project: DhakaRoadNet")
            appendLine("Model: YOLOv8n road-object detector")
            appendLine("Android model: TensorFlow Lite FP16")
            appendLine("Parameters: 3.02M")
            appendLine("Classes: 24")
            appendLine("Input size: 640 x 640")
            appendLine("Confidence threshold: ${formatPercent(output.confidenceThreshold)}")
            appendLine("Image size: ${output.imageWidth} x ${output.imageHeight} px")
            appendLine("Inference time: ${output.inferenceTimeMs} ms")
            append("Total detections: ${output.detections.size}")
        }
    }

    private fun addDetectionRows(container: LinearLayout, output: DetectionOutput) {
        container.removeAllViews()
        if (output.detections.isEmpty()) {
            container.addView(
                createDetailTextView(
                    "No objects detected above ${formatPercent(output.confidenceThreshold)} confidence."
                )
            )
            return
        }

        output.detections.forEachIndexed { index, detection ->
            val box = detection.box
            val rowText = buildString {
                appendLine("${index + 1}. ${readableLabel(detection.label)} - ${formatPercent(detection.confidence)}")
                appendLine("Class ID: ${detection.classId}")
                appendLine("Left: ${box.left.toInt()} px, Top: ${box.top.toInt()} px")
                append("Right: ${box.right.toInt()} px, Bottom: ${box.bottom.toInt()} px")
            }
            container.addView(createDetailTextView(rowText))
        }
    }

    private fun createDetailTextView(textValue: String): TextView {
        return TextView(this).apply {
            text = textValue
            textSize = 14f
            setLineSpacing(2f, 1f)
            setTextColor(ContextCompat.getColor(this@MainActivity, R.color.dhaka_text_secondary))
            background = ContextCompat.getDrawable(this@MainActivity, R.drawable.bg_chip)
            setPadding(dp(12), dp(10), dp(12), dp(10))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(8)
            }
        }
    }

    private fun readableLabel(label: String): String {
        return label.replace('_', ' ')
    }

    private fun formatPercent(value: Float): String {
        return String.format(Locale.US, "%.1f%%", value * 100f)
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    override fun onDestroy() {
        if (::presenter.isInitialized) {
            presenter.release()
        }
        super.onDestroy()
    }
}
