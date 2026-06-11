package com.example.dhakaroadnet

data class ProjectInfo(
    val title: String,
    val body: String,
    val rows: List<String> = emptyList()
)

object ProjectInfoContent {
    fun classesInfo(labels: List<String>): ProjectInfo {
        return ProjectInfo(
            title = "24 Road-Object Classes",
            body = "DhakaRoadNet is trained for local road scenes where common traffic objects, vulnerable road users, road defects, and markings can appear together. These are the model labels used by the Android app.",
            rows = labels.mapIndexed { index, label ->
                "${index + 1}. $label\n${classDescription(label)}"
            }
        )
    }

    fun edgeAiInfo(): ProjectInfo {
        return ProjectInfo(
            title = "Edge AI Goal",
            body = "The project moves from dataset preparation to YOLOv8 training, evaluation, TFLite export, and Android deployment. In V1, inference runs locally on the phone, so images do not need to be sent to a server.",
            rows = listOf(
                "Privacy\nImages stay on-device during app inference.",
                "Deployment\nThe FP16 TFLite model is packaged inside the Android app assets.",
                "Future direction\nThe disabled video button is reserved for real-time camera stream detection."
            )
        )
    }

    fun modelInfo(): ProjectInfo {
        return ProjectInfo(
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

    fun tfliteInfo(): ProjectInfo {
        return ProjectInfo(
            title = "TFLite FP16 Export",
            body = "The Android app uses the FP16 TensorFlow Lite export because it matched the trained model behavior reliably during testing.",
            rows = listOf(
                "File\ndhakaroadnet_yolov8n_fp16.tflite",
                "Labels\nlabels.txt is packaged with the model.",
                "Why FP16\nIt is smaller than a full precision model and was more stable than the experimental INT8 export for V1."
            )
        )
    }

    fun datasetInfo(): ProjectInfo {
        return ProjectInfo(
            title = "Dataset Focus",
            body = "The dataset focuses on Bangladeshi urban traffic conditions, especially the object mix seen on Dhaka roads. This makes the project more locally meaningful than a generic demo detector.",
            rows = listOf(
                "Scene type\nRoad images with mixed vehicles, people, road hazards, and markings.",
                "Dataset pipeline\nDownload, verification, visualization, training, evaluation, export, and Android testing are organized in notebooks.",
                "Research value\nThe project demonstrates the full path from custom data to an on-device AI application."
            )
        )
    }

    fun androidInfo(): ProjectInfo {
        return ProjectInfo(
            title = "Android V1 App",
            body = "This app is a native Kotlin/XML Android implementation. It uses gallery upload or camera capture, preprocesses the bitmap, runs the local TFLite model, and draws detections on the result image.",
            rows = listOf(
                "Architecture\nSmall MVP-style app with Activity, Presenter, Detector, Preprocessor, and Renderer.",
                "Current flow\nSelect image, run detection, compare input and output, then open annotation details.",
                "Next step\nReal-time video detection can be added after the image workflow is stable."
            )
        )
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
}
