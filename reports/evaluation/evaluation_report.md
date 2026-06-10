# DhakaRoadNet Evaluation Report

Generated: 2026-06-10T11:20:31

## Evaluation Objective

This report evaluates the YOLOv8 road-object detector for DhakaRoadNet as a mini research project. The goal is to measure detection quality, diagnose model weaknesses, and decide whether the trained model is ready for Android/TFLite deployment.

## Model and Dataset

- Model weights: `Pending: no best.pt discovered`
- Dataset YAML: `C:\Users\USER\Desktop\ML\WorkSpace\DhakaRoadNet-EdgeAI\data\roboflow\data_yolov8.yaml`
- Number of classes: 24
- Dataset root: `C:\Users\USER\Desktop\ML\WorkSpace\DhakaRoadNet-EdgeAI\data\roboflow`

### Dataset Split Summary

| split | images | labels | empty_labels | boxes | boxes_per_image |
| --- | --- | --- | --- | --- | --- |
| train | 1754 | 1754 | 3 | 19164 | 10.926 |
| valid | 195 | 195 | 0 | 2013 | 10.323 |
| test | 196 | 196 | 0 | 2271 | 11.587 |

### Lowest-Support Classes

| class_id | class_name | test | train | valid | total |
| --- | --- | --- | --- | --- | --- |
| 14 | Police car | 1.0 | 0.0 | 1.0 | 2.0 |
| 7 | Leguna | 2.0 | 24.0 | 1.0 | 27.0 |
| 13 | Pickup truck | 2.0 | 24.0 | 4.0 | 30.0 |
| 8 | Manhole | 4.0 | 36.0 | 5.0 | 45.0 |
| 10 | Mini truck | 2.0 | 66.0 | 5.0 | 73.0 |
| 4 | Dog | 7.0 | 63.0 | 5.0 | 75.0 |
| 9 | Micro Bus | 11.0 | 101.0 | 12.0 | 124.0 |
| 19 | Speed Breaker | 13.0 | 114.0 | 9.0 | 136.0 |
| 17 | Road barrier | 7.0 | 117.0 | 14.0 | 138.0 |
| 5 | Garbage van | 10.0 | 116.0 | 21.0 | 147.0 |

## Core Metrics

| Split | Precision | Recall | F1 | mAP50 | mAP50-95 |
|---|---:|---:|---:|---:|---:|
| Validation | Pending | Pending | Pending | Pending | Pending |
| Test | Pending | Pending | Pending | Pending | Pending |

## Diagnostic Plots

- Confusion matrix: `Pending: run evaluation with plots enabled.`
- PR curve: `Pending: run evaluation with plots enabled.`
- Precision curve: `Pending: run evaluation with plots enabled.`
- Recall curve: `Pending: run evaluation with plots enabled.`
- F1 curve: `Pending: run evaluation with plots enabled.`

## Weak-Class Diagnosis

| class_id | class_name | train_instances | valid_instances | test_instances | total_instances | likely_weakness_reasons |
| --- | --- | --- | --- | --- | --- | --- |
| 14 | Police car | 0 | 1 | 1 | 2 | very limited total samples; rare class and visual similarity with other vehicles |
| 7 | Leguna | 24 | 1 | 2 | 27 | very limited total samples; rare class and visual similarity with other vehicles |
| 13 | Pickup truck | 24 | 4 | 2 | 30 | very limited total samples; rare class and visual similarity with other vehicles |
| 8 | Manhole | 36 | 5 | 4 | 45 | very limited total samples; road-surface appearance varies with lighting and camera angle |
| 10 | Mini truck | 66 | 5 | 2 | 73 | limited training samples; rare class and visual similarity with other vehicles |
| 4 | Dog | 63 | 5 | 7 | 75 | limited training samples; small object size |
| 9 | Micro Bus | 101 | 12 | 11 | 124 | inspect confusion matrix and examples |
| 19 | Speed Breaker | 114 | 9 | 13 | 136 | road-surface appearance varies with lighting and camera angle |
| 17 | Road barrier | 117 | 14 | 7 | 138 | inspect confusion matrix and examples |
| 5 | Garbage van | 116 | 21 | 10 | 147 | inspect confusion matrix and examples |
| 22 | Van | 137 | 19 | 25 | 181 | inspect confusion matrix and examples |
| 21 | Truck | 156 | 24 | 25 | 205 | small object size |

Likely causes to inspect:

- Class imbalance, especially rare classes.
- Small object size for distant pedestrians, vehicles, and road-surface defects.
- Occlusion and crowding in dense Dhaka traffic scenes.
- Annotation ambiguity between visually similar vehicles.
- Lighting, road texture, and camera-motion variation for potholes, manholes, speed breakers, and zebra crossings.

## Learning Curve Interpretation

Training results status: `missing_training_results_csv`

- Overfitting: train loss decreases while validation loss rises or validation mAP falls.
- Underfitting: both losses remain high and mAP remains low.
- Healthy training: train and validation losses decrease together and mAP improves before plateauing.

## False Positives, False Negatives, and True Positives

- True positives indicate correctly localized and classified road objects.
- False positives may come from background clutter, similar vehicle classes, duplicate boxes, shadows, or reflections.
- False negatives may come from small objects, occlusion, motion blur, low contrast, rare classes, or missing annotation patterns.

Detection-level failure file: `Pending: run error analysis.`

## Prediction Gallery

Prediction outputs are saved under `reports\evaluation\predictions` after `RUN_PREDICTIONS=True`.

Recommended galleries:

- validation predictions
- test predictions
- custom images
- sample videos
- best detections
- failure cases

## Key Findings

Pending until model evaluation is executed.

## Limitations

- Final numerical conclusions require trained weights and completed validation/test evaluation.
- Rare classes may not have enough validation/test examples for stable class-wise conclusions.
- mAP does not directly measure Android latency, memory, or battery impact.
- Offline images may not fully represent live camera motion blur and compression.

## Future Work

- Add more samples for rare and confused classes.
- Improve annotation consistency for visually similar vehicle categories.
- Evaluate YOLOv8s if YOLOv8n underfits.
- Tune confidence threshold based on precision-recall tradeoff.
- Export to TFLite and benchmark on target Android hardware.

## Android/TFLite Readiness Checklist

- [ ] `best.pt` exists and is reproducible from the training notebook.
- [ ] Validation and test mAP50/mAP50-95 are recorded.
- [ ] Precision/recall tradeoff is acceptable for the app use case.
- [ ] Worst classes are understood and documented.
- [ ] Confusion matrix and PR/F1/P/R curves are saved.
- [ ] Prediction gallery contains successful and failed examples.
- [ ] Model is exported to TFLite.
- [ ] INT8 quantization is tested if latency/model size matters.
- [ ] Android inference latency is measured on target hardware.
- [ ] Labels file matches the training class order exactly.
- [ ] Camera preprocessing matches YOLO training image resizing expectations.
