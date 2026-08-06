package com.example.eduapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.eduapp.database.AppDao
import com.example.eduapp.database.User
import com.example.eduapp.worker.DailyReminderWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Shared ViewModel for the application.
 * This class acts as the 'Single Source of Truth' for the UI, handling:
 * 1. Data Persistence: Communicates with the Room Database (AppDao).
 * 2. Reactive UI State: Uses StateFlow to broadcast settings changes (Theme, Font, Sound, etc.).
 * 3. Background Tasks: Schedules daily practice reminders using WorkManager.
 */
class AppViewModel(private val dao: AppDao) : ViewModel() {

    // Reactive stream of all users/scores from the database, automatically updating the UI when data changes.
    val users: Flow<List<User>> = dao.getAllUsers()

    // Observable state for Dark Mode. UI will re-render when this changes.
    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    // Observable state for Font Scale. Used in the custom theme to scale typography.
    private val _fontSizeMultiplier = MutableStateFlow(1.0f)
    val fontSizeMultiplier: StateFlow<Float> = _fontSizeMultiplier.asStateFlow()

    // Master toggle for game sound effects.
    private val _isSoundEnabled = MutableStateFlow(true)
    val isSoundEnabled: StateFlow<Boolean> = _isSoundEnabled.asStateFlow()

    // Tracks if the user wants daily practice notifications.
    private val _isReminderEnabled = MutableStateFlow(false)
    val isReminderEnabled: StateFlow<Boolean> = _isReminderEnabled.asStateFlow()

    /**
     * Updates the dark mode theme setting.
     */
    fun toggleTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
    }

    /**
     * Updates the font size multiplier.
     */
    fun setFontSizeMultiplier(multiplier: Float) {
        _fontSizeMultiplier.value = multiplier
    }

    /**
     * Updates the sound enabled/disabled setting.
     */
    fun toggleSound(enabled: Boolean) {
        _isSoundEnabled.value = enabled
    }

    /**
     * Toggles daily reminder and schedules/cancels background work.
     * Uses WorkManager to ensure the reminder is sent even if the app is closed.
     */
    fun toggleReminder(enabled: Boolean, workManager: WorkManager) {
        _isReminderEnabled.value = enabled
        if (enabled) {
            // Create a request for a task that runs every 24 hours.
            val reminderRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(
                24, TimeUnit.HOURS
            ).build()

            // Enqueue the work. 'KEEP' ensures we don't restart the 24h cycle if already scheduled.
            workManager.enqueueUniquePeriodicWork(
                "daily_practice_reminder",
                ExistingPeriodicWorkPolicy.KEEP,
                reminderRequest
            )
        } else {
            // Cancel the scheduled work if the user turns off reminders.
            workManager.cancelUniqueWork("daily_practice_reminder")
        }
    }

    /**
     * Adds a new user to the database.
     */
    fun addUser(username: String) {
        viewModelScope.launch {
            val user = User(username = username)
            dao.insert(user)
        }
    }

    /**
     * Saves game results after a session is completed.
     */
    fun saveGameResult(username: String, level: String, score: Int, duration: Int) {
        viewModelScope.launch {
            val user = User(
                username = username,
                level = level,
                score = score,
                duration = duration
            )
            dao.insert(user)
        }
    }

    /**
     * Deletes all score records from the database.
     */
    fun clearUsers() {
        viewModelScope.launch {
            dao.deleteAll()
        }
    }
}
