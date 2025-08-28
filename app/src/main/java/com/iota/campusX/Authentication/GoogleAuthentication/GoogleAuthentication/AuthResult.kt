package com.iota.campusX.Authentication.GoogleAuthentication.GoogleAuthentication

sealed class AuthResult(){
  object Idle : AuthResult()
  object Loading : AuthResult()
  object SignedIn : AuthResult()
  data class Error(val message: String) : AuthResult()
  object SignedOut : AuthResult()
}