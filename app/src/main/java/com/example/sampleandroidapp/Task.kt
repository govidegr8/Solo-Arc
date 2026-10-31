package com.example.sampleandroidapp

data class Task(
    val id: String,
    val name: String,
    val detail: String,
    val xp: Int,
    val time: String,
    val category: String = "",
)

val defaultTasks = listOf(
    Task("move", "Move your body", "20 min exercise", 5, "08:00"),
    Task("focus", "Deep work session", "45 min distraction-free", 10, "10:00"),
    Task("reset", "Reset your space", "10 min tidy-up", 15, "18:00"),
    Task("reflect", "Night reflection", "Journal one lesson", 20, "21:30"),
)
