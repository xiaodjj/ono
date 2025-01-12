package moe.ono.hooks.guard;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import android.content.pm.ApplicationInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;

import de.robv.android.xposed.XC_MethodHook;
import moe.ono.startup.HookBase;

public class HookTextView implements HookBase {
    public static String method_name = "拦截卡屏消息";

    public void init(@NonNull ClassLoader cl, @NonNull ApplicationInfo ai) {
        XC_MethodHook hodorHodor = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                CharSequence text = (CharSequence) param.args[0];
                if (text != null) {
                    if (text.length() > 8000) {
                        String truncatedText = "ovo: *疑似卡屏，已阻止此文字消息的加载";
                        param.args[0] = truncatedText;
                    }
                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        };


        findAndHookMethod(TextView.class, "setText", CharSequence.class, TextView.BufferType.class,
                boolean.class, int.class, hodorHodor);
        findAndHookMethod(TextView.class, "setText", CharSequence.class, hodorHodor);
    }

    @Override
    public String getName() {
        return method_name;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public Boolean isEnable() {
        return null;
    }
}
