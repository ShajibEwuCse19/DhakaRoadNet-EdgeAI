package com.example.dhakaroadnet

import java.util.Locale

object DetectionTextFormatter {
    fun buildDetectionSummary(output: DetectionOutput): String {
        if (output.detections.isEmpty()) {
            return "Offline FP16 result: no objects above ${formatPercent(output.confidenceThreshold)} confidence. Inference: ${output.inferenceTimeMs} ms."
        }

        val objectCounts = output.detections
            .groupingBy { readableLabel(it.label) }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .joinToString(", ") { "${it.key} x${it.value}" }
        val topDetections = output.detections
            .take(3)
            .joinToString(", ") { "${readableLabel(it.label)} ${formatPercent(it.confidence)}" }

        return "Offline FP16 result: ${output.detections.size} object(s), ${output.inferenceTimeMs} ms, threshold ${formatPercent(output.confidenceThreshold)}.\nCounts: $objectCounts.\nTop: $topDetections.\nTap Info for annotation data."
    }

    fun buildModelInfo(output: DetectionOutput): String {
        return buildString {
            appendLine("Project: DhakaRoadNet")
            appendLine("Model: YOLOv8n road-object detector")
            appendLine("Android model: TensorFlow Lite FP16")
            appendLine("Parameters: 3.02M")
            appendLine("Classes: 24")
            appendLine("Input size: 640 x 640")
            appendLine("Confidence threshold: ${formatPercent(output.confidenceThreshold)}")
            appendLine("Image size: ${output.imageWidth} x ${output.imageHeight} px")
            appendLine("Inference time: ${output.inferenceTimeMs} ms")
            append("Total detections: ${output.detections.size}")
        }
    }

    fun buildDetectionRows(output: DetectionOutput): List<String> {
        if (output.detections.isEmpty()) {
            return listOf(
                "No objects detected above ${formatPercent(output.confidenceThreshold)} confidence."
            )
        }

        return output.detections.mapIndexed { index, detection ->
            val box = detection.box
            buildString {
                appendLine("${index + 1}. ${readableLabel(detection.label)} - ${formatPercent(detection.confidence)}")
                appendLine("Class ID: ${detection.classId}")
                appendLine("Left: ${box.left.toInt()} px, Top: ${box.top.toInt()} px")
                append("Right: ${box.right.toInt()} px, Bottom: ${box.bottom.toInt()} px")
            }
        }
    }

    private fun readableLabel(label: String): String {
        return label.replace('_', ' ')
    }

    private fun formatPercent(value: Float): String {
        return String.format(Locale.US, "%.1f%%", value * 100f)
    }
}
