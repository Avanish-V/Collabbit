package com.iota.campusX.Authentication.GoogleAuthentication.GoogleAuthentication

data class SignInState(
    val userId: String = "",
    val userToken : String = "",
    val isSignInSuccessful:Boolean = false,
    val signInError:String? = null
)
