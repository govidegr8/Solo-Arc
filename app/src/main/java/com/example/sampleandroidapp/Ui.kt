package com.example.sampleandroidapp

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.math.roundToInt

object Ui {
    val BG = android.graphics.Color.rgb(15, 20, 36)
    val SURFACE = android.graphics.Color.rgb(29, 36, 59)
    val TEXT = android.graphics.Color.rgb(242, 244, 255)
    val MUTED = android.graphics.Color.rgb(164, 174, 202)
    val ACCENT = android.graphics.Color.rgb(133, 157, 255)
    val GREEN = android.graphics.Color.rgb(110, 220, 180)
    val GOLD = android.graphics.Color.rgb(255, 202, 92)
    val RED = android.graphics.Color.rgb(255, 112, 124)

    fun dp(context: Context, value: Int) =
        (value * context.resources.displayMetrics.density).roundToInt()

    fun column(context: Context, l: Int, t: Int, r: Int, b: Int) =
        LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(context, l), dp(context, t), dp(context, r), dp(context, b))
        }

    fun label(context: Context, text: String, size: Int, color: Int, bold: Boolean = false) =
        TextView(context).apply {
            this.text = text
            textSize = size.toFloat()
            setTextColor(color)
            includeFontPadding = false
            if (bold) typeface = Typeface.DEFAULT_BOLD
        }

    fun rounded(context: Context, color: Int, radius: Int) =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(context, radius).toFloat()
        }

    fun outlined(context: Context, fill: Int, stroke: Int, width: Int, radius: Int) =
        GradientDrawable().apply {
            setColor(fill)
            setStroke(dp(context, width), stroke)
            cornerRadius = dp(context, radius).toFloat()
        }

    fun margin(context: Context, l: Int, t: Int, r: Int, b: Int) =
        LinearLayout.LayoutParams(-1, -2).apply {
            setMargins(dp(context, l), dp(context, t), dp(context, r), dp(context, b))
        }

    fun displayTime(value: String): String {
        val parts = value.split(":").map(String::toInt)
        val hour = parts[0]
        val displayHour = if (hour % 12 == 0) 12 else hour % 12
        return "$displayHour:${"%02d".format(parts[1])} ${if (hour < 12) "AM" else "PM"}"
    }

    fun greeting(): String = when (java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)) {
        in 5..11 -> "Good morning,"
        in 12..17 -> "Good afternoon,"
        else -> "Good evening,"
    }
}
