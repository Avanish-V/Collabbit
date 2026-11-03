package com.iota.campusX.Feature.Post.presentation.feedmenu

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class MenuController {

    var context by mutableStateOf<MenuContext?>(null)
        private set

    var dialogContext by mutableStateOf<MenuContext?>(null)
        private set

    var options by mutableStateOf<List<MenuItem>>(emptyList())
        private set

    fun show(context: MenuContext, options: List<MenuItem>) {
        this.context = context
        this.options = options
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
