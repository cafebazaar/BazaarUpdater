package com.farsitel.bazaar.updater

import android.content.Context
import android.content.pm.PackageInfo

internal object Security {

    fun verifyBazaarIsInstalled(context: Context): Boolean {

        return getPackageInfo(context, BAZAAR_PACKAGE_NAME) != null
    }
}

internal fun getPackageInfo(context: Context, packageName: String): PackageInfo? = try {
    val packageManager = context.packageManager
    packageManager.getPackageInfo(packageName, 0)
} catch (ignored: Exception) {
    null
}