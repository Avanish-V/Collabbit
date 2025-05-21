package com.iota.campusX.Utils

import java.util.UUID


fun generateUID(): String{
    return UUID.randomUUID().toString()
}