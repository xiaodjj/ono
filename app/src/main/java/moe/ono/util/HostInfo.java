package moe.ono.util;

import android.app.Application;

import androidx.annotation.NonNull;

import moe.ono.BuildConfig;

/**
 * Helper class for getting host information. Keep it as simple as possible.
 */
public class HostInfo {

    public static final String PACKAGE_NAME_QQ = "com.tencent.mobileqq";
    public static final String PACKAGE_NAME_QQ_INTERNATIONAL = "com.tencent.mobileqqi";
    public static final String PACKAGE_NAME_QQ_LITE = "com.tencent.qqlite";
    public static final String PACKAGE_NAME_QQ_HD = "com.tencent.minihd.qq";
    public static final String PACKAGE_NAME_TIM = "com.tencent.tim";
    public static final String PACKAGE_NAME_SELF = BuildConfig.APPLICATION_ID;

    private HostInfo() {
        throw new AssertionError("No instance for you!");
    }

    @NonNull
    public static Application getApplication() {
        return moe.ono.HostInfo.getHostInfo().getApplication();
    }

    @NonNull
    public static String getPackageName() {
        return moe.ono.HostInfo.getHostInfo().getPackageName();
    }

    @NonNull
    public static String getAppName() {
        return moe.ono.HostInfo.getHostInfo().getHostName();
    }

    @NonNull
    public static String getVersionName() {
        return moe.ono.HostInfo.getHostInfo().getVersionName();
    }

    public static int getVersionCode32() {
        return moe.ono.HostInfo.getHostInfo().getVersionCode32();
    }

    public static int getVersionCode() {
        return getVersionCode32();
    }

    public static long getLongVersionCode() {
        return moe.ono.HostInfo.getHostInfo().getVersionCode();
    }

    public static boolean isInModuleProcess() {
        return moe.ono.HostInfo.isInModuleProcess();
    }

    public static boolean isInHostProcess() {
        return !isInModuleProcess();
    }

    public static boolean isAndroidxFileProviderAvailable() {
        return moe.ono.HostInfo.isAndroidxFileProviderAvailable();
    }

    public static boolean isTim() {
        return moe.ono.HostInfo.isTim();
    }

    public static boolean isQQLite() {
        return PACKAGE_NAME_QQ_LITE.equals(getPackageName());
    }

    public static boolean isQQHD() {
        return PACKAGE_NAME_QQ_HD.equals(getPackageName());
    }

    public static boolean isPlayQQ() {
        return !moe.ono.HostInfo.isPlayQQ();
    }

    public static boolean isQQ() {
        //Improve this method when supporting more clients.
        return !moe.ono.HostInfo.isTim();
    }

    public static boolean requireMinQQVersion(long versionCode) {
        return isQQ() && getLongVersionCode() >= versionCode;
    }

    public static boolean requireMinPlayQQVersion(long versionCode) {
        return isPlayQQ() && getLongVersionCode() >= versionCode;
    }

    public static boolean requireMinTimVersion(long versionCode) {
        return isTim() && getLongVersionCode() >= versionCode;
    }
}
