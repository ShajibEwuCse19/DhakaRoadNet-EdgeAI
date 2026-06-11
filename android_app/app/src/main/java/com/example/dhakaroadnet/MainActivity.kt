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
import com.example.dhakaroadnet.databinding.BottomSheetProjectInfoBinding
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
        binding.classesInfoCard.setOnClickListener { showClassesInfo() }
        binding.edgeInfoCard.setOnClickListener { showEdgeAiInfo() }
        binding.modelInfoCard.setOnClickListener { showModelInfo() }
        binding.tfliteInfoCard.setOnClickListener { showTfliteInfo() }
        binding.datasetInfoCard.setOnClickListener { showDatasetInfo() }
        binding.androidInfoCard.setOnClickListener { showAndroidInfo() }
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
            binding.contentScroll.post { binding.contentScroll.smoothScrollTo(0, 0) }
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
        binding.contentScroll.post { binding.contentScroll.smoothScrollTo(0, 0) }
    }

    private fun updateButtonState() {
        val hasImage = selectedBitmap != null
        val hasDetectionDetails = latestOutput != null

        binding.homeContent.isVisible = !hasImage
        binding.detectionContent.isVisible = hasImage
        binding.detectButton.isVisible = hasImage
        binding.detailsButton.isVisible = hasImage
        binding.clearButton.isVisible = hasImage
        binding.videoButton.isEnabled = false

        binding.selectButton.isEnabled = !detectionRunning
        binding.detectButton.isEnabled = !detectionRunning && hasImage
        binding.detailsButton.isEnabled = !detectionRunning && hasDetectionDetails
        binding.clearButton.isEnabled = !detectionRunning && hasImage
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

        return "${output.detections.size} object(s) detected: $objectCounts. Inference: ${output.inferenceTimeMs} ms. Tap Info for annotation data."
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

    private fun showClassesInfo() {
        showProjectInfo(
            title = "24 Road-Object Classes",
            body = "DhakaRoadNet is trained for local road scenes where common traffic objects, vulnerable road users, road defects, and markings can appear together. These are the model labels used by the Android app.",
            rows = loadClassInfoRows()
        )
    }

    private fun showEdgeAiInfo() {
        showProjectInfo(
            title = "Edge AI Goal",
            body = "The project moves from dataset preparation to YOLOv8 training, evaluation, TFLite export, and Android deployment. In V1, inference runs locally on the phone, so images do not need to be sent to a server.",
            rows = listOf(
                "Privacy\nImages stay on-device during app inference.",
                "Deployment\nThe FP16 TFLite model is packaged inside the Android app assets.",
                "Future direction\nThe disabled video button is reserved for real-time camera stream detection."
            )
        )
    }

    private fun showModelInfo() {
        showProjectInfo(
            title = "YOLOv8n Model",
            body = "YOLOv8n was selected because it is compact enough for mobile deployment while still giving practical detection quality for this beginner Edge AI project.",
            rows = listOf(
                "Model family\nYOLOv8 nano object detector.",
                "Parameters\nThe trained best.pt model has about 3.02 million parameters.",
                "Input\nThe Android pipeline prepares images for 640 x 640 model inference.",
                "Output\nThe exported model returns detected boxes, confidence scores, and class IDs."
            )
        )
    }

    private fun showTfliteInfo() {
        showProjectInfo(
            title = "TFLite FP16 Export",
            body = "The Android app uses the FP16 TensorFlow Lite export because it matched the trained model behavior reliably during testing.",
            rows = listOf(
                "File\ndhakaroadnet_yolov8n_fp16.tflite",
                "Labels\nlabels.txt is packaged with the model.",
                "Why FP16\nIt is smaller than a full precision model and was more stable than the experimental INT8 export for V1."
            )
        )
    }

    private fun showDatasetInfo() {
        showProjectInfo(
            title = "Dataset Focus",
            body = "The dataset focuses on Bangladeshi urban traffic conditions, especially the object mix seen on Dhaka roads. This makes the project more locally meaningful than a generic demo detector.",
            rows = listOf(
                "Scene type\nRoad images with mixed vehicles, people, road hazards, and markings.",
                "Dataset pipeline\nDownload, verification, visualization, training, evaluation, export, and Android testing are organized in notebooks.",
                "Research value\nThe project demonstrates the full path from custom data to an on-device AI application."
            )
        )
    }

    private fun showAndroidInfo() {
        showProjectInfo(
            title = "Android V1 App",
            body = "This app is a native Kotlin/XML Android implementation. It uses gallery upload or camera capture, preprocesses the bitmap, runs the local TFLite model, and draws detections on the result image.",
            rows = listOf(
                "Architecture\nSmall MVP-style app with Activity, Presenter, Detector, Preprocessor, and Renderer.",
                "Current flow\nSelect image, run detection, compare input and output, then open annotation details.",
                "Next step\nReal-time video detection can be added after the image workflow is stable."
            )
        )
    }

    private fun showProjectInfo(title: String, body: String, rows: List<String> = emptyList()) {
        val sheetBinding = BottomSheetProjectInfoBinding.inflate(layoutInflater)
        sheetBinding.projectInfoTitleText.text = title
        sheetBinding.projectInfoBodyText.text = body
        sheetBinding.projectInfoDivider.isVisible = rows.isNotEmpty()
        sheetBinding.projectInfoListContainer.removeAllViews()

        rows.forEach { row ->
            sheetBinding.projectInfoListContainer.addView(createDetailTextView(row))
        }

        BottomSheetDialog(this).apply {
            setContentView(sheetBinding.root)
            show()
        }
    }

    private fun loadClassInfoRows(): List<String> {
        val labels = assets.open(DhakaRoadNetDetector.LABELS_FILE)
            .bufferedReader()
            .use { reader ->
                reader.readLines()
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
            }

        return labels.mapIndexed { index, label ->
            "${index + 1}. $label\n${classDescription(label)}"
        }
    }

    private fun classDescription(label: String): String {
        return when (label) {
            "Auto rickshaw" -> "Small three-wheeler vehicle common in city traffic."
            "Bicycle" -> "Light two-wheeler that needs careful detection near mixed traffic."
            "Bus" -> "Large passenger vehicle with high road-space impact."
            "Car" -> "Common private vehicle class for urban traffic monitoring."
            "Dog" -> "Animal class included for road safety and unexpected obstacles."
            "Garbage van" -> "Service vehicle often seen in city road environments."
            "Human" -> "Pedestrian or road user class important for safety analysis."
            "Leguna" -> "Local public transport vehicle common in Bangladesh."
            "Manhole" -> "Road-surface utility object that can affect safe driving."
            "Micro Bus" -> "Medium passenger vehicle class."
            "Mini truck" -> "Small cargo vehicle used in urban delivery traffic."
            "Minivan" -> "Small passenger or utility van class."
            "Motorbike" -> "High-frequency two-wheeler class in Dhaka traffic."
            "Pickup truck" -> "Light cargo vehicle class."
            "Police car" -> "Emergency or law-enforcement vehicle class."
            "Pothole" -> "Road defect class important for road-condition monitoring."
            "Rickshaw" -> "Human-powered transport class common in local roads."
            "Road barrier" -> "Temporary or fixed road-control object."
            "SUV" -> "Larger private vehicle class."
            "Speed Breaker" -> "Road calming structure that can affect vehicle motion."
            "Three wheelers -CNG-" -> "CNG three-wheeler class common in Bangladesh."
            "Truck" -> "Large cargo vehicle class."
            "Van" -> "Utility or passenger van class."
            "Zebra Crossing" -> "Road marking class for pedestrian crossing areas."
            else -> "Road-object class used by the DhakaRoadNet detector."
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
