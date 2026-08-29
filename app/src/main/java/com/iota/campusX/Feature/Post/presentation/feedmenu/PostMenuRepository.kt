package com.iota.campusX.Feature.Post.presentation.feedmenu

interface PostMenuRepository {
    suspend fun getMenuOptions(context: MenuContext): List<MenuItem>
}