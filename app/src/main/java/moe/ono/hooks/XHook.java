package moe.ono.hooks;

import static moe.ono.constants.Constants.PrekCfgXXX;
import static moe.ono.hooks._core.factory.HookItemFactory.getItem;

import androidx.annotation.NonNull;

import java.lang.reflect.Method;
import java.util.Objects;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import moe.ono.config.ConfigManager;
import moe.ono.hooks._base.BaseSwitchFunctionHookItem;

public class XHook {

    public interface BeforeHookedMethod {

        void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable;
    }

    public interface AfterHookedMethod {

        void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable;
    }

    public interface BeforeAndAfterHookedMethod {

        void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable;

        void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable;
    }

    public static void hookAfter(final @NonNull Method method,
                                 int priority, final @NonNull AfterHookedMethod afterHookedMethod) {
        Objects.requireNonNull(method, "method == null");
        XposedBridge.hookMethod(method, afterAlways(priority, afterHookedMethod));
    }

    public static void hookBefore(final @NonNull Method method,
                                  int priority, final @NonNull BeforeHookedMethod beforeHookedMethod) {
        Objects.requireNonNull(method, "method == null");
        XposedBridge.hookMethod(method, beforeAlways(priority, beforeHookedMethod));
    }

    public static XC_MethodHook beforeAlways(int priority,
                                             final @NonNull BeforeHookedMethod beforeHookedMethod) {
        return new XC_MethodHook(priority) {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                beforeHookedMethod.beforeHookedMethod(param);
            }
        };
    }

    public static XC_MethodHook afterAlways(int priority,
                                            final @NonNull AfterHookedMethod afterHookedMethod) {
        return new XC_MethodHook(priority) {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                afterHookedMethod.afterHookedMethod(param);
            }
        };
    }

    public static void hookAfter(final @NonNull Method method,
                                 final @NonNull AfterHookedMethod afterHookedMethod) {
        hookAfter(method, 50, afterHookedMethod);
    }

    public static void hookBefore(final @NonNull Method method,
                                  final @NonNull BeforeHookedMethod beforeHookedMethod) {
        hookBefore(method, 50, beforeHookedMethod);
    }

    public static void hookBefore(final @NonNull BaseSwitchFunctionHookItem baseSwitchFunctionHookItem, final @NonNull Method method,
                                  final @NonNull BeforeHookedMethod beforeHookedMethod) {
        if (baseSwitchFunctionHookItem.isEnabled()) {
            hookBefore(method, ConfigManager.dGetInt(PrekCfgXXX+"hook_priority",50), beforeHookedMethod);
        }
    }
}
