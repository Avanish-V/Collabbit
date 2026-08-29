package com.iota.campusX.Feature.Post.presentation.feedmenu

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class MenuController {

    var context by mutableStateOf<MenuContext?>(null)
        private set

    var dialogContext by mutableStateOf<MenuContext?>(null)
        private set

    fun show(context: MenuContext) {
        this.context = context
    }

    fun showDialog(context: MenuContext){
        dialogContext = context
    }

    fun dismissDialog(){
        dialogContext = null
    }

    fun dismiss() {
        context = null
    }

}

@Composable
fun rememberMenuController() = remember {
    MenuController()
}