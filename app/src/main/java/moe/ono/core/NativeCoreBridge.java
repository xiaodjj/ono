package moe.ono.core;

import android.content.Context;

import androidx.annotation.NonNull;

import com.tencent.mmkv.MMKV;

import java.io.File;

import moe.ono.startup.StartupInfo;
import moe.ono.util.HostInfo;


public class NativeCoreBridge {
    static {
        System.loadLibrary("dexkit");
    }


    private NativeCoreBridge() {
        throw new AssertionError("No instances for you!");
    }


    private static boolean sPrimaryNativeLibraryInitialized = false;

    public static void initNativeCore() {
        Context context = HostInfo.getApplication();

        // init mmkv
        initializeMmkvForPrimaryNativeLibrary(context);

        // no native code yet ...
    }

    /**
     * Load native library and initialize MMKV
     *
     * @param ctx Application context
     * @throws LinkageError if failed to load native library
     */
    public static void initializeMmkvForPrimaryNativeLibrary(@NonNull Context ctx) {
        if (sPrimaryNativeLibraryInitialized) {
            return;
        }
        File filesDir = ctx.getFilesDir();
        File mmkvDir = new File(filesDir, "ono_mmkv");
        if (!mmkvDir.exists()) {
            mmkvDir.mkdirs();
        }
        // MMKV requires a ".tmp" cache directory, we have to create it manually
        File cacheDir = new File(mmkvDir, ".tmp");
        if (!cacheDir.exists()) {
            cacheDir.mkdir();
        }
        MMKV.initialize(ctx, mmkvDir.getAbsolutePath());
        MMKV.mmkvWithID("global_config", MMKV.MULTI_PROCESS_MODE);
        MMKV.mmkvWithID("global_cache", MMKV.MULTI_PROCESS_MODE);
        sPrimaryNativeLibraryInitialized = true;
    }

}
