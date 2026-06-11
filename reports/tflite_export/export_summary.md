# DhakaRoadNet TFLite Export Summary

Generated: 2026-06-11 12:02:07

## Source Model

- Model: `C:\Users\USER\Desktop\ML\WorkSpace\DhakaRoadNet-EdgeAI\model\checkpoints\yolov8n_dhakaroadnet_baseline\best.pt`
- Dataset YAML: `C:\Users\USER\Desktop\ML\WorkSpace\DhakaRoadNet-EdgeAI\data\roboflow\data_yolov8.yaml`
- Image size: `640`
- NMS included in export: `True`

## Exported Files

| name        | status   | path                                                                                                          |   size_mb | half   | int8   | nms   |   imgsz | raw_export_result                                                                                                                           | error   |
|:------------|:---------|:--------------------------------------------------------------------------------------------------------------|----------:|:-------|:-------|:------|--------:|:--------------------------------------------------------------------------------------------------------------------------------------------|:--------|
| FP32 TFLite | success  | C:\Users\USER\Desktop\ML\WorkSpace\DhakaRoadNet-EdgeAI\model\exported\tflite\dhakaroadnet_yolov8n_fp32.tflite |     11.78 | False  | False  | True  |     640 | C:\Users\USER\Desktop\ML\WorkSpace\DhakaRoadNet-EdgeAI\model\checkpoints\yolov8n_dhakaroadnet_baseline\best_saved_model\best_float32.tflite |         |
| FP16 TFLite | success  | C:\Users\USER\Desktop\ML\WorkSpace\DhakaRoadNet-EdgeAI\model\exported\tflite\dhakaroadnet_yolov8n_fp16.tflite |     11.79 | True   | False  | True  |     640 | C:\Users\USER\Desktop\ML\WorkSpace\DhakaRoadNet-EdgeAI\model\checkpoints\yolov8n_dhakaroadnet_baseline\best_saved_model\best_float16.tflite |         |
| INT8 TFLite | success  | C:\Users\USER\Desktop\ML\WorkSpace\DhakaRoadNet-EdgeAI\model\exported\tflite\dhakaroadnet_yolov8n_int8.tflite |      3.18 | False  | True   | True  |     640 | C:\Users\USER\Desktop\ML\WorkSpace\DhakaRoadNet-EdgeAI\model\checkpoints\yolov8n_dhakaroadnet_baseline\best_saved_model\best_int8.tflite    |         |

## Android Files

- TFLite models: `C:\Users\USER\Desktop\ML\WorkSpace\DhakaRoadNet-EdgeAI\model\exported\tflite`
- Labels file: `C:\Users\USER\Desktop\ML\WorkSpace\DhakaRoadNet-EdgeAI\model\exported\tflite\labels.txt`
