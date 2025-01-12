package moe.ono.startup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.lsposed.hiddenapibypass.HiddenApiBypass;

import java.io.File;
import java.lang.reflect.Field;


import moe.ono.BuildConfig;
import moe.ono.constants.Constants;
import moe.ono.loader.hookapi.IHookBridge;
import moe.ono.loader.hookapi.ILoaderService;
import moe.ono.util.IoUtils;

@Keep
public class StartupAgent {

    private static boolean sInitialized = false;

    private StartupAgent() {
        throw new AssertionError("No instance for you!");
    }

    @Keep
    public static void startup(
            @NonNull String modulePath,
            @NonNull String hostDataDir,
            @NonNull ILoaderService loaderService,
            @NonNull ClassLoader hostClassLoader,
            @Nullable IHookBridge hookBridge
    ) {
        if (sInitialized) {
            throw new IllegalStateException("StartupAgent already initialized");
        }
        sInitialized = true;
        if ("true".equals(System.getProperty(StartupAgent.class.getName()))) {
            android.util.Log.e(BuildConfig.TAG, "Error: "+BuildConfig.TAG+" reloaded??");
            return;
        }

        System.setProperty(StartupAgent.class.getName(), "true");
        StartupInfo.setModulePath(modulePath);
        StartupInfo.setLoaderService(loaderService);
        StartupInfo.setHookBridge(hookBridge);
        StartupInfo.setInHostProcess(true);
        // bypass hidden api
        ensureHiddenApiAccess();
        checkWriteXorExecuteForModulePath(modulePath);
        // we want context
        Context ctx = getBaseApplicationImpl(hostClassLoader);
        if (ctx == null) {
            if (hookBridge == null) {
                initializeHookBridgeForEarlyStartup(hostDataDir);
            }
            StartupHook.getInstance().initializeBeforeAppCreate(hostClassLoader);
        } else {
            StartupHook.getInstance().initializeAfterAppCreate(ctx);
        }
    }

    private static void initializeHookBridgeForEarlyStartup(@NonNull String hostDataDir) {
        if (StartupInfo.getHookBridge() != null) {
            return;
        }
        android.util.Log.w(BuildConfig.TAG, "initializeHookBridgeForEarlyStartup w/o context");
        File hostDataDirFile = new File(hostDataDir);
        if (!hostDataDirFile.exists()) {
            throw new IllegalStateException("Host data dir not found: " + hostDataDir);
        }
    }

    private static void checkWriteXorExecuteForModulePath(@NonNull String modulePath) {
        File moduleFile = new File(modulePath);
        if (moduleFile.canWrite()) {
            android.util.Log.w(BuildConfig.TAG, "Module path is writable: " + modulePath);
            android.util.Log.w(BuildConfig.TAG, "This may cause issues on Android 15+, please check your Xposed framework");
        }
    }

    public static Context getBaseApplicationImpl(@NonNull ClassLoader classLoader) {
        Context app;
        try {
            Class<?> clz = classLoader.loadClass(Constants.CLAZZ_BASE_APPLICATION_IMPL);
            Field fsApp = null;
            for (Field f : clz.getDeclaredFields()) {
                if (f.getType() == clz) {
                    fsApp = f;
                    break;
                }
            }
            if (fsApp == null) {
                throw new UnsupportedOperationException("field BaseApplicationImpl.sApplication not found");
            }
            app = (Context) fsApp.get(null);
            return app;
        } catch (ReflectiveOperationException e) {
            android.util.Log.e(BuildConfig.TAG, "getBaseApplicationImpl: failed", e);
            throw IoUtils.unsafeThrow(e);
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private static void ensureHiddenApiAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && !isHiddenApiAccessible()) {
            android.util.Log.w(BuildConfig.TAG, "Hidden API access not accessible, SDK_INT is " + Build.VERSION.SDK_INT);
            HiddenApiBypass.setHiddenApiExemptions("L");
        }
    }

    @SuppressLint({"BlockedPrivateApi", "PrivateApi"})
    public static boolean isHiddenApiAccessible() {
        Class<?> kContextImpl;
        try {
            kContextImpl = Class.forName("android.app.ContextImpl");
        } catch (ClassNotFoundException e) {
            return false;
        }
        Field mActivityToken = null;
        Field mToken = null;
        try {
            mActivityToken = kContextImpl.getDeclaredField("mActivityToken");
        } catch (NoSuchFieldException ignored) {
        }
        try {
            mToken = kContextImpl.getDeclaredField("mToken");
        } catch (NoSuchFieldException ignored) {
        }
        return mActivityToken != null || mToken != null;
    }

}
