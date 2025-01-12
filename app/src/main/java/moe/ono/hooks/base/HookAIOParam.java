package moe.ono.hooks.base;

import static moe.ono.constants.Constants.MethodCacheKey_AIOParam;
import static moe.ono.util.Utils.findMethodByName;

import android.content.pm.ApplicationInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import moe.ono.config.ConfigManager;
import moe.ono.hooks._base.BaseFunctionHookItem;
import moe.ono.hooks._core.annotation.HookItem;
import moe.ono.reflex.XField;
import moe.ono.startup.HookBase;
import moe.ono.util.Initiator;
import moe.ono.util.Logger;
import moe.ono.util.Session;

@HookItem(path = "获取AIOParam", description = "")
public class HookAIOParam extends BaseFunctionHookItem {

    private void hookAIOParam(ClassLoader classLoader) {
        String cachedMethodSignature = ConfigManager.getDefaultConfig().getString(MethodCacheKey_AIOParam, null);
        Method targetMethod = null;

        if (cachedMethodSignature != null) {
            try {
                String[] parts = cachedMethodSignature.split("#");
                String className = parts[0];
                String methodName = parts[1];
                Class<?> clazz = classLoader.loadClass(className);
                targetMethod = findMethodByName(clazz, methodName);
            } catch (Exception e) {
                Logger.e("Error loading method from cache: " + e);
            }
        }

        try {
            XposedBridge.hookMethod(targetMethod, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Bundle bundle = (Bundle) param.args[0];
                    Object cAIOParam = bundle.getParcelable("aio_param");

                    Object AIOSession = XField.obj(cAIOParam).type(Initiator.loadClass("com.tencent.aio.data.AIOSession")).get();
                    Object AIOContact = XField.obj(AIOSession).type(Initiator.loadClass("com.tencent.aio.data.AIOContact")).get();

                    String cPeerUID = XField.obj(AIOContact).name("f").type(String.class).get();
                    int cChatType = XField.obj(AIOContact).name("e").type(int.class).get();
                    Session.setCurrentPeerID(cPeerUID);
                    Session.setCurrentChatType(cChatType);
                }
            });
        } catch (Exception e) {
            Logger.e("Error hooking method: " + e);
        }
    }

    @Override
    public void load(@NonNull ClassLoader classLoader) throws Throwable {
        hookAIOParam(classLoader);
    }
}