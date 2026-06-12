package com.example.dhakaroadnet

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream

/**
 * @author Shajib
 */
object LiveFrameConverter {
    fun toBitmap(image: ImageProxy): Bitmap {
        val nv21 = yuv420ToNv21(image)
        val yuvImage = YuvImage(
            nv21,
            ImageFormat.NV21,
            image.width,
            image.height,
            null
        )
        val output = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), JPEG_QUALITY, output)
        val bytes = output.toByteArray()
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            ?: error("Could not decode camera frame.")

        return rotateBitmap(bitmap, image.imageInfo.rotationDegrees)
    }

    private fun yuv420ToNv21(image: ImageProxy): ByteArray {
        val width = image.width
        val height = image.height
        val yPlane = image.planes[0]
        val uPlane = image.planes[1]
        val vPlane = image.planes[2]
        val output = ByteArray(width * height * 3 / 2)

        var outputOffset = 0
        for (row in 0 until height) {
            val rowOffset = row * yPlane.rowStride
            for (col in 0 until width) {
                output[outputOffset++] = yPlane.buffer.get(rowOffset + col * yPlane.pixelStride)
            }
        }

        val chromaHeight = height / 2
        val chromaWidth = width / 2
        for (row in 0 until chromaHeight) {
            val uRowOffset = row * uPlane.rowStride
            val vRowOffset = row * vPlane.rowStride
            for (col in 0 until chromaWidth) {
                output[outputOffset++] = vPlane.buffer.get(vRowOffset + col * vPlane.pixelStride)
                output[outputOffset++] = uPlane.buffer.get(uRowOffset + col * uPlane.pixelStride)
            }
        }

        return output
    }

    private fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
        if (rotationDegrees == 0) return bitmap

        val matrix = Matrix().apply {
            postRotate(rotationDegrees.toFloat())
        }
        val rotated = Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
        bitmap.recycle()
        return rotated
    }

    private const val JPEG_QUALITY = 80
}
