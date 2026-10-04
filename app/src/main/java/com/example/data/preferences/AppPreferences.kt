package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("brain_booster_prefs", Context.MODE_PRIVATE)

    var hasCompletedOnboarding: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, value).apply()

    var activeProfileId: Long
        get() = prefs.getLong(KEY_ACTIVE_PROFILE_ID, 1L)
        set(value) = prefs.edit().putLong(KEY_ACTIVE_PROFILE_ID, value).apply()

    var soundEffectsEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND_EFFECTS, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND_EFFECTS, value).apply()

    var voiceInstructionsEnabled: Boolean
        get() = prefs.getBoolean(KEY_VOICE_INSTRUCTIONS, true)
        set(value) = prefs.edit().putBoolean(KEY_VOICE_INSTRUCTIONS, value).apply()

    var backgroundMusicEnabled: Boolean
        get() = prefs.getBoolean(KEY_BG_MUSIC, true)
        set(value) = prefs.edit().putBoolean(KEY_BG_MUSIC, value).apply()

    var isDarkMode: Boolean
        get() = prefs.getBoolean(KEY_DARK_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_DARK_MODE, value).apply()

    var soundVolume: Float
        get() = prefs.getFloat(KEY_SOUND_VOLUME, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_SOUND_VOLUME, value).apply()

    var selectedDifficulty: String
        get() = prefs.getString(KEY_DIFFICULTY, "MEDIUM") ?: "MEDIUM"
        set(value) = prefs.edit().putString(KEY_DIFFICULTY, value).apply()

    var parentPin: String
        get() = prefs.getString(KEY_PARENT_PIN, "1234") ?: "1234"
        set(value) = prefs.edit().putString(KEY_PARENT_PIN, value).apply()

    companion object {
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_ACTIVE_PROFILE_ID = "active_profile_id"
        private const val KEY_SOUND_EFFECTS = "sound_effects"
        private const val KEY_VOICE_INSTRUCTIONS = "voice_instructions"
        private const val KEY_BG_MUSIC = "bg_music"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_SOUND_VOLUME = "sound_volume"
        private const val KEY_DIFFICULTY = "selected_difficulty"
        private const val KEY_PARENT_PIN = "parent_pin"
    }
}
