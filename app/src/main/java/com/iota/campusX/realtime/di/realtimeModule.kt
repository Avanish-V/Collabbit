package com.iota.campusX.realtime.di



import com.iota.campusX.Feature.Notificattion.sync.NotificationSyncManager
import com.iota.campusX.Feature.Notificattion.sync.NotificationSyncManagerImpl
import com.iota.campusX.realtime.dispatcher.SocketEventDispatcher
import com.iota.campusX.realtime.dispatcher.SocketEventDispatcherImpl
import com.iota.campusX.realtime.handler.NotificationEventHandler
import com.iota.campusX.realtime.handler.SocketEventHandler
import com.iota.campusX.realtime.manager.RealtimeManager
import com.iota.campusX.realtime.manager.RealtimeManagerImpl
import com.iota.campusX.realtime.socket.RealtimeSocketManager
import com.iota.campusX.realtime.socket.RealtimeSocketManagerImpl
import org.koin.dsl.module

val realtimeModule = module {


    // Later
    // factory<SocketEventHandler> {
    //     ChatSocketHandler(...)
    // }

    single<SocketEventDispatcher> {
        SocketEventDispatcherImpl(
            handlers = getAll()
        )
    }

    single<RealtimeSocketManager> {
        RealtimeSocketManagerImpl(
            client = get(),
            dispatcher = get(),
            json = get(),
            auth = get()
        )
    }

    single<RealtimeManager> {
        RealtimeManagerImpl(
            socketManager = get()
        )
    }


    single<NotificationSyncManager> { NotificationSyncManagerImpl(get()) }

    single<SocketEventHandler> {
        NotificationEventHandler(
            syncManager = get(),
            json = get()
        )
    }
}