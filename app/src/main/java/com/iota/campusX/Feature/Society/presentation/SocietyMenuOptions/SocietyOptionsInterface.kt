package com.iota.campusX.Feature.Society.presentation.SocietyMenuOptions

import androidx.navigation.NavHostController
import com.iota.campusX.Screens.Post.DataModel.FeedContent
import com.iota.campusX.Screens.Post.PostMenuActions.MenuAction
import com.iota.campusX.ui.UIComponents.ReportReason

interface SocietyOptionsInterface {

    suspend fun getMenuOptions(content: SocietyData): List<SocietyMenuOptions>

    suspend fun executeAction(action: SocietyMenuOptions,content: SocietyData,hasAlreadySubscribe: Boolean?): Result<Unit>

}

data class SocietyData(
    val roomId: String,
    val isOwner: Boolean,
    val ownerId: String,
    val navHostController: NavHostController?
)