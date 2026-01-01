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
        "Link",
        R.drawable.user_link_reguler,
        R.drawable.user_link_bold,
        Routes.Main.Connection.routes
    ),
    navItems(
        "Account",
        R.drawable.user_normal,
        R.drawable.user_bold,
        Routes.Main.Profile.routes
    )
)