package com.iota.campusX.Utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.O)
fun Context.vibrate() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = getSystemService(VibratorManager::class.java)
        vibratorManager.defaultVibrator.vibrate(
            VibrationEffect.createOneShot(100L, VibrationEffect.DEFAULT_AMPLITUDE)
        )
    } else {
        val vibrator = getSystemService(Vibrator::class.java)
        vibrator?.vibrate(
            VibrationEffect.createOneShot(100L, VibrationEffect.DEFAULT_AMPLITUDE)
        )
    }
}
