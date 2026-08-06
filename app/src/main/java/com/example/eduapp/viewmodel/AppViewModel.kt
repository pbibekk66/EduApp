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
 * Manages game data persistence (Room) and UI settings (Theme, Font, Sound, Notifications).
 */
class AppViewModel(private val dao: AppDao) : ViewModel() {

    // Database observation
    val users: Flow<List<User>> = dao.getAllUsers()

    // SETTING: Dark Mode preference
    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    // SETTING: Font scale multiplier (applies to all typography)
    private val _fontSizeMultiplier = MutableStateFlow(1.0f)
    val fontSizeMultiplier: StateFlow<Float> = _fontSizeMultiplier.asStateFlow()

    // SETTING: Sound effects master toggle
    private val _isSoundEnabled = MutableStateFlow(true)
    val isSoundEnabled: StateFlow<Boolean> = _isSoundEnabled.asStateFlow()

    // SETTING: Daily reminder notification toggle
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
     * Toggles daily reminder and schedules/cancels work.
     */
    fun toggleReminder(enabled: Boolean, workManager: WorkManager) {
        _isReminderEnabled.value = enabled
        if (enabled) {
            val reminderRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(
                24, TimeUnit.HOURS
            ).build()

            workManager.enqueueUniquePeriodicWork(
                "daily_practice_reminder",
                ExistingPeriodicWorkPolicy.KEEP,
                reminderRequest
            )
        } else {
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
