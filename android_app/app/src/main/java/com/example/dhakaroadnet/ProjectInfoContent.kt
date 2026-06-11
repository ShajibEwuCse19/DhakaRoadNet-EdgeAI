package com.example.dhakaroadnet

data class ProjectInfo(
    val title: String,
    val body: String,
    val rows: List<String> = emptyList()
)

data class ProjectTopicCard(
    val highlight: String,
    val title: String,
    val subtitle: String,
    val accentColorRes: Int,
    val info: ProjectInfo
)

object ProjectInfoContent {
    fun topicCards(labels: List<String>): List<ProjectTopicCard> {
        return listOf(
            topic(
                "Custom",
                "Real Dhaka dataset",
                "Handcrafted from local road scenes",
                R.color.dhaka_accent,
                customDatasetInfo()
            ),
            topic(
                "Handmade",
                "Video to dataset",
                "Recorded, extracted, checked, labeled",
                R.color.dhaka_primary,
                videoDatasetPipelineInfo()
            ),
            topic(
                "Unique",
                "Not benchmark data",
                "Local traffic, local classes, local context",
                R.color.dhaka_road_soft,
                uniquenessInfo()
            ),
            topic("24", "Road classes", "Vehicles, people, hazards, markings", R.color.dhaka_accent, classesInfo(labels)),
            topic("2,145", "Dataset images", "1,754 train, 195 valid, 196 test", R.color.dhaka_primary, datasetSplitInfo()),
            topic("23,448", "Annotation boxes", "Dense scenes with many objects", R.color.dhaka_primary, annotationInfo()),
            topic("10.9", "Boxes per image", "Average train-set object density", R.color.dhaka_road_soft, objectDensityInfo()),
            topic("9,079", "Human instances", "Largest class in the dataset", R.color.dhaka_accent, majorClassInfo()),
            topic("Rare", "Class imbalance", "Police car, Leguna, Pickup truck", R.color.dhaka_error, rareClassInfo()),
            topic("Weak", "Failure diagnosis", "Small, rare, crowded, similar objects", R.color.dhaka_error, weakClassInfo()),
            topic("RTX 3050", "Personal GPU", "Training ran on own device", R.color.dhaka_primary, trainingHardwareInfo()),
            topic("6143 MiB", "GPU memory", "NVIDIA GeForce RTX 3050", R.color.dhaka_road_soft, gpuMemoryInfo()),
            topic("CUDA 12.6", "PyTorch CUDA", "CUDA-enabled training stack", R.color.dhaka_primary, cudaInfo()),
            topic("PyTorch", "2.12.0+cu126", "Training and evaluation backend", R.color.dhaka_primary, pytorchInfo()),
            topic("Ultralytics", "8.4.63", "YOLO training and evaluation", R.color.dhaka_primary, ultralyticsInfo()),
            topic("Python", "3.14.2", "Training/evaluation notebook runtime", R.color.dhaka_road_soft, pythonTrainingInfo()),
            topic("TensorFlow", "2.20.0", "TFLite export environment", R.color.dhaka_accent, tensorflowInfo()),
            topic("YOLOv8n", "Baseline model", "Compact detector for mobile deployment", R.color.dhaka_primary, modelInfo()),
            topic("120", "Training epochs", "Full baseline run recorded", R.color.dhaka_primary, trainingConfigInfo()),
            topic("Batch 16", "Training batch", "640 image size, AdamW optimizer", R.color.dhaka_road_soft, optimizerInfo()),
            topic("Augment", "Road strategy", "Mosaic, HSV, scale, translate", R.color.dhaka_accent, augmentationInfo()),
            topic("3.02M", "Parameters", "3,015,528 learned values", R.color.dhaka_primary, parameterInfo()),
            topic("0.798", "Validation mAP50", "mAP50-95 reached 0.539", R.color.dhaka_primary, validationMetricsInfo()),
            topic("0.743", "Test mAP50", "mAP50-95 reached 0.478", R.color.dhaka_primary, testMetricsInfo()),
            topic("0.7995", "Best curve mAP50", "Learning curve best checkpoint signal", R.color.dhaka_accent, learningCurveInfo()),
            topic("FP16", "Android model", "Chosen for stable V1 app behavior", R.color.dhaka_primary, tfliteInfo()),
            topic("FP32", "Debug export", "Full precision TFLite comparison", R.color.dhaka_road_soft, fp32Info()),
            topic("INT8", "Small export note", "3.18 MB but unstable samples", R.color.dhaka_error, int8Info()),
            topic("Offline", "Edge AI inference", "Images stay on the phone", R.color.dhaka_primary, edgeAiInfo()),
            topic("Gradio", "Python demo", "Pre-Android model testing UI", R.color.dhaka_accent, gradioInfo()),
            topic("Notebooks", "Full ML pipeline", "Dataset, train, eval, demo, export", R.color.dhaka_road_soft, notebooksInfo()),
            topic("Android", "Kotlin/XML V1", "Gallery, camera, TFLite, renderer", R.color.dhaka_primary, androidInfo())
        )
    }

