package com.iota.campusX.Utils

import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.api.Context


fun checkAutoAppUpdate(context: android.content.Context){


    val appUpdateManager = AppUpdateManagerFactory.create(context)

// Returns an intent object that you use to check for an update.
    val appUpdateInfoTask = appUpdateManager.appUpdateInfo

// Checks that the platform will allow the specified type of update.
//    appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
//        if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
//            // This example applies an immediate update. To apply a flexible update
//            // instead, pass in AppUpdateType.FLEXIBLE
//            && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
//        ) {
//            appUpdateManager.startUpdateFlowForResult(
//                // Pass the intent that is returned by 'getAppUpdateInfo()'.
//                appUpdateInfo,
//                // an activity result launcher registered via registerForActivityResult
//                //activityResultLauncher,
//                // Or pass 'AppUpdateType.FLEXIBLE' to newBuilder() for
//                // flexible updates.
//                AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE)
//                    .setAllowAssetPackDeletion(true)
//                    .build())
//        }
//    }


}