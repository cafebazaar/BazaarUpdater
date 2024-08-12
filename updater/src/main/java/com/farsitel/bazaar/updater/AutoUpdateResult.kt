package com.farsitel.bazaar.updater

sealed class AutoUpdateResult {
    data class Success(val isEnable: Boolean) : AutoUpdateResult()
    data class Error(val throwable: Throwable) : AutoUpdateResult()
}

inline fun AutoUpdateResult.doOnSuccess(call: (Boolean) -> Unit): AutoUpdateResult {
    if (this is AutoUpdateResult.Success) call(isEnable)
    return this
}

inline fun AutoUpdateResult.doOnError(call: (Throwable) -> Unit): AutoUpdateResult {
    if (this is AutoUpdateResult.Error) call(throwable)
    return this
}