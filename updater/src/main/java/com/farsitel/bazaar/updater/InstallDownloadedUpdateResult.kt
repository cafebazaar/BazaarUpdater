@file:JvmName("-InstallDownloadedUpdateResult")

package com.farsitel.bazaar.updater

/**
 * Result of asking Bazaar to install the update it already downloaded for this
 * application. [Success] means Bazaar prepared the download and opened its install
 * flow, not that Android finished installing the package.
 */
public sealed class InstallDownloadedUpdateResult {

    public object Success : InstallDownloadedUpdateResult()

    public data class Error(val throwable: Throwable) : InstallDownloadedUpdateResult()

    public fun isSuccess(): Boolean = this is Success

    public fun getError(): Throwable? = (this as? Error)?.throwable
}

public inline fun InstallDownloadedUpdateResult.doOnSuccess(
    call: () -> Unit,
): InstallDownloadedUpdateResult {
    if (this is InstallDownloadedUpdateResult.Success) call()
    return this
}

public inline fun InstallDownloadedUpdateResult.doOnError(
    call: (Throwable) -> Unit,
): InstallDownloadedUpdateResult {
    if (this is InstallDownloadedUpdateResult.Error) call(throwable)
    return this
}
