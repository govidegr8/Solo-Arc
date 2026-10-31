# Solo Arc

Solo Arc is a Kotlin Android app for daily quests, motivation, progress tracking, and workouts. It uses a Material-inspired dark interface with a lightweight level-up system for completing daily tasks.

The project is local-first and uses Android Views rather than Compose.

## Features

- Daily quests with scheduled times, XP rewards, categories, and completion state.
- Quest creation, editing, pinning, completion/undo, and deletion.
- Quest sorting by pinned state, upcoming time, missed state, and completion.
- XP, levels, daily completion counts, streaks, and monthly progress.
- Daily and per-quest reminders.
- Weekly progress chart and monthly completion calendar.
- Cardio workouts with duration, calories, and distance.
- Strength workouts with exercises, sets, reps, and weight in kilograms.
- Workout editing, deletion, daily summaries, and weekly/monthly session counts.
- Camera launch from the nutrition placeholder. Nutrition analysis is not implemented yet.

## Technology

- Kotlin
- Android Views and platform dialogs
- Android Gradle Plugin 8.13.1
- Compile/target SDK 36
- Minimum SDK 24
- JVM target 11
- `SharedPreferences` for local persistence
- JUnit 4 for JVM unit tests

No third-party runtime workout or nutrition API is currently required.

## Getting started

### Requirements

- Android Studio or a compatible Gradle environment
- JDK 11 or newer
- Android SDK 36
- Android device or emulator

### Build

From the project root:

```bash
./gradlew assembleDebug
```

### Install on a USB-connected device

Enable USB debugging, connect the device, and run:

```bash
export ANDROID_HOME="$HOME/Library/Android/sdk"
./gradlew --no-daemon installDebug
adb shell am start -n com.example.sampleandroidapp/.MainActivity
```

### Run tests

```bash
./gradlew --no-daemon testDebugUnitTest
```

The tests are JVM tests and do not require a connected device.

## Project structure

### Application entry point

- [`MainActivity.kt`](app/src/main/java/com/example/sampleandroidapp/MainActivity.kt)  
  Creates the application shell, bottom navigation, swipe navigation, repositories, permissions, and the three main tabs.

### Main screens

- [`HomeScreen.kt`](app/src/main/java/com/example/sampleandroidapp/HomeScreen.kt)  
  Home tab, compact header, XP momentum card, quest list, sorting, animations, and pinning.

- [`ProgressScreen.kt`](app/src/main/java/com/example/sampleandroidapp/ProgressScreen.kt)  
  Level, XP, streak, weekly chart, and monthly calendar.

- [`ProfileScreen.kt`](app/src/main/java/com/example/sampleandroidapp/ProfileScreen.kt)  
  Profile content, reminder settings, workout tracker, summaries, and nutrition camera placeholder.

### Quest domain and persistence

- [`Task.kt`](app/src/main/java/com/example/sampleandroidapp/Task.kt)  
  `Task` model and built-in quests.

- [`TaskRepository.kt`](app/src/main/java/com/example/sampleandroidapp/TaskRepository.kt)  
  Stores quests, XP, levels, streaks, completion state, pin state, custom quests, task overrides, deletions, and monthly resets.

- [`QuestDialogs.kt`](app/src/main/java/com/example/sampleandroidapp/QuestDialogs.kt)  
  Create/edit quest sheet, XP selection, category selection, time picker, and deletion.

### Workout domain and persistence

- [`Workout.kt`](app/src/main/java/com/example/sampleandroidapp/Workout.kt)  
  Workout models, strength sets, exercise metadata, and cardio/strength catalogs.

- [`WorkoutRepository.kt`](app/src/main/java/com/example/sampleandroidapp/WorkoutRepository.kt)  
  Stores workout entries as JSON in `SharedPreferences`.

- [`WorkoutDialogs.kt`](app/src/main/java/com/example/sampleandroidapp/WorkoutDialogs.kt)  
  Workout entry/edit sheet. Cardio and strength fields are separated, use numeric inputs where appropriate, and support adding strength sets.

### Shared UI and notifications

- [`Ui.kt`](app/src/main/java/com/example/sampleandroidapp/Ui.kt)  
  Shared colors, spacing, density conversion, rounded backgrounds, labels, and layout helpers.

- [`CategoryIconView.kt`](app/src/main/java/com/example/sampleandroidapp/CategoryIconView.kt)  
  Draws category-specific quest icons.

- [`ReminderScheduler.kt`](app/src/main/java/com/example/sampleandroidapp/ReminderScheduler.kt)  
  Schedules daily and quest reminders.

- [`ReminderReceiver.kt`](app/src/main/java/com/example/sampleandroidapp/ReminderReceiver.kt)  
  Receives reminder alarms and posts notifications.

### Android configuration

- [`AndroidManifest.xml`](app/src/main/AndroidManifest.xml)  
  Declares the launcher activity, reminder receiver, notification permission, and camera permission.

- [`app/build.gradle.kts`](app/build.gradle.kts)  
  Android configuration, SDK versions, Kotlin target, and test dependencies.

## Data flow

```text
MainActivity
  ├── TaskRepository
  ├── ReminderScheduler
  ├── WorkoutRepository
  └── Screen builders
        ├── HomeScreen
        ├── ProgressScreen
        └── ProfileScreen
```

Screens render platform Views and call repository methods when data changes. The current tab is refreshed or the affected list is updated after changes.

## Persistence

All current user data is stored locally in the `winter_arc` `SharedPreferences` file:

- Quest definitions and custom quests
- Quest overrides and deleted built-in quest IDs
- Completion and pin state
- XP, streak, monthly progress, and reminder time
- Workout entries under the `workout_entries` JSON key

There is no sign-in, cloud synchronization, or remote database.

## XP and level rules

- New installations start at `0 XP`.
- Level formula: `xp / 100 + 1`.
- Completing a quest adds that quest’s XP.
- Undoing a quest removes its XP without allowing XP below zero.
- Legacy starting values of 50 XP and the earlier 20 XP value are migrated to 0.
- A new month clears daily progress and resets XP/streak to the configured starting value.

## Testing structure

- [`TestPreferences.kt`](app/src/test/java/com/example/sampleandroidapp/TestPreferences.kt)  
  In-memory `SharedPreferences` implementation for repository tests.

- [`TaskRepositoryTest.kt`](app/src/test/java/com/example/sampleandroidapp/TaskRepositoryTest.kt)  
  XP defaults, migration, quest completion/undo, and built-in quest XP tests.

- [`WorkoutRepositoryTest.kt`](app/src/test/java/com/example/sampleandroidapp/WorkoutRepositoryTest.kt)  
  Workout serialization, updates, deletion, date filtering, and date offsets.

## Current limitations and future ideas

- Nutrition photo analysis is only a camera placeholder; it does not identify food or calculate calories/protein.
- Workout data is local-only.
- Exercise selection currently uses a built-in catalog rather than a remote exercise API.
- Health Connect, wger synchronization, and bundled open exercise data are possible future improvements.
- There are currently no instrumented UI tests.

## Package

`com.example.sampleandroidapp`

Main activity: `com.example.sampleandroidapp.MainActivity`
