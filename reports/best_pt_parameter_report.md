# DhakaRoadNet best.pt Parameter Report

Generated for the final trained DhakaRoadNet YOLOv8n checkpoint.

## Source Model

- Model checkpoint: `model/checkpoints/yolov8n_dhakaroadnet_baseline/best.pt`
- Base architecture: YOLOv8n object detector
- Dataset task: road object detection
- Number of classes: 24
- Input image size used during training/export: 640

## Final Parameter Count

| Item | Value |
|---|---:|
| Total parameters | 3,015,528 |
| Parameters in millions | 3.016 M |
| Approximate wording | about 3.02 million parameters |

The final `best.pt` model contains **3,015,528 parameters**, or approximately **3.02 million parameters**.

## How The Parameter Count Was Calculated

The parameter count was calculated by loading the trained YOLO checkpoint and summing the number of scalar values in every parameter tensor.

Formula:

```text
total_parameters = sum(number_of_values_in_each_parameter_tensor)
```

Python verification code:

```python
from ultralytics import YOLO

model = YOLO("model/checkpoints/yolov8n_dhakaroadnet_baseline/best.pt").model

total_params = sum(p.numel() for p in model.parameters())
params_in_millions = total_params / 1_000_000

print(total_params)
print(params_in_millions)
```

Output:

```text
3015528
3.015528
```

## Layer-Level Parameter Breakdown

| Layer index | Layer type | Parameters |
|---:|---|---:|
| 0 | Conv | 464 |
| 1 | Conv | 4,672 |
| 2 | C2f | 7,360 |
| 3 | Conv | 18,560 |
| 4 | C2f | 49,664 |
| 5 | Conv | 73,984 |
| 6 | C2f | 197,632 |
| 7 | Conv | 295,424 |
| 8 | C2f | 460,288 |
| 9 | SPPF | 164,608 |
| 10 | Upsample | 0 |
| 11 | Concat | 0 |
| 12 | C2f | 148,224 |
| 13 | Upsample | 0 |
| 14 | Concat | 0 |
| 15 | C2f | 37,248 |
| 16 | Conv | 36,992 |
| 17 | Concat | 0 |
| 18 | C2f | 123,648 |
| 19 | Conv | 147,712 |
| 20 | Concat | 0 |
| 21 | C2f | 493,056 |
| 22 | Detect | 755,992 |
| **Total** |  | **3,015,528** |

Layers such as `Upsample` and `Concat` have zero parameters because they only reshape or combine feature maps. They do not learn weights.

## Why This Can Differ From Generic YOLOv8n

Generic YOLOv8n is often described as having around 3.2 million parameters. The exact count can change slightly depending on the detection head and number of classes.

This DhakaRoadNet model was trained for **24 custom road-object classes**, so its final detection head is adapted for this project. The correct project-specific number is therefore:

```text
3,015,528 parameters
```

## Note About Trainable Parameters

When this checkpoint is loaded for inference, trainable parameters may appear as `0` if the model is not in active training mode or gradients are disabled. That does not mean the model has no parameters. For model size and architecture reporting, the important value is the total parameter count:

```text
Total parameters = 3,015,528
```

