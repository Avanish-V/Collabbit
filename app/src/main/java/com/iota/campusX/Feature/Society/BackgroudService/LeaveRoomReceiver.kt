package com.iota.campusX.Feature.Society.BackgroudService

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.iota.campusX.Feature.Society.domain.repository.StreamRepository
import com.iota.campusX.feature.society.backgroundservice.AudioForegroundService
import org.koin.java.KoinJavaComponent.inject

class LeaveRoomReceiver : BroadcastReceiver() {

    // Inject your repository (or ViewModel if scoped properly)
    private val streamRepository: StreamRepository by inject(StreamRepository::class.java)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_LEAVE_ROOM) {
            // Leave Agora room
            streamRepository.leaveChannel()
            // Stop Foreground Service
            context.stopService(Intent(context, AudioForegroundService::class.java))
        }
    }

    companion object {
        const val ACTION_LEAVE_ROOM = "com.iota.campusX.ACTION_LEAVE_ROOM"
    }
}
