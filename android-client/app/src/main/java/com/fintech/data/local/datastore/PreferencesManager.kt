package com.fintech.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "fintech_preferences")

/**
 * DataStore for app preferences and user settings
 */
@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        // Auth keys
        private val ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val USER_ID = stringPreferencesKey("user_id")
        private val USER_EMAIL = stringPreferencesKey("user_email")
        private val USER_NAME = stringPreferencesKey("user_name")

        // Settings keys
        private val THEME_MODE = stringPreferencesKey("theme_mode")
        private val CURRENCY = stringPreferencesKey("currency")
        private val FIRST_LAUNCH = booleanPreferencesKey("first_launch")
        private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")

        // Sync keys
        private val LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
        private val SYNC_ON_WIFI_ONLY = booleanPreferencesKey("sync_on_wifi_only")
    }

    // Auth state
    val accessToken: Flow<String?> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[ACCESS_TOKEN] }

    val refreshToken: Flow<String?> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[REFRESH_TOKEN] }

    val userId: Flow<String?> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[USER_ID] }

    val isLoggedIn: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[ACCESS_TOKEN] != null }

    val userInfo: Flow<UserInfo?> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs ->
            val id = prefs[USER_ID]
            val email = prefs[USER_EMAIL]
            val name = prefs[USER_NAME]
            if (id != null && email != null) {
                UserInfo(id, email, name)
            } else null
        }

    // Settings
    val themeMode: Flow<String> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[THEME_MODE] ?: "system" }

    val currency: Flow<String> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[CURRENCY] ?: "VND" }

    val isFirstLaunch: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[FIRST_LAUNCH] ?: true }

    val onboardingCompleted: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[ONBOARDING_COMPLETED] ?: false }

    val notificationsEnabled: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[NOTIFICATIONS_ENABLED] ?: true }

    val lastSyncTime: Flow<Long> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[LAST_SYNC_TIME] ?: 0L }

    val syncOnWifiOnly: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[SYNC_ON_WIFI_ONLY] ?: false }

    // Save auth tokens
    suspend fun saveAuthTokens(accessToken: String, refreshToken: String) {
        dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN] = accessToken
            prefs[REFRESH_TOKEN] = refreshToken
        }
    }

    // Save user info
    suspend fun saveUserInfo(userId: String, email: String, name: String?) {
        dataStore.edit { prefs ->
            prefs[USER_ID] = userId
            prefs[USER_EMAIL] = email
            prefs[USER_NAME] = name ?: ""
        }
    }

    // Clear auth data (logout)
    suspend fun clearAuthData() {
        dataStore.edit { prefs ->
            prefs.remove(ACCESS_TOKEN)
            prefs.remove(REFRESH_TOKEN)
            prefs.remove(USER_ID)
            prefs.remove(USER_EMAIL)
            prefs.remove(USER_NAME)
        }
    }

    // Update theme
    suspend fun updateTheme(theme: String) {
        dataStore.edit { prefs ->
            prefs[THEME_MODE] = theme
        }
    }

    // Update currency
    suspend fun updateCurrency(currency: String) {
        dataStore.edit { prefs ->
            prefs[CURRENCY] = currency
        }
    }

    // Set first launch
    suspend fun setFirstLaunch(isFirst: Boolean) {
        dataStore.edit { prefs ->
            prefs[FIRST_LAUNCH] = isFirst
        }
    }

    // Set onboarding completed
    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { prefs ->
            prefs[ONBOARDING_COMPLETED] = completed
        }
    }

    // Set notifications enabled
    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    // Update last sync time
    suspend fun updateLastSyncTime(time: Long) {
        dataStore.edit { prefs ->
            prefs[LAST_SYNC_TIME] = time
        }
    }

    // Set sync on wifi only
    suspend fun setSyncOnWifiOnly(wifiOnly: Boolean) {
        dataStore.edit { prefs ->
            prefs[SYNC_ON_WIFI_ONLY] = wifiOnly
        }
    }

    // Clear all data
    suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }
}

data class UserInfo(
    val id: String,
    val email: String,
    val name: String?
)
