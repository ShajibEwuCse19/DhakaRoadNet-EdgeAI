package com.example.dhakaroadnet

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.atomic.AtomicBoolean

class DetectionPresenter(
    context: Context,
    private val view: DetectionContract.View
) {
    private val detector = DhakaRoadNetDetector(context)
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val released = AtomicBoolean(false)

    fun runDetection(bitmap: Bitmap) {
        if (released.get()) return
        view.setLoading(true)
        executor.execute {
            try {
                val output = detector.detect(bitmap)
                val annotatedBitmap = DetectionRenderer.draw(bitmap, output.detections)
                mainHandler.post {
                    if (released.get()) return@post
                    view.setLoading(false)
                    view.showDetectionResult(output, annotatedBitmap)
                }
            } catch (exception: Exception) {
                mainHandler.post {
                    if (released.get()) return@post
                    view.setLoading(false)
                    view.showError(exception.message ?: "Detection failed.")
                }
            }
        }
    }

    fun release() {
        if (!released.compareAndSet(false, true)) return

        try {
            executor.execute { detector.close() }
        } catch (_: RejectedExecutionException) {
            detector.close()
        } finally {
            executor.shutdown()
            mainHandler.removeCallbacksAndMessages(null)
        }
    }
}
