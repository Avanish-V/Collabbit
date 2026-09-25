package com.iota.campusX.Feature.Auth.data.datasource

import android.content.Context
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

interface AuthCredentialDataSource {
    suspend fun signIn(context: Context): GoogleIdTokenCredential?
    fun signOut()
}
