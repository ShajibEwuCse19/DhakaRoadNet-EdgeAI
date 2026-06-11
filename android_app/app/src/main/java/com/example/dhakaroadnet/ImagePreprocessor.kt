package com.example.dhakaroadnet

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.min
import kotlin.math.roundToInt

data class PreprocessResult(
    val inputBuffer: ByteBuffer,
    val scale: Float,
    val padX: Float,
    val padY: Float,
    val originalWidth: Int,
    val originalHeight: Int,
    val inputSize: Int
)

object ImagePreprocessor {
    private const val CHANNELS = 3
    private const val FLOAT_BYTES = 4

    fun prepare(bitmap: Bitmap, inputSize: Int): PreprocessResult {
        val source = bitmap.copy(Bitmap.Config.ARGB_8888, false) ?: bitmap
        val scale = min(
            inputSize.toFloat() / source.width.toFloat(),
            inputSize.toFloat() / source.height.toFloat()
        )
        val resizedWidth = (source.width * scale).roundToInt()
        val resizedHeight = (source.height * scale).roundToInt()
        val padX = (inputSize - resizedWidth) / 2f
        val padY = (inputSize - resizedHeight) / 2f

        val modelBitmap = Bitmap.createBitmap(inputSize, inputSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(modelBitmap)
        canvas.drawColor(Color.rgb(114, 114, 114))
        val destination = RectF(padX, padY, padX + resizedWidth, padY + resizedHeight)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(source, null, destination, paint)

        val inputBuffer = ByteBuffer
            .allocateDirect(inputSize * inputSize * CHANNELS * FLOAT_BYTES)
            .order(ByteOrder.nativeOrder())
        val pixels = IntArray(inputSize * inputSize)
        modelBitmap.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize)

        for (pixel in pixels) {
            inputBuffer.putFloat(((pixel shr 16) and 0xFF) / 255f)
            inputBuffer.putFloat(((pixel shr 8) and 0xFF) / 255f)
            inputBuffer.putFloat((pixel and 0xFF) / 255f)
        }
        inputBuffer.rewind()

        return PreprocessResult(
            inputBuffer = inputBuffer,
            scale = scale,
            padX = padX,
            padY = padY,
            originalWidth = source.width,
            originalHeight = source.height,
            inputSize = inputSize
        )
    }
}
