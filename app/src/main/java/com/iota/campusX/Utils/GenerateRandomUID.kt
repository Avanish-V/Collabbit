package com.iota.campusX.Utils

import java.util.UUID
import kotlin.random.Random


//fun generateUID(): String{
//    return UUID.randomUUID().toString()
//}


object FirestoreIdGenerator {
    private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    private const val LENGTH = 20

    fun generate(): String {
        val sb = StringBuilder(LENGTH)
        repeat(LENGTH) {
            sb.append(ALPHABET[Random.nextInt(ALPHABET.length)])
        }
        return sb.toString()
    }
}
