package com.example.sampleandroidapp

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.*
import java.util.Calendar

class ProgressScreen(
    private val context: MainActivity,
    private val repository: TaskRepository,
) {
    fun build(): View {
        val content = Ui.column(context, 20, 22, 20, 24)
        content.addView(Ui.label(context, "Progress", 28, Ui.TEXT, true))
        content.addView(Ui.label(context, "Your consistency is your superpower.", 15, Ui.MUTED), Ui.margin(context, 0, 5, 0, 20))
        val stats = LinearLayout(context).apply { gravity = Gravity.CENTER }
        listOf("LEVEL\n${repository.level}", "TOTAL XP\n${repository.xp}", "STREAK\n${repository.streak} 🔥").forEach { value ->
            stats.addView(Ui.column(context, 0, 14, 0, 14).apply {
                gravity = Gravity.CENTER
                background = Ui.rounded(context, Ui.SURFACE, 20)
                addView(Ui.label(context, value, 14, Ui.TEXT, true).apply { gravity = Gravity.CENTER })
            }, LinearLayout.LayoutParams(0, Ui.dp(context, 94), 1f).apply { setMargins(0, 0, Ui.dp(context, 8), 0) })
        }
        content.addView(stats)
        content.addView(Ui.label(context, "Last 7 days", 19, Ui.TEXT, true), Ui.margin(context, 0, 24, 0, 12))
        val chart = LinearLayout(context).apply {
            gravity = Gravity.BOTTOM
            setPadding(Ui.dp(context, 12), Ui.dp(context, 18), Ui.dp(context, 12), Ui.dp(context, 12))
            background = Ui.rounded(context, Ui.SURFACE, 22)
        }
        for (offset in 6 downTo 0) {
            val date = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -offset) }.time
            val bar = View(context).apply { background = Ui.rounded(context, if (offset == 0) Ui.ACCENT else Color.rgb(87, 101, 151), 100) }
            chart.addView(bar, LinearLayout.LayoutParams(0, Ui.dp(context, (25 + repository.countForDay(date) * 18).coerceAtMost(70)), 1f).apply {
                setMargins(Ui.dp(context, 5), 0, Ui.dp(context, 5), 0)
            })
        }
        content.addView(chart, LinearLayout.LayoutParams(-1, Ui.dp(context, 120)))
        content.addView(Ui.label(context, "Complete quests consistently to build a stronger arc.", 14, Ui.MUTED), Ui.margin(context, 0, 12, 0, 0))
        content.addView(Ui.label(context, "This month", 19, Ui.TEXT, true), Ui.margin(context, 0, 24, 0, 12))
        content.addView(monthlyCalendar())
        return ScrollView(context).apply { addView(content) }
    }

    private fun monthlyCalendar(): View {
        val calendar = Calendar.getInstance()
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDay = (calendar.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }.get(Calendar.DAY_OF_WEEK) - 1
        val card = Ui.column(context, 12, 14, 12, 14).apply { background = Ui.rounded(context, Ui.SURFACE, 22) }
        val weekdays = LinearLayout(context)
        listOf("S", "M", "T", "W", "T", "F", "S").forEach {
            weekdays.addView(Ui.label(context, it, 11, Ui.MUTED, true).apply { gravity = Gravity.CENTER }, LinearLayout.LayoutParams(0, Ui.dp(context, 28), 1f))
        }
        card.addView(weekdays)
        var row: LinearLayout? = null
        for (index in 0 until firstDay + daysInMonth) {
            if (index % 7 == 0) {
                row = LinearLayout(context)
                card.addView(row, LinearLayout.LayoutParams(-1, Ui.dp(context, 42)))
            }
            val day = index - firstDay + 1
            val cell = Ui.label(context, if (day > 0) day.toString() else "", 12, if (day > 0) Ui.TEXT else Color.TRANSPARENT).apply {
                gravity = Gravity.CENTER
                if (day > 0) {
                    val date = (calendar.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, day) }.time
                    val completed = repository.completedOn(date)
                    background = Ui.rounded(
                        context,
                        when {
                            completed == repository.tasks.size -> Ui.GREEN
                            completed > 0 -> Color.rgb(67, 87, 151)
                            else -> Color.TRANSPARENT
                        },
                        100,
                    )
                }
            }
            row?.addView(cell, LinearLayout.LayoutParams(0, Ui.dp(context, 38), 1f).apply {
                setMargins(Ui.dp(context, 2), Ui.dp(context, 2), Ui.dp(context, 2), Ui.dp(context, 2))
            })
        }
        return card
    }
}
