package com.iota.campusX.Feature.Reply

import androidx.lifecycle.ViewModel
import com.iota.campusX.Feature.Post.domain.UseCases.CreateReplyUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.GetRepliesUseCase
import com.iota.campusX.Feature.Reply.domain.repository.ReplyRepository

class ViewUserReplyViewModel(
    private val getRepliesUseCase: GetRepliesUseCase,
    private val createReplyUseCase: CreateReplyUseCase,
    private val replyRepository: ReplyRepository
) : ViewModel() {



}