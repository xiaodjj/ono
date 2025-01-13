package moe.ono.dexkit;

import static moe.ono.config.ConfigManager.cGetBoolean;
import static moe.ono.config.ConfigManager.cGetString;
import static moe.ono.config.ConfigManager.cPutBoolean;
import static moe.ono.config.ConfigManager.cPutString;
import static moe.ono.constants.Constants.MethodCacheKey_AIOParam;
import static moe.ono.constants.Constants.MethodCacheKey_InputRoot;
import static moe.ono.constants.Constants.MethodCacheKey_MarkdownAIO;
import static moe.ono.constants.Constants.MethodCacheKey_getBuddyName;
import static moe.ono.constants.Constants.MethodCacheKey_getDiscussionMemberShowName;
import static moe.ono.util.Initiator.loadClass;
import static moe.ono.util.Utils.findMethodByName;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;

import org.luckypray.dexkit.DexKitBridge;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;

import java.lang.reflect.Method;
import java.util.Map;

import moe.ono.config.ConfigManager;
import moe.ono.util.Logger;

public class TargetManager {
    public static boolean isNeedFindTarget() {
        return cGetBoolean("isNeedFindTarget", true);
    }

    public static void setIsNeedFindTarget(boolean b) {
        cPutBoolean("isNeedFindTarget", b);
    }

    public static String getLastQQVersion() {
        return cGetString("LastQQVersion", "");
    }

    public static void setLastQQVersion(String version) {
        cPutString("LastQQVersion", version);
    }


    private static void doFind(DexKitBridge bridge, ClassLoader hostClassLoader, FindMethod findMethod, String key) {
        Method targetMethod;

        MethodData methodData = bridge.findMethod(findMethod).single();

        try {
            targetMethod = methodData.getMethodInstance(hostClassLoader);
            String methodSignature = targetMethod.getDeclaringClass().getName() + "#" + targetMethod.getName();
            cPutString(key, methodSignature);
        } catch (Exception e) {
            Logger.e(key, e);
        }
    }


    public static void runMethodFinder(ApplicationInfo ai, ClassLoader cl, Activity activity, final OnTaskCompleteListener listener) {
        var ref = new Object() {
            String result = "";
        };


        new Thread(() -> {
            DexKitExecutor executor = new DexKitExecutor(ai.sourceDir, cl);

            executor.execute((bridge, classLoader) -> doFind(bridge, cl, FindMethod.create()
                    .matcher(MethodMatcher.create()
                            .usingStrings("rootVMBuild")
                    )
            , MethodCacheKey_AIOParam));
            ref.result = ref.result + "-> " + cGetString(MethodCacheKey_AIOParam, null);

            executor.execute((bridge, classLoader) -> doFind(bridge, cl, FindMethod.create()
                            .matcher(MethodMatcher.create()
                                    .usingStrings("inputRoot.findViewById(R.id.send_btn)")
                            )
                    , MethodCacheKey_InputRoot));
            ref.result = ref.result + "\n\n-> " + cGetString(MethodCacheKey_InputRoot, null);

            executor.execute((bridge, classLoader) -> doFind(bridge, cl, FindMethod.create()
                            .matcher(MethodMatcher.create()
                                    .usingStrings("AIOMarkdownContentComponent")
                                    .usingStrings("bind status=")
                                    .paramCount(2)
                            )
                    , MethodCacheKey_MarkdownAIO));
            ref.result = ref.result + "\n\n-> " + cGetString(MethodCacheKey_MarkdownAIO, null);


            executor.execute((bridge, classLoader) -> doFind(bridge, cl, FindMethod.create()
                            .matcher(MethodMatcher.create()
                                    .usingStrings("getBuddyName()")
                            )
                    , MethodCacheKey_getBuddyName));
            ref.result = ref.result + "\n\n-> " + cGetString(MethodCacheKey_getBuddyName, null);


            executor.execute((bridge, classLoader) -> doFind(bridge, cl, FindMethod.create()
                            .matcher(MethodMatcher.create()
                                    .usingStrings("getDiscussionMemberShowName uin is null")
                            )
                    , MethodCacheKey_getDiscussionMemberShowName));
            ref.result = ref.result + "\n\n-> " + cGetString(MethodCacheKey_getDiscussionMemberShowName, null);


            ref.result = ref.result + "\n\n\n... 搜索结果已缓存";

            activity.runOnUiThread(() -> {
                if (listener != null) {
                    listener.onTaskComplete(ref.result);
                }
            });
        }).start();
    }

    public static void removeAllMethodSignature() {
        ConfigManager defaultConfig = ConfigManager.getCache();
        Map<String, ?> allEntries = defaultConfig.getAll();
        SharedPreferences.Editor editor = defaultConfig.edit();

        for (String key : allEntries.keySet()) {
            if (key.startsWith("method_")) {
                editor.remove(key);
            }
        }
        editor.apply();
    }

    public static Method getMethod(String key) {
        String cachedMethodSignature = cGetString(key, null);

        try {
            String[] parts = cachedMethodSignature.split("#");
            String className = parts[0];
            String methodName = parts[1];
            Class<?> clazz = loadClass(className);
            return findMethodByName(clazz, methodName);
        } catch (Exception e) {
            Logger.e(key, e);
        }
        return null;
    }

    public interface OnTaskCompleteListener {
        void onTaskComplete(String result);
    }
}
