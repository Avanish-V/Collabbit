package com.iota.campusX.Authentication.GoogleAuthentication.GoogleAuthentication

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

class CredentialAuthDataSource(
  private val context: Context,
) {
  private val credentialManager = CredentialManager.create(context)
  suspend fun signIn(): GoogleIdTokenCredential? {

    val googleIdOption = GetGoogleIdOption.Builder()
      .setServerClientId("1083272757839-jgnafarj06d7e4em1jo4ba69df0na3mg.apps.googleusercontent.com") // Dev
      //.setServerClientId("446123587571-ruihpipupo4ti7d3clpegr418tr2mdbm.apps.googleusercontent.com") // Prod
      .setFilterByAuthorizedAccounts(false) // let user pick any account
      .setAutoSelectEnabled(true)
      .build()

    val request = GetCredentialRequest.Builder()
      .addCredentialOption(googleIdOption)
      .build()

    return try {

      val result = credentialManager.getCredential(context, request)

      val credential = result.credential as? CustomCredential

      if (credential?.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {

        GoogleIdTokenCredential.createFrom(credential.data)

      } else null

    } catch (e: GetCredentialException) {

      null

    }

  }
  fun signOut() {
    // No backend: just clear local app state/session
    // You may also allow the user to remove the Google account from device if needed.
  }

}
