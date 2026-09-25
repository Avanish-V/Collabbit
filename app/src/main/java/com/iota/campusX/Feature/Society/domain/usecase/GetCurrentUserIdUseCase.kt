package com.iota.campusX.Feature.Society.domain.usecase

import com.google.firebase.auth.FirebaseAuth

class GetCurrentUserIdUseCase(private val auth: FirebaseAuth) {
    operator fun invoke(): String? = auth.currentUser?.uid
}
