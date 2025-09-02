package com.iota.campusX.Feature.Post.Savers

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import com.alihaider.richlinkpreview.MetaData
import com.alihaider.richlinkpreview.ResponseListener
import com.alihaider.richlinkpreview.RichPreview

class LinkPreviewViewModel : ViewModel() {
    private val _previews = mutableStateMapOf<String, MetaData>()
    val previews: Map<String, MetaData> get() = _previews

    fun loadPreview(url: String) {
        if (_previews.containsKey(url)) return

        val richPreview = RichPreview(object : ResponseListener {
            override fun onData(meta: MetaData) {
                _previews[url] = meta
            }

            override fun onError(e: Exception) {}
        })
        richPreview.getPreview(url)
    }
}
