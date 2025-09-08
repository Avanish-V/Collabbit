package com.iota.campusX.Feature.Post.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PollOption(
    val optionId: String = "",
    val text: String = "",
    val label: String = "",
)

@Serializable
data class Vote(
    val userId: String = "",
    val optionId: String = ""
)


@Serializable
data class Poll(
    val id: String = "",
    val question: String = "",
    var options: List<PollOption> = emptyList(),
    var hasVoted: Boolean = false,
    var votes: List<Vote> = emptyList(),
    val isActive: Boolean = true,
    val selectedOptionId: String? = null
)
