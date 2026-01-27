package com.chonkcheck.android.data.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sharedPreferences: SharedPreferences = createEncryptedPrefs()

    fun saveAccessToken(token: String) {
        sharedPreferences.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    fun getAccessToken(): String? = sharedPreferences.getString(KEY_ACCESS_TOKEN, null)

    fun saveRefreshToken(token: String) {
        sharedPreferences.edit().putString(KEY_REFRESH_TOKEN, token).apply()
    }

    fun getRefreshToken(): String? = sharedPreferences.getString(KEY_REFRESH_TOKEN, null)

    fun saveIdToken(token: String) {
        sharedPreferences.edit().putString(KEY_ID_TOKEN, token).apply()
    }

    fun getIdToken(): String? = sharedPreferences.getString(KEY_ID_TOKEN, null)

    fun clearTokens() {
        sharedPreferences.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_ID_TOKEN)
            .apply()
    }

    fun hasValidToken(): Boolean = getAccessToken() != null

    private fun createEncryptedPrefs(): SharedPreferences {
        return try {
            createEncryptedSharedPreferences()
        } catch (e: Exception) {
            // Encryption keys corrupted - clear and recreate
            clearCorruptedPrefs()
            createEncryptedSharedPreferences()
        }
    }

    private fun createEncryptedSharedPreferences(): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun clearCorruptedPrefs() {
        // Delete the corrupted SharedPreferences file
        context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()

        // Also try to delete the file directly
        try {
            val prefsFile = java.io.File(context.filesDir.parent, "shared_prefs/$PREFS_FILE_NAME.xml")
            if (prefsFile.exists()) {
                prefsFile.delete()
            }
        } catch (_: Exception) {
            // Ignore file deletion errors
        }

        // Clear the master key from Android Keystore
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            keyStore.deleteEntry(MasterKey.DEFAULT_MASTER_KEY_ALIAS)
        } catch (_: Exception) {
            // Ignore keystore errors
        }
    }

    companion object {
        private const val PREFS_FILE_NAME = "chonkcheck_secure_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_ID_TOKEN = "id_token"
    }
}
