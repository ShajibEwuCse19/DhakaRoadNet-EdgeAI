package com.example.dhakaroadnet

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executors

class DetectionPresenter(
    context: Context,
    private val view: DetectionContract.View
) {
    private val detector = DhakaRoadNetDetector(context)
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    fun runDetection(bitmap: Bitmap) {
        view.setLoading(true)
        executor.execute {
            try {
                val output = detector.detect(bitmap)
                val annotatedBitmap = DetectionRenderer.draw(bitmap, output.detections)
                mainHandler.post {
                    view.setLoading(false)
                    view.showDetectionResult(output, annotatedBitmap)
                }
            } catch (exception: Exception) {
                mainHandler.post {
                    view.setLoading(false)
                    view.showError(exception.message ?: "Detection failed.")
                }
            }
        }
    }

    fun release() {
        detector.close()
        executor.shutdownNow()
    }
}
