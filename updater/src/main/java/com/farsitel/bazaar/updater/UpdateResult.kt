package com.farsitel.bazaar.updater

public sealed class UpdateResult {
    public data class NeedUpdate(val targetVersion: Long) : UpdateResult()
    public object AlreadyUpdated : UpdateResult()
    public data class Error(val throwable: Throwable) : UpdateResult()
}

public inline fun UpdateResult.doOnUpdateNeeded(call: (Long) -> Unit): UpdateResult {
    if (this is UpdateResult.NeedUpdate) call(targetVersion)
    return this
}

public inline fun UpdateResult.doOnAlreadyUpdated(call: () -> Unit): UpdateResult {
    if (this is UpdateResult.AlreadyUpdated) call()
    return this
}

public inline fun UpdateResult.doOnError(call: (Throwable) -> Unit): UpdateResult {
    if (this is UpdateResult.Error) call(throwable)
    return this
}