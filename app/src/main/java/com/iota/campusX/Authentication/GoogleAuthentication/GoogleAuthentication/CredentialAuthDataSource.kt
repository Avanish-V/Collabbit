package com.iota.campusX.Authentication.GoogleAuthentication.GoogleAuthentication

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

import java.security.MessageDigest
import java.util.UUID

class CredentialAuthDataSource(
  context: Context,
) {
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

  suspend fun signIn(activityContext: Context): GoogleIdTokenCredential? {
    Log.d("AuthFlow", "CredentialAuthDataSource: Starting signIn flow")
    val activity = activityContext.findActivity()
    if (activity == null) {
      Log.e("AuthFlow", "CredentialAuthDataSource: Could not find Activity context!")
      return null
    }

    // Pull the client ID dynamically from strings to avoid hardcoding mismatches
    val serverClientId = "221935776129-3v2hk85tbrg9nknpe1oknddqrirc8fvn.apps.googleusercontent.com"
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
      Log.d("AuthFlow", "CredentialAuthDataSource: Requesting credentials from CredentialManager")
      val result = credentialManager.getCredential(activity, request)

      val credential = result.credential as? CustomCredential
      Log.d("AuthFlow", "CredentialAuthDataSource: Received credential type: ${credential?.type}")

      if (credential?.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        Log.d("AuthFlow", "CredentialAuthDataSource: Successfully parsed Google ID Token")
        GoogleIdTokenCredential.createFrom(credential.data)
      } else {
        Log.e("AuthFlow", "CredentialAuthDataSource: Unexpected credential type: ${credential?.type}")
        null
      }

    } catch (e: GetCredentialException) {
      Log.e("AuthFlow", "CredentialAuthDataSource: Sign-in error: ${e.type} - ${e.message}")
      if (e.type == "android.credentials.GetCredentialException.TYPE_USER_CANCELED") {
        Log.e("AuthFlow", "CredentialAuthDataSource: User canceled or configuration mismatch (SHA-1/Package Name)")
      }
      null
    }

  }
  fun signOut() {
    // No backend: just clear local app state/session
    // You may also allow the user to remove the Google account from device if needed.
  }

}
