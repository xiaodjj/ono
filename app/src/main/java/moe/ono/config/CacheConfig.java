package moe.ono.config;

import android.annotation.SuppressLint;
import android.app.Activity;

import java.util.ArrayList;
import java.util.Objects;

import moe.ono.util.Logger;

public class CacheConfig {

    private CacheConfig() {
        throw new AssertionError("No instance for you!");
    }

    private static Boolean isAutoMosaicNameNT;
    private static Boolean isSetEntry;
    private static final ArrayList<String> registerReceiverList = new ArrayList<>();
    private static int recreateCount;
    private static String rkeyGroup;
    private static String rkeyPrivate;
    @SuppressLint("StaticFieldLeak")
    private static Activity splashActivity;

    public static String getRKeyGroup() {
        if (rkeyGroup == null){
            Logger.e("rkeyGroup is null!");
            throw new NullPointerException("rkeyGroup is null!");
        }
        return rkeyGroup;
    }

    public static String getRKeyPrivate() {
        if (rkeyGroup == null){
            Logger.e("rkeyPrivate is null!");
            throw new NullPointerException("rkeyPrivate is null!");
        }
        return rkeyPrivate;
    }

    public static void setRKeyGroup(String rk) {
        Objects.requireNonNull(rk);
        CacheConfig.rkeyGroup = rk;
    }

    public static void setRKeyPrivate(String rk) {
        Objects.requireNonNull(rk);
        CacheConfig.rkeyPrivate = rk;
    }

    public static void setSplashActivity(Activity activity) {
        Objects.requireNonNull(activity);
        CacheConfig.splashActivity = activity;
    }

    public static Activity getSplashActivity() {
        if (splashActivity == null){
            Logger.e("splashActivity is null!");
            throw new NullPointerException("splashActivity is null!");
        }
        return splashActivity;
    }

    public static int getRecreateCount() {
        return Objects.requireNonNullElse(recreateCount, 0);
    }

    public static Boolean isAutoMosaicNameNT() {
        return Objects.requireNonNullElse(isAutoMosaicNameNT, false);
    }

    public static Boolean isSetEntry() {
        return Objects.requireNonNullElse(isSetEntry, false);
    }

    public static void setAutoMosaicNameNT(Boolean isAutoMosaicNameNT) {
        Objects.requireNonNull(isAutoMosaicNameNT);
        CacheConfig.isAutoMosaicNameNT = isAutoMosaicNameNT;
    }


    public static void setIsSetEntry(Boolean isSetEntry) {
        Objects.requireNonNull(isSetEntry);
        CacheConfig.isSetEntry = isSetEntry;
    }

    public static void addRecreateCount() {
        CacheConfig.recreateCount = getRecreateCount() + 1;
    }

    public static void addReceiver(String name) {
        Objects.requireNonNull(name);

        if (!isReceiverRegistered(name)) {
            CacheConfig.registerReceiverList.add(name);
        } else {
            Logger.i("Receiver " + name + " is already registered.");
        }
    }

    public static boolean isReceiverRegistered(String name) {
        Objects.requireNonNull(name, "Receiver name cannot be null");
        try {
            return CacheConfig.registerReceiverList.contains(name);
        } catch (Exception e) {
            return false;
        }

    }

}