    private fun topic(
        highlight: String,
        title: String,
        subtitle: String,
        colorRes: Int,
        info: ProjectInfo
    ): ProjectTopicCard {
        return ProjectTopicCard(highlight, title, subtitle, colorRes, info)
    }

    private fun customDatasetInfo() = ProjectInfo(
        title = "Custom Real Dhaka Dataset",
        body = "DhakaRoadNet is built around a custom dataset from real Dhaka road scenes. It is not a standard benchmark dataset or a generic sample detector.",
        rows = listOf(
            "Data source\nRoad video and image collection from real Dhaka traffic scenes.",
            "Dataset type\nCustom, local, project-specific road-object dataset.",
            "Why it matters\nThe object mix reflects Bangladeshi roads: rickshaws, CNG three-wheelers, pedestrians, buses, road defects, markings, and dense mixed traffic.",
            "Research value\nThe app demonstrates a complete local-data-to-edge-deployment pipeline instead of only reusing a public benchmark."
        )
    )

    private fun videoDatasetPipelineInfo() = ProjectInfo(
        title = "Handcrafted Video-to-Dataset Pipeline",
        body = "The dataset story is a manual project workflow: collect road footage, extract useful frames, verify image/label health, train a detector, evaluate it, export it, and deploy it on Android.",
        rows = listOf(
            "Collection\nRoad scenes were personally recorded or collected from Dhaka road environments.",
            "Preparation\nFrames/images were curated and prepared for YOLO object detection.",
            "Verification\nNotebook 01 checks splits, missing labels, empty labels, annotation counts, class distribution, and sample visualizations.",
            "Deployment path\nThe same project moves from dataset notebooks to Android TFLite inference."
        )
    )

    private fun uniquenessInfo() = ProjectInfo(
        title = "Not Benchmark Data",
        body = "DhakaRoadNet should be presented as a unique local dataset project, not as a benchmark leaderboard claim.",
        rows = listOf(
            "Benchmark difference\nBenchmark datasets are standardized public evaluation sets. This project uses local custom data made for a Dhaka-road Edge AI app.",
            "Local value\nClasses and scenes are selected for Bangladeshi urban roads rather than generic global object detection.",
            "Positioning\nUse this as a practical custom-data research prototype and Android demo."
        )
    )

    private fun classesInfo(labels: List<String>) = ProjectInfo(
        title = "24 Road-Object Classes",
        body = "The detector is trained for common local road objects, vulnerable road users, road defects, and markings.",
        rows = labels.mapIndexed { index, label -> "${index + 1}. $label\n${classDescription(label)}" }
    )

    private fun datasetSplitInfo() = ProjectInfo(
        title = "Dataset Split Summary",
        body = "The verified dataset contains 2,145 images across train, validation, and test splits.",
        rows = listOf(
            "Train\n1,754 images, 1,754 label files, 19,164 boxes, 81.77% of images.",
            "Validation\n195 images, 195 label files, 2,013 boxes, 9.09% of images.",
            "Test\n196 images, 196 label files, 2,271 boxes, 9.14% of images.",
            "Health check\nNo images without label files and no labels without image files were reported."
        )
    )

    private fun annotationInfo() = ProjectInfo(
        title = "Annotation Box Count",
        body = "The dataset contains 23,448 total bounding boxes across train, validation, and test.",
        rows = listOf(
            "Train boxes\n19,164 annotated objects.",
            "Validation boxes\n2,013 annotated objects.",
            "Test boxes\n2,271 annotated objects.",
            "Meaning\nThis is a dense road-scene dataset; many images contain multiple traffic participants and objects."
        )
    )

