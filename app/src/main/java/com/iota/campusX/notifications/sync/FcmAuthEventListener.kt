package com.iota.campusX.notifications.sync

import com.iota.campusX.Feature.Auth.domain.repository.AuthEventListener

class FcmAuthEventListener(
    private val fcmTokenSyncManager: FcmTokenSyncManager
) : AuthEventListener {
    override suspend fun onSignInSuccess() {
        fcmTokenSyncManager.onLogin()
    }

    override suspend fun onSignOut() {
        fcmTokenSyncManager.onLogout()
    }
}
