package com.iota.campusX.Feature.Auth.data.datasource

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.iota.campusX.Koin.AppConstants
import java.security.MessageDigest
import java.util.UUID

class CredentialAuthDataSourceImpl(
    context: Context
) : AuthCredentialDataSource {

    private val credentialManager = CredentialManager.create(context)

    private fun generateNonce(): String {
        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    private fun Context.findActivity(): Activity? {
        var context = this
        while (context is ContextWrapper) {
            if (context is Activity) return context
            context = context.baseContext
        }
        return null
    }

    override suspend fun signIn(context: Context): GoogleIdTokenCredential? {
        Log.d("AuthFlow", "CredentialAuthDataSourceImpl: Starting signIn flow")
        val activity = context.findActivity()
        if (activity == null) {
            Log.e("AuthFlow", "CredentialAuthDataSourceImpl: Could not find Activity context!")
            return null
        }

        val serverClientId = AppConstants.GOOGLE_SERVER_CLIENT_ID
        Log.d("AuthFlow", "AuthFlow: Using Server Client ID: $serverClientId")

        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(serverClientId)
            .setNonce(generateNonce())
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            Log.d("AuthFlow", "CredentialAuthDataSourceImpl: Requesting credentials from CredentialManager")
            val result = credentialManager.getCredential(activity, request)
            val credential = result.credential as? CustomCredential

            if (credential?.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                Log.d("AuthFlow", "CredentialAuthDataSourceImpl: Successfully parsed Google ID Token")
                GoogleIdTokenCredential.createFrom(credential.data)
            } else {
                Log.e("AuthFlow", "CredentialAuthDataSourceImpl: Unexpected credential type: ${credential?.type}")
                null
            }
        } catch (e: GetCredentialException) {
            Log.e("AuthFlow", "CredentialAuthDataSourceImpl: Sign-in error: ${e.type} - ${e.message}")
            null
        }
    }

    override fun signOut() {
        // Clear local credential session state if necessary
    }
}
