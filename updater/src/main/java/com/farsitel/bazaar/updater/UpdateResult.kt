package com.farsitel.bazaar.updater

sealed class UpdateResult {
    data class NeedUpdate(val targetVersion: Long) : UpdateResult()
    object AlreadyUpdated : UpdateResult()
    data class Error(val message: String) : UpdateResult()
}

inline fun UpdateResult.doOnUpdateNeeded(call: (Long) -> Unit) {
    if (this is UpdateResult.NeedUpdate) call(targetVersion)
}

inline fun UpdateResult.doOnAlreadyUpdated(call: () -> Unit) {
    if (this is UpdateResult.AlreadyUpdated) call()
}

inline fun UpdateResult.doOnError(call: (String) -> Unit) {
    if (this is UpdateResult.Error) call(message)
}