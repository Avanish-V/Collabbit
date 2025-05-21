package com.iota.campusX.Authentication.GoogleAuthentication.GoogleAuthentication

data class SignInResult(
    val status:Boolean?,
    val userId: String = "",
    val userToken: String = "",
    val errorMessage:String?
)

