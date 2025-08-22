package com.iota.campusX.Screens.Post.PostMenuActions

import com.iota.campusX.R

sealed class MenuAction(val id: String, val label: String,val icon:Int, val requiresConfirmation: Boolean = false) {

    data object Edit : MenuAction("edit", "Edit", R.drawable.edit)

    data object Delete : MenuAction("delete", "Delete",R.drawable.trash, requiresConfirmation = true)

    data object Report : MenuAction("report", "Report",R.drawable.warning_2, requiresConfirmation = true)

}
