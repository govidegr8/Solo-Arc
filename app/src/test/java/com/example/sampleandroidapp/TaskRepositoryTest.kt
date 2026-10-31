package com.example.sampleandroidapp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskRepositoryTest {
    @Test
    fun newRepositoryStartsAtZeroXp() {
        val repository = TaskRepository(TestPreferences())

        assertEquals(0, repository.xp)
        assertEquals(1, repository.level)
    }

    @Test
    fun migrationConvertsLegacyStartingXpToZero() {
        val preferences = TestPreferences()
        preferences.edit().putInt("xp", 50).apply()
        val repository = TaskRepository(preferences)

        repository.migrateStartingXp()

        assertEquals(0, repository.xp)
    }

    @Test
    fun completingAndUndoingTaskChangesXpAndCompletion() {
        val repository = TaskRepository(TestPreferences())
        val task = defaultTasks.first()

        repository.toggle(task)
        assertEquals(task.xp, repository.xp)
        assertTrue(repository.isCompleted(task))

        repository.toggle(task)
        assertEquals(0, repository.xp)
        assertTrue(!repository.isCompleted(task))
    }

    @Test
    fun defaultTasksUseExpectedXpValues() {
        assertEquals(listOf(5, 10, 15, 20), defaultTasks.map { it.xp })
    }
}
