package com.iota.campusX.Screens.Post

import kotlinx.serialization.Serializable

@Serializable
data class PollOption(
    val id: String = "",
    val text: String = "",
    val label: String = "",
    val votes: Int = 0
)


@Serializable
data class Poll(
    val id: String = "",
    val question: String = "",
    var options: List<PollOption>? = emptyList(),
    val hasVoted: Boolean = false
)
