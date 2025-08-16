package com.iota.campusX.Feature.Report.domain

import com.iota.campusX.Screens.Home.BottomSheet.ReportReason

interface ReportRepository {
   suspend fun createReportOnPost(reportReason: ReportReason,postId:String,campusId: String?): Result<Boolean>
}