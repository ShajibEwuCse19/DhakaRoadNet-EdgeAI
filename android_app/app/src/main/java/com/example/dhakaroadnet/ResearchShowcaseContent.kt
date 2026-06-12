package com.example.dhakaroadnet

import java.util.Locale

data class ShowcaseItem(
    val marker: String,
    val title: String,
    val body: String,
    val accentColorRes: Int
)

data class DemoSample(
    val title: String,
    val body: String,
    val imageResId: Int
)

object ResearchShowcaseContent {
    val pipelineStages = listOf(
        ShowcaseItem(
            "01",
            "Custom Dhaka dataset",
            "Road scenes were collected from real Dhaka traffic, then curated into a local object-detection dataset rather than a benchmark-only demo.",
            R.color.dhaka_accent
        ),
        ShowcaseItem(
            "02",
            "Dataset verification",
            "The notebook pipeline checks split health, label/image pairing, class distribution, annotation density, and visual samples.",
            R.color.dhaka_primary
        ),
        ShowcaseItem(
            "03",
            "YOLOv8n training",
            "The baseline detector was trained for 120 epochs with 24 custom classes, 640 input size, AdamW, and road-scene augmentations.",
            R.color.dhaka_primary
        ),
        ShowcaseItem(
            "04",
            "best.pt checkpoint",
            "Training produced the selected best.pt YOLO checkpoint, the project model used for evaluation, Gradio testing, and TFLite export.",
            R.color.dhaka_primary
        ),
        ShowcaseItem(
            "05",
            "Gradio deployment and test",
            "The best.pt model was loaded into a Gradio demo to upload Dhaka road images, run Python-side detection, and verify predictions before Android export.",
            R.color.dhaka_accent
        ),
        ShowcaseItem(
            "06",
            "Evaluation analysis",
            "Validation and test metrics, confusion matrices, weak-class diagnosis, and prediction grids are used to explain model behavior.",
            R.color.dhaka_road_soft
        ),
        ShowcaseItem(
            "07",
            "TFLite FP16 export",
            "The trained checkpoint was exported to TensorFlow Lite FP16 with NMS included for Android deployment.",
            R.color.dhaka_accent
        ),
        ShowcaseItem(
            "08",
            "Offline Android Edge AI",
            "The app packages the FP16 model and labels, then runs still-image and live-camera inference locally on the phone.",
            R.color.dhaka_primary
        )
    )

    val demoSamples = listOf(
        DemoSample(
            "Zebra crossing scene",
            "Dhaka road frame with bus, rickshaw, pedestrians, motorbike, cars, and zebra crossing.",
            R.drawable.demo_sample_road_1
        ),
        DemoSample(
            "Dense mixed traffic",
            "Crowded urban traffic sample with many small vehicles and pedestrians.",
            R.drawable.demo_sample_road_2
        ),
        DemoSample(
            "Rickshaw-heavy road",
            "Busy local scene with rickshaws, buses, cars, motorbikes, and pedestrians.",
            R.drawable.demo_sample_road_3
        )
    )

    val benchmarkFacts = listOf(
        ShowcaseItem(
            "FP16",
            "Packaged model",
            "${DhakaRoadNetDetector.MODEL_FILE} runs from Android assets with TensorFlow Lite.",
            R.color.dhaka_primary
        ),
        ShowcaseItem(
            "640",
            "Model input",
            "Images and live frames are preprocessed to ${DhakaRoadNetDetector.INPUT_SIZE} x ${DhakaRoadNetDetector.INPUT_SIZE} before inference.",
            R.color.dhaka_accent
        ),
        ShowcaseItem(
            "4T",
            "Image detector",
            "Still-image detection uses ${DhakaRoadNetDetector.DEFAULT_THREAD_COUNT} TFLite threads for one-shot inference.",
            R.color.dhaka_road_soft
        ),
        ShowcaseItem(
            "1T",
            "Live detector",
            "Live camera detection uses ${DhakaRoadNetDetector.LIVE_THREAD_COUNT} TFLite thread to keep repeated video inference stable.",
            R.color.dhaka_primary
        ),
        ShowcaseItem(
            "Live",
            "Camera proof",
            "The Video screen reports detection count, inference time, FPS, and threshold while drawing boxes over CameraX preview.",
            R.color.dhaka_primary
        )
    )

    val researchLimitations = listOf(
        ShowcaseItem(
            "Rare",
            "Class imbalance",
            "Police car, Leguna, Pickup truck, Manhole, and Mini truck need more samples for stronger class-wise reliability.",
            R.color.dhaka_error
        ),
        ShowcaseItem(
            "Small",
            "Crowding and object size",
            "Human, Motorbike, Dog, and Truck can be affected by occlusion, distance, and dense traffic scenes.",
            R.color.dhaka_error
        ),
        ShowcaseItem(
            "Road",
            "Surface objects",
            "Pothole, Speed Breaker, Manhole, and Zebra Crossing vary with camera angle, shadows, and road texture.",
            R.color.dhaka_accent
        ),
        ShowcaseItem(
            "INT8",
            "Future calibration",
            "INT8 export is smaller, but FP16 is kept for the Android V1 because its prediction behavior is more stable.",
            R.color.dhaka_road_soft
        )
    )

    fun buildDetectionBenchmark(output: DetectionOutput): String {
        return buildString {
            appendLine("Runtime evidence")
            appendLine("Model: ${DhakaRoadNetDetector.MODEL_FILE}")
            appendLine("TFLite: FP16, ${DhakaRoadNetDetector.DEFAULT_THREAD_COUNT} threads, ${DhakaRoadNetDetector.INPUT_SIZE} x ${DhakaRoadNetDetector.INPUT_SIZE} input")
            appendLine("This run: ${output.detections.size} detection(s), ${output.inferenceTimeMs} ms, threshold ${formatPercent(output.confidenceThreshold)}")
            append("Offline status: no server call, image analyzed locally on device")
        }
    }

    private fun formatPercent(value: Float): String {
        return String.format(Locale.US, "%.1f%%", value * 100f)
    }
}
