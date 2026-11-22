package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.AppSettingsState
import com.example.data.model.CurrencyOption
import com.example.data.model.WallpaperOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "cafeteria_settings"
)

class SettingsDataStore(private val context: Context) {
    private object Keys {
        val GLASS_OPACITY = floatPreferencesKey("glass_opacity")
        val BLUR_INTENSITY = floatPreferencesKey("blur_intensity")
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val WALLPAPER_KEY = stringPreferencesKey("wallpaper_key")
        val CURRENCY_CODE = stringPreferencesKey("currency_code")
        val OWNER_PIN = stringPreferencesKey("owner_pin")
    }

    val settingsFlow: Flow<AppSettingsState> = context.settingsDataStore.data.map { prefs ->
        AppSettingsState(
            glassOpacity = prefs[Keys.GLASS_OPACITY] ?: 0.40f,
            blurIntensity = prefs[Keys.BLUR_INTENSITY] ?: 16f,
            isDarkMode = prefs[Keys.IS_DARK_MODE] ?: true,
            wallpaper = WallpaperOption.fromKey(prefs[Keys.WALLPAPER_KEY] ?: WallpaperOption.ESPRESSO.key),
            currency = CurrencyOption.fromCode(prefs[Keys.CURRENCY_CODE] ?: CurrencyOption.IQD.code),
            ownerPin = prefs[Keys.OWNER_PIN] ?: ""
        )
    }

    suspend fun updateGlassOpacity(value: Float) {
        context.settingsDataStore.edit { it[Keys.GLASS_OPACITY] = value.coerceIn(0.12f, 0.85f) }
    }

    suspend fun updateBlurIntensity(value: Float) {
        context.settingsDataStore.edit { it[Keys.BLUR_INTENSITY] = value.coerceIn(0f, 28f) }
    }

    suspend fun updateDarkMode(isDark: Boolean) {
        context.settingsDataStore.edit { it[Keys.IS_DARK_MODE] = isDark }
    }

    suspend fun updateWallpaper(wallpaper: WallpaperOption) {
        context.settingsDataStore.edit { it[Keys.WALLPAPER_KEY] = wallpaper.key }
    }

    suspend fun updateCurrency(currency: CurrencyOption) {
        context.settingsDataStore.edit { it[Keys.CURRENCY_CODE] = currency.code }
    }

    suspend fun updateOwnerPin(pin: String) {
        context.settingsDataStore.edit { it[Keys.OWNER_PIN] = pin.trim() }
    }
}
