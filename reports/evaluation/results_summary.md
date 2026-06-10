# DhakaRoadNet Results Summary

## Snapshot

- Model: `Pending: no best.pt discovered`
- Dataset: `C:\Users\USER\Desktop\ML\WorkSpace\DhakaRoadNet-EdgeAI\data\roboflow\data_yolov8.yaml`
- Classes: 24
- Validation mAP50: Pending
- Validation mAP50-95: Pending
- Test mAP50: Pending
- Test mAP50-95: Pending

## Metrics Table

_Pending: run evaluation to populate this table._

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
- Confusion matrix: `Pending: run evaluation with plots enabled.`
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