    private fun objectDensityInfo() = ProjectInfo(
        title = "Object Density",
        body = "Dhaka road scenes are crowded, and the dataset reflects that object density.",
        rows = listOf(
            "Train density\n10.926 boxes per train image.",
            "Validation density\n10.323 boxes per validation image.",
            "Test density\n11.587 boxes per test image.",
            "Challenge\nCrowding and occlusion make road-object detection harder than clean single-object examples."
        )
    )

    private fun majorClassInfo() = ProjectInfo(
        title = "Largest Classes",
        body = "The dataset is dominated by common road participants from dense Dhaka traffic.",
        rows = listOf(
            "Human\n9,079 total instances.",
            "Bus\n3,138 total instances.",
            "Rickshaw\n2,851 total instances.",
            "Car\n1,775 total instances.",
            "Three wheelers -CNG-\n1,760 total instances."
        )
    )

    private fun rareClassInfo() = ProjectInfo(
        title = "Rare-Class Imbalance",
        body = "Some classes have very limited examples, so their metrics are less stable and they need more data.",
        rows = listOf(
            "Police car\n2 total instances, with 0 train instances.",
            "Leguna\n27 total instances.",
            "Pickup truck\n30 total instances.",
            "Manhole\n45 total instances.",
            "Mini truck\n73 total instances."
        )
    )

    private fun weakClassInfo() = ProjectInfo(
        title = "Weak-Class Diagnosis",
        body = "The evaluation report identifies likely causes for weak classes and detection failures.",
        rows = listOf(
            "Rare classes\nPolice car, Leguna, and Pickup truck suffer from very limited samples and vehicle similarity.",
            "Road-surface classes\nManhole, Speed Breaker, Zebra Crossing, and Pothole vary with angle, lighting, and road texture.",
            "Small objects\nDog, Human, Motorbike, and Truck can be affected by object size, occlusion, and crowding.",
            "Next data step\nCollect more rare-class samples and improve annotation consistency for similar vehicles."
        )
    )

    private fun trainingHardwareInfo() = ProjectInfo(
        title = "Personal GPU Training",
        body = "The YOLOv8n baseline was trained and evaluated on a personal NVIDIA GPU device, not a cloud benchmark setup.",
        rows = listOf(
            "GPU\nNVIDIA GeForce RTX 3050.",
            "Device index\nCUDA device 0.",
            "Project meaning\nThis proves the pipeline can be developed on accessible personal hardware.",
            "Training note\nThe notebook was configured to prefer CUDA when available and CPU otherwise."
        )
    )

    private fun gpuMemoryInfo() = ProjectInfo(
        title = "GPU Memory",
        body = "The evaluation output reports the training/evaluation GPU memory as 6143 MiB.",
        rows = listOf(
            "Reported device\nNVIDIA GeForce RTX 3050, 6143 MiB.",
            "Practical impact\nYOLOv8n is a good first model choice because it fits personal-device training and mobile deployment goals.",
            "Batch selection\nBatch 16 was used with image size 640."
        )
    )

    private fun cudaInfo() = ProjectInfo(
        title = "CUDA Training Stack",
        body = "The training notebook records a CUDA-enabled PyTorch setup.",
        rows = listOf(
            "PyTorch CUDA\n12.6.",
            "Driver note\nThe notebook mentions an NVIDIA driver reporting CUDA 12.7 support and using PyTorch CUDA 12.6 wheels.",
            "CUDA ready\nNotebook output reports CUDA ready: True.",
            "GPU name\nNVIDIA GeForce RTX 3050."
        )
    )

    private fun pytorchInfo() = ProjectInfo(
        title = "PyTorch Version",
        body = "Training and evaluation used PyTorch as the deep-learning backend.",
        rows = listOf(
            "Training/evaluation torch\n2.12.0+cu126.",
            "CUDA status\nCUDA available during training/evaluation notebook execution.",
            "Export notebook torch\n2.12.0+cpu was used in the separate TFLite export environment."
        )
    )

    private fun ultralyticsInfo() = ProjectInfo(
        title = "Ultralytics YOLO Version",
        body = "Ultralytics was used for YOLOv8 training, validation, prediction, and export workflow.",
        rows = listOf(
            "Evaluation runtime\nUltralytics 8.4.63.",
            "Export runtime\nUltralytics 8.4.64.",
            "Model family\nYOLOv8 object detection.",
            "Output shape\nExport logs show output shape (1, 300, 6) with NMS included."
        )
    )

