package com.example.dhakaroadnet

import android.Manifest
import android.content.ContentValues
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import com.example.dhakaroadnet.databinding.ActivityMainBinding
import com.example.dhakaroadnet.databinding.BottomSheetDetectionDetailsBinding
import com.example.dhakaroadnet.databinding.ItemProjectTopicCardBinding
import com.example.dhakaroadnet.databinding.ItemDemoSampleCardBinding
import com.example.dhakaroadnet.databinding.ItemShowcaseCardBinding
import com.example.dhakaroadnet.databinding.BottomSheetProjectInfoBinding
import com.example.dhakaroadnet.databinding.PopupFirstRunTipBinding
import com.google.android.material.card.MaterialCardView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File

/**
 * @author Shajib
 */
class MainActivity : AppCompatActivity(), DetectionContract.View {
    private lateinit var binding: ActivityMainBinding
    private lateinit var presenter: DetectionPresenter

    private var selectedBitmap: Bitmap? = null
    private var latestOutput: DetectionOutput? = null
    private var latestAnnotatedBitmap: Bitmap? = null
    private var pendingCameraUri: Uri? = null
    private var detectionRunning = false
    private var currentSlideIndex = 0
    private var isSlideshowPaused = false
    private val slideshowHandler = Handler(Looper.getMainLooper())
    private val slideshowRunnable = object : Runnable {
        override fun run() {
            moveSlide(1)
            scheduleNextSlide()
        }
    }

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

