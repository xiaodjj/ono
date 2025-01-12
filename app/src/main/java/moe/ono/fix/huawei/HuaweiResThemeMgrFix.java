package moe.ono.fix.huawei;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;

import androidx.annotation.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import moe.ono.BuildConfig;
import moe.ono.util.Logger;

public class HuaweiResThemeMgrFix {

    private HuaweiResThemeMgrFix() {
    }

    private static boolean sHwThemeManagerHooked = false;
    private static boolean sHwThemeManagerFailed = false;

    public static void initHook(@NonNull Context context) {
        if (sHwThemeManagerHooked || sHwThemeManagerFailed) {
            return;
        }
        String packageName = context.getPackageName();
        // android.hwtheme.HwThemeManager#getDataSkinThemePackages()ArrayList
        Class<?> kHwThemeManager;
        try {
            kHwThemeManager = Class.forName("android.hwtheme.HwThemeManager");
        } catch (ClassNotFoundException e) {
            sHwThemeManagerFailed = true;
            // not huawei, skip
            return;
        }
        Method getDataSkinThemePackages;
        try {
            getDataSkinThemePackages = kHwThemeManager.getDeclaredMethod("getDataSkinThemePackages");
        } catch (NoSuchMethodException e) {
            sHwThemeManagerFailed = true;
            logIfDebugVersion(e);
            // maybe older version, skip
            return;
        }
        XposedBridge.hookMethod(getDataSkinThemePackages, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                ArrayList<String> list = (ArrayList<String>) param.getResult();
                if (list != null) {
                    list.remove(packageName);
                }
            }
        });
        sHwThemeManagerHooked = true;
    }

    private static boolean sHwResourcesImplFixFailed = false;
    private static Field sResourcesImplField = null;
    private static Field sHwResourcesImplField = null;
    private static Method getDataThemePackagesMethod = null;

    @SuppressLint("PrivateApi")
    public static void fix(@NonNull Context context, @NonNull Resources resources) {
        if (sHwThemeManagerFailed) {
            return;
        }
        try {
            Class.forName("android.content.res.AbsResourcesImpl");
            Class.forName("android.content.res.HwResourcesImpl");
        } catch (ClassNotFoundException e) {
            sHwResourcesImplFixFailed = true;
            // not huawei, skip
            return;
        }
        if (sResourcesImplField == null || sHwResourcesImplField == null || getDataThemePackagesMethod == null) {
            try {
                sResourcesImplField = Resources.class.getDeclaredField("mResourcesImpl");
                sResourcesImplField.setAccessible(true);
                Class<?> kResourcesImpl = Class.forName("android.content.res.ResourcesImpl");
                sHwResourcesImplField = kResourcesImpl.getDeclaredField("mHwResourcesImpl");
                sHwResourcesImplField.setAccessible(true);
                Class<?> kAbsResourcesImpl = Class.forName("android.content.res.AbsResourcesImpl");
                getDataThemePackagesMethod = kAbsResourcesImpl.getDeclaredMethod("getDataThemePackages");
                getDataThemePackagesMethod.setAccessible(true);
            } catch (ReflectiveOperationException e) {
                sHwResourcesImplFixFailed = true;
                logIfDebugVersion(e);
            }
        }
        if (sHwResourcesImplFixFailed) {
            return;
        }
        try {
            Object resImpl = sResourcesImplField.get(resources);
            Object hw = sHwResourcesImplField.get(resImpl);
            ArrayList<String> dataThemePackages = (ArrayList<String>) getDataThemePackagesMethod.invoke(hw);
            if (dataThemePackages != null) {
                dataThemePackages.remove(context.getPackageName());
            }
        } catch (ReflectiveOperationException e) {
            sHwResourcesImplFixFailed = true;
            logIfDebugVersion(e);
        }
    }

    private static void logIfDebugVersion(Throwable e) {
        if (BuildConfig.DEBUG) {
            Logger.e(e);
        }
    }

}
