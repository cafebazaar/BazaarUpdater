@file:JvmName("-UpdateResult")

package com.farsitel.bazaar.updater

public sealed class UpdateResult {
    public data class NeedUpdate(val targetVersion: Long) : UpdateResult()
    public object AlreadyUpdated : UpdateResult()
    public data class Error(val throwable: Throwable) : UpdateResult()

    public fun isAlreadyUpdated(): Boolean {
        return this is AlreadyUpdated
    }

    public fun isUpdateNeeded(): Boolean {
        return this is NeedUpdate
    }

    public fun getTargetVersion(): Long {
        return (this as? NeedUpdate)?.targetVersion ?: BAZAAR_ERROR_RESULT
    }

    public fun getError(): Throwable? {
        return (this as? Error)?.throwable
    }
}

public inline fun UpdateResult.doOnUpdateNeeded(call: (Long) -> Unit): UpdateResult {
    if (isUpdateNeeded()) call(getTargetVersion())
    return this
}

public inline fun UpdateResult.doOnAlreadyUpdated(call: () -> Unit): UpdateResult {
    if (isAlreadyUpdated()) call()
    return this
}

public inline fun UpdateResult.doOnError(call: (Throwable) -> Unit): UpdateResult {
    if (this is UpdateResult.Error) call(throwable)
    return this
}