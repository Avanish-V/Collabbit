package com.iota.campusX.Navigation

sealed class Routes(val routes:String){

    data object Register : Routes("REGISTER"){
        data object SignIn : Routes("SIGNIN")
        data object CreateProfile : Routes("CREATE_PROFILE")
    }

    data object Main : Routes("MAIN"){

        data object Home : Routes("HOME")
        data object PostViewScreen : Routes("POST_VIEW_SCREEN")
        data object ChatList : Routes("CHAT_LIST")
        data object SendMessage : Routes("SEND_MESSAGE")
        data object Search : Routes("SEARCH")
        data object Voxci : Routes("VOXCI")
        data object Notification : Routes("NOTIFICATION")
        data object Profile : Routes("PROFILE")
        data object ProfileByID : Routes("PROFILE_By_Id")
        data object EditProfile : Routes("EDIT_PROFILE")
        data object Connections : Routes("CONNECTIONS")
        data object Followers : Routes("FOLLOWERS")
        data object CreatePost : Routes("CREATE_POST")
        data object ReplyPost : Routes("REPLY_POST")
        data object Setting : Routes("SETTING")
        data object Society: Routes("SOCIETY")
        data object CreateSociety: Routes("CREATE_SOCIETY")
        data object JoinSociety: Routes("JOIN_SOCIETY")

    }

    companion object {
        val bottomBarRoutes = listOf(
            Main.Home.routes,
            Main.Search.routes,
            Main.Notification.routes,
            Main.Profile.routes,
            Main.Society.routes
        )

    }
    
}

fun shouldShowBottomBar(currentRoute: String?): Boolean {
    return currentRoute in Routes.bottomBarRoutes
}

