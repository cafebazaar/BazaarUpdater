package com.farsitel.bazaar.updater

/**
 * Which of the three update situations this application is in, as Bazaar knows it.
 */
public enum class NewVersionState {

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

    /**
     * The installed version is the newest one Bazaar knows, so there is nothing to
     * install.
     */
    NO_NEW_VERSION,
}

internal fun newVersionStateOf(value: Int): NewVersionState {
    return when (value) {
        NEW_VERSION_STATE_AVAILABLE -> NewVersionState.NEW_VERSION_AVAILABLE
        NEW_VERSION_STATE_DOWNLOADED -> NewVersionState.NEW_VERSION_DOWNLOADED
        NEW_VERSION_STATE_NONE -> NewVersionState.NO_NEW_VERSION
        // An answer this SDK does not know is treated as nothing to install.
        else -> NewVersionState.NO_NEW_VERSION
    }
}
