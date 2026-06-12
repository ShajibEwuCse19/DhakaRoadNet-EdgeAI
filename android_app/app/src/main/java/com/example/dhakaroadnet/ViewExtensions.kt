package com.example.dhakaroadnet

import android.content.Context
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat

fun Context.createDetailTextView(textValue: String): TextView {
    return TextView(this).apply {
        text = textValue
        textSize = 14f
        setLineSpacing(2f, 1f)
        setTextColor(ContextCompat.getColor(this@createDetailTextView, R.color.dhaka_text_secondary))
        background = ContextCompat.getDrawable(this@createDetailTextView, R.drawable.bg_chip)
        setPadding(dp(12), dp(10), dp(12), dp(10))
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dp(8)
        }
    }
}

fun Context.dp(value: Int): Int {
    return (value * resources.displayMetrics.density).toInt()
}
