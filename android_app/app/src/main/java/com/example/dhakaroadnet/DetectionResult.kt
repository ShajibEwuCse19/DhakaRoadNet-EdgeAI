package com.example.dhakaroadnet

import android.graphics.RectF

/**
 * @author Shajib
 */
data class DetectionResult(
    val classId: Int,
    val label: String,
    val confidence: Float,
    val box: RectF
)

data class DetectionOutput(
    val detections: List<DetectionResult>,
    val inferenceTimeMs: Long,
    val confidenceThreshold: Float,
    val imageWidth: Int,
    val imageHeight: Int
)
