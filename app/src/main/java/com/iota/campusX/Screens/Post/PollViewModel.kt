package com.iota.campusX.Screens.Post

import androidx.lifecycle.ViewModel
import com.iota.campusX.Utils.generateUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class PollViewModel : ViewModel() {

    private val _poll = MutableStateFlow<Poll?>(null)
    val poll: StateFlow<Poll?> = _poll.asStateFlow()

    fun createPoll(question: String) {

        val newPoll = Poll(
            id = generateUID(),
            question = question,
            options = listOf(
                PollOption(
                    optionId = "option_1",
                    text = "",
                    label = "Option 1"
                )
            )  // start with no options
        )
        _poll.value = newPoll
    }

    fun addPollOption() {


        poll.value?.options?.count()?.let { if (it > 4 ) return }

        when(poll.value?.options?.count()){
            1 -> _poll.value = poll.value?.copy(
                options = poll.value?.options?.plus(
                    PollOption(
                        optionId = "option_2",
                        text = "",
                        label = "Option 2"
                    )
                )
            )

            2 -> _poll.value = poll.value?.copy(
                options = poll.value?.options?.plus(
                    PollOption(
                        optionId = "option_3",
                        text = "",
                        label = "Option 3"
                    )
                )
            )

            3 -> _poll.value = poll.value?.copy(
                options = poll.value?.options?.plus(
                    PollOption(
                        optionId = "option_4",
                        text = "",
                        label = "Option 4"
                    )
                )
            )
        }

    }

    fun updatePollOptionText(optionId: String, newText: String) {
        val currentPoll = _poll.value ?: return
        val updatedOptions = currentPoll.options?.map {
            if (it.optionId == optionId) it.copy(text = newText) else it
        }
        _poll.value = currentPoll.copy(options = updatedOptions)
    }

    fun updatePollQuestion(newQuestion: String) {
        val currentPoll = _poll.value ?: return
        _poll.value = currentPoll.copy(question = newQuestion)
    }

    fun removePollOption(optionId: String) {
        if (poll.value?.options?.count() == 1) return
        val currentPoll = _poll.value ?: return
        val updatedOptions = currentPoll.options?.filter { it.optionId != optionId }
        _poll.value = currentPoll.copy(options = updatedOptions)
    }
}
