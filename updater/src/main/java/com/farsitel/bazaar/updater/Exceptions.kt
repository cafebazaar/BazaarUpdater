package com.farsitel.bazaar.updater

class UnknownException(
    override val message: String = "There is some problems, maybe the sign or package" +
            " Name is not same as the application published on Bazaar or maybe Bazaar client is not sync"
) : RuntimeException()

class BazaarIsNotInstalledException(
    override val message: String = "Bazaar is not installed in your device!"
) : RuntimeException()
