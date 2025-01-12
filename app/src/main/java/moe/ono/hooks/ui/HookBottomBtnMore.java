package moe.ono.hooks.ui;

import static moe.ono.constants.Constants.CLAZZ_ACTIVITY_SPLASH;
import static moe.ono.constants.Constants.CLAZZ_PANEL_ICON_LINEAR_LAYOUT;
import static moe.ono.util.Initiator.loadClass;
import static moe.ono.util.SyncUtils.runOnUiThread;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import moe.ono.hooks._base.BaseSwitchFunctionHookItem;
import moe.ono.hooks._core.annotation.HookItem;
import moe.ono.creator.ElementSender;
import moe.ono.hooks.XHook;
import moe.ono.reflex.XMethod;
import moe.ono.util.Logger;

@SuppressLint("DiscouragedApi")
@HookItem(
        path = "开发者选项/QQPacketHelper",
        description = "开启后需在聊天界面长按加号呼出，或长按发送按钮呼出。"
)
public class HookBottomBtnMore extends BaseSwitchFunctionHookItem {
    private void hookTargetActivity() {
        try {
            Method method = XMethod.clz(CLAZZ_PANEL_ICON_LINEAR_LAYOUT).ret(ImageView.class).ignoreParam().get();
            XHook.hookAfter(method, param -> {
                ImageView imageView = (ImageView) param.getResult();
                if ("更多功能".contentEquals(imageView.getContentDescription())){
                    imageView.setOnLongClickListener(view -> {
                        runOnUiThread(() -> ElementSender.createView(null, view.getContext(), ""));
                        return true;
                    });
                }
            });
        } catch (NoSuchMethodException e) {
            Logger.e(e, true);
        }
    }


    @Override
    public void load(@NonNull ClassLoader classLoader) {
        try {
            Class<?> clazz = loadClass(CLAZZ_ACTIVITY_SPLASH);
            XposedHelpers.findAndHookMethod(clazz, "doOnCreate", Bundle.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            hookTargetActivity();
                        }
                    });
        } catch (ClassNotFoundException e) {
            Logger.e("Failed to hook target method: " + e, true);
        }
    }

    @Override
    public boolean isAlwaysRun() {
        return true;
    }

}