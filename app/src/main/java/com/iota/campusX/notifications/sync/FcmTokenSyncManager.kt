package com.iota.campusX.notifications.sync

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.iota.campusX.Feature.UserProfile.domain.repository.UserProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

class FcmTokenSyncManager(
    private val context: Context,
    private val fcmTokenStore: FcmTokenStore,
    private val auth: FirebaseAuth,
    private val userProfileRepository: UserProfileRepository
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var syncJob: Job? = null

    fun onNewToken(token: String) {
        val safeToken = token.take(8) + "..."
        Log.d(TAG, "onNewToken received: $safeToken")
        fcmTokenStore.savePendingToken(token)

        if (auth.currentUser != null) {
            triggerSync(token)
        } else {
            Log.d(TAG, "User logged out. Pending FCM token stored for next login.")
        }
    }

    fun onLogin() {
        Log.d(TAG, "onLogin triggered FCM token sync")
        val pendingToken = fcmTokenStore.getPendingToken()
        triggerSync(pendingToken)
    }

    fun onAppStart() {
        if (auth.currentUser != null) {
            Log.d(TAG, "onAppStart triggered FCM token check")
            triggerSync()
        }
    }

    fun onNotificationPermissionGranted() {
        if (auth.currentUser != null) {
            Log.d(TAG, "onNotificationPermissionGranted triggered FCM token sync")
            triggerSync()
        }
    }

    suspend fun onLogout() {
        Log.d(TAG, "onLogout triggered device token unregister")
        try {
            syncJob?.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cancel token sync job on logout", e)
        }

        try {
            withTimeoutOrNull(3000L) {
                userProfileRepository.updateFcmToken("")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Best effort device token unregister failed on logout", e)
        } finally {
            fcmTokenStore.clearSyncedToken()
            fcmTokenStore.clearPendingToken()
        }
    }

    fun triggerSync(inputToken: String? = null) {
        syncJob?.cancel()
        syncJob = scope.launch {
            syncTokenWithRetry(inputToken)
        }
    }

    private suspend fun syncTokenWithRetry(inputToken: String?) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.d(TAG, "User is logged out. Skipping FCM token sync.")
            return
        }

        val tokenToSync = inputToken
            ?: fcmTokenStore.getPendingToken()
            ?: fetchFcmTokenFromFirebase()

        if (tokenToSync.isNullOrBlank()) {
            Log.w(TAG, "No FCM token available to sync.")
            return
        }

        val safeTokenLog = tokenToSync.take(8) + "..."

        if (fcmTokenStore.shouldSkipSync(tokenToSync)) {
            Log.d(TAG, "FCM token $safeTokenLog is unchanged and was synced within 3 days. Skipping network call.")
            return
        }

        var backoffMs = 5000L
        val maxBackoffMs = 60000L
        var attempts = 0
        val maxAttempts = 5

        while (attempts < maxAttempts) {
            attempts++
            Log.d(TAG, "Syncing FCM token $safeTokenLog with backend (attempt $attempts/$maxAttempts)...")
            val syncResult = userProfileRepository.updateFcmToken(tokenToSync)

            if (syncResult.isSuccess) {
                Log.d(TAG, "Successfully synced FCM token $safeTokenLog")
                fcmTokenStore.markTokenSynced(tokenToSync)
                fcmTokenStore.clearPendingToken()
                return
            } else {
                val exception = syncResult.exceptionOrNull()
                val errorMsg = exception?.message ?: "Unknown error"
                Log.e(TAG, "Failed to sync FCM token $safeTokenLog: $errorMsg")

                if (isNonRetryable4xx(exception)) {
                    Log.w(TAG, "Non-retryable 4xx client error for FCM token sync ($errorMsg). Stopping retries.")
                    return
                }

                Log.d(TAG, "Retrying FCM token sync in ${backoffMs / 1000}s...")
                delay(backoffMs)
                backoffMs = (backoffMs * 2).coerceAtMost(maxBackoffMs)
            }
        }
    }

    private suspend fun fetchFcmTokenFromFirebase(): String? {
        return try {
            FirebaseMessaging.getInstance().token.await()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching FCM token from FirebaseMessaging", e)
            null
        }
    }

    companion object {
        private const val TAG = "FcmTokenSyncManager"

        fun isNonRetryable4xx(exception: Throwable?): Boolean {
            if (exception is io.ktor.client.plugins.ClientRequestException) {
                val statusCode = exception.response.status.value
                if (statusCode in 400..499 && statusCode != 401) {
                    return true
                }
            }
            return false
        }
    }
}
