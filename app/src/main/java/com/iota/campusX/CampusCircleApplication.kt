package com.iota.campusX

import android.app.Application
import android.util.Log
import com.iota.campusX.Feature.Collab.di.collabModule
import com.iota.campusX.Feature.ExploreSwipe.di.exploreSwipeModule
import com.iota.campusX.Feature.Notificattion.di.notificationModule
import com.iota.campusX.Feature.Opportunities.di.opportunitiesModule
import com.iota.campusX.Feature.Post.di.postModule
import com.iota.campusX.Feature.UserProfile.di.profileModule
import com.iota.campusX.Feature.Auth.di.authModule
import com.iota.campusX.Koin.chatModule
import com.iota.campusX.Koin.coreModule
import com.iota.campusX.Koin.firebaseModule
import com.iota.campusX.Koin.navigationModule
import com.iota.campusX.Koin.replyModule
import com.iota.campusX.Koin.reportModule
import com.iota.campusX.Koin.societyModule
import com.iota.campusX.core.notifications.NotificationChannelManager
import com.iota.campusX.Koin.realtimeModule
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
                collabModule,
                opportunitiesModule,
                realtimeModule,
                societyModule,
                exploreSwipeModule
            )
        }
    }
}