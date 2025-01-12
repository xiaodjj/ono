package moe.ono.util;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.PermissionChecker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import moe.ono.HostInfo;


@SuppressLint("PrivateApi")
public class SyncUtils {
    public static final int PROC_MAIN = 1;
    public static final int PROC_MSF = 1 << 1;
    public static final int PROC_PEAK = 1 << 2;
    public static final int PROC_TOOL = 1 << 3;
    public static final int PROC_QZONE = 1 << 4;
    public static final int PROC_VIDEO = 1 << 5;
    public static final int PROC_MINI = 1 << 6;
    public static final int PROC_PLUGIN_PROCESS = 1 << 7;
    public static final int PROC_QQFAV = 1 << 8;
    public static final int PROC_TROOP = 1 << 9;
    public static final int PROC_UNITY = 1 << 10;
    public static final int PROC_WXA_CONTAINER = 1 << 11;

    public static final int PROC_OTHERS = 1 << 31;
    private static final Map<Long, Collection<String>> sTlsFlags = new HashMap<>();
    private static int mProcType = 0;
    private static String mProcName = null;
    private static Handler sHandler;
    private static final ExecutorService sExecutor = Executors.newCachedThreadPool();
    private static final String DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION_SUFFIX = ".DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION";

    private SyncUtils() {
        throw new AssertionError("No instance for you!");
    }

    @Nullable
    public static String getDynamicReceiverNotExportedPermission(@NonNull Context ctx) {
        String currentPackageName = ctx.getPackageName();
        String[] possiblePermissions = new String[]{
                currentPackageName + DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION_SUFFIX,
                "com.tencent.msg.permission.pushnotify",
                "com.tencent.tim.msg.permission.pushnotify",
                "com.tencent.qqlite.msg.permission.pushnotify",
        };
        String permissionForReceiver = null;
        // pick the first available permission
        for (String permission : possiblePermissions) {
            if (PermissionChecker.checkSelfPermission(ctx, permission) == PermissionChecker.PERMISSION_GRANTED) {
                permissionForReceiver = permission;
                break;
            }
        }
        return permissionForReceiver;
    }


    public static int getProcessType() {
        if (mProcType != 0) {
            return mProcType;
        }
        String[] parts = getProcessName().split(":");
        if (parts.length == 1) {
            if ("unknown".equals(parts[0])) {
                return PROC_MAIN;
            } else if ("com.tencent.ilink.ServiceProcess".equals(parts[0])) {
                mProcType = PROC_OTHERS;
            } else {
                mProcType = PROC_MAIN;
            }
        } else {
            String tail = parts[parts.length - 1];
            if ("MSF".equals(tail)) {
                mProcType = PROC_MSF;
            } else if ("peak".equals(tail)) {
                mProcType = PROC_PEAK;
            } else if ("tool".equals(tail)) {
                mProcType = PROC_TOOL;
            } else if (tail.startsWith("qzone")) {
                mProcType = PROC_QZONE;
            } else if ("video".equals(tail)) {
                mProcType = PROC_VIDEO;
            } else if (tail.startsWith("mini")) {
                mProcType = PROC_MINI;
            } else if (tail.startsWith("plugin")) {
                mProcType = PROC_PLUGIN_PROCESS;
            } else if (tail.startsWith("troop")) {
                mProcType = PROC_TROOP;
            } else if (tail.startsWith("unity")) {
                mProcType = PROC_UNITY;
            } else if (tail.startsWith("wxa_container")) {
                mProcType = PROC_WXA_CONTAINER;
            } else if (tail.startsWith("qqfav")) {
                mProcType = PROC_QQFAV;
            } else {
                mProcType = PROC_OTHERS;
            }
        }
        return mProcType;
    }

    public static boolean isMainProcess() {
        return getProcessType() == PROC_MAIN;
    }

    public static boolean isTargetProcess(int target) {
        return (getProcessType() & target) != 0;
    }

    public static String getProcessName() {
        if (mProcName != null) {
            return mProcName;
        }
        String name = "unknown";
        int retry = 0;
        do {
            try {
                List<ActivityManager.RunningAppProcessInfo> runningAppProcesses =
                        ((ActivityManager) HostInfo.getHostInfo().getApplication().getSystemService(Context.ACTIVITY_SERVICE))
                                .getRunningAppProcesses();
                if (runningAppProcesses != null) {
                    for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : runningAppProcesses) {
                        if (runningAppProcessInfo != null && runningAppProcessInfo.pid == android.os.Process.myPid()) {
                            mProcName = runningAppProcessInfo.processName;
                            return runningAppProcessInfo.processName;
                        }
                    }
                }
            } catch (Throwable e) {
                Logger.e("getProcessName error " + e);
            }
            retry++;
            if (retry >= 3) {
                break;
            }
        } while ("unknown".equals(name));
        return name;
    }

    public static void runOnUiThread(@NonNull Runnable r) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            r.run();
        } else {
            post(r);
        }
    }

    public static void async(@NonNull Runnable r) {
        sExecutor.execute(r);
    }

    @SuppressLint("LambdaLast")
    public static void postDelayed(@NonNull Runnable r, long ms) {
        if (sHandler == null) {
            sHandler = new Handler(Looper.getMainLooper());
        }
        sHandler.postDelayed(r, ms);
    }

    public static void postDelayed(long ms, @NonNull Runnable r) {
        postDelayed(r, ms);
    }

    public static void post(@NonNull Runnable r) {
        postDelayed(r, 0L);
    }

    public static void requiresUiThread() {
        requiresUiThread(null);
    }

    public static void requiresUiThread(@Nullable String msg) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalStateException(msg == null ? "UI thread required" : msg);
        }
    }

    public static void requiresNonUiThread() {
        requiresNonUiThread(null);
    }

    public static void requiresNonUiThread(@Nullable String msg) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new IllegalStateException(msg == null ? "non-UI thread required" : msg);
        }
    }
}