    private fun pythonTrainingInfo() = ProjectInfo(
        title = "Python Training Runtime",
        body = "The training and evaluation notebooks record Python 3.14.2 for the main ML workflow.",
        rows = listOf(
            "Training notebook\nPython 3.14.2.",
            "Evaluation notebook\nPython 3.14.2.",
            "Export note\nTensorFlow export was moved to a Python 3.12.10 environment for better compatibility."
        )
    )

    private fun tensorflowInfo() = ProjectInfo(
        title = "TensorFlow Export Runtime",
        body = "TensorFlow was used for TensorFlow Lite export, not for Android runtime training.",
        rows = listOf(
            "Python\n3.12.10 in the TFLite export environment.",
            "TensorFlow\n2.20.0 available during export.",
            "Minimum check\nThe notebook required tensorflow >= 2.19.0.",
            "Android result\nThe exported FP16 model is packaged in the app assets."
        )
    )

    private fun modelInfo() = ProjectInfo(
        title = "YOLOv8n Model",
        body = "YOLOv8n was selected because it is compact enough for personal GPU training and mobile deployment while still giving useful road-scene detection quality.",
        rows = listOf(
            "Model family\nYOLOv8 nano object detector.",
            "Input\n640 x 640 image size.",
            "Classes\n24 custom road-object classes.",
            "Deployment target\nTensorFlow Lite model running inside the Android app."
        )
    )

    private fun trainingConfigInfo() = ProjectInfo(
        title = "Training Configuration",
        body = "The baseline training run used a reproducible YOLOv8n setup.",
        rows = listOf(
            "Epochs\n120.",
            "Patience\n25.",
            "Seed\n42.",
            "Deterministic\nTrue.",
            "Save period\nEvery 10 epochs.",
            "AMP\nEnabled."
        )
    )

    private fun optimizerInfo() = ProjectInfo(
        title = "Optimizer and Batch Setup",
        body = "Training used a practical configuration for a compact detector and personal GPU.",
        rows = listOf(
            "Batch\n16.",
            "Image size\n640.",
            "Optimizer\nAdamW.",
            "Initial learning rate\n0.001.",
            "Final LR factor\n0.01.",
            "Weight decay\n0.0005.",
            "Warmup epochs\n3.0.",
            "Cosine LR\nTrue."
        )
    )

    private fun augmentationInfo() = ProjectInfo(
        title = "Urban-Road Augmentation",
        body = "Augmentations were selected for realistic road-scene variation without unrealistic flips.",
        rows = listOf(
            "Horizontal flip\n0.5, realistic for road scenes.",
            "Vertical flip\n0.0, because upside-down road scenes are unrealistic.",
            "Mosaic\n0.8, helpful for small dataset and small objects.",
            "Mixup\n0.05, light regularization.",
            "Scale\n0.4 and translate 0.1.",
            "HSV shifts\nHue 0.015, saturation 0.6, value 0.4.",
            "Close mosaic\n15 final epochs on more natural images."
        )
    )

    private fun parameterInfo() = ProjectInfo(
        title = "Model Parameter Count",
        body = "The trained `best.pt` checkpoint contains 3,015,528 parameters, approximately 3.02 million.",
        rows = listOf(
            "Calculation\nSum the number of scalar values in every model parameter tensor.",
            "Total parameters\n3,015,528.",
            "Why it differs from generic YOLOv8n\nThe detection head is adapted for 24 custom classes.",
            "Deployment meaning\nSmall enough for an Android Edge AI V1 prototype."
        )
    )

    private fun validationMetricsInfo() = ProjectInfo(
        title = "Validation Metrics",
        body = "Validation metrics show the baseline model learned the custom road-object task well enough for a V1 demo.",
        rows = listOf(
            "Precision\n0.8362.",
            "Recall\n0.7468.",
            "F1\n0.7890.",
            "mAP50\n0.7980.",
            "mAP50-95\n0.5390."
        )
    )

    private fun testMetricsInfo() = ProjectInfo(
        title = "Test Metrics",
        body = "The held-out test split provides a more realistic check of model quality.",
        rows = listOf(
            "Precision\n0.8148.",
            "Recall\n0.6914.",
            "F1\n0.7480.",
            "mAP50\n0.7429.",
            "mAP50-95\n0.4783.",
            "Interpretation\nGood baseline for a custom beginner Edge AI project, with room to improve rare and small classes."
        )
    )

