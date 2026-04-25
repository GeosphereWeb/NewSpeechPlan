package de.geosphere.speechplaning.data.util

import android.content.Context
import android.content.pm.PackageManager

fun interface AppChecker {
    fun isAppInstalled(packageName: String): Boolean
}

@Suppress("SwallowedException")
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
