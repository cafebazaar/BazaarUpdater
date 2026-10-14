package com.farsitel.bazaar.updater

/**
 * Which update situation this application is in, as Bazaar knows it.
 */
public enum class NewVersionState {

    /**
     * The check did not produce a state: it failed, or Bazaar answered with a number
     * this SDK does not know. [NewVersionStateResult.getError] explains a failed check.
     */
    UNKNOWN,

    /**
     * The installed version is the newest one Bazaar knows, so there is nothing to
     * install.
     */
    NO_NEW_VERSION,

    /**
     * Bazaar knows a newer version, but it is not downloaded yet, so there is nothing
     * local to install. [BazaarUpdater.updateApplication] opens Bazaar on that update.
     */
    NEW_VERSION_AVAILABLE,

    /**
     * Bazaar downloaded a newer version and only its install is left, for example
     * because the device refused the silent install the scheduler attempted.
     * [BazaarUpdater.installDownloadedUpdate] is the way to finish it.
     */
    NEW_VERSION_DOWNLOADED,
}

internal fun newVersionStateOf(value: Int): NewVersionState {
    return when (value) {
        NEW_VERSION_STATE_NONE -> NewVersionState.NO_NEW_VERSION
        NEW_VERSION_STATE_AVAILABLE -> NewVersionState.NEW_VERSION_AVAILABLE
        NEW_VERSION_STATE_DOWNLOADED -> NewVersionState.NEW_VERSION_DOWNLOADED
        // Wire 0 is UNKNOWN, and so is any number a future Bazaar could send.
        else -> NewVersionState.UNKNOWN
    }
}
