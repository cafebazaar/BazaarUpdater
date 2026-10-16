package com.farsitel.bazaar.updater

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Delivers the SDK's answers on the main thread.
 *
 * The SDK owns this scope on purpose. [BazaarUpdater]'s public overloads hand the
 * service connections the caller's scope (a `lifecycleScope` for an Activity), and an
 * answer must not be cancelled because the caller's screen went away while the remote
 * call was in flight — the service-disconnect and binding-death answers exist for
 * exactly that moment. The supervisor job keeps one listener's failure from cancelling
 * the answers that follow.
 */
internal val mainThreadScope: CoroutineScope =
    CoroutineScope(SupervisorJob() + Dispatchers.Main)
