package com.iota.campusX.Feature.Report.domain

import com.iota.campusX.ui.UIComponents.ReportReason

interface ReportRepository {
   suspend fun createReportOnPost(reportReason: ReportReason,postId:String): Result<Unit>
}