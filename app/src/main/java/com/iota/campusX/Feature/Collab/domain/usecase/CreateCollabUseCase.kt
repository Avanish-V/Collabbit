package com.iota.campusX.Feature.Collab.domain.usecase

import com.iota.campusX.Feature.Collab.data.model.CollabResponse
import com.iota.campusX.Feature.Collab.data.model.CreateCollabRequest
import com.iota.campusX.Feature.Collab.domain.repository.CollabRepository

class CreateCollabUseCase(private val repository: CollabRepository)  {
    suspend operator fun invoke(request: CreateCollabRequest): Result<CollabResponse> {
        if(request.title.isBlank()) return Result.failure(Exception("Title cannot be empty"))
        if (request.description.isBlank()) return Result.failure(Exception("Description cannot be empty"))
        if (request.requirements.isEmpty()) return Result.failure(Exception("Requirements cannot be empty"))
        if (request.participantsNeeded <= 0) return Result.failure(Exception("Participants needed must be greater than 0"))
        return repository.createCollab(request)
    }
}