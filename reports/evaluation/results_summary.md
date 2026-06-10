# DhakaRoadNet Results Summary

## Snapshot

- Model: `C:\Users\USER\Desktop\ML\WorkSpace\DhakaRoadNet-EdgeAI\model\runs\yolov8n_dhakaroadnet_baseline\weights\best.pt`
- Dataset: `C:\Users\USER\Desktop\ML\WorkSpace\DhakaRoadNet-EdgeAI\data\roboflow\data_yolov8.yaml`
- Classes: 24
- Validation mAP50: 0.7980
- Validation mAP50-95: 0.5390
- Test mAP50: 0.7429
- Test mAP50-95: 0.4783

## Metrics Table

| split | precision | recall | f1 | map50 | map50_95 | fitness | save_dir |
| --- | --- | --- | --- | --- | --- | --- | --- |
| validation | 0.8362406519722372 | 0.7467531418310944 | 0.7889675078091094 | 0.7980055789906281 | 0.5390460360387831 | 0.5390460360387831 | C:\Users\USER\Desktop\ML\WorkSpace\DhakaRoadNet-EdgeAI\reports\evaluation\artifacts\yolov8n_dhakaroadnet_baseline_validation |
| test | 0.8147640952953049 | 0.6913929906141892 | 0.7480258065527376 | 0.7428670182226806 | 0.4782787146225128 | 0.4782787146225128 | C:\Users\USER\Desktop\ML\WorkSpace\DhakaRoadNet-EdgeAI\reports\evaluation\artifacts\yolov8n_dhakaroadnet_baseline_test |

## Class Weakness Summary

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

## GitHub Figure References

- Class distribution: `reports/figures/class_distribution.png`
- Confusion matrix: `reports/evaluation/plots/yolov8n_dhakaroadnet_baseline_confusion_matrix.png`
- PR curve: `Pending: run evaluation with plots enabled.`
- Sample predictions: `reports\evaluation\predictions`

## Key Findings

Pending until model evaluation is executed.

## Limitations

Pending final quantitative and qualitative review.

## Future Work

- Evaluate YOLOv8s after YOLOv8n baseline.
- Add rare-class data.
- Export and benchmark TFLite model on Android.
