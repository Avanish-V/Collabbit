package com.iota.campusX.Feature.Post.presentation.feedmenu

import com.iota.campusX.R

sealed class MenuItem(val id: String, val label: String, val icon:Int, val requiresConfirmation: Boolean = false,) {


    data object Edit : MenuItem("edit", "Edit", R.drawable.edit_2)

    data object Delete : MenuItem("delete", "Delete", R.drawable.trash, requiresConfirmation = true)

    data object Report : MenuItem("report", "Report", R.drawable.alert_triangle, requiresConfirmation = true)

}