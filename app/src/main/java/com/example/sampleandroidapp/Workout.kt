package com.example.sampleandroidapp

data class WorkoutEntry(
    val id: String,
    val date: String,
    val type: String,
    val activity: String,
    val durationMinutes: Int = 0,
    val calories: Int = 0,
    val distanceKm: Double = 0.0,
    val sets: List<WorkoutSet> = emptyList(),
    val notes: String = "",
)

data class WorkoutSet(
    val reps: Int,
    val weightKg: Double,
)

data class Exercise(
    val name: String,
    val category: String,
    val equipment: String,
    val muscles: String,
)

val exercises = listOf(
    Exercise("Squat", "Legs", "Barbell", "Quads · glutes"),
    Exercise("Bench press", "Push", "Barbell", "Chest · triceps"),
    Exercise("Deadlift", "Pull", "Barbell", "Back · hamstrings"),
    Exercise("Overhead press", "Push", "Dumbbells", "Shoulders · triceps"),
    Exercise("Row", "Pull", "Dumbbells", "Back · biceps"),
    Exercise("Pull-up", "Pull", "Bodyweight", "Back · biceps"),
    Exercise("Lunge", "Legs", "Bodyweight", "Quads · glutes"),
)

val cardioActivities = listOf("Running", "Walking", "Cycling", "Swimming", "Rowing", "Jump rope")
val strengthExercises = exercises.map { it.name }
