package com.iota.campusX.Koin

// FirebaseModule.kt
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import org.koin.dsl.module

val firebaseModule = module {

    single<FirebaseFirestore> {
        val firestore = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(false) // 🔹 Disable offline persistence
            .build()
        firestore.firestoreSettings = settings
        firestore
    }
}
