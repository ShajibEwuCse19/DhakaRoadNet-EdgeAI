package com.example.dhakaroadnet

import android.graphics.Bitmap

interface DetectionContract {
    interface View {
        fun setLoading(isLoading: Boolean)
        fun showDetectionResult(output: DetectionOutput, annotatedBitmap: Bitmap)
        fun showError(message: String)
    }
}
