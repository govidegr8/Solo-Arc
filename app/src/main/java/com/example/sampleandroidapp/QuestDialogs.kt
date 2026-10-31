package com.example.sampleandroidapp

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

class QuestDialogs(
    private val context: MainActivity,
    private val repository: TaskRepository,
    private val scheduler: ReminderScheduler?,
) {
    fun showAddTaskDialog(onAdded: () -> Unit) {
        val dialog = Dialog(context)
        val sheet = Ui.column(context, 20, 24, 20, 20).apply { background = Ui.rounded(context, Ui.SURFACE, 28) }
        sheet.addView(Ui.label(context, "New quest", 24, Ui.TEXT, true))
        sheet.addView(Ui.label(context, "Choose a small action and when you want to show up.", 14, Ui.MUTED), Ui.margin(context, 0, 8, 0, 18))
        val name = formField("Quest name")
        val detail = formField("What does success look like?")
        sheet.addView(name, LinearLayout.LayoutParams(-1, Ui.dp(context, 56)))
        sheet.addView(detail, LinearLayout.LayoutParams(-1, Ui.dp(context, 56)).apply { topMargin = Ui.dp(context, 12) })
        val category = categorySelector()
        sheet.addView(category.view, LinearLayout.LayoutParams(-1, Ui.dp(context, 56)).apply { topMargin = Ui.dp(context, 12) })
        val xpOptions = listOf(5, 10, 15, 20)
        var xpIndex = xpOptions.lastIndex
        val xpOptionsRow = LinearLayout(context).apply {
            gravity = Gravity.CENTER_VERTICAL
            background = Ui.rounded(context, Color.rgb(42, 52, 83), 16)
            setPadding(Ui.dp(context, 12), 0, Ui.dp(context, 12), 0)
        }
        xpOptionsRow.addView(Ui.label(context, "XP", 15, Ui.MUTED, true), LinearLayout.LayoutParams(Ui.dp(context, 40), -2))
        lateinit var updateXpButtons: () -> Unit
        val optionButtons = xpOptions.mapIndexed { index, value ->
            TextView(context).apply {
                text = value.toString()
                textSize = 14f
                gravity = Gravity.CENTER
                setTextColor(Ui.ACCENT)
                setOnClickListener {
                    xpIndex = index
                    updateXpButtons()
                }
            }.also { button ->
                xpOptionsRow.addView(button, LinearLayout.LayoutParams(0, Ui.dp(context, 44), 1f).apply {
                    if (index > 0) marginStart = Ui.dp(context, 6)
                })
            }
        }
        updateXpButtons = {
            optionButtons.forEachIndexed { index, button ->
                button.background = Ui.rounded(
                    context,
                    if (index == xpIndex) Color.rgb(78, 94, 150) else Color.rgb(53, 65, 103),
                    12,
                )
                button.setTextColor(if (index == xpIndex) Color.WHITE else Ui.ACCENT)
            }
        }
        updateXpButtons()
        sheet.addView(xpOptionsRow, LinearLayout.LayoutParams(-1, Ui.dp(context, 56)).apply { topMargin = Ui.dp(context, 12) })
        var selectedTime = "08:00"
        val timeButton = TextView(context).apply {
            text = "⏰  Scheduled for 8:00 AM"
            textSize = 15f
            setTextColor(Ui.ACCENT)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(Ui.dp(context, 16), 0, Ui.dp(context, 16), 0)
            background = Ui.rounded(context, Color.rgb(42, 52, 83), 16)
            setOnClickListener {
                showTimePicker(selectedTime) { hour, minute ->
                    selectedTime = "%02d:%02d".format(hour, minute)
                    text = "⏰  Scheduled for ${Ui.displayTime(selectedTime)}"
                }
            }
        }
        sheet.addView(timeButton, LinearLayout.LayoutParams(-1, Ui.dp(context, 56)).apply { topMargin = Ui.dp(context, 12) })
        val actions = LinearLayout(context).apply { gravity = Gravity.END or Gravity.CENTER_VERTICAL }
        actions.addView(sheetButton("Cancel", Ui.MUTED) { dialog.dismiss() }, LinearLayout.LayoutParams(-2, Ui.dp(context, 52)))
        actions.addView(sheetButton("Add quest", Ui.ACCENT) {
            val title = name.text.toString().trim()
            if (title.isEmpty()) {
                name.error = "Give your quest a name"
            } else {
                repository.addTask(Task("custom_${System.currentTimeMillis()}", title, detail.text.toString().trim().ifEmpty { "Personal quest" }, xpOptions[xpIndex], selectedTime, category.value()))
                scheduler?.prepare(repository.tasks, repository.reminderTime)
                dialog.dismiss()
                onAdded()
            }
        }, LinearLayout.LayoutParams(-2, Ui.dp(context, 52)).apply { marginStart = Ui.dp(context, 8) })
        sheet.addView(actions, Ui.margin(context, 0, 14, 0, 0))
        showSheet(dialog, sheet)
    }

    fun showEditTaskDialog(task: Task, onUpdated: () -> Unit) {
        val dialog = Dialog(context)
        val sheet = Ui.column(context, 20, 24, 20, 20).apply { background = Ui.rounded(context, Ui.SURFACE, 28) }
        sheet.addView(Ui.label(context, "Edit quest", 24, Ui.TEXT, true))
        val name = formField("Quest name")
        name.setText(task.name)
        val detail = formField("What does success look like?")
        detail.setText(task.detail)
        sheet.addView(name, LinearLayout.LayoutParams(-1, Ui.dp(context, 56)))
        sheet.addView(detail, LinearLayout.LayoutParams(-1, Ui.dp(context, 56)).apply { topMargin = Ui.dp(context, 12) })
        val category = categorySelector(task.category)
        sheet.addView(category.view, LinearLayout.LayoutParams(-1, Ui.dp(context, 56)).apply { topMargin = Ui.dp(context, 12) })

        val xpOptions = listOf(5, 10, 15, 20)
        var xpIndex = xpOptions.indexOf(task.xp).coerceAtLeast(0)
        val xpRow = LinearLayout(context).apply {
            gravity = Gravity.CENTER_VERTICAL
            background = Ui.rounded(context, Color.rgb(42, 52, 83), 16)
            setPadding(Ui.dp(context, 12), 0, Ui.dp(context, 12), 0)
        }
        xpRow.addView(Ui.label(context, "XP", 15, Ui.MUTED, true), LinearLayout.LayoutParams(Ui.dp(context, 40), -2))
        lateinit var updateXpButtons: () -> Unit
        val xpButtons = xpOptions.mapIndexed { index, value ->
            TextView(context).apply {
                text = value.toString()
                textSize = 14f
                gravity = Gravity.CENTER
                setOnClickListener {
                    xpIndex = index
                    updateXpButtons()
                }
            }.also { button ->
                xpRow.addView(button, LinearLayout.LayoutParams(0, Ui.dp(context, 44), 1f).apply {
                    if (index > 0) marginStart = Ui.dp(context, 6)
                })
            }
        }
        updateXpButtons = {
            xpButtons.forEachIndexed { index, button ->
                button.background = Ui.rounded(context, if (index == xpIndex) Color.rgb(78, 94, 150) else Color.rgb(53, 65, 103), 12)
                button.setTextColor(if (index == xpIndex) Color.WHITE else Ui.ACCENT)
            }
        }
        updateXpButtons()
        sheet.addView(xpRow, LinearLayout.LayoutParams(-1, Ui.dp(context, 56)).apply { topMargin = Ui.dp(context, 12) })

        var selectedTime = task.time
        val timeButton = TextView(context).apply {
            text = "⏰  Scheduled for ${Ui.displayTime(selectedTime)}"
            textSize = 15f
            setTextColor(Ui.ACCENT)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(Ui.dp(context, 16), 0, Ui.dp(context, 16), 0)
            background = Ui.rounded(context, Color.rgb(42, 52, 83), 16)
            setOnClickListener {
                showTimePicker(selectedTime) { hour, minute ->
                    selectedTime = "%02d:%02d".format(hour, minute)
                    text = "⏰  Scheduled for ${Ui.displayTime(selectedTime)}"
                }
            }
        }
        sheet.addView(timeButton, LinearLayout.LayoutParams(-1, Ui.dp(context, 56)).apply { topMargin = Ui.dp(context, 12) })
        val actions = LinearLayout(context).apply { gravity = Gravity.END or Gravity.CENTER_VERTICAL }
        actions.addView(sheetButton("Delete", Ui.RED) {
            repository.deleteTask(task)
            scheduler?.prepare(repository.tasks, repository.reminderTime)
            dialog.dismiss()
            onUpdated()
        }, LinearLayout.LayoutParams(-2, Ui.dp(context, 52)).apply { marginEnd = Ui.dp(context, 8) })
        actions.addView(sheetButton("Cancel", Ui.MUTED) { dialog.dismiss() }, LinearLayout.LayoutParams(-2, Ui.dp(context, 52)))
        actions.addView(sheetButton("Save", Ui.ACCENT) {
            val title = name.text.toString().trim()
            if (title.isEmpty()) {
                name.error = "Give your quest a name"
            } else {
                repository.updateTask(Task(task.id, title, detail.text.toString().trim().ifEmpty { "Personal quest" }, xpOptions[xpIndex], selectedTime, category.value()))
                scheduler?.prepare(repository.tasks, repository.reminderTime)
                dialog.dismiss()
                onUpdated()
            }
        }, LinearLayout.LayoutParams(-2, Ui.dp(context, 52)).apply { marginStart = Ui.dp(context, 8) })
        sheet.addView(actions, Ui.margin(context, 0, 14, 0, 0))
        showSheet(dialog, sheet)
    }

    fun showTimePicker(initial: String, onSelected: (Int, Int) -> Unit) {
        val dialog = Dialog(context)
        val parts = initial.split(":").map(String::toInt)
        var hour = parts[0] % 12
        if (hour == 0) hour = 12
        var minute = parts[1]
        var isPm = parts[0] >= 12
        val sheet = Ui.column(context, 20, 24, 20, 20).apply { background = Ui.rounded(context, Ui.SURFACE, 28) }
        sheet.addView(Ui.label(context, "Choose quest time", 23, Ui.TEXT, true))
        sheet.addView(Ui.label(context, "Set a moment to show up for yourself.", 14, Ui.MUTED), Ui.margin(context, 0, 8, 0, 18))
        val timeLabel = Ui.label(context, "", 36, Ui.TEXT, true).apply { gravity = Gravity.CENTER }
        sheet.addView(timeLabel, LinearLayout.LayoutParams(-1, Ui.dp(context, 64)))
        fun update() { timeLabel.text = "%d:%02d %s".format(hour, minute, if (isPm) "PM" else "AM") }
        update()
        val controls = LinearLayout(context).apply { gravity = Gravity.CENTER }
        fun control(text: String, action: () -> Unit) = TextView(context).apply {
            this.text = text
            textSize = 22f
            gravity = Gravity.CENTER
            setTextColor(Ui.ACCENT)
            background = Ui.rounded(context, Color.rgb(42, 52, 83), 100)
            setOnClickListener {
                action()
                update()
                animate().scaleX(1.08f).scaleY(1.08f).setDuration(80).withEndAction {
                    animate().scaleX(1f).scaleY(1f).setDuration(120).start()
                }.start()
            }
        }
        controls.addView(control("−") { hour = if (hour == 1) 12 else hour - 1 }, LinearLayout.LayoutParams(Ui.dp(context, 52), Ui.dp(context, 52)).apply { marginEnd = Ui.dp(context, 12) })
        controls.addView(control("+") { hour = if (hour == 12) 1 else hour + 1 }, LinearLayout.LayoutParams(Ui.dp(context, 52), Ui.dp(context, 52)).apply { marginEnd = Ui.dp(context, 24) })
        controls.addView(control("−") { minute = (minute + 55) % 60 }, LinearLayout.LayoutParams(Ui.dp(context, 52), Ui.dp(context, 52)).apply { marginEnd = Ui.dp(context, 12) })
        controls.addView(control("+") { minute = (minute + 5) % 60 }, LinearLayout.LayoutParams(Ui.dp(context, 52), Ui.dp(context, 52)))
        sheet.addView(controls, Ui.margin(context, 0, 14, 0, 0))
        val period = sheetButton(if (isPm) "PM" else "AM", Ui.ACCENT) {}
        period.setOnClickListener { isPm = !isPm; period.text = if (isPm) "PM" else "AM"; update() }
        sheet.addView(period, LinearLayout.LayoutParams(-1, Ui.dp(context, 52)).apply { topMargin = Ui.dp(context, 14) })
        val actions = LinearLayout(context).apply { gravity = Gravity.END or Gravity.CENTER_VERTICAL }
        actions.addView(sheetButton("Cancel", Ui.MUTED) { dialog.dismiss() }, LinearLayout.LayoutParams(-2, Ui.dp(context, 52)))
        actions.addView(sheetButton("Set time", Ui.ACCENT) {
            onSelected(if (isPm) (hour % 12) + 12 else hour % 12, minute)
            dialog.dismiss()
        }, LinearLayout.LayoutParams(-2, Ui.dp(context, 52)).apply { marginStart = Ui.dp(context, 8) })
        sheet.addView(actions, Ui.margin(context, 0, 12, 0, 0))
        showSheet(dialog, sheet)
    }

    private fun formField(hint: String) = EditText(context).apply {
        this.hint = hint
        textSize = 16f
        setTextColor(Ui.TEXT)
        setHintTextColor(Ui.MUTED)
        setSingleLine(true)
        gravity = Gravity.CENTER_VERTICAL
        setPadding(Ui.dp(context, 16), 0, Ui.dp(context, 16), 0)
        background = Ui.rounded(context, Color.rgb(42, 52, 83), 16)
    }

    private fun categorySelector(selected: String = ""): CategorySelector {
        val options = listOf("", "Physical", "Learning", "Nutrition")
        var index = options.indexOf(selected).coerceAtLeast(0)
        val row = LinearLayout(context).apply {
            gravity = Gravity.CENTER_VERTICAL
            background = Ui.rounded(context, Color.rgb(42, 52, 83), 16)
            setPadding(Ui.dp(context, 12), 0, Ui.dp(context, 12), 0)
        }
        row.addView(Ui.label(context, "Category", 14, Ui.MUTED, true), LinearLayout.LayoutParams(Ui.dp(context, 72), -2))
        lateinit var refresh: () -> Unit
        val buttons = options.mapIndexed { buttonIndex, option ->
            TextView(context).apply {
                text = if (option.isEmpty()) "None" else option
                textSize = 11f
                gravity = Gravity.CENTER
                setOnClickListener {
                    index = buttonIndex
                    refresh()
                }
            }.also { button ->
                row.addView(button, LinearLayout.LayoutParams(0, Ui.dp(context, 44), 1f).apply {
                    if (buttonIndex > 0) marginStart = Ui.dp(context, 4)
                })
            }
        }
        refresh = {
            buttons.forEachIndexed { buttonIndex, button ->
                button.background = Ui.rounded(context, if (buttonIndex == index) Color.rgb(78, 94, 150) else Color.rgb(53, 65, 103), 12)
                button.setTextColor(if (buttonIndex == index) Color.WHITE else Ui.ACCENT)
            }
        }
        refresh()
        return CategorySelector(row) { options[index] }
    }

    private data class CategorySelector(val view: View, val value: () -> String)

    private fun sheetButton(text: String, color: Int, action: () -> Unit) =
        TextView(context).apply {
            this.text = text
            textSize = 14f
            gravity = Gravity.CENTER
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(color)
            setPadding(Ui.dp(context, 12), 0, Ui.dp(context, 12), 0)
            setOnClickListener { action() }
        }

    private fun showSheet(dialog: Dialog, sheet: View) {
        dialog.setContentView(sheet)
        dialog.setCanceledOnTouchOutside(true)
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setDimAmount(0.55f)
            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            attributes = attributes.apply {
                width = WindowManager.LayoutParams.MATCH_PARENT
                gravity = Gravity.BOTTOM
            }
        }
        dialog.show()
        dialog.window?.setLayout(-1, WindowManager.LayoutParams.WRAP_CONTENT)
        sheet.translationY = Ui.dp(context, 40).toFloat()
        sheet.alpha = 0f
        sheet.animate().translationY(0f).alpha(1f).setDuration(240)
            .setInterpolator(AccelerateDecelerateInterpolator()).start()
    }
}
