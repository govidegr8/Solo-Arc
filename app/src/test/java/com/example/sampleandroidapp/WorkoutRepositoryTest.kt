package com.example.sampleandroidapp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class WorkoutRepositoryTest {
    @Test
    fun savesAndRestoresCardioAndStrengthEntries() {
        val repository = WorkoutRepository(TestPreferences())
        val strength = WorkoutEntry(
            id = "strength-1",
            date = "2026-10-31",
            type = "Strength",
            activity = "Squat",
            sets = listOf(WorkoutSet(8, 60.0), WorkoutSet(6, 70.0)),
            notes = "Progressive overload",
        )
        val cardio = WorkoutEntry(
            id = "cardio-1",
            date = "2026-10-30",
            type = "Cardio",
            activity = "Running",
            durationMinutes = 30,
            calories = 250,
            distanceKm = 4.2,
        )

        repository.save(strength)
        repository.save(cardio)

        assertEquals(listOf(strength, cardio), repository.entries)
        assertEquals(listOf(strength), repository.entriesFor("2026-10-31"))
    }

    @Test
    fun savingSameIdUpdatesExistingEntry() {
        val repository = WorkoutRepository(TestPreferences())
        repository.save(WorkoutEntry("workout-1", "2026-10-31", "Cardio", "Walking", durationMinutes = 20))
        repository.save(WorkoutEntry("workout-1", "2026-10-31", "Cardio", "Walking", durationMinutes = 35, calories = 120))

        assertEquals(1, repository.entries.size)
        assertEquals(35, repository.entries.single().durationMinutes)
        assertEquals(120, repository.entries.single().calories)
    }

    @Test
    fun deleteRemovesOnlyRequestedEntry() {
        val repository = WorkoutRepository(TestPreferences())
        repository.save(WorkoutEntry("keep", "2026-10-31", "Cardio", "Walking"))
        repository.save(WorkoutEntry("remove", "2026-10-31", "Cardio", "Cycling"))

        repository.delete("remove")

        assertEquals(listOf("keep"), repository.entries.map { it.id })
        assertTrue(repository.entriesFor("2026-10-31").all { it.id != "remove" })
    }

    @Test
    fun dateDaysAgoUsesRequestedCalendarOffset() {
        val repository = WorkoutRepository(TestPreferences())
        val expected = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -3) }

        assertEquals(repository.dateKey(expected.time), repository.dateDaysAgo(3))
    }
}
