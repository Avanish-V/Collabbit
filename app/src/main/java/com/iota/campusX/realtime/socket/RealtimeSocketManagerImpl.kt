 package com.iota.campusX.realtime.socket

import android.util.Log
import com.iota.campusX.realtime.connection.SocketConnectionState
import com.iota.campusX.realtime.dispatcher.SocketEventDispatcher
import com.iota.campusX.realtime.model.SocketEvent
import com.iota.campusX.realtime.model.SocketEventType
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.milliseconds

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.channels.BufferOverflow
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class RealtimeSocketManagerImpl(

    private val client: HttpClient,
    private val dispatcher: SocketEventDispatcher,
    private val json: Json,
    private val auth: FirebaseAuth

) : RealtimeSocketManager {

    private var reconnectAttempt = 0
    private var heartbeatJob: Job? = null

    private var manuallyDisconnected = false

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var session: DefaultClientWebSocketSession? = null

    private var socketJob: Job? = null
    private val _connectionState = MutableStateFlow<SocketConnectionState>(
        SocketConnectionState.Disconnected)

    init {
        Log.i(TAG, "RealtimeSocketManagerImpl initialized")
    }

    private val _incomingEvents = MutableSharedFlow<SocketEvent>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val incomingEvents: SharedFlow<SocketEvent> = _incomingEvents.asSharedFlow()

    override fun connect() {

        manuallyDisconnected = false

        Log.i(TAG, "connect() called")

        if (socketJob?.isActive == true) {
            Log.i(TAG, "Already connected")
            return
        }

        socketJob = scope.launch {
            connectionLoop()
        }
    }

    override fun disconnect() {

        manuallyDisconnected = true

        socketJob?.cancel()

        socketJob = null

        scope.launch {
            session?.close()
        }

        session = null

        _connectionState.value = SocketConnectionState.Disconnected
    }

    override suspend fun send(event: SocketEvent) {
        val socket = session ?: return
        socket.send(
            Frame.Text(
                json.encodeToString(event)
            )
        )
    }

    override val connectionState = _connectionState.asStateFlow()

    private suspend fun openConnection() {
        Log.i(TAG, "Opening connection...")
        _connectionState.value =
            SocketConnectionState.Connecting

        val currentUser = auth.currentUser
        val token = try {
            currentUser?.getIdToken(true)?.await()?.token
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get Firebase token", e)
            null
        }

        val pathWithToken = if (token != null) "/ws/realtime?token=$token" else "/ws/realtime"

        client.webSocket(
            path = pathWithToken
        ) {

            session = this

            reconnectAttempt = 0

            _connectionState.value = SocketConnectionState.Connected


            startHeartbeat()

            try {

                receiveLoop()

            } finally {

                heartbeatJob?.cancel()
                heartbeatJob = null

                session = null
            }

        }

    }

    private suspend fun reconnect(
        cause: Throwable
    ) {

        if (manuallyDisconnected) return

        reconnectAttempt++

        val delayMillis = minOf(
            INITIAL_RECONNECT_DELAY * (1L shl (reconnectAttempt - 1)),
            MAX_RECONNECT_DELAY
        )

        Log.w(
            TAG,
            "Reconnect attempt #$reconnectAttempt in ${delayMillis}ms",
            cause
        )

        _connectionState.value =
            SocketConnectionState.Reconnecting(
                reconnectAttempt
            )

        delay(delayMillis.milliseconds)
    }

    private suspend fun DefaultClientWebSocketSession.receiveLoop() {

        for (frame in incoming) {

            when (frame) {

                is Frame.Text ->

                    processTextFrame(frame)

                is Frame.Pong -> {
                    Log.d(TAG, "Pong received")
                }

                is Frame.Close ->

                    return

                else -> Unit
            }

        }

    }

    private suspend fun processTextFrame(frame: Frame.Text) {

        val event = try {
            json.decodeFromString<SocketEvent>(frame.readText())
        } catch (e: SerializationException) {
            Log.e(TAG, "Invalid socket payload", e)
            return
        }

        _incomingEvents.emit(event)

        try {
            dispatcher.dispatch(event)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to handle event: ${event.eventType}", e)
        }
    }

    private companion object {

        const val TAG = "RealtimeSocket"

        private const val INITIAL_RECONNECT_DELAY = 2_000L

        private const val MAX_RECONNECT_DELAY = 30_000L

    }

    private suspend fun connectionLoop() {

        while (!manuallyDisconnected) {

            try {

                openConnection()

                reconnectAttempt = 0

            } catch (e: CancellationException) {

                throw e

            } catch (e: Exception) {

                reconnect(e)

            }

        }

    }


    private fun startHeartbeat() {

        heartbeatJob?.cancel()

        heartbeatJob = scope.launch {

            while (isActive) {

                delay(30_000)

                sendPing()

            }

        }
    }

    private suspend fun sendPing() {

        val socket = session ?: return


        val ping = SocketEvent(

            eventType = SocketEventType.PING,

            payload = json.encodeToJsonElement(EmptyPayload())

        )

        socket.send(
            Frame.Text(
                json.encodeToString(ping)
            )
        )
    }

}

@Serializable
data class EmptyPayload(
    val value: String = ""
)