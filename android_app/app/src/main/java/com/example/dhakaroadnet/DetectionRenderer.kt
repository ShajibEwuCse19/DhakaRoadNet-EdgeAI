package com.example.dhakaroadnet

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import kotlin.math.max

/**
 * @author Shajib
 */
object DetectionRenderer {
    private val palette = intArrayOf(
        Color.rgb(0, 200, 83),
        Color.rgb(244, 161, 26),
        Color.rgb(0, 137, 123),
        Color.rgb(25, 118, 210),
        Color.rgb(198, 40, 40),
        Color.rgb(106, 27, 154)
    )

    fun draw(bitmap: Bitmap, detections: List<DetectionResult>): Bitmap {
        val output = bitmap.copy(Bitmap.Config.ARGB_8888, true) ?: Bitmap
            .createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            .also { fallback ->
                Canvas(fallback).drawBitmap(bitmap, 0f, 0f, null)
            }
        val canvas = Canvas(output)
        val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = max(4f, output.width / 220f)
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = max(28f, output.width / 34f)
            style = Paint.Style.FILL
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        val labelBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
        val textBounds = Rect()

        detections.forEach { detection ->
            val color = palette[detection.classId % palette.size]
            boxPaint.color = color
            labelBgPaint.color = color
            canvas.drawRect(detection.box, boxPaint)

            val label = "${detection.label} ${(detection.confidence * 100).toInt()}%"
            textPaint.getTextBounds(label, 0, label.length, textBounds)

            val labelPadding = 10f
            val labelHeight = textBounds.height() + labelPadding * 2
            val labelWidth = textBounds.width() + labelPadding * 2
            val labelTop = if (detection.box.top - labelHeight >= 0f) {
                detection.box.top - labelHeight
            } else {
                detection.box.top
            }
            val labelRect = RectF(
                detection.box.left,
                labelTop,
                detection.box.left + labelWidth,
                labelTop + labelHeight
            )
            canvas.drawRoundRect(labelRect, 8f, 8f, labelBgPaint)
            canvas.drawText(
                label,
                labelRect.left + labelPadding,
                labelRect.bottom - labelPadding,
                textPaint
            )
        }

        return output
    }
}
