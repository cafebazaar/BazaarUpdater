package com.farsitel.bazaar.updater

import android.content.Context
import androidx.core.content.pm.PackageInfoCompat

internal object VersionParser {

    fun parseUpdateResponse(version: Long, context: Context): UpdateResult {
        return when {
            version == BAZAAR_ERROR_RESULT -> UpdateResult.Error(ERROR_CORRECT_VERSION)
            isUpdated(updatedVersion = version, context = context) -> UpdateResult.AlreadyUpdated
            else -> UpdateResult.NeedUpdate(version)
        }
    }

    private fun isUpdated(updatedVersion: Long, context: Context): Boolean {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val currentVersion = PackageInfoCompat.getLongVersionCode(packageInfo)
        return currentVersion >= updatedVersion
    }
}