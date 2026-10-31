package com.example.sampleandroidapp

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView

class WorkoutDialogs(private val context: MainActivity, private val repository: WorkoutRepository) {
    fun showEntryDialog(existing: WorkoutEntry? = null, onSaved: () -> Unit) {
        val dialog = Dialog(context)
        val sheet = Ui.column(context, 20, 20, 20, 18).apply {
            background = Ui.rounded(context, Ui.SURFACE, 28)
        }
        val scroll = ScrollView(context).apply { addView(sheet) }
        sheet.addView(Ui.label(context, if (existing == null) "Start a workout" else "Edit workout", 24, Ui.TEXT, true))
        sheet.addView(Ui.label(context, "Log the work, then let your history show the progress.", 13, Ui.MUTED), Ui.margin(context, 0, 6, 0, 12))

        val typeGroup = RadioGroup(context).apply { orientation = RadioGroup.HORIZONTAL }
        val cardioOption = RadioButton(context).apply { text = "Cardio"; setTextColor(Ui.TEXT); textSize = 14f }
        val strengthOption = RadioButton(context).apply { text = "Strength"; setTextColor(Ui.TEXT); textSize = 14f }
        typeGroup.addView(cardioOption); typeGroup.addView(strengthOption)
        if (existing?.type == "Strength") strengthOption.isChecked = true else cardioOption.isChecked = true
        sheet.addView(typeGroup)

        val activityContainer = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        sheet.addView(activityContainer, fieldParams())
        val cardioFields = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        val strengthFields = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        sheet.addView(cardioFields, fieldParams())
        sheet.addView(strengthFields, fieldParams())

        val notes = field("Notes (optional)", existing?.notes ?: "", InputType.TYPE_CLASS_TEXT)
        sheet.addView(notes, inputParams())
        val message = Ui.label(context, "", 12, Ui.RED, true)
        sheet.addView(message, Ui.margin(context, 0, 8, 0, 0))

        val selectedActivity = arrayOf(existing?.activity ?: cardioActivities.first())
        fun activityButtons(options: List<String>) {
            activityContainer.removeAllViews()
            if (options.none { it == selectedActivity[0] }) selectedActivity[0] = options.first()
            activityContainer.addView(Ui.label(context, if (options === cardioActivities) "Activity" else "Exercise", 12, Ui.MUTED, true))
            activityContainer.addView(TextView(context).apply {
                text = selectedActivity[0]
                textSize = 15f
                gravity = Gravity.CENTER_VERTICAL
                setTextColor(Ui.ACCENT)
                setPadding(Ui.dp(context, 14), 0, Ui.dp(context, 14), 0)
                background = Ui.rounded(context, Color.rgb(42, 52, 83), 16)
                setOnClickListener {
                    val next = (options.indexOf(selectedActivity[0]) + 1) % options.size
                    selectedActivity[0] = options[next]
                    text = selectedActivity[0]
                }
            }, inputParams())
        }

        val duration = field("Duration (minutes)", existing?.durationMinutes?.takeIf { it > 0 }?.toString() ?: "", InputType.TYPE_CLASS_NUMBER)
        val calories = field("Calories", existing?.calories?.takeIf { it > 0 }?.toString() ?: "", InputType.TYPE_CLASS_NUMBER)
        val distance = field("Distance (km, optional)", existing?.distanceKm?.takeIf { it > 0 }?.toString() ?: "", InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
        cardioFields.addView(duration, inputParams())
        cardioFields.addView(calories, inputParams())
        cardioFields.addView(distance, inputParams())

        val setRows = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        val setCount = existing?.sets?.size?.coerceAtLeast(1) ?: 1
        fun addSetRow(set: WorkoutSet? = null) {
            val row = LinearLayout(context).apply {
                gravity = Gravity.CENTER_VERTICAL
                orientation = LinearLayout.HORIZONTAL
            }
            row.addView(Ui.label(context, "${setRows.childCount + 1}", 14, Ui.MUTED, true), LinearLayout.LayoutParams(Ui.dp(context, 30), Ui.dp(context, 52)))
            row.addView(field("kg", set?.weightKg?.takeIf { it > 0 }?.toString() ?: "", InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL), LinearLayout.LayoutParams(0, Ui.dp(context, 52), 1f).apply { marginEnd = Ui.dp(context, 6) })
            row.addView(field("reps", set?.reps?.takeIf { it > 0 }?.toString() ?: "", InputType.TYPE_CLASS_NUMBER), LinearLayout.LayoutParams(0, Ui.dp(context, 52), 1f))
            setRows.addView(row, Ui.margin(context, 0, 6, 0, 0))
        }
        strengthFields.addView(Ui.label(context, "Sets  ·  weight in kg  ·  reps", 12, Ui.MUTED, true))
        repeat(setCount) { addSetRow(existing?.sets?.getOrNull(it)) }
        val addSet = TextView(context).apply {
            text = "+ Add set"
            textSize = 14f
            setTextColor(Ui.ACCENT)
            setPadding(0, Ui.dp(context, 8), 0, Ui.dp(context, 4))
            setOnClickListener { addSetRow() }
        }
        strengthFields.addView(setRows)
        strengthFields.addView(addSet)

        fun updateMode() {
            val strength = strengthOption.isChecked
            selectedActivity[0] = if (strength) (existing?.activity?.takeIf { it in strengthExercises } ?: strengthExercises.first()) else (existing?.activity?.takeIf { it in cardioActivities } ?: cardioActivities.first())
            activityButtons(if (strength) strengthExercises else cardioActivities)
            cardioFields.visibility = if (strength) View.GONE else View.VISIBLE
            strengthFields.visibility = if (strength) View.VISIBLE else View.GONE
        }
        typeGroup.setOnCheckedChangeListener { _, _ -> updateMode() }
        updateMode()

        val actions = LinearLayout(context).apply { gravity = Gravity.END or Gravity.CENTER_VERTICAL }
        if (existing != null) actions.addView(button("Delete", Ui.RED) {
            repository.delete(existing.id); dialog.dismiss(); onSaved()
        }, buttonParams())
        actions.addView(button("Cancel", Ui.MUTED) { dialog.dismiss() }, buttonParams())
        actions.addView(button("Save", Ui.ACCENT) {
            val strength = strengthOption.isChecked
            val sets = if (strength) setRows.children().mapNotNull { row ->
                val fields = (row as LinearLayout).children().filterIsInstance<EditText>().toList()
                val weight = fields.getOrNull(0)?.text?.toString()?.toDoubleOrNull() ?: 0.0
                val reps = fields.getOrNull(1)?.text?.toString()?.toIntOrNull() ?: 0
                if (weight > 0 && reps > 0) WorkoutSet(reps, weight) else null
            }.toList() else emptyList()
            val invalid = if (strength) sets.isEmpty() else (duration.intValue() <= 0 || calories.intValue() < 0)
            if (invalid) {
                message.text = if (strength) "Add at least one set with weight and reps." else "Enter a duration and valid calories."
                return@button
            }
            repository.save(WorkoutEntry(
                id = existing?.id ?: "workout_${System.currentTimeMillis()}",
                date = existing?.date ?: repository.dateKey(),
                type = if (strength) "Strength" else "Cardio",
                activity = selectedActivity[0],
                durationMinutes = duration.intValue(),
                calories = calories.intValue(),
                distanceKm = distance.doubleValue().coerceAtLeast(0.0),
                sets = sets,
                notes = notes.text.toString().trim(),
            ))
            dialog.dismiss(); onSaved()
        }, buttonParams())
        sheet.addView(actions, Ui.margin(context, 0, 12, 0, 0))
        showSheet(dialog, scroll)
    }

    private fun field(hint: String, value: String = "", inputType: Int = InputType.TYPE_CLASS_TEXT) = EditText(context).apply {
        this.hint = hint; setText(value); textSize = 15f; setTextColor(Ui.TEXT); setHintTextColor(Ui.MUTED)
        this.inputType = inputType; setSingleLine(true); gravity = Gravity.CENTER_VERTICAL
        setPadding(Ui.dp(context, 14), 0, Ui.dp(context, 14), 0)
        background = Ui.rounded(context, Color.rgb(42, 52, 83), 16)
    }

    private fun EditText.intValue() = text.toString().trim().toIntOrNull() ?: 0
    private fun EditText.doubleValue() = text.toString().trim().toDoubleOrNull() ?: 0.0
    private fun LinearLayout.children() = (0 until childCount).asSequence().map { getChildAt(it) }
    private fun buttonParams() = LinearLayout.LayoutParams(-2, Ui.dp(context, 52)).apply { marginStart = Ui.dp(context, 8) }
    private fun button(text: String, color: Int, action: () -> Unit) = TextView(context).apply {
        this.text = text; textSize = 14f; gravity = Gravity.CENTER; setTypeface(null, android.graphics.Typeface.BOLD)
        setTextColor(color); setPadding(Ui.dp(context, 12), 0, Ui.dp(context, 12), 0); setOnClickListener { action() }
    }
    private fun inputParams() = LinearLayout.LayoutParams(-1, Ui.dp(context, 56)).apply { topMargin = Ui.dp(context, 8) }
    private fun fieldParams() = LinearLayout.LayoutParams(-1, -2).apply { topMargin = Ui.dp(context, 8) }
    private fun showSheet(dialog: Dialog, sheet: View) {
        dialog.setContentView(sheet)
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)); setDimAmount(0.55f)
            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            attributes = attributes.apply { width = WindowManager.LayoutParams.MATCH_PARENT; gravity = Gravity.BOTTOM }
        }
        dialog.show(); dialog.window?.setLayout(-1, WindowManager.LayoutParams.WRAP_CONTENT)
    }
}
