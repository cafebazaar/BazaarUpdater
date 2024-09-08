@file:JvmName("-PackageInfoUtils")
@file:JvmSynthetic

package com.farsitel.bazaar.updater

import android.content.Context
import android.content.pm.PackageInfo
import androidx.core.content.pm.PackageInfoCompat

internal fun getPackageInfo(context: Context, packageName: String): PackageInfo? = try {
    context.packageManager.getPackageInfo(packageName, 0)
} catch (ignored: Exception) {
    null
}

internal fun getPackageVersionCode(context: Context, packageName: String): Long? {
    return getPackageInfo(context, packageName)?.let { info ->
        PackageInfoCompat.getLongVersionCode(info)
    }
}

internal fun verifyBazaarIsInstalled(context: Context): Boolean {
    return getPackageInfo(context, BAZAAR_PACKAGE_NAME) != null
}

internal fun getBazaarVersionCode(context: Context): Long {
    return getPackageVersionCode(context, BAZAAR_PACKAGE_NAME) ?: BAZAAR_ERROR_RESULT
}