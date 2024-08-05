package com.farsitel.bazaar.updater

sealed class UpdateResult {
    data class NeedUpdate(val targetVersion: Long) : UpdateResult()
    object AlreadyUpdated : UpdateResult()
    data class Error(val throwable: Throwable) : UpdateResult()
}

inline fun UpdateResult.doOnUpdateNeeded(call: (Long) -> Unit): UpdateResult {
    if (this is UpdateResult.NeedUpdate) call(targetVersion)
    return this
}

inline fun UpdateResult.doOnAlreadyUpdated(call: () -> Unit): UpdateResult {
    if (this is UpdateResult.AlreadyUpdated) call()
    return this
}

inline fun UpdateResult.doOnError(call: (Throwable) -> Unit): UpdateResult {
    if (this is UpdateResult.Error) call(throwable)
    return this
}