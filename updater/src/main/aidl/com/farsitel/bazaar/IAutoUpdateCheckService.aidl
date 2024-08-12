package com.farsitel.bazaar;
interface IAutoUpdateCheckService {
    boolean isAutoUpdateEnable(String packageName);
    boolean update(String packageName); // false mean you don't have permission
}
