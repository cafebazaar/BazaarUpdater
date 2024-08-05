package com.farsitel.bazaar;
interface IUpdateCheckService {
    long getVersionCode(String packageName);
    long getRemoteVersionCode(String packageName);
}