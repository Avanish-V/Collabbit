package com.iota.campusX.Feature.Notificattion.data.api

import com.iota.campusX.Feature.Notificattion.data.dto.NotificationResponse
import com.iota.campusX.Feature.Post.data.remote.api.PageResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch

class NotificationApi(

    private val client: HttpClient

) {

    suspend fun getNotifications(

        page: Int,

        size: Int

    ): PageResponse<NotificationResponse> {

        return client.get(

            "/notifications"

        ) {

            parameter("page", page)

            parameter("size", size)

        }.body()
    }

    suspend fun getNotification(
        id: Long
    ): NotificationResponse {

        return client.get(

            "/notifications/$id"

        ).body()

    }

    suspend fun syncNotifications(
        after: Long
    ): List<NotificationResponse> {

        return client.get(

            "/notifications/sync"

        ) {

            parameter("after", after)

        }.body()

    }

    suspend fun unreadCount(): Long {

        return client.get(

            "/notifications/unread-count"

        ).body()
    }

    suspend fun markRead(

        id: Long

    ) {

        client.patch(

            "/notifications/$id/read"

        )
    }

    suspend fun markAllRead() {

        client.patch(
            "/notifications/read-all"
        )
    }

}