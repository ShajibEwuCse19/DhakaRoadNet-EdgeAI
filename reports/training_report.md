# DhakaRoadNet Training Report

## Experiment Identity

- Experiment name:
- Date:
- Research goal:
- Dataset YAML:
- Dataset version/source:
- Number of classes:
- Train/val/test images:

## Hardware and Environment

- OS:
- Python version:
- PyTorch version:
- Ultralytics version:
- Device:
- GPU name and VRAM:
- CUDA version:

## Model

- Pretrained model:
- Reason for selection:
- Input image size:
- Edge deployment target:

## Training Configuration

- Epochs:
- Batch size:
- Optimizer:
- Initial learning rate:
- Scheduler:
- Weight decay:
- Early stopping patience:
- Seed:
- Deterministic mode:
- Save period:

## Augmentation Configuration

| Augmentation | Value | Reason | Keep/change next run |
|---|---:|---|---|
| fliplr | 0.5 | Road scenes remain realistic after horizontal flip. | |
| flipud | 0.0 | Upside-down road scenes are unrealistic. | |
| degrees | 5.0 | Mild camera tilt only. | |
| translate | 0.10 | Improves robustness to object position and partial crops. | |
| scale | 0.40 | Simulates distance changes without extreme distortion. | |
| shear | 2.0 | Small camera/perspective variation. | |
| perspective | 0.0005 | Mild road-scene perspective variation. | |
| hsv_h | 0.015 | Small camera/weather color shift. | |
| hsv_s | 0.60 | Handles saturation changes. | |
| hsv_v | 0.40 | Handles brightness/shadow variation. | |
| mosaic | 0.80 | Helps small dataset and small-object learning. | |
| mixup | 0.05 | Light regularization only. | |
| copy_paste | 0.0 | Avoid without curated object masks/cutouts. | |
| close_mosaic | 15 | Finish training on natural images. | |

## Results

- Best epoch:
- best.pt path:
- last.pt path:
- mAP50:
- mAP50-95:
- Precision:
- Recall:
- Train box loss:
- Train class loss:
- Train DFL loss:
- Validation box loss:
- Validation class loss:
- Validation DFL loss:

## Learning Curve Notes

- Underfitting signs:
- Overfitting signs:
- Validation plateau epoch:
- Any instability or spikes:

## Qualitative Evaluation

- Strong classes:
- Weak classes:
- Common false positives:
- Common false negatives:
- Small-object issues:
- Occlusion/crowding issues:
- Lighting/weather issues:

## Android / Edge AI Notes

- Candidate for export:
- Expected export format:
- Quantization plan:
- Latency concern:
- Accuracy concern:

## Decision for Next Experiment

- Keep same model or try YOLOv8s:
- Change augmentation:
- Change image size:
- Change batch size:
- Change learning rate:
- Collect more data for classes:
- Final decision:
