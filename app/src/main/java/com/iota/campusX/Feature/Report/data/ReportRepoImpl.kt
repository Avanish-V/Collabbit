package com.iota.campusX.Feature.Report.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.iota.campusX.Feature.Report.domain.ReportRepository
import com.iota.campusX.ui.UIComponents.ReportReason
import kotlinx.coroutines.tasks.await

class ReportRepoImpl(private val firestore: FirebaseFirestore,private val auth: FirebaseAuth): ReportRepository {

    override suspend fun createReportOnPost(
        reportReason: ReportReason,
        postId: String,
    ): Result<Unit> {

        if (auth.currentUser?.uid.toString().isEmpty()) return Result.failure(Exception("User ID not found"))

        val reportData = mapOf(
            "reason" to reportReason,
            "postId" to postId,
            "reportedBy" to auth.currentUser?.uid.toString(),
        )
        return try {

            firestore.collection("Reports")
                .add(reportData)
                .await()
            Result.success((Unit))

        }catch (e: Exception){
            Result.failure(e)
        }
    }

}