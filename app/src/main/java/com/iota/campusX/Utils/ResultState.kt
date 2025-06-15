package com.iota.campusX.Utils

sealed class ResultState<out T>{

    object Loading : ResultState<Nothing>()

    data class Success<out R>(val data : R):ResultState<R>()

    data class Error(val message: String):ResultState<Nothing>()

}

