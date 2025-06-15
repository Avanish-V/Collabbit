package com.iota.campusX.Screens.Post

import kotlinx.serialization.Serializable

@Serializable
data class PollOption(
    val optionId: String = "",
    val text: String = "",
    val label: String = "",
    var votes: List<String> = emptyList()
)


@Serializable
data class Poll(
    val id: String = "",
    val question: String = "",
    var options: List<PollOption>? = emptyList(),
    var hasVoted: Boolean = false
)
