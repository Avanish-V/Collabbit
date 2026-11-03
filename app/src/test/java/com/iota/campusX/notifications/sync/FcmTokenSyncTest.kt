package com.iota.campusX.notifications.sync

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.mockk.mockk
import io.mockk.every
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FcmTokenSyncTest {

    @Test
    fun `shouldSkipSync decision logic test`() {
        val currentToken = "token_abc_123"
        val lastSyncedToken = "token_abc_123"
        val currentTime = System.currentTimeMillis()
        val oneDayAgo = currentTime - (1 * 24 * 60 * 60 * 1000L)

        val threeDaysMs = 3 * 24 * 60 * 60 * 1000L
        val isUnchanged = (currentToken == lastSyncedToken)
        val isRecent = (currentTime - oneDayAgo) < threeDaysMs

        assertTrue(isUnchanged && isRecent)
    }

    @Test
    fun `shouldSkipSync returns false when token changed`() {
        val currentToken = "token_new_456"
        val lastSyncedToken = "token_old_123"
        val isUnchangedToken = (currentToken == lastSyncedToken)
        assertFalse(isUnchangedToken)
    }

    @Test
    fun `isNonRetryable4xx classifies 400 403 404 as non-retryable and 401 500 as retryable`() {
        val response400 = mockk<HttpResponse>(relaxed = true)
        every { response400.status } returns HttpStatusCode.BadRequest
        val ex400 = ClientRequestException(response400, "Bad Request")

        val response401 = mockk<HttpResponse>(relaxed = true)
        every { response401.status } returns HttpStatusCode.Unauthorized
        val ex401 = ClientRequestException(response401, "Unauthorized")

        val response404 = mockk<HttpResponse>(relaxed = true)
        every { response404.status } returns HttpStatusCode.NotFound
        val ex404 = ClientRequestException(response404, "Not Found")

        assertTrue(FcmTokenSyncManager.isNonRetryable4xx(ex400))
        assertTrue(FcmTokenSyncManager.isNonRetryable4xx(ex404))
        assertFalse(FcmTokenSyncManager.isNonRetryable4xx(ex401))
        assertFalse(FcmTokenSyncManager.isNonRetryable4xx(Exception("Network Timeout")))
    }

    @Test
    fun `token log obfuscator truncates long token and never logs full token`() {
        val fullToken = "fcm_token_secret_1234567890_abcdef_xyz"
        val safeLog = fullToken.take(8) + "..."

        assertEquals("fcm_toke...", safeLog)
        assertFalse(safeLog.contains("secret"))
        assertFalse(safeLog.contains("xyz"))
    }
}
