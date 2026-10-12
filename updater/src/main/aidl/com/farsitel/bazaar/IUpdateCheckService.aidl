package com.farsitel.bazaar;
interface IUpdateCheckService {
    long getVersionCode(String packageName);
    long getRemoteVersionCode(String packageName);

    /**
     * Which of the three update situations [packageName] is in:
     *   0 - no new version: the installed version is the newest one Bazaar knows;
     *   1 - new version available: Bazaar knows a newer version that is not downloaded;
     *   2 - new version downloaded: a newer version is downloaded and only its install
     *       is left.
     * Only the app that owns the package can ask about it: the caller is identified by
     * the uid behind the package name it passes, and anything else answers 0.
     *
     * When it answers 2, the caller finishes that download through Bazaar's own app
     * page, which already installs a ready download when the user taps install there.
     */
    int getNewVersionState(String packageName);

    /**
     * Fetches the install metadata of the update Bazaar already downloaded for
     * [packageName], so its installation can be finished without any Bazaar screen
     * being open yet. Only the app that owns the package can ask for it.
     *
     * False means the request failed, including a missing download, no connection and
     * server errors. True means the model is ready for the install flow that the
     * caller opens next.
     */
    boolean prepareDownloadedUpdateInstall(String packageName);
}
