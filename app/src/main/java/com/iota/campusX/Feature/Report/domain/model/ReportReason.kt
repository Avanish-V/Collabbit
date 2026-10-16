package com.iota.campusX.Feature.Report.domain.model

data class ReportReason(
    val type: ReportType = ReportType.NONE,
    val title: String = "",
    val description: String = ""
)

val reportReasons = listOf(
    ReportReason(
        ReportType.SPAM,
        "Spam or misleading",
        "This content is unwanted promotional or misleading."
    ),
    ReportReason(
        ReportType.HATE_SPEECH,
        "Hate speech or abuse",
        "Offensive or threatening content."
    ),
    ReportReason(
        ReportType.HARASSMENT,
        "Harassment or bullying",
        "Targeted insults, threats, or unwanted contact."
    ),
    ReportReason(
        ReportType.VIOLENCE,
        "Violence or harmful acts",
        "Promotes violence or self-harm."
    ),
    ReportReason(ReportType.SEXUAL_CONTENT, "Sexual content", "Inappropriate or explicit content."),
    ReportReason(
        ReportType.MISINFORMATION,
        "False information",
        "Contains false or misleading facts."
    ),
    ReportReason(ReportType.OTHER, "Other", "Does not fall under a specific category.")
)

enum class ReportType {
    NONE,
    SPAM,
    HATE_SPEECH,
    HARASSMENT,
    VIOLENCE,
    SEXUAL_CONTENT,
    MISINFORMATION,
    OTHER
}
