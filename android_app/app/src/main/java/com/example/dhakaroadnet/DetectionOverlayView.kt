package com.example.dhakaroadnet

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.max
import kotlin.math.min

class DetectionOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var output: DetectionOutput? = null

    private val palette = intArrayOf(
        Color.rgb(0, 200, 83),
        Color.rgb(244, 161, 26),
        Color.rgb(0, 137, 123),
        Color.rgb(25, 118, 210),
        Color.rgb(198, 40, 40),
        Color.rgb(106, 27, 154)
    )
    private val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }
    private val labelBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val textBounds = Rect()

    fun setOutput(output: DetectionOutput?) {
        this.output = output
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val currentOutput = output ?: return
        if (currentOutput.imageWidth == 0 || currentOutput.imageHeight == 0) return

        val transform = calculateFitCenterTransform(currentOutput)
        boxPaint.strokeWidth = max(4f, width / 220f)
        textPaint.textSize = max(24f, width / 36f)

        currentOutput.detections.forEach { detection ->
            drawDetection(canvas, detection, transform)
        }
    }

    private fun drawDetection(
        canvas: Canvas,
        detection: DetectionResult,
        transform: OverlayTransform
    ) {
        val color = palette[detection.classId % palette.size]
        boxPaint.color = color
        labelBgPaint.color = color

        val box = detection.box
        val mappedBox = RectF(
            transform.offsetX + box.left * transform.scale,
            transform.offsetY + box.top * transform.scale,
            transform.offsetX + box.right * transform.scale,
            transform.offsetY + box.bottom * transform.scale
        )
        canvas.drawRect(mappedBox, boxPaint)

        val label = "${detection.label} ${(detection.confidence * 100).toInt()}%"
        textPaint.getTextBounds(label, 0, label.length, textBounds)

        val padding = 9f
        val labelWidth = textBounds.width() + padding * 2
        val labelHeight = textBounds.height() + padding * 2
        val labelLeft = min(max(mappedBox.left, 0f), max(0f, width - labelWidth))
        val labelTop = if (mappedBox.top - labelHeight >= 0f) {
            mappedBox.top - labelHeight
        } else {
            mappedBox.top
        }.coerceIn(0f, max(0f, height - labelHeight))
        val labelRect = RectF(
            labelLeft,
            labelTop,
            labelLeft + labelWidth,
            labelTop + labelHeight
        )

        canvas.drawRoundRect(labelRect, 8f, 8f, labelBgPaint)
        canvas.drawText(label, labelRect.left + padding, labelRect.bottom - padding, textPaint)
    }

    private fun calculateFitCenterTransform(output: DetectionOutput): OverlayTransform {
        val scale = min(
            width.toFloat() / output.imageWidth.toFloat(),
            height.toFloat() / output.imageHeight.toFloat()
        )
        val drawnWidth = output.imageWidth * scale
        val drawnHeight = output.imageHeight * scale

        return OverlayTransform(
            scale = scale,
            offsetX = (width - drawnWidth) / 2f,
            offsetY = (height - drawnHeight) / 2f
        )
    }

    private data class OverlayTransform(
        val scale: Float,
        val offsetX: Float,
        val offsetY: Float
    )
}
