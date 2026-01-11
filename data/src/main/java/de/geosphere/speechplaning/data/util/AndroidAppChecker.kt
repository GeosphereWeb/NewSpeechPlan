package de.geosphere.speechplaning.data.util

import android.content.Context
import android.content.pm.PackageManager

interface AppChecker {
    fun isAppInstalled(packageName: String): Boolean
}

class AndroidAppChecker(private val context: Context) : AppChecker {
    override fun isAppInstalled(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}
