package com.iota.campusX.MessagingService

import android.app.Application
import android.util.Log
import com.iota.campusX.Feature.Collab.di.collabModule
import com.iota.campusX.Feature.Notificattion.di.notificationModule
import com.iota.campusX.Feature.Opportunities.di.opportunitiesModule
import com.iota.campusX.Feature.Post.di.postModule
import com.iota.campusX.Feature.UserProfile.di.profileModule
import com.iota.campusX.Koin.*
import com.iota.campusX.NetworkMonitor.networkModule
import com.iota.campusX.realtime.di.realtimeModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class CampusCircleApplication : Application() {

    override fun onCreate() {

        super.onCreate()
        Log.d("CampusXApp", "Application onCreate() called")

        NotificationChannelManager.create(this)

        startKoin {
            androidContext(this@CampusCircleApplication)
            modules(
                notificationModule,
                coreModule,
                authModule,
                postModule,
                firebaseModule,
                chatModule,
                profileModule,
                reportModule,
                navigationModule,
                replyModule,
                themeMode,
                networkModule,
                collabModule,
                opportunitiesModule,
                realtimeModule
            )
        }
    }
}
