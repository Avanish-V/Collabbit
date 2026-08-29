package com.iota.campusX.Feature.Post.presentation.feedmenu


class PostMenuRepositoryImpl (

): PostMenuRepository {

    override suspend fun getMenuOptions(context: MenuContext): List<MenuItem> {
        return buildList {
            if (context.isOwner){
                add(MenuItem.Edit)
                add(MenuItem.Delete)
                add(MenuItem.Report)
            }else{
                add(MenuItem.Report)
            }
        }
    }
}