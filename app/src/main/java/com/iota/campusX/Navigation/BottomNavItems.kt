package com.iota.campusX.Navigation

import com.iota.campusX.R



data class navItems(

    val item: String,
    val icon: Int,
    val iconBold: Int,
    val route: Any
)


val navBarItems = listOf(
    navItems(
        "Explore",
        R.drawable.home,
        R.drawable.home,
        Home
    ),
    navItems(
        "Collab",
        R.drawable.heart_partner_handshake,
        R.drawable.heart_partner_handshake,
        Collab
    ),
    navItems(
        "Create",
        R.drawable.plus_square,
        R.drawable.plus_square,
        CreatePost
    ),
    navItems(
        "Opportunities",
        R.drawable.briefcase,
        R.drawable.briefcase,
        Opportunities
    ),
    navItems(
        "Profile",
        R.drawable.user,
        R.drawable.user,
        Profile()
    )
)
