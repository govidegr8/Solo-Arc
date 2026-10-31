package com.example.sampleandroidapp

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class ProfileScreen(
    private val context: MainActivity,
    private val repository: TaskRepository,
    private val scheduler: ReminderScheduler,
    private val workoutRepository: WorkoutRepository,
) {
    fun build(): View {
        val content = Ui.column(context, 20, 22, 20, 24)
        content.addView(Ui.label(context, "Your system", 28, Ui.TEXT, true))
        content.addView(Ui.label(context, "A quiet place to reset and keep going.", 15, Ui.MUTED), Ui.margin(context, 0, 5, 0, 22))
        val card = Ui.column(context, 18, 18, 18, 18).apply { background = Ui.rounded(context, Ui.SURFACE, 24) }
        card.addView(Ui.label(context, "SOLO ARC", 12, Ui.ACCENT, true))
        card.addView(Ui.label(context, "Focused. Dedicated. Unstoppable.", 20, Ui.TEXT, true), Ui.margin(context, 0, 10, 0, 4))
        card.addView(Ui.label(context, "Daily system reminder", 12, Ui.MUTED, true))
        val reminder = TextView(context).apply {
            text = "⏰  Remind me at ${Ui.displayTime(repository.reminderTime)}"
            textSize = 15f
            setTextColor(Ui.ACCENT)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(Ui.dp(context, 16), 0, Ui.dp(context, 16), 0)
            background = Ui.rounded(context, Color.rgb(42, 52, 83), 16)
            setOnClickListener {
                QuestDialogs(this@ProfileScreen.context, repository, scheduler).showTimePicker(repository.reminderTime) { hour, minute ->
                    repository.reminderTime = "%02d:%02d".format(hour, minute)
                    text = "⏰  Remind me at ${Ui.displayTime(repository.reminderTime)}"
                    scheduler.prepare(repository.tasks, repository.reminderTime)
                }
            }
        }
        card.addView(reminder, LinearLayout.LayoutParams(-1, Ui.dp(context, 56)).apply { topMargin = Ui.dp(context, 10) })
        content.addView(card)
        content.addView(dailyQuote(), Ui.margin(context, 0, 16, 0, 0))
        content.addView(workoutTracker(), Ui.margin(context, 0, 16, 0, 0))
        content.addView(nutritionPlaceholder(), Ui.margin(context, 0, 16, 0, 0))
        content.addView(Ui.label(context, "Your quests are stored locally on this device.", 14, Ui.MUTED), Ui.margin(context, 2, 18, 2, 0))
        return ScrollView(context).apply { addView(content) }
    }

    private fun workoutTracker(): View {
        val card = Ui.column(context, 18, 18, 18, 18).apply { background = Ui.rounded(context, Ui.SURFACE, 24) }
        card.addView(Ui.label(context, "WORKOUT TRACKER", 12, Ui.GREEN, true))
        card.addView(Ui.label(context, "Build strength and stamina.", 20, Ui.TEXT, true), Ui.margin(context, 0, 8, 0, 10))
        val today = workoutRepository.entriesFor(workoutRepository.dateKey())
        val calories = today.sumOf { it.calories }
        val duration = today.sumOf { it.durationMinutes }
        val volume = today.sumOf { entry -> entry.sets.sumOf { it.reps * it.weightKg } }
        card.addView(Ui.label(context, "$duration min  ·  $calories kcal  ·  ${"%.0f".format(volume)} kg volume", 13, Ui.MUTED))
        card.addView(TextView(context).apply {
            text = "+  Log workout"
            textSize = 15f
            gravity = Gravity.CENTER
            setTextColor(Ui.ACCENT)
            background = Ui.rounded(context, Color.rgb(42, 52, 83), 16)
            setOnClickListener { WorkoutDialogs(this@ProfileScreen.context, workoutRepository).showEntryDialog { showTabRefresh() } }
        }, LinearLayout.LayoutParams(-1, Ui.dp(context, 52)).apply { topMargin = Ui.dp(context, 12) })
        today.forEach { entry ->
            val detail = if (entry.type == "Cardio") {
                "${entry.durationMinutes} min · ${entry.calories} kcal"
            } else {
                "${entry.sets.size} sets · ${entry.sets.sumOf { it.reps }} reps · ${"%.1f".format(entry.sets.sumOf { it.reps * it.weightKg })} kg"
            }
            val entryView = Ui.label(context, "${entry.activity}  ·  $detail", 13, Ui.TEXT)
            entryView.setOnClickListener {
                WorkoutDialogs(this@ProfileScreen.context, workoutRepository).showEntryDialog(entry) { showTabRefresh() }
            }
            card.addView(entryView, Ui.margin(context, 0, 12, 0, 0))
        }
        card.addView(Ui.label(context, "Weekly: ${workoutRepository.entries.count { it.date >= workoutRepository.dateDaysAgo(6) }} sessions  ·  Monthly: ${workoutRepository.entries.count { it.date.startsWith(workoutRepository.dateKey().substring(0, 7)) }} sessions", 12, Ui.MUTED), Ui.margin(context, 0, 14, 0, 0))
        return card
    }

    private fun nutritionPlaceholder(): View =
        Ui.column(context, 18, 18, 18, 18).apply {
            background = Ui.rounded(context, Color.rgb(42, 52, 83), 22)
            isClickable = true
            isFocusable = true
            setOnClickListener { this@ProfileScreen.context.openNutritionCamera() }
            addView(Ui.label(context, "NUTRITION PHOTO TRACKING", 12, Ui.MUTED, true))
            addView(Ui.label(context, "Coming soon", 18, Ui.TEXT, true), Ui.margin(context, 0, 8, 0, 0))
            addView(Ui.label(context, "Food photo calorie and protein tracking will be added later.", 13, Ui.MUTED), Ui.margin(context, 0, 4, 0, 0))
        }

    private fun showTabRefresh() {
        context.refreshCurrentTab()
    }

    private fun dailyQuote(): View {
        val quote = QUOTES[java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR) % QUOTES.size]
        val card = Ui.column(context, 18, 18, 18, 18).apply { background = Ui.rounded(context, Color.rgb(42, 52, 83), 22) }
        card.addView(Ui.label(context, "TODAY'S SIGNAL", 12, Ui.ACCENT, true))
        card.addView(Ui.label(context, "“${quote.first}”", 17, Ui.TEXT, true), Ui.margin(context, 0, 8, 0, 6))
        card.addView(Ui.label(context, "— ${quote.second}", 13, Ui.MUTED))
        return card
    }

    companion object {
        private val QUOTES = listOf(
            "Small disciplines repeated with consistency create remarkable results." to "Solo Arc",
            "You do not rise to the level of your goals. You fall to the level of your systems." to "James Clear",
            "The secret of getting ahead is getting started." to "Mark Twain",
            "First we make our habits, then our habits make us." to "John Dryden",
            "Success is the sum of small efforts, repeated day in and day out." to "Robert Collier",
            "The future depends on what you do today." to "Mahatma Gandhi",
            "A little progress each day adds up to big results." to "Solo Arc",
        )
    }
}