    private fun learningCurveInfo() = ProjectInfo(
        title = "Learning Curve Summary",
        body = "The learning curve report records all 120 epochs and shows strong improvement from the first epoch to the final model.",
        rows = listOf(
            "mAP50 first\n0.07695.",
            "mAP50 last\n0.79825.",
            "mAP50 best\n0.7995.",
            "mAP50-95 first\n0.03366.",
            "mAP50-95 last/best\n0.53799.",
            "Train box loss\n1.96594 first to 0.92216 last."
        )
    )

    private fun tfliteInfo() = ProjectInfo(
        title = "TFLite FP16 Export",
        body = "The Android app uses the FP16 TensorFlow Lite export because it matched the trained model behavior reliably during testing.",
        rows = listOf(
            "File\ndhakaroadnet_yolov8n_fp16.tflite.",
            "Size\n11.79 MB.",
            "NMS\nIncluded in export.",
            "Input size\n640.",
            "Why FP16\nSmaller than full precision in many deployment contexts and more stable than the experimental INT8 output for this V1 app."
        )
    )

    private fun fp32Info() = ProjectInfo(
        title = "FP32 TFLite Export",
        body = "The FP32 export is kept as a full-precision comparison and debugging artifact.",
        rows = listOf(
            "Status\nSuccess.",
            "Size\n11.78 MB.",
            "NMS\nIncluded.",
            "Use\nCompare behavior against FP16 and PyTorch best.pt."
        )
    )

    private fun int8Info() = ProjectInfo(
        title = "INT8 TFLite Export Note",
        body = "INT8 export succeeded and is much smaller, but prediction checks showed unstable/noisy behavior on some samples.",
        rows = listOf(
            "Status\nSuccess.",
            "Size\n3.18 MB.",
            "NMS\nIncluded.",
            "Prediction issue\nSome INT8 samples reported 300 detections, unlike PyTorch/FP16 behavior.",
            "Decision\nUse FP16 first in the Android app; keep INT8 for future calibration and testing."
        )
    )

    private fun edgeAiInfo() = ProjectInfo(
        title = "Offline Edge AI Goal",
        body = "The project moves from custom local data to a phone-side AI detector. In V1, inference runs locally on the phone.",
        rows = listOf(
            "Privacy\nImages stay on-device during app inference.",
            "Deployment\nThe FP16 TFLite model is packaged inside Android assets.",
            "No server\nThe app does not need a cloud API to detect road objects.",
            "Future direction\nThe disabled video button is reserved for real-time camera stream detection."
        )
    )

    private fun gradioInfo() = ProjectInfo(
        title = "Gradio Deployment Demo",
        body = "Notebook 04 loads the trained YOLOv8 model into a simple Gradio interface before Android deployment.",
        rows = listOf(
            "Purpose\nUpload a road image, run detection, and review annotated output in Python.",
            "Device\nThe notebook reports NVIDIA GeForce RTX 3050 when CUDA is available.",
            "Value\nFast model sanity check before TensorFlow Lite export and Android integration."
        )
    )

    private fun notebooksInfo() = ProjectInfo(
        title = "Notebook Pipeline",
        body = "The project notebooks document the full ML workflow from dataset to Android-ready TFLite model.",
        rows = listOf(
            "01\nDataset download, verification, class distribution, and sample visualization.",
            "02\nYOLOv8 training pipeline and configuration snapshot.",
            "03\nEvaluation, metrics, weak-class analysis, and reports.",
            "04\nGradio model deployment demo.",
            "05\nTFLite export, labels, prediction checks, and export report."
        )
    )

    private fun androidInfo() = ProjectInfo(
        title = "Android V1 App",
        body = "This app is a native Kotlin/XML Android implementation for offline DhakaRoadNet image detection.",
        rows = listOf(
            "Architecture\nActivity, Presenter, Detector, Preprocessor, Renderer, project-info catalog, and reusable UI helpers.",
            "Current flow\nSelect image, run detection, compare input and output, then open annotation details.",
            "Model file\ndhakaroadnet_yolov8n_fp16.tflite.",
            "Labels\nlabels.txt packaged with the model.",
            "Next step\nReal-time video detection can be added after the image workflow is stable."
        )
    )

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
