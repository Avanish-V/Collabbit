package com.iota.campusX.Feature.Society.presentation.SocietyMenuOptions

import com.iota.campusX.R

sealed class SocietyMenuOptions( val label: String, val icon:Int, val requiresConfirmation: Boolean = false) {

    data object Edit : SocietyMenuOptions(label = "Edit",icon = R.drawable.edit)

    data object Notify : SocietyMenuOptions(label = "Notify",  icon =R.drawable.notification_bold)

    data object Delete : SocietyMenuOptions(label = "Delete", icon =R.drawable.trash, requiresConfirmation = true,)

}