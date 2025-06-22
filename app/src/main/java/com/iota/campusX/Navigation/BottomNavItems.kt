package com.iota.campusX.Navigation

import com.iota.campusX.R



data class navItems(

    val item: String,
    val icon: Int,
    val iconBold: Int,
    val route: String
)


val navBarItems = listOf(
    navItems(
        "Home",
        R.drawable.home_normal,
        R.drawable.home_bold,
        Routes.Main.Home.routes
    ),
    navItems(
        "Search",
        R.drawable.search_normal,
        R.drawable.search_bold,
        Routes.Main.Search.routes
    ),
    navItems(
        "Society",
        R.drawable.people,
        R.drawable.people_bold,
        Routes.Main.Society.routes
    ),
    navItems(
        "Notification",
        R.drawable.notification_normal,
        R.drawable.notification_bold,
        Routes.Main.Notification.routes
    ),
    navItems(
        "Account",
        R.drawable.user_normal,
        R.drawable.user_bold,
        Routes.Main.Profile.routes
    )
)