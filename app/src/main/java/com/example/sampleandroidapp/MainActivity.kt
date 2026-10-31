package com.example.sampleandroidapp

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private val repository by lazy {
        TaskRepository(getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE))
    }
    private val scheduler by lazy { ReminderScheduler(this) }
    private val workoutRepository by lazy { WorkoutRepository(getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)) }
    private lateinit var page: FrameLayout
    private lateinit var navigation: LinearLayout
    private var initialLevelChange = LevelChange.NONE
    private var selectedTab = 0

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        window.statusBarColor = Ui.BG
        window.navigationBarColor = Ui.BG
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        repository.migrateStartingXp()
        repository.resetProgressIfNewMonth()
        initialLevelChange = repository.evaluateMissedDay()
        scheduler.prepare(repository.tasks, repository.reminderTime)
        requestNotificationPermission()
        setContentView(buildShell())
        showTab(0, animate = false)
    }

    private fun buildShell(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Ui.BG)
        }
        page = object : FrameLayout(this) {
            private var downX = 0f
            private var downY = 0f
            private var swiped = false

            override fun onInterceptTouchEvent(event: android.view.MotionEvent): Boolean {
                when (event.actionMasked) {
                    android.view.MotionEvent.ACTION_DOWN -> {
                        downX = event.x
                        downY = event.y
                        swiped = false
                    }
                    android.view.MotionEvent.ACTION_MOVE -> {
                        val dx = event.x - downX
                        val dy = event.y - downY
                        if (!swiped && kotlin.math.abs(dx) > Ui.dp(this@MainActivity, 48) && kotlin.math.abs(dx) > kotlin.math.abs(dy) * 1.25f) {
                            swiped = true
                            val targetTab = if (dx < 0) selectedTab + 1 else selectedTab - 1
                            if (targetTab in 0..2) {
                                swiped = true
                                showTab(targetTab, dx < 0)
                                return true
                            }
                        }
                    }
                }
                return false
            }

            override fun onTouchEvent(event: android.view.MotionEvent): Boolean =
                swiped || super.onTouchEvent(event)
        }
        root.addView(page, LinearLayout.LayoutParams(-1, 0, 1f))
        navigation = LinearLayout(this).apply {
            gravity = Gravity.CENTER
            setPadding(Ui.dp(this@MainActivity, 10), Ui.dp(this@MainActivity, 6), Ui.dp(this@MainActivity, 10), Ui.dp(this@MainActivity, 6))
            background = Ui.rounded(this@MainActivity, Ui.SURFACE, 24)
        }
        root.addView(navigation, LinearLayout.LayoutParams(-1, Ui.dp(this, 68)).apply {
            setMargins(Ui.dp(this@MainActivity, 12), 0, Ui.dp(this@MainActivity, 12), Ui.dp(this@MainActivity, 8))
        })
        listOf("⌂", "↗", "◉").forEachIndexed { index, icon ->
            navigation.addView(TextView(this).apply {
                text = icon
                textSize = 24f
                gravity = Gravity.CENTER
                setTextColor(Ui.MUTED)
                setOnClickListener { showTab(index) }
            }, LinearLayout.LayoutParams(0, Ui.dp(this, 56), 1f))
        }
        return root
    }

    private fun showTab(
        tab: Int,
        forward: Boolean? = null,
        animate: Boolean = true,
        force: Boolean = false,
    ) {
        val targetTab = tab.coerceIn(0, 2)
        if (!force && targetTab == selectedTab && page.childCount > 0) return
        val previousTab = selectedTab
        selectedTab = targetTab
        val content = when (targetTab) {
            1 -> ProgressScreen(this, repository).build()
            2 -> ProfileScreen(this, repository, scheduler, workoutRepository).build()
            else -> HomeScreen(
                this,
                repository,
                scheduler,
                initialLevelChange,
                { QuestDialogs(this, repository, scheduler).showAddTaskDialog { showTab(0, animate = false, force = true) } },
                { showTab(0, animate = false, force = true) },
            ).build().also {
                initialLevelChange = LevelChange.NONE
            }

        }
        val oldContent = page.getChildAt(0)
        page.addView(content, FrameLayout.LayoutParams(-1, -1))
        for (index in 0 until navigation.childCount) {
            val item = navigation.getChildAt(index) as TextView
            item.setTextColor(if (index == targetTab) Ui.ACCENT else Ui.MUTED)
            item.background = if (index == targetTab) Ui.rounded(this, Color.rgb(48, 57, 91), 18) else null
        }
        if (!animate || oldContent == null) {
            page.removeAllViews()
            page.addView(content, FrameLayout.LayoutParams(-1, -1))
            return
        }
        val movingForward = forward ?: (targetTab > previousTab)
        val distance = page.width.toFloat().coerceAtLeast(resources.displayMetrics.widthPixels.toFloat())
        content.translationX = if (movingForward) distance else -distance
        content.alpha = 1f
        oldContent.animate()
            .translationX(if (movingForward) -distance else distance)
            .setDuration(260)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
        content.animate()
            .translationX(0f)
            .setDuration(260)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                if (page.childCount > 1) page.removeView(oldContent)
            }
            .start()
    }

    fun refreshCurrentTab() {
        showTab(selectedTab, animate = false, force = true)
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 7)
        }
    }

    fun openNutritionCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST)
            return
        }
        val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, CAMERA_REQUEST)
        } else {
            Toast.makeText(this, "No camera app is available.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, results: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, results)
        if (requestCode == CAMERA_PERMISSION_REQUEST &&
            results.firstOrNull() == PackageManager.PERMISSION_GRANTED
        ) {
            openNutritionCamera()
        }
    }

    @Deprecated("Use the camera activity result when the nutrition analyzer is implemented.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            Toast.makeText(this, "Photo captured. Nutrition analysis is coming soon.", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val PREFERENCES_NAME = "winter_arc"
        private const val CAMERA_PERMISSION_REQUEST = 8
        private const val CAMERA_REQUEST = 9
    }
}
