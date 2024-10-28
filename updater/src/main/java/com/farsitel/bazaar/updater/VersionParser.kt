package com.farsitel.bazaar.updater

import android.content.Context

internal object VersionParser {

    @JvmSynthetic
    fun parseUpdateResponse(version: Long, context: Context): UpdateResult {
        return when {
            version == BAZAAR_ERROR_RESULT -> UpdateResult.Error(UnknownException())
            isUpdated(updatedVersion = version, context = context) -> UpdateResult.AlreadyUpdated
            else -> UpdateResult.NeedUpdate(version)
        }
    }

    private fun isUpdated(updatedVersion: Long, context: Context): Boolean {
        val currentVersion = requireNotNull(getPackageVersionCode(context, context.packageName))
        return currentVersion >= updatedVersion
    }
}