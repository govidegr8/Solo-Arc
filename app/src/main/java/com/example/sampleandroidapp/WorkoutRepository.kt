package com.example.sampleandroidapp

import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WorkoutRepository(private val preferences: SharedPreferences) {
    val entries: List<WorkoutEntry>
        get() {
            val saved = JSONArray(preferences.getString(KEY, "[]"))
            return (0 until saved.length()).map { parse(saved.getJSONObject(it)) }
        }

    fun save(entry: WorkoutEntry) {
        val saved = JSONArray(preferences.getString(KEY, "[]"))
        var replaced = false
        for (index in 0 until saved.length()) {
            if (saved.getJSONObject(index).getString("id") == entry.id) {
                saved.put(index, entry.toJson())
                replaced = true
                break
            }
        }
        if (!replaced) saved.put(entry.toJson())
        preferences.edit().putString(KEY, saved.toString()).apply()
    }

    fun delete(id: String) {
        val saved = JSONArray(preferences.getString(KEY, "[]"))
        val remaining = JSONArray()
        for (index in 0 until saved.length()) {
            if (saved.getJSONObject(index).getString("id") != id) remaining.put(saved.getJSONObject(index))
        }
        preferences.edit().putString(KEY, remaining.toString()).apply()
    }

    fun entriesFor(date: String): List<WorkoutEntry> = entries.filter { it.date == date }

    fun dateKey(date: Date = Date()): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date)

    fun dateDaysAgo(days: Int): String =
        dateKey(Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -days) }.time)

    private fun parse(json: JSONObject): WorkoutEntry =
        WorkoutEntry(
            id = json.getString("id"),
            date = json.getString("date"),
            type = json.getString("type"),
            activity = json.getString("activity"),
            durationMinutes = json.optInt("duration", 0),
            calories = json.optInt("calories", 0),
            distanceKm = json.optDouble("distance", 0.0),
            sets = (0 until json.optJSONArray("sets").orEmpty().length()).map {
                val set = json.getJSONArray("sets").getJSONObject(it)
                WorkoutSet(set.getInt("reps"), set.optDouble("weightKg", 0.0))
            },
            notes = json.optString("notes", ""),
        )

    private fun WorkoutEntry.toJson() = JSONObject().apply {
        put("id", id)
        put("date", date)
        put("type", type)
        put("activity", activity)
        put("duration", durationMinutes)
        put("calories", calories)
        put("distance", distanceKm)
        put("notes", notes)
        put("sets", JSONArray().also { array -> sets.forEach { array.put(JSONObject().put("reps", it.reps).put("weightKg", it.weightKg)) } })
    }

    companion object {
        private const val KEY = "workout_entries"
    }
}

private fun JSONArray?.orEmpty(): JSONArray = this ?: JSONArray()
