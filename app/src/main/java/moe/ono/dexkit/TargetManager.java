package moe.ono.dexkit;

import static moe.ono.constants.Constants.MethodCacheKey_AIOParam;
import static moe.ono.constants.Constants.MethodCacheKey_InputRoot;
import static moe.ono.constants.Constants.MethodCacheKey_MarkdownAIO;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.widget.Toast;

import org.luckypray.dexkit.DexKitBridge;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;

import java.lang.reflect.Method;
import java.util.Map;

import moe.ono.config.ConfigManager;
import moe.ono.util.Logger;
import moe.ono.util.SyncUtils;

public class TargetManager {
    public static boolean isNeedFindTarget() {
        return ConfigManager.getDefaultConfig().getBooleanOrDefault("isNeedFindTarget", true);
    }

    public static void setIsNeedFindTarget(boolean b) {
        ConfigManager.getDefaultConfig().edit().putBoolean("isNeedFindTarget", b).apply();
    }

    public static String getLastQQVersion() {
        return ConfigManager.getDefaultConfig().getStringOrDefault("LastQQVersion", "");
    }

    public static void setLastQQVersion(String version) {
        ConfigManager.getDefaultConfig().edit().putString("LastQQVersion", version).apply();
    }


    private static void findAIOParam(DexKitBridge bridge, ClassLoader hostClassLoader) {
        Method targetMethod;

        MethodData methodData = bridge.findMethod(FindMethod.create()
                .matcher(MethodMatcher.create()
                        .usingStrings("rootVMBuild")
                )
        ).single();

        try {
            targetMethod = methodData.getMethodInstance(hostClassLoader);
            String methodSignature = targetMethod.getDeclaringClass().getName() + "#" + targetMethod.getName();
            ConfigManager.getDefaultConfig().edit().putString(MethodCacheKey_AIOParam, methodSignature).apply();
        } catch (Exception e) {
            Logger.e("Error finding method: " + e);
        }
    }

    public static void findInputRoot(DexKitBridge bridge, ClassLoader hostClassLoader) {
        MethodData methodData = bridge.findMethod(FindMethod.create()
                .matcher(MethodMatcher.create()
                        .usingStrings("inputRoot.findViewById(R.id.send_btn)")
                )
        ).single();


        try {
            Method targetMethod = methodData.getMethodInstance(hostClassLoader);
            String methodSignature = targetMethod.getDeclaringClass().getName() + "#" + targetMethod.getName();
            ConfigManager.getDefaultConfig().edit().putString(MethodCacheKey_InputRoot, methodSignature).apply();
        } catch (Exception e) {
            Logger.e("Error finding method: " + e);
        }
    }

    public static void findMarkdownAIO(DexKitBridge bridge, ClassLoader hostClassLoader) {
        MethodData methodData = bridge.findMethod(FindMethod.create()
                .matcher(MethodMatcher.create()
                        .usingStrings("AIOMarkdownContentComponent")
                        .usingStrings("bind status=")
                        .paramCount(2)
                )
        ).single();

        try {
            Method targetMethod = methodData.getMethodInstance(hostClassLoader);
            String methodSignature = targetMethod.getDeclaringClass().getName() + "#" + targetMethod.getName();
            ConfigManager.getDefaultConfig().edit().putString(MethodCacheKey_MarkdownAIO, methodSignature).apply();
        } catch (Exception e) {
            Logger.e("Error finding method: " + e);
        }

    }

    public static void runMethodFinder(ApplicationInfo ai, ClassLoader cl, Activity activity, final OnTaskCompleteListener listener) {
        var ref = new Object() {
            String result = "";
        };


        new Thread(() -> {
            DexKitExecutor executor = new DexKitExecutor(ai.sourceDir, cl);

            executor.execute((bridge, classLoader) -> findAIOParam(bridge, cl));
            ref.result = ref.result + "findAIOParam -> " + ConfigManager.getDefaultConfig().getString(MethodCacheKey_AIOParam, null);

            executor.execute((bridge, classLoader) -> findInputRoot(bridge, cl));
            ref.result = ref.result + "\n\nfindInputRoot -> " + ConfigManager.getDefaultConfig().getString(MethodCacheKey_InputRoot, null);

            executor.execute(TargetManager::findMarkdownAIO);
            ref.result = ref.result + "\n\nfindMarkdownAIO -> " + ConfigManager.getDefaultConfig().getString(MethodCacheKey_MarkdownAIO, null);

            ref.result = ref.result + "\n\n\n... 搜索结果已缓存";

            activity.runOnUiThread(() -> {
                if (listener != null) {
                    listener.onTaskComplete(ref.result);
                }
            });
        }).start();
    }

    public static void removeAllMethodSignature() {
        ConfigManager defaultConfig = ConfigManager.getDefaultConfig();
        Map<String, ?> allEntries = defaultConfig.getAll();
        SharedPreferences.Editor editor = defaultConfig.edit();

        for (String key : allEntries.keySet()) {
            if (key.startsWith("method_")) {
                editor.remove(key);
            }
        }
        editor.apply();
    }

    public interface OnTaskCompleteListener {
        void onTaskComplete(String result);
    }
}
