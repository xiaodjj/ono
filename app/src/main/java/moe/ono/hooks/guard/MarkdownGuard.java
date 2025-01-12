package moe.ono.hooks.guard;

import static moe.ono.constants.Constants.MethodCacheKey_MarkdownAIO;
import static moe.ono.util.Utils.findMethodByName;
import static moe.ono.util.Utils.replaceInvalidLinks;
import android.annotation.SuppressLint;
import android.content.pm.ApplicationInfo;
import android.util.Log;

import androidx.annotation.NonNull;

import com.tencent.qqnt.kernel.nativeinterface.MarkdownElement;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import moe.ono.BuildConfig;
import moe.ono.config.ConfigManager;
import moe.ono.hooks._base.BaseSwitchFunctionHookItem;
import moe.ono.hooks._core.annotation.HookItem;
import moe.ono.startup.HookBase;
import moe.ono.util.Logger;


@SuppressLint("DiscouragedApi")
@HookItem(path = "修复/拦截异常 Markdown 消息", description = "劫持非官方机器人发送的 Markdown 消息，并阻止其中的图片资源加载")
public class MarkdownGuard extends BaseSwitchFunctionHookItem {
    public void fix(@NonNull ClassLoader cl) {
        String cachedMethodSignature = ConfigManager.getDefaultConfig().getString(MethodCacheKey_MarkdownAIO, null);
        Method targetMethod = null;

        if (cachedMethodSignature != null) {
            try {
                String[] parts = cachedMethodSignature.split("#");
                String className = parts[0];
                String methodName = parts[1];
                Class<?> clazz = cl.loadClass(className);
                targetMethod = findMethodByName(clazz, methodName);
            } catch (Exception e) {
                Logger.e("Error loading method from cache: " + e);
            }
        }



        try {
            XposedBridge.hookMethod(targetMethod, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    String content = ((MarkdownElement)param.args[0]).getContent();
                    String[] replacedMessage = replaceInvalidLinks(content);
                    Logger.d("replacedMessage\n" + replacedMessage[0]);
                    String content_replaced = replacedMessage[0];
                    if (Boolean.parseBoolean(replacedMessage[1])){
                        content_replaced = content_replaced.replace("`", "^");
                        content_replaced += "\n***\n# 以下消息来自ovo!\n- 提示: 此markdown不合法！已阻止资源加载\n- 原因：此消息内包含了一个或多个非官方的图片资源链接。\ncontent:\n```markdown\n"+content+"```";
                    }
                    MarkdownElement markdownElement = new MarkdownElement(content_replaced);
                    param.args[0] = markdownElement;
                }
            });
        } catch (Exception e) {
            Log.e(BuildConfig.TAG,"err:"+e);
        }
    }

    @Override
    public void load(@NonNull ClassLoader classLoader) throws Throwable {
        fix(classLoader);
    }
}