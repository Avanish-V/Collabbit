package com.iota.campusX.Feature.Report.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.iota.campusX.Feature.Report.domain.ReportRepository
import com.iota.campusX.Screens.Home.BottomSheet.ReportReason
import kotlinx.coroutines.tasks.await

class ReportRepoImpl(private val firestore: FirebaseFirestore,private val auth: FirebaseAuth): ReportRepository {

    override suspend fun createReportOnPost(
        reportReason: ReportReason,
        postId: String,
        campusId: String?
    ): Result<Boolean> {

        if (auth.currentUser?.uid.toString().isEmpty()) return Result.failure(Exception("User ID not found"))

        val reportData = mapOf(
            "reason" to reportReason,
            "postId" to postId,
            "reportedBy" to auth.currentUser?.uid.toString(),
            "campusId" to campusId
        )
        return try {

            firestore.collection("Reports")
                .add(reportData)
                .await()
            Result.success((true))

        }catch (e: Exception){
            Result.failure(e)
        }
    }

}