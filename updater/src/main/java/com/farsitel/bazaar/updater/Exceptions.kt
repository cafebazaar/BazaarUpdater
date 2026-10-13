package com.farsitel.bazaar.updater

import android.content.ComponentName

public class UnknownException(
    override val message: String = "There is some problems, maybe the sign or package" +
            " Name is not same as the application published on Bazaar or maybe Bazaar client is not sync"
) : RuntimeException()

public class BazaarIsNotUpdate(
    override val message: String = "This feature is supported in bazaar version" +
            " $BAZAAR_CODE_AUTO_UPDATE_SUPPORTED and above"
) : RuntimeException()

public class BazaarIsNotInstalledException(
    override val message: String = "Bazaar is not installed in your device!"
) : RuntimeException()

public class InstallDownloadedUpdateException(
    override val message: String = "Bazaar could not prepare the downloaded update for installation!"
) : RuntimeException()

public class InstallWindowNotOpenedException(
    override val message: String = "Bazaar's install window could not be opened. Call" +
            " installDownloadedUpdate while a screen of your app is visible to the user."
) : RuntimeException()

public class ServiceDisconnectionException(
    public val componentName: ComponentName?,
    override val message: String = "Service $componentName is disconnected."
) : RuntimeException()
