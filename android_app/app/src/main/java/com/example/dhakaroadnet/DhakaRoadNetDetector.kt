package com.example.dhakaroadnet

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.os.SystemClock
import org.tensorflow.lite.Interpreter
import java.io.Closeable
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class DhakaRoadNetDetector(
    context: Context,
    private val threadCount: Int = DEFAULT_THREAD_COUNT
) : Closeable {
    private val appContext = context.applicationContext
    private val labels: List<String> = loadLabels()
    private val interpreterLock = Any()
    @Volatile
    private var closed = false
    private val interpreterDelegate = lazy {
        Interpreter(loadModel(), Interpreter.Options().apply {
            setNumThreads(threadCount)
        })
    }
    private val interpreter: Interpreter
        get() = interpreterDelegate.value

    fun detect(bitmap: Bitmap, confidenceThreshold: Float = DEFAULT_CONFIDENCE): DetectionOutput {
        val prepared = ImagePreprocessor.prepare(bitmap, INPUT_SIZE)
        val output = Array(1) { Array(MAX_DETECTIONS) { FloatArray(OUTPUT_COLUMNS) } }

        val startedAt = SystemClock.elapsedRealtimeNanos()
        synchronized(interpreterLock) {
            check(!closed) { "Detector has already been released." }
            interpreter.run(prepared.inputBuffer, output)
        }
        val inferenceTimeMs = (SystemClock.elapsedRealtimeNanos() - startedAt) / 1_000_000

        val detections = parseDetections(output[0], prepared, confidenceThreshold)
        return DetectionOutput(
            detections = detections,
            inferenceTimeMs = inferenceTimeMs,
            confidenceThreshold = confidenceThreshold,
            imageWidth = bitmap.width,
            imageHeight = bitmap.height
        )
    }

    private fun parseDetections(
        rows: Array<FloatArray>,
        prepared: PreprocessResult,
        threshold: Float
    ): List<DetectionResult> {
        return rows.mapNotNull { row ->
            val confidence = row[4]
            if (confidence.isNaN() || confidence < threshold) return@mapNotNull null

            val classId = row[5].roundToInt()
            if (classId !in labels.indices) return@mapNotNull null

            val box = mapBoxToOriginalImage(row, prepared)
            if (box.width() < 2f || box.height() < 2f) return@mapNotNull null

            DetectionResult(
                classId = classId,
                label = labels[classId],
                confidence = confidence,
                box = box
            )
        }.sortedByDescending { it.confidence }
    }

    private fun mapBoxToOriginalImage(row: FloatArray, prepared: PreprocessResult): RectF {
        val inputSize = prepared.inputSize.toFloat()
        val x1 = ((row[0] * inputSize) - prepared.padX) / prepared.scale
        val y1 = ((row[1] * inputSize) - prepared.padY) / prepared.scale
        val x2 = ((row[2] * inputSize) - prepared.padX) / prepared.scale
        val y2 = ((row[3] * inputSize) - prepared.padY) / prepared.scale

        val left = min(max(x1, 0f), prepared.originalWidth.toFloat())
        val top = min(max(y1, 0f), prepared.originalHeight.toFloat())
        val right = min(max(x2, 0f), prepared.originalWidth.toFloat())
        val bottom = min(max(y2, 0f), prepared.originalHeight.toFloat())

        return RectF(
            min(left, right),
            min(top, bottom),
            max(left, right),
            max(top, bottom)
        )
    }

    private fun loadModel(): MappedByteBuffer {
        appContext.assets.openFd(MODEL_FILE).use { descriptor ->
            FileInputStream(descriptor.fileDescriptor).use { input ->
                return input.channel.map(
                    FileChannel.MapMode.READ_ONLY,
                    descriptor.startOffset,
                    descriptor.declaredLength
                )
            }
        }
    }

    private fun loadLabels(): List<String> {
        return appContext.assets.open(LABELS_FILE).bufferedReader().useLines { lines ->
            lines.map { it.trim() }
                .filter { it.isNotEmpty() }
                .toList()
        }
    }

    override fun close() {
        synchronized(interpreterLock) {
            if (!closed && interpreterDelegate.isInitialized()) {
                interpreterDelegate.value.close()
            }
            closed = true
        }
    }

    companion object {
        const val MODEL_FILE = "dhakaroadnet_yolov8n_fp16.tflite"
        const val LABELS_FILE = "labels.txt"
        const val INPUT_SIZE = 640
        const val DEFAULT_THREAD_COUNT = 4
        const val LIVE_THREAD_COUNT = 1
        const val THREAD_COUNT = DEFAULT_THREAD_COUNT
        const val DEFAULT_CONFIDENCE = 0.25f
        private const val MAX_DETECTIONS = 300
        private const val OUTPUT_COLUMNS = 6
    }
}
