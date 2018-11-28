package com.fx.elephfin.verupdate;


/**
 * 版本信息
 * Created by 莫仁周 on 2017/2/28 0028.
 */

public class AppVersionInfo {
    private int versionCode = 0;
    private String versionName = "";
    private String apkDownloadUrl = "";

    public String getApkDownloadUrl() {
        return apkDownloadUrl;
    }

    public void setApkDownloadUrl(String apkDownloadUrl) {
        this.apkDownloadUrl = apkDownloadUrl;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }
}
