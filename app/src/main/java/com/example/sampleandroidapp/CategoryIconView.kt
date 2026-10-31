package com.example.sampleandroidapp

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.view.View

class CategoryIconView(context: MainActivity, private val category: String) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = Ui.dp(context, 2).toFloat()
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    override fun onDraw(canvas: Canvas) {
        paint.color = when (category) {
            "Physical" -> Color.rgb(255, 164, 92)
            "Learning" -> Color.rgb(133, 157, 255)
            "Nutrition" -> Color.rgb(110, 220, 180)
            else -> Ui.MUTED
        }
        val x = width / 2f
        val y = height / 2f
        when (category) {
            "Physical" -> {
                paint.style = Paint.Style.FILL
                canvas.drawPath(Path().apply {
                    moveTo(x + 5, y - 17)
                    lineTo(x - 7, y - 1)
                    lineTo(x + 1, y - 1)
                    lineTo(x - 5, y + 17)
                    lineTo(x + 9, y - 5)
                    lineTo(x + 1, y - 5)
                    close()
                }, paint)
            }
            "Learning" -> {
                paint.style = Paint.Style.STROKE
                canvas.drawRoundRect(x - 16, y - 13, x + 16, y + 13, 3f, 3f, paint)
                canvas.drawLine(x, y - 12, x, y + 12, paint)
                canvas.drawLine(x - 11, y - 6, x - 4, y - 6, paint)
                canvas.drawLine(x + 4, y - 6, x + 11, y - 6, paint)
            }
            "Nutrition" -> {
                paint.style = Paint.Style.FILL
                canvas.drawOval(x - 12, y - 6, x + 12, y + 15, paint)
                paint.style = Paint.Style.STROKE
                canvas.drawLine(x, y + 12, x, y + 18, paint)
                canvas.drawArc(x, y - 17, x + 11, y - 5, 200f, 125f, false, paint)
            }
        }
    }
}
