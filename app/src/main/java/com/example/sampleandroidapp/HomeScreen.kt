package com.example.sampleandroidapp

import android.graphics.Color
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HomeScreen(
    private val context: MainActivity,
    private val repository: TaskRepository,
    private val scheduler: ReminderScheduler,
    private val initialLevelChange: LevelChange = LevelChange.NONE,
    private val onAddTask: () -> Unit,
    private val onTaskUpdated: () -> Unit,
) {
    private class PinIconView(context: MainActivity, private val color: Int) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = Ui.dp(context, 1).toFloat()
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            paint.color = color
            val centerX = width / 2f
            val centerY = height / 2f
            val body = Path().apply {
                moveTo(centerX, centerY - Ui.dp(context, 9))
                cubicTo(
                    centerX - Ui.dp(context, 6), centerY - Ui.dp(context, 9),
                    centerX - Ui.dp(context, 8), centerY - Ui.dp(context, 3),
                    centerX - Ui.dp(context, 6), centerY + Ui.dp(context, 2),
                )
                lineTo(centerX, centerY + Ui.dp(context, 9))
                lineTo(centerX + Ui.dp(context, 6), centerY + Ui.dp(context, 2))
                cubicTo(
                    centerX + Ui.dp(context, 8), centerY - Ui.dp(context, 3),
                    centerX + Ui.dp(context, 6), centerY - Ui.dp(context, 9),
                    centerX, centerY - Ui.dp(context, 9),
                )
                close()
            }
            canvas.drawPath(body, paint)
            canvas.drawLine(
                centerX,
                centerY + Ui.dp(context, 9),
                centerX,
                centerY + Ui.dp(context, 13),
                paint,
            )
            canvas.drawLine(
                centerX - Ui.dp(context, 4),
                centerY + Ui.dp(context, 13),
                centerX + Ui.dp(context, 4),
                centerY + Ui.dp(context, 13),
                paint,
            )
        }
    }

    fun build(): View {
        val root = FrameLayout(context).apply { setBackgroundColor(Ui.BG) }
        val scroll = ScrollView(context).apply {
            overScrollMode = View.OVER_SCROLL_NEVER
            clipToPadding = false
        }

        class CategoryIconView(context: MainActivity, private val category: String) : View(context) {
            private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = Ui.dp(context, 2).toFloat()
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
            }

            override fun onDraw(canvas: Canvas) {
                super.onDraw(canvas)
                paint.color = when (category) {
                    "Physical" -> Color.rgb(255, 164, 92)
                    "Learning" -> Color.rgb(133, 157, 255)
                    "Nutrition" -> Color.rgb(110, 220, 180)
                    else -> Ui.MUTED
                }
                val centerX = width / 2f
                val centerY = height / 2f
                when (category) {
                    "Physical" -> {
                        val path = Path().apply {
                            moveTo(centerX + 5, centerY - 17)
                            lineTo(centerX - 7, centerY - 1)
                            lineTo(centerX + 1, centerY - 1)
                            lineTo(centerX - 5, centerY + 17)
                            lineTo(centerX + 9, centerY - 5)
                            lineTo(centerX + 1, centerY - 5)
                            close()
                        }
                        paint.style = Paint.Style.FILL
                        canvas.drawPath(path, paint)
                    }
                    "Learning" -> {
                        paint.style = Paint.Style.STROKE
                        val book = Path().apply {
                            moveTo(centerX, centerY - 12)
                            cubicTo(centerX - 7, centerY - 15, centerX - 13, centerY - 14, centerX - 16, centerY - 10)
                            lineTo(centerX - 16, centerY + 12)
                            cubicTo(centerX - 10, centerY + 9, centerX - 5, centerY + 9, centerX, centerY + 13)
                            moveTo(centerX, centerY - 12)
                            cubicTo(centerX + 7, centerY - 15, centerX + 13, centerY - 14, centerX + 16, centerY - 10)
                            lineTo(centerX + 16, centerY + 12)
                            cubicTo(centerX + 10, centerY + 9, centerX + 5, centerY + 9, centerX, centerY + 13)
                        }
                        canvas.drawPath(book, paint)
                        canvas.drawLine(centerX, centerY - 11, centerX, centerY + 12, paint)
                    }
                    "Nutrition" -> {
                        paint.style = Paint.Style.FILL
                        canvas.drawOval(centerX - 12, centerY - 6, centerX + 12, centerY + 15, paint)
                        paint.style = Paint.Style.STROKE
                        canvas.drawLine(centerX, centerY + 12, centerX, centerY + 18, paint)
                        canvas.drawArc(centerX, centerY - 17, centerX + 11, centerY - 5, 200f, 125f, false, paint)
                    }
                }
            }
        }
        val content = Ui.column(context, 20, 18, 20, 16)
        val header = LinearLayout(context).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(Ui.dp(context, 20), Ui.dp(context, 12), Ui.dp(context, 20), Ui.dp(context, 12))
            background = Ui.rounded(context, Ui.SURFACE, 0)
            elevation = Ui.dp(context, 4).toFloat()
        }
        val title = Ui.column(context, 0, 0, 0, 0)
        val greeting = Ui.label(context, Ui.greeting(), 13, Ui.MUTED)
        val statement = Ui.label(context, "Your arc continues.", 21, Ui.TEXT, true)
        val date = Ui.label(context, SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date()), 13, Ui.MUTED)
        title.addView(greeting)
        title.addView(statement, Ui.margin(context, 0, 2, 0, 0))
        title.addView(date, Ui.margin(context, 0, 4, 0, 0))
        header.addView(title, LinearLayout.LayoutParams(0, -2, 1f))
        val levelBadge = Ui.label(context, "LVL ${repository.level}", 13, Ui.ACCENT, true).apply {
            gravity = Gravity.CENTER
            background = Ui.rounded(context, Color.rgb(43, 53, 89), 100)
            setPadding(Ui.dp(context, 16), Ui.dp(context, 10), Ui.dp(context, 16), Ui.dp(context, 10))
        }
        header.addView(levelBadge)
        val summary = summaryCard()
        content.addView(summary.view, Ui.margin(context, 0, 0, 0, 14))
        if (initialLevelChange != LevelChange.NONE) {
            levelBadge.post { animateLevelChange(levelBadge, summary.view, initialLevelChange) }
        }
        val questHeader = LinearLayout(context).apply { gravity = Gravity.CENTER_VERTICAL }
        questHeader.addView(Ui.label(context, "Today's quests", 19, Ui.TEXT, true), LinearLayout.LayoutParams(0, -2, 1f))
        questHeader.addView(Ui.label(context, "${repository.tasks.size} total", 13, Ui.MUTED))
        content.addView(questHeader, Ui.margin(context, 0, 0, 0, 8))
        val quests = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        val questViews = mutableMapOf<String, View>()
        lateinit var refreshQuests: () -> Unit
        refreshQuests = {
            val oldPositions = questViews.mapValues { (_, view) -> view.top }
            val orderedTasks = sortedTasks()
            orderedTasks.forEachIndexed { index, task ->
                questViews.getOrPut(task.id) {
                    questRow(task, index, levelBadge, summary.view, summary.update, refreshQuests)
                }.also { view ->
                    updateQuestRow(view, task)
                }
            }
            questViews.keys.retainAll(orderedTasks.map { it.id }.toSet())
            quests.removeAllViews()
            orderedTasks.forEach { task ->
                quests.addView(questViews.getValue(task.id), Ui.margin(context, 0, 0, 0, 7))
            }
            quests.post {
                orderedTasks.forEach { task ->
                    val view = questViews.getValue(task.id)
                    val previousTop = oldPositions[task.id] ?: view.top
                    val delta = previousTop - view.top
                    if (delta != 0) {
                        view.translationY = delta.toFloat()
                        view.animate().translationY(0f).setDuration(280)
                            .setInterpolator(AccelerateDecelerateInterpolator()).start()
                    }
                }
            }
        }
        refreshQuests()
        content.addView(quests)
        content.addView(Button(context).apply {
            text = "+  Add custom quest"
            textSize = 14f
            isAllCaps = false
            minHeight = Ui.dp(context, 56)
            minimumHeight = Ui.dp(context, 56)
            setTextColor(Ui.ACCENT)
            background = Ui.rounded(context, Color.rgb(36, 45, 73), 18)
            setOnClickListener { onAddTask() }
        }, Ui.margin(context, 0, 4, 0, 0))
        scroll.addView(content)

        val expandedHeight = Ui.dp(context, 104)
        val compactHeight = Ui.dp(context, 70)
        root.addView(scroll, FrameLayout.LayoutParams(-1, -1).apply {
            topMargin = expandedHeight
        })
        root.addView(header, FrameLayout.LayoutParams(-1, expandedHeight).apply {
            gravity = Gravity.TOP
        })
        scroll.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            val compact = scrollY > Ui.dp(context, 20)
            val targetHeight = if (compact) compactHeight else expandedHeight
            val params = header.layoutParams
            if (params.height != targetHeight) {
                header.animate().cancel()
                header.layoutParams = params.apply { height = targetHeight }
                header.setPadding(
                    Ui.dp(context, 20),
                    Ui.dp(context, if (compact) 6 else 12),
                    Ui.dp(context, 20),
                    Ui.dp(context, if (compact) 6 else 12),
                )
                greeting.animate().alpha(if (compact) 0f else 1f).setDuration(160).start()
                statement.animate().scaleX(if (compact) 0.92f else 1f)
                    .scaleY(if (compact) 0.92f else 1f)
                    .setDuration(160)
                    .start()
                date.animate().alpha(1f).setDuration(160).start()
            }
        }
        return root
    }

    private fun sortedTasks(): List<Task> {
        val now = Calendar.getInstance()
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val minutesInDay = 24 * 60
        fun taskMinutes(task: Task): Int {
            val parts = task.time.split(":").map(String::toInt)
            return parts[0] * 60 + parts[1]
        }
        fun distanceFromNow(task: Task): Int =
            (taskMinutes(task) - currentMinutes + minutesInDay) % minutesInDay

        return repository.tasks.sortedWith(
            compareByDescending<Task> { repository.isPinned(it) }
                .thenBy { if (repository.isPinned(it)) 0 else if (repository.isCompleted(it)) 2 else 1 }
                .thenBy { if (repository.isPinned(it)) 0 else isMissed(it, currentMinutes) }
                .thenBy { if (repository.isPinned(it)) 0 else if (isMissed(it, currentMinutes)) taskMinutes(it) else distanceFromNow(it) },
        )
    }

    private fun isMissed(task: Task, currentMinutes: Int): Boolean =
        !repository.isCompleted(task) && task.time.toMinutes() < currentMinutes

    private fun String.toMinutes(): Int {
        val parts = split(":").map(String::toInt)
        return parts[0] * 60 + parts[1]
    }

    private fun summaryCard(): SummaryCard {
        val card = Ui.column(context, 20, 20, 20, 20).apply { background = Ui.rounded(context, Ui.ACCENT, 24) }
        val row = LinearLayout(context).apply { gravity = Gravity.CENTER_VERTICAL }
        val copy = Ui.column(context, 0, 0, 0, 0)
        copy.addView(Ui.label(context, "TODAY'S MOMENTUM", 12, Color.rgb(220, 227, 255), true))
        val progressLabel = Ui.label(context, "", 22, Color.WHITE, true)
        copy.addView(progressLabel, Ui.margin(context, 0, 8, 0, 0))
        row.addView(copy, LinearLayout.LayoutParams(0, -2, 1f))
        val xpLabel = Ui.label(context, "", 18, Color.WHITE, true).apply { gravity = Gravity.CENTER }
        row.addView(xpLabel)
        card.addView(row)
        val track = FrameLayout(context).apply {
            background = Ui.rounded(context, Color.argb(80, 255, 255, 255), 100)
        }
        val fill = View(context).apply { background = Ui.rounded(context, Color.WHITE, 100) }
        track.addView(fill, FrameLayout.LayoutParams(0, Ui.dp(context, 6)))
        card.addView(track, Ui.margin(context, 0, 18, 0, 0))
        fun update() {
            progressLabel.text = "${repository.completedToday()} / ${repository.tasks.size} quests claimed"
            val levelProgress = repository.xp % 100
            xpLabel.text = "${levelProgress}%\nXP"
            track.post {
                fill.layoutParams = FrameLayout.LayoutParams(
                    (track.width * (levelProgress / 100f)).toInt(),
                    Ui.dp(context, 6),
                )
            }
        }
        update()
        return SummaryCard(card, ::update)
    }

    private fun questRow(
        task: Task,
        index: Int,
        levelBadge: TextView,
        summaryCard: View,
        updateSummary: () -> Unit,
        refreshQuests: () -> Unit,
    ): View {
        val row = LinearLayout(context).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(Ui.dp(context, 16), Ui.dp(context, 16), Ui.dp(context, 14), Ui.dp(context, 16))
        }
        row.addView(View(context), LinearLayout.LayoutParams(Ui.dp(context, 5), Ui.dp(context, 52)))
        row.addView(View(context), LinearLayout.LayoutParams(Ui.dp(context, 48), Ui.dp(context, 48)))
        val copy = Ui.column(context, 0, 0, 0, 0)
        if (task.category.isNotEmpty()) {
            copy.addView(Ui.label(context, task.category.uppercase(), 10, categoryColor(task.category), true), Ui.margin(context, 0, 0, 0, 3))
        }
        val title = Ui.label(context, task.name, 16, Ui.TEXT, true)
        copy.addView(title)
        copy.addView(Ui.label(context, "${task.detail}  ·  ${Ui.displayTime(task.time)}  ·  +${task.xp} XP", 12, Ui.MUTED), Ui.margin(context, 0, 4, 0, 0))
        row.addView(copy, LinearLayout.LayoutParams(0, -2, 1f).apply { setMargins(Ui.dp(context, 10), 0, Ui.dp(context, 6), 0) })
        updateQuestRow(row, task)
        val handler = Handler(Looper.getMainLooper())
        var pendingTap: Runnable? = null
        row.setOnClickListener {
            pendingTap?.let {
                handler.removeCallbacks(it)
                pendingTap = null
                repository.togglePinned(task)
                refreshQuests()
                return@setOnClickListener
            }
            pendingTap = Runnable {
                pendingTap = null
                val levelChange = repository.toggle(task)
                if (levelChange != LevelChange.NONE) {
                    levelBadge.text = "LVL ${repository.level}"
                    animateLevelChange(levelBadge, summaryCard, levelChange)
                }
                updateSummary()
                refreshQuests()
            }.also { handler.postDelayed(it, ViewConfiguration.getDoubleTapTimeout().toLong()) }
        }
        row.setOnLongClickListener {
            QuestDialogs(context, repository, scheduler).showEditTaskDialog(task) {
                onTaskUpdated()
            }
            true
        }
        return row
    }

    private fun updateQuestRow(row: View, task: Task) {
        val container = row as LinearLayout
        val complete = repository.isCompleted(task)
        val missed = !complete && task.time.toMinutes() < Calendar.getInstance().let {
            it.get(Calendar.HOUR_OF_DAY) * 60 + it.get(Calendar.MINUTE)
        }
        container.background = when {
            repository.isPinned(task) -> Ui.outlined(context, Ui.SURFACE, Ui.GOLD, 1, 20)
            complete -> Ui.rounded(context, Color.rgb(28, 52, 55), 20)
            missed -> Ui.outlined(context, Ui.SURFACE, Ui.RED, 1, 20)
            else -> Ui.rounded(context, Ui.SURFACE, 20)
        }
        container.getChildAt(0).background = Ui.rounded(context, categoryColor(task.category), 100)
        val oldCheck = container.getChildAt(1)
        val check: View = when {
            repository.isPinned(task) -> PinIconView(context, Ui.GOLD)
            complete -> Ui.label(context, "✓", 23, Ui.GREEN).apply { gravity = Gravity.CENTER }
            task.category.isNotEmpty() -> CategoryIconView(context, task.category)
            else -> Ui.label(context, "○", 23, Ui.ACCENT).apply { gravity = Gravity.CENTER }
        }
        container.removeViewAt(1)
        container.addView(check, 1, oldCheck.layoutParams)
        val copy = container.getChildAt(2) as LinearLayout
        val titleIndex = if (task.category.isNotEmpty()) 1 else 0
        (copy.getChildAt(titleIndex) as TextView).setTextColor(if (complete) Ui.MUTED else Ui.TEXT)
    }

    private data class SummaryCard(val view: View, val update: () -> Unit)

    private fun categoryColor(category: String): Int = when (category) {
        "Physical" -> Color.rgb(255, 139, 103)
        "Learning" -> Color.rgb(133, 157, 255)
        "Nutrition" -> Color.rgb(110, 220, 180)
        else -> Ui.MUTED
    }

    private fun animateLevelChange(badge: TextView, card: View, change: LevelChange) {
        val isUpgrade = change == LevelChange.UP
        val highlight = if (isUpgrade) Ui.GOLD else Ui.RED
        val tone = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
        tone.startTone(if (isUpgrade) ToneGenerator.TONE_PROP_BEEP2 else ToneGenerator.TONE_PROP_NACK, 180)
        badge.postDelayed({ tone.release() }, 300)

        ValueAnimator.ofObject(ArgbEvaluator(), Color.rgb(43, 53, 89), highlight, Color.rgb(43, 53, 89)).apply {
            duration = 900
            addUpdateListener { badge.background = Ui.rounded(context, it.animatedValue as Int, 100) }
            start()
        }

        badge.setTextColor(highlight)
        badge.animate().cancel()
        badge.scaleX = 1f
        badge.scaleY = 1f
        badge.animate().scaleX(1.18f).scaleY(1.18f).setDuration(180).withEndAction {
            badge.animate().scaleX(1f).scaleY(1f).setDuration(260).start()
        }.start()
        badge.postDelayed({ badge.setTextColor(Ui.ACCENT) }, 900)
        ValueAnimator.ofObject(ArgbEvaluator(), Ui.ACCENT, highlight, Ui.ACCENT).apply {
            duration = 900
            addUpdateListener { card.background = Ui.rounded(context, it.animatedValue as Int, 24) }
            start()
        }
        card.animate().cancel()
        card.scaleX = 1f
        card.scaleY = 1f
        card.animate().scaleX(1.03f).scaleY(1.03f).setDuration(180).withEndAction {
            card.animate().scaleX(1f).scaleY(1f).setDuration(260).start()
        }.start()
    }

}
