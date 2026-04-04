package com.aoya.telegami.utils

import android.content.ComponentName
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import com.aoya.telegami.BuildConfig

object PackageHelper {
    fun findEnabledAppComponent(context: Context): ComponentName? {
        with(context.packageManager) {
            val pkgInfo = getPackageInfo(BuildConfig.APPLICATION_ID, PackageManager.GET_ACTIVITIES)!!

            return pkgInfo.activities?.firstOrNull { it.targetActivity != null }?.asComponentName()
        }
    }
}

fun ActivityInfo.asComponentName() = ComponentName(packageName, name)
