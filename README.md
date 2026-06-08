### DhakaRoadNet: An Edge AI System for Real-Time Road Object Detection Using a Custom Urban Traffic Dataset

OnDeviceRoadDetector/
│
├── 📂 data/
│   ├── 📂 raw/                        # Desktop থেকে original images
│   │   ├── videos/                    # Original Dhaka road videos
│   │   └── frames/                    # Video থেকে extract করা raw frames
│   │
│   ├── 📂 roboflow/                   # Roboflow থেকে download করা dataset
│   │   ├── train/
│   │   │   ├── images/
│   │   │   └── labels/
│   │   ├── valid/
│   │   │   ├── images/
│   │   │   └── labels/
│   │   ├── test/
│   │   │   ├── images/
│   │   │   └── labels/
│   │   └── data.yaml
│   │
│   └── 📂 samples/                    # README + Demo এর জন্য sample images
│
├── 📂 notebooks/
│   ├── 01_dataset_verification.ipynb  # Data check, class distribution plot
│   ├── 02_training.ipynb              # YOLOv8 training
│   ├── 03_evaluation.ipynb            # mAP, Confusion Matrix, PR Curve
│   └── 04_export_tflite.ipynb         # TFLite INT8 export
│
├── 📂 models/
│   ├── 📂 checkpoints/                # Training এর সময় save হওয়া weights
│   │   ├── best.pt                    # Best performing checkpoint
│   │   └── last.pt                    # Last epoch checkpoint
│   │
│   ├── 📂 exported/                   # Final export files
│   │   ├── best_int8.tflite           # Android এ যাবে এটা
│   │   └── best_float32.tflite        # Backup (non-quantized)
│   │
│   └── 📂 runs/                       # YOLOv8 auto-generate করে (gitignore করো)
│
├── 📂 android/                        # Android Studio Project
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── java/com/yourname/roaddetector/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── ObjectDetector.kt
│   │   │   │   └── BoundingBoxOverlay.kt
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   │   └── activity_main.xml
│   │   │   │   └── values/
│   │   │   └── assets/
│   │   │       ├── best_int8.tflite   # Exported model
│   │   │       └── labels.txt         # Class names
│   │   └── build.gradle
│   └── build.gradle
│
├── 📂 reports/
│   ├── 📂 figures/                    # Training graphs, plots
│   │   ├── confusion_matrix.png
│   │   ├── pr_curve.png
│   │   ├── results.png                # Loss + mAP curves
│   │   └── predictions_sample.jpg     # Sample detection output
│   │
│   ├── 📂 metrics/                    # Evaluation numbers
│   │   └── results.csv                # YOLOv8 auto-generate করে
│   │
│   └── summary.md                     # Project summary 
│
├── 📂 demo/
│   ├── demo_video.mp4                 # Android এ live detection recording
│   └── screenshots/                   # App screenshots
│
├── .gitignore
├── requirements.txt                   # Python dependencies
└── README.md                          # Full project documentation
