@file:JvmName("-NewVersionStateResult")

package com.farsitel.bazaar.updater

/**
 * The answer of [BazaarUpdater.getNewVersionState]: the state Bazaar reported, or the
 * failure that stopped the check.
 */
public sealed class NewVersionStateResult {

    public data class Result(
        @JvmSynthetic
        internal val state: NewVersionState,
    ) : NewVersionStateResult()

    public data class Error(val throwable: Throwable) : NewVersionStateResult()

    /** The state Bazaar reported, or null when the check failed. */
    public fun getState(): NewVersionState? {
        return (this as? Result)?.state
    }

    public fun getError(): Throwable? {
        return (this as? Error)?.throwable
    }
}

public inline fun NewVersionStateResult.doOnResult(
    call: (NewVersionState) -> Unit,
): NewVersionStateResult {
    getState()?.let(call)
    return this
}

public inline fun NewVersionStateResult.doOnError(
    call: (Throwable) -> Unit,
): NewVersionStateResult {
    getError()?.let(call)
    return this
}
