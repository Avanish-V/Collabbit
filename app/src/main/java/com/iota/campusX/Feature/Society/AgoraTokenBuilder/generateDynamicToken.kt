package com.iota.campusX.Feature.Society.AgoraTokenBuilder

import com.iota.campusX.Feature.Society.AgoraTokenBuilder.media.RtcTokenBuilder2

 fun generateDynamicToken(channelId: String, uid: Int):String{

        val tokenBuilder = RtcTokenBuilder2()
        val timestamp = (System.currentTimeMillis() / 1000 + 3600).toInt()
        val token = tokenBuilder.buildTokenWithUid(
            "8be7292d7bb44b46b541bc72316ebc5a","80e43674c9ce4c20bca7db785983fdd7",
            channelId,
            uid,RtcTokenBuilder2.Role.ROLE_PUBLISHER,
            timestamp,timestamp
        )
        return token
    }

fun generateDigitRandom(): Int {
    return (1000..9999).random()
}