package com.iota.campusX.Feature.Society.presentation.SocietyMenuOptions

import com.iota.campusX.R

sealed class SocietyMenuOptions( val label: String, val icon:Int, val requiresConfirmation: Boolean = false) {

    data object Edit : SocietyMenuOptions("Edit",  R.drawable.edit)

    data object Notify : SocietyMenuOptions("Notify",  R.drawable.notification_bold)


    data object Delete : SocietyMenuOptions("delete", R.drawable.trash, requiresConfirmation = true,)

}