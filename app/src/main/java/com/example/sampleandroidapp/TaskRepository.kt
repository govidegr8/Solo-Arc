package com.example.sampleandroidapp

import android.content.SharedPreferences
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaskRepository(private val preferences: SharedPreferences) {
    fun migrateStartingXp() {
        if (preferences.getBoolean(STARTING_XP_MIGRATION_KEY, false) &&
            preferences.getBoolean(ZERO_STARTING_XP_MIGRATION_KEY, false)
        ) return

        val savedXp = preferences.getInt(XP_KEY, INITIAL_XP)
        if (savedXp == OLD_INITIAL_XP ||
            (preferences.getBoolean(STARTING_XP_MIGRATION_KEY, false) && savedXp == 20)
        ) {
            preferences.edit().putInt(XP_KEY, INITIAL_XP).apply()
        }
        preferences.edit()
            .putBoolean(STARTING_XP_MIGRATION_KEY, true)
            .putBoolean(ZERO_STARTING_XP_MIGRATION_KEY, true)
            .apply()
    }

    fun resetProgressIfNewMonth() {
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
        if (preferences.getString(MONTH_KEY, null) == currentMonth) return

        val editor = preferences.edit()
        preferences.all.keys
            .filter { it.startsWith("done_") || it.startsWith("count_") || it.startsWith("completed_day_") }
            .forEach(editor::remove)
        editor.putInt(XP_KEY, INITIAL_XP)
            .putInt(STREAK_KEY, 0)
            .putString(MONTH_KEY, currentMonth)
            .apply()
    }

    val tasks: List<Task>
        get() {
            val result = defaultTasks.toMutableList()
            val saved = JSONArray(preferences.getString(CUSTOM_TASKS_KEY, "[]"))
            for (index in 0 until saved.length()) {
                val item = saved.getJSONArray(index)
                result.add(
                    Task(
                        item.getString(0),
                        item.getString(1),
                        item.getString(2),
                        item.getInt(3),
                        if (item.length() > 4) item.getString(4) else "08:00",
                        if (item.length() > 5) item.getString(5) else "",
                    ),
                )
            }
            val overrides = JSONArray(preferences.getString(TASK_OVERRIDES_KEY, "[]"))
            for (index in 0 until overrides.length()) {
                val item = overrides.getJSONArray(index)
                val taskIndex = result.indexOfFirst { it.id == item.getString(0) }
                if (taskIndex >= 0) {
                    result[taskIndex] = Task(
                        item.getString(0),
                        item.getString(1),
                        item.getString(2),
                        item.getInt(3),
                        item.getString(4),
                        if (item.length() > 5) item.getString(5) else "",
                    )
                }
            }
            val deleted = JSONArray(preferences.getString(DELETED_TASKS_KEY, "[]"))
                .let { values -> (0 until values.length()).map { values.getString(it) }.toSet() }
            return result.filterNot { it.id in deleted }
        }

    val xp: Int
        get() = preferences.getInt(XP_KEY, INITIAL_XP)

    val level: Int
        get() = xp / XP_PER_LEVEL + 1

    val streak: Int
        get() = preferences.getInt(STREAK_KEY, 0)

    var reminderTime: String
        get() = preferences.getString(REMINDER_TIME_KEY, "08:00") ?: "08:00"
        set(value) = preferences.edit().putString(REMINDER_TIME_KEY, value).apply()

    fun isCompleted(task: Task): Boolean =
        preferences.getBoolean(completionKey(task), false)

    fun isPinned(task: Task): Boolean =
        preferences.getBoolean(pinnedKey(task), false)

    fun togglePinned(task: Task) {
        preferences.edit().putBoolean(pinnedKey(task), !isPinned(task)).apply()
    }

    fun completedToday(): Int = tasks.count(::isCompleted)

    fun addTask(task: Task) {
        val saved = JSONArray(preferences.getString(CUSTOM_TASKS_KEY, "[]"))
        saved.put(task.toJson())
        preferences.edit().putString(CUSTOM_TASKS_KEY, saved.toString()).apply()
    }

    fun updateTask(task: Task) {
        val customTasks = JSONArray(preferences.getString(CUSTOM_TASKS_KEY, "[]"))
        for (index in 0 until customTasks.length()) {
            if (customTasks.getJSONArray(index).getString(0) == task.id) {
                customTasks.put(index, task.toJson())
                preferences.edit().putString(CUSTOM_TASKS_KEY, customTasks.toString()).apply()
                return
            }
        }

        val overrides = JSONArray(preferences.getString(TASK_OVERRIDES_KEY, "[]"))
        var replaced = false
        for (index in 0 until overrides.length()) {
            if (overrides.getJSONArray(index).getString(0) == task.id) {
                overrides.put(index, task.toJson())
                replaced = true
                break
            }
        }
        if (!replaced) overrides.put(task.toJson())
        preferences.edit().putString(TASK_OVERRIDES_KEY, overrides.toString()).apply()
    }

    fun deleteTask(task: Task) {
        val customTasks = JSONArray(preferences.getString(CUSTOM_TASKS_KEY, "[]"))
        val remaining = JSONArray()
        for (index in 0 until customTasks.length()) {
            val item = customTasks.getJSONArray(index)
            if (item.getString(0) != task.id) remaining.put(item)
        }

        val deleted = JSONArray(preferences.getString(DELETED_TASKS_KEY, "[]"))
        if ((0 until deleted.length()).none { deleted.getString(it) == task.id }) {
            deleted.put(task.id)
        }
        preferences.edit()
            .putString(CUSTOM_TASKS_KEY, remaining.toString())
            .putString(DELETED_TASKS_KEY, deleted.toString())
            .remove("pinned_${task.id}")
            .apply()
    }

    fun toggle(task: Task): LevelChange {
        val wasComplete = isCompleted(task)
        val completedBefore = completedToday()
        val previousLevel = level
        val updatedXp = if (wasComplete) (xp - task.xp).coerceAtLeast(0) else xp + task.xp
        val editor = preferences.edit()
        if (wasComplete) {
            editor.remove(completionKey(task))
                .putInt(XP_KEY, updatedXp)
                .putInt(countKey(), (completedBefore - 1).coerceAtLeast(0))
                .putInt(STREAK_KEY, if (completedBefore == 1) (streak - 1).coerceAtLeast(0) else streak)
        } else {
            editor.putBoolean(completionKey(task), true)
                .putInt(XP_KEY, updatedXp)
                .putInt(countKey(), completedBefore + 1)
                .putInt(STREAK_KEY, streak + if (completedBefore == 0) 1 else 0)
        }
        editor.apply()
        return when {
            updatedXp / XP_PER_LEVEL + 1 > previousLevel -> LevelChange.UP
            updatedXp / XP_PER_LEVEL + 1 < previousLevel -> LevelChange.DOWN
            else -> LevelChange.NONE
        }
    }

    fun evaluateMissedDay(): LevelChange {
        val today = today()
        val lastEvaluatedDay = preferences.getString(LAST_EVALUATED_DAY_KEY, null)
        if (lastEvaluatedDay == today) return LevelChange.NONE

        val missed = lastEvaluatedDay != null && completedOn(lastEvaluatedDay) < tasks.size
        val editor = preferences.edit().putString(LAST_EVALUATED_DAY_KEY, today)
        if (missed) {
            editor.putInt(XP_KEY, (xp - XP_PER_LEVEL).coerceAtLeast(0))
        }
        editor.apply()
        return if (missed) LevelChange.DOWN else LevelChange.NONE
    }

    fun countForDay(date: Date): Int =
        preferences.getInt("count_${dayKey(date)}", 0)

    fun completedOn(date: Date): Int =
        completedOn(dayKey(date))

    private fun completedOn(day: String): Int =
        tasks.count { task ->
            preferences.getBoolean("done_${day}_${task.id}", false)
        }

    private fun completionKey(task: Task) = "done_${today()}_${task.id}"
    private fun pinnedKey(task: Task) = "pinned_${task.id}"
    private fun countKey() = "count_${today()}"
    private fun today() = dayKey(Date())
    private fun dayKey(date: Date) = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date)

    companion object {
        private const val CUSTOM_TASKS_KEY = "custom_tasks"
        private const val TASK_OVERRIDES_KEY = "task_overrides"
        private const val DELETED_TASKS_KEY = "deleted_tasks"
        private const val XP_KEY = "xp"
        private const val STREAK_KEY = "streak"
        private const val MONTH_KEY = "progress_month"
        private const val REMINDER_TIME_KEY = "reminder_time"
        private const val LAST_EVALUATED_DAY_KEY = "last_evaluated_day"
        private const val STARTING_XP_MIGRATION_KEY = "starting_xp_migrated"
        private const val ZERO_STARTING_XP_MIGRATION_KEY = "zero_starting_xp_migrated"
        private const val INITIAL_XP = 0
        private const val OLD_INITIAL_XP = 50
        private const val XP_PER_LEVEL = 100
    }

    private fun Task.toJson() =
        JSONArray().put(id).put(name).put(detail).put(xp).put(time).put(category)
}

enum class LevelChange {
    NONE,
    UP,
    DOWN,
}
