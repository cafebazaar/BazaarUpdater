@file:JvmName("-Constants")
@file:JvmSynthetic

package com.farsitel.bazaar.updater

internal const val BAZAAR_UPDATE_INTENT = "com.farsitel.bazaar.service.UpdateCheckService.BIND"
internal const val BAZAAR_AUTO_UPDATE_INTENT = "com.farsitel.bazaar.service.AutoUpdateCheckService.BIND"
internal const val BAZAAR_PACKAGE_NAME = "com.farsitel.bazaar"
internal const val BAZAAR_THIRD_PARTY_APP_DETAIL = "bazaar://details/modal?id="
internal const val BAZAAR_THIRD_PARTY_AUTO_UPDATE = "bazaar://autoupdate/modal?id="
internal const val BAZAAR_THIRD_PARTY_INSTALL_DOWNLOADED_UPDATE = "bazaar://install/modal?id="
internal const val PREPARED_QUERY = "prepared=1"
internal const val BAZAAR_WEB_APP_DETAIL = "https://cafebazaar.ir/app/?id="
internal const val BAZAAR_ERROR_RESULT = -1L

/**
 * The numbers of [IUpdateCheckService.getNewVersionState]; the AIDL documents the
 * contract. Anything else that ever crosses the binder is treated as [NEW_VERSION_STATE_NONE].
 */
internal const val NEW_VERSION_STATE_NONE = 0
internal const val NEW_VERSION_STATE_AVAILABLE = 1
internal const val NEW_VERSION_STATE_DOWNLOADED = 2

internal const val BAZAAR_CODE_REMOTE_VERSION_SUPPORTED = 2400700
internal const val BAZAAR_CODE_AUTO_UPDATE_SUPPORTED = 2600200

/**
 * Bazaar version that first answers [IUpdateCheckService.getNewVersionState] and
 * [IUpdateCheckService.prepareDownloadedUpdateInstall], i.e. 30.1.0. Bump this if the
 * methods ship in a later release.
 */
internal const val BAZAAR_CODE_UPDATE_DOWNLOADED_SUPPORTED = 3000100
