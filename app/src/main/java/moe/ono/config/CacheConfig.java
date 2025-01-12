package moe.ono.config;

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
