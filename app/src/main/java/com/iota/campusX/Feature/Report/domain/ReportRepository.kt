package com.iota.campusX.Feature.Report.domain

import com.iota.campusX.Feature.Report.domain.model.ReportReason

interface ReportRepository {
   suspend fun createReportOnPost(reportReason: ReportReason,postId:String): Result<Unit>
}