    private val savePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            saveDetectionResult()
        } else {
            showError("Storage permission is required to save results on this Android version.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        presenter = DetectionPresenter(this, this)
        bindActions()
        setupBackPressHandling()
        setupReportSlideshow()
        setupResearchShowcase()
        setupProjectTopics()
        resetScreen()
        maybeShowFirstRunTips()
    }

    private fun bindActions() {
        binding.apply {
            selectButton.setOnClickListener { showImageSourceDialog() }
            detectButton.setOnClickListener { runDetection() }
            detailsButton.setOnClickListener { showDetailsBottomSheet() }
            clearButton.setOnClickListener { resetScreen() }
            videoButton.setOnClickListener { openLiveDetection() }
            saveResultButton.setOnClickListener { saveDetectionResult() }
            shareResultButton.setOnClickListener { shareDetectionResult() }
        }
    }

    private fun setupBackPressHandling() {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    showExitConfirmationDialog()
                }
            }
        )
    }

    private fun showExitConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Exit DhakaRoadNet?")
            .setMessage("Do you want to close the app or stay on this screen?")
            .setNegativeButton("Stay", null)
            .setPositiveButton("Exit") { _, _ -> finish() }
            .show()
    }

    private fun maybeShowFirstRunTips() {
        val preferences = getSharedPreferences(FIRST_RUN_PREFS, MODE_PRIVATE)
        if (preferences.getBoolean(KEY_FIRST_RUN_TIPS_SHOWN, false)) return

        binding.root.postDelayed({
            if (!isFinishing && !isDestroyed) {
                showFirstRunTip(0)
            }
        }, FIRST_RUN_TIP_DELAY_MS)
    }

    private fun showFirstRunTip(index: Int) {
        val tips = firstRunTips()
        val tip = tips[index]
        val popupBinding = PopupFirstRunTipBinding.inflate(layoutInflater)

        popupBinding.apply {
            tipStepText.text = "Quick guide ${index + 1} of ${tips.size}"
            tipTitleText.text = tip.title
            tipBodyText.text = tip.body
            tipActionButton.text = if (index == tips.lastIndex) "Got it" else "Next"
        }

        val popupWindow = PopupWindow(
            popupBinding.root,
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            isOutsideTouchable = false
            elevation = dp(10).toFloat()
        }

        popupBinding.tipActionButton.setOnClickListener {
            popupWindow.dismiss()
            if (index == tips.lastIndex) {
                getSharedPreferences(FIRST_RUN_PREFS, MODE_PRIVATE)
                    .edit()
                    .putBoolean(KEY_FIRST_RUN_TIPS_SHOWN, true)
                    .apply()
            } else {
                binding.root.postDelayed({
                    if (!isFinishing && !isDestroyed) {
                        showFirstRunTip(index + 1)
                    }
                }, NEXT_TIP_DELAY_MS)
            }
        }

        popupWindow.showAtLocation(
            binding.root,
            Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL,
            0,
            dp(104)
        )
    }

    private fun firstRunTips(): List<FirstRunTip> {
        return listOf(
            FirstRunTip(
                title = "Select a road image",
                body = "Use Select to choose a gallery image, take one photo, or load a bundled Dhaka sample. Then tap Detect to run DhakaRoadNet on-device."
            ),
            FirstRunTip(
                title = "Try live video detection",
                body = "Use Video to open the back camera. The app runs the FP16 TFLite model in real time and draws boxes directly over the camera preview."
            )
        )
    }

    private fun setupReportSlideshow() {
        binding.reportImageView.setOnClickListener {
            moveSlide(1)
            scheduleNextSlide()
        }
        binding.reportImageView.setOnLongClickListener {
            toggleSlideshowPause()
            true
        }
        showSlide(0)
        scheduleNextSlide()
    }

    private fun scheduleNextSlide() {
        slideshowHandler.removeCallbacks(slideshowRunnable)
        if (isSlideshowPaused || ProjectSlides.slides.size <= 1) return
        slideshowHandler.postDelayed(slideshowRunnable, SLIDE_DELAY_MS)
    }

    private fun moveSlide(offset: Int) {
        val slideCount = ProjectSlides.slides.size
        if (slideCount == 0) return
        currentSlideIndex = (currentSlideIndex + offset + slideCount) % slideCount
        showSlide(currentSlideIndex)
    }

    private fun showSlide(index: Int) {
        if (ProjectSlides.slides.isEmpty()) return
        val slide = ProjectSlides.slides[index]
        binding.apply {
            reportImageView.animate().cancel()
            reportImageView.alpha = SLIDE_START_ALPHA
            reportImageView.setImageResource(slide.imageResId)
            reportImageView.animate()
                .alpha(1f)
                .setDuration(SLIDE_FADE_MS)
                .start()
            reportCaptionText.text = slide.caption
        }
        renderSlideDots(index)
    }

    private fun renderSlideDots(activeIndex: Int) {
        binding.apply {
            reportDotContainer.removeAllViews()
            ProjectSlides.slides.forEachIndexed { index, _ ->
                reportDotContainer.addView(createSlideDot(index, isActive = index == activeIndex))
            }
        }
    }

    private fun createSlideDot(index: Int, isActive: Boolean): TextView {
        return TextView(this).apply {
            text = "•"
            textSize = 24f
            alpha = if (isActive) 1f else 0.45f
            gravity = Gravity.CENTER
            contentDescription = "Show slide ${index + 1}"
            setTextColor(
                ContextCompat.getColor(
                    this@MainActivity,
                    if (isActive) R.color.dhaka_primary else R.color.dhaka_outline
                )
            )
            setOnClickListener {
                currentSlideIndex = index
                showSlide(currentSlideIndex)
                scheduleNextSlide()
            }
            layoutParams = LinearLayout.LayoutParams(
                dp(32),
                dp(32)
            ).apply {
                marginStart = dp(3)
                marginEnd = dp(3)
            }
        }
    }

    private fun toggleSlideshowPause() {
        isSlideshowPaused = !isSlideshowPaused
        binding.slidePauseIndicator.isVisible = isSlideshowPaused
        if (isSlideshowPaused) {
            slideshowHandler.removeCallbacks(slideshowRunnable)
            Toast.makeText(this, "Slideshow paused.", Toast.LENGTH_SHORT).show()
        } else {
            scheduleNextSlide()
            Toast.makeText(this, "Slideshow resumed.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupResearchShowcase() {
        renderShowcaseItems(binding.pipelineContainer, ResearchShowcaseContent.pipelineStages)
        renderDemoSamples()
        renderShowcaseItems(binding.modelBenchmarkContainer, ResearchShowcaseContent.benchmarkFacts)
        renderShowcaseItems(binding.researchLimitContainer, ResearchShowcaseContent.researchLimitations)
    }

    private fun renderShowcaseItems(container: LinearLayout, items: List<ShowcaseItem>) {
        container.removeAllViews()
        items.forEach { item ->
            container.addView(createShowcaseCard(item))
        }
    }

    private fun createShowcaseCard(item: ShowcaseItem): MaterialCardView {
        val cardBinding = ItemShowcaseCardBinding.inflate(LayoutInflater.from(this))
        val accentColor = ContextCompat.getColor(this, item.accentColorRes)

        cardBinding.apply {
            showcaseMarkerText.text = item.marker
            showcaseMarkerText.backgroundTintList = ColorStateList.valueOf(accentColor)
            showcaseTitleText.text = item.title
            showcaseBodyText.text = item.body
        }

        return cardBinding.root.apply {
            strokeColor = accentColor
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(10)
            }
        }
    }

    private fun renderDemoSamples() {
        binding.apply {
            demoSampleContainer.removeAllViews()
            ResearchShowcaseContent.demoSamples.forEach { sample ->
                demoSampleContainer.addView(createDemoSampleCard(sample))
            }
        }
    }

    private fun createDemoSampleCard(sample: DemoSample): MaterialCardView {
        val cardBinding = ItemDemoSampleCardBinding.inflate(LayoutInflater.from(this))

        cardBinding.apply {
            sampleImageView.setImageResource(sample.imageResId)
            sampleTitleText.text = sample.title
            sampleBodyText.text = sample.body
            sampleActionButton.setOnClickListener { loadDemoSample(sample) }
        }

        return cardBinding.root.apply {
            setOnClickListener { loadDemoSample(sample) }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(10)
            }
        }
    }

    private fun setupProjectTopics() {
        binding.projectTopicContainer.removeAllViews()
        ProjectInfoContent.topicCards(loadLabels())
            .chunked(TOPIC_COLUMNS)
            .forEach { rowTopics ->
                binding.projectTopicContainer.addView(createTopicRow(rowTopics))
            }
    }

    private fun createTopicRow(topics: List<ProjectTopicCard>): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(12)
            }

            topics.forEachIndexed { index, topic ->
                addView(createTopicCard(topic, index))
            }
        }
    }

    private fun createTopicCard(topic: ProjectTopicCard, indexInRow: Int): MaterialCardView {
        val cardBinding = ItemProjectTopicCardBinding.inflate(LayoutInflater.from(this))
        val accentColor = ContextCompat.getColor(this, topic.accentColorRes)

        cardBinding.apply {
            topicHighlightText.text = topic.highlight
            topicTitleText.text = topic.title
            topicSubtitleText.text = topic.subtitle
            topicHighlightText.setTextColor(accentColor)
        }

        return cardBinding.root.apply {
            strokeColor = accentColor
            setOnClickListener { showProjectInfo(topic.info) }
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                if (indexInRow == 0) {
                    marginEnd = dp(6)
                } else {
                    marginStart = dp(6)
                }
            }
        }
    }

    private fun showImageSourceDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Select road image")
            .setItems(arrayOf("Upload from gallery", "Take photo with camera", "Use bundled demo sample")) { _, which ->
                when (which) {
                    0 -> openGallery()
                    1 -> openCamera()
                    else -> showDemoSampleDialog()
                }
            }
            .show()
    }

    private fun showDemoSampleDialog() {
        val samples = ResearchShowcaseContent.demoSamples
        val sampleTitles = samples.map { it.title }.toTypedArray()

        MaterialAlertDialogBuilder(this)
            .setTitle("Try built-in sample")
            .setItems(sampleTitles) { _, which ->
                loadDemoSample(samples[which])
            }
            .show()
    }

    private fun openLiveDetection() {
        startActivity(Intent(this, LiveDetectionActivity::class.java))
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
            loadBitmapIntoWorkspace(
                bitmap,
                "Image selected. Tap Detect to run DhakaRoadNet on-device."
            )
        } catch (exception: Exception) {
            showError("Could not read image: ${exception.message}")
        }
    }

    private fun loadDemoSample(sample: DemoSample) {
        try {
            val bitmap = decodeSampleBitmap(sample)
            loadBitmapIntoWorkspace(
                bitmap,
                "Loaded sample: ${sample.title}. Tap Detect to run the packaged FP16 model locally."
            )
        } catch (exception: Exception) {
            showError("Could not load sample: ${exception.message}")
        }
    }

    private fun loadBitmapIntoWorkspace(bitmap: Bitmap, summary: String) {
        selectedBitmap = bitmap
        latestOutput = null
        latestAnnotatedBitmap = null

        binding.apply {
            inputImageView.setImageBitmap(bitmap)
            inputPlaceholderText.isVisible = false
            resultImageView.setImageDrawable(null)
            resultPlaceholderText.isVisible = true
            resultBenchmarkText.isVisible = false
            summaryText.text = summary
            updateButtonState()
            contentScroll.post { binding.contentScroll.smoothScrollTo(0, 0) }
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

    private fun decodeSampleBitmap(sample: DemoSample): Bitmap {
        val decoded = BitmapFactory.decodeResource(resources, sample.imageResId)
            ?: error("Sample image is missing.")
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
        binding.apply {
            progressBar.isVisible = isLoading
            if (isLoading) {
                summaryText.text = "Running detection locally with the TFLite FP16 model..."
                resultBenchmarkText.isVisible = false
            }
        }
        updateButtonState()
    }

    override fun showDetectionResult(output: DetectionOutput, annotatedBitmap: Bitmap) {
        latestOutput = output
        latestAnnotatedBitmap = annotatedBitmap

        binding.apply {
            resultImageView.setImageBitmap(annotatedBitmap)
            resultPlaceholderText.isVisible = false
            summaryText.text = DetectionTextFormatter.buildDetectionSummary(output)
            resultBenchmarkText.text = ResearchShowcaseContent.buildDetectionBenchmark(output)
            resultBenchmarkText.isVisible = true
        }
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

        binding.apply {
            inputImageView.setImageDrawable(null)
            resultImageView.setImageDrawable(null)
            inputPlaceholderText.isVisible = true
            resultPlaceholderText.isVisible = true
            resultBenchmarkText.isVisible = false
            progressBar.isVisible = false
            summaryText.text = "Ready. Select an image to begin."
            updateButtonState()
            contentScroll.post { binding.contentScroll.smoothScrollTo(0, 0) }
        }
    }

    private fun updateButtonState() {
        val hasImage = selectedBitmap != null
        val hasDetectionDetails = latestOutput != null

        binding.apply {
            homeContent.isVisible = !hasImage
            detectionContent.isVisible = hasImage
            detectButton.isVisible = hasImage
            detailsButton.isVisible = hasImage
            clearButton.isVisible = hasImage
            videoButton.isEnabled = !detectionRunning

            selectButton.isEnabled = !detectionRunning
            detectButton.isEnabled = !detectionRunning && hasImage
            detailsButton.isEnabled = !detectionRunning && hasDetectionDetails
            clearButton.isEnabled = !detectionRunning && hasImage
            saveResultButton.isEnabled = !detectionRunning && latestAnnotatedBitmap != null
            shareResultButton.isEnabled = !detectionRunning && latestAnnotatedBitmap != null
        }
    }

    private fun saveDetectionResult() {
        val bitmap = latestAnnotatedBitmap
        if (bitmap == null) {
            showError("Run detection before saving.")
            return
        }

        if (needsLegacyStoragePermission() && !hasLegacyStoragePermission()) {
            savePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            return
        }

        try {
            val savedUri = saveBitmapToGallery(bitmap)
            Toast.makeText(this, "Saved detection result to gallery.", Toast.LENGTH_LONG).show()
            binding.summaryText.text = "Saved detection result: $savedUri"
        } catch (exception: Exception) {
            showError("Could not save result: ${exception.message}")
        }
    }

    private fun shareDetectionResult() {
        val bitmap = latestAnnotatedBitmap
        if (bitmap == null) {
            showError("Run detection before sharing.")
            return
        }

        try {
            val shareUri = createSharedDetectionResult(bitmap)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, shareUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, "Share detection result"))
        } catch (exception: Exception) {
            showError("Could not share result: ${exception.message}")
        }
    }

    private fun needsLegacyStoragePermission(): Boolean {
        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.P
    }

    private fun hasLegacyStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun saveBitmapToGallery(bitmap: Bitmap): Uri {
        val fileName = "DhakaRoadNet_${System.currentTimeMillis()}.jpg"
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    "${Environment.DIRECTORY_PICTURES}/DhakaRoadNet"
                )
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val resolver = contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: error("Could not create gallery image.")

        try {
            resolver.openOutputStream(uri)?.use { output ->
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, SAVE_IMAGE_QUALITY, output)) {
                    error("Image compression failed.")
                }
            } ?: error("Could not open gallery image.")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }
            return uri
        } catch (exception: Exception) {
            resolver.delete(uri, null, null)
            throw exception
        }
    }

    private fun createSharedDetectionResult(bitmap: Bitmap): Uri {
        val shareDirectory = File(cacheDir, "shared_results").apply { mkdirs() }
        val shareFile = File(shareDirectory, "dhakaroadnet_detection_result.jpg")

        shareFile.outputStream().use { output ->
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, SAVE_IMAGE_QUALITY, output)) {
                error("Image compression failed.")
            }
        }

        return FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            shareFile
        )
    }

    private fun showDetailsBottomSheet() {
        val output = latestOutput
        if (output == null) {
            showError("Run detection first.")
            return
        }

        val sheetBinding = BottomSheetDetectionDetailsBinding.inflate(layoutInflater)
        sheetBinding.modelInfoText.text = DetectionTextFormatter.buildModelInfo(output)
        sheetBinding.detectionListContainer.removeAllViews()
        DetectionTextFormatter.buildDetectionRows(output).forEach { row ->
            sheetBinding.detectionListContainer.addView(createDetailTextView(row))
        }

        BottomSheetDialog(this).apply {
            setContentView(sheetBinding.root)
            show()
        }
    }

    private fun showProjectInfo(info: ProjectInfo) {
        val sheetBinding = BottomSheetProjectInfoBinding.inflate(layoutInflater)
        sheetBinding.apply {
            projectInfoTitleText.text = info.title
            projectInfoBodyText.text = info.body
            projectInfoDivider.isVisible = info.rows.isNotEmpty()
            projectInfoListContainer.removeAllViews()
        }

        info.rows.forEach { row ->
            sheetBinding.projectInfoListContainer.addView(createDetailTextView(row))
        }

        BottomSheetDialog(this).apply {
            setContentView(sheetBinding.root)
            show()
        }
    }

    private fun loadLabels(): List<String> {
        return assets.open(DhakaRoadNetDetector.LABELS_FILE)
            .bufferedReader()
            .use { reader ->
                reader.readLines()
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
            }
    }

    override fun onDestroy() {
        slideshowHandler.removeCallbacks(slideshowRunnable)
        if (::presenter.isInitialized) {
            presenter.release()
        }
        super.onDestroy()
    }

    companion object {
        private const val SLIDE_DELAY_MS = 4_500L
        private const val TOPIC_COLUMNS = 2
        private const val FIRST_RUN_PREFS = "first_run_tips"
        private const val KEY_FIRST_RUN_TIPS_SHOWN = "tips_shown"
        private const val FIRST_RUN_TIP_DELAY_MS = 700L
        private const val NEXT_TIP_DELAY_MS = 180L
        private const val SAVE_IMAGE_QUALITY = 95
        private const val SLIDE_FADE_MS = 220L
        private const val SLIDE_START_ALPHA = 0.25f
    }

    private data class FirstRunTip(
        val title: String,
        val body: String
    )
}
