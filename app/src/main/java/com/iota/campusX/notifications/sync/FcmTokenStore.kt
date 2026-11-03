package com.iota.campusX.notifications.sync

import android.content.Context
import android.content.SharedPreferences

class FcmTokenStore(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun savePendingToken(token: String) {
        prefs.edit().putString(KEY_PENDING_TOKEN, token).apply()
    }

    fun getPendingToken(): String? {
        return prefs.getString(KEY_PENDING_TOKEN, null)
    }

    fun clearPendingToken() {
        prefs.edit().remove(KEY_PENDING_TOKEN).apply()
    }

    fun markTokenSynced(token: String, timestamp: Long = System.currentTimeMillis()) {
        prefs.edit()
            .putString(KEY_LAST_SYNCED_TOKEN, token)
            .putLong(KEY_LAST_SYNCED_TIME, timestamp)
            .apply()
    }

    fun getLastSyncedToken(): String? {
        return prefs.getString(KEY_LAST_SYNCED_TOKEN, null)
    }

    fun getLastSyncedTime(): Long {
        return prefs.getLong(KEY_LAST_SYNCED_TIME, 0L)
    }

    fun shouldSkipSync(token: String): Boolean {
        val lastToken = getLastSyncedToken()
        val lastTime = getLastSyncedTime()
        val threeDaysMs = 3 * 24 * 60 * 60 * 1000L
        return token == lastToken && (System.currentTimeMillis() - lastTime) < threeDaysMs
    }

    fun clearSyncedToken() {
        prefs.edit()
            .remove(KEY_LAST_SYNCED_TOKEN)
            .remove(KEY_LAST_SYNCED_TIME)
            .apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "fcm_token_store"
        private const val KEY_PENDING_TOKEN = "pending_fcm_token"
        private const val KEY_LAST_SYNCED_TOKEN = "last_synced_fcm_token"
        private const val KEY_LAST_SYNCED_TIME = "last_synced_fcm_time"
    }
}
