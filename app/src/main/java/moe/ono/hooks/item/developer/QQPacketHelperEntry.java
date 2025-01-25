package moe.ono.hooks.item.developer;

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
import moe.ono.reflex.XMethod;
import moe.ono.util.Logger;

@SuppressLint("DiscouragedApi")
@HookItem(
        path = "开发者选项/QQPacketHelper",
        description = "开启后需在聊天界面长按加号呼出，或长按发送按钮呼出"
)
public class QQPacketHelperEntry extends BaseSwitchFunctionHookItem {
    private void hook() {
        try {
            Method method = XMethod.clz(CLAZZ_PANEL_ICON_LINEAR_LAYOUT).ret(ImageView.class).ignoreParam().get();
            hookAfter(method, param -> {
                ImageView imageView = (ImageView) param.getResult();
                if ("更多功能".contentEquals(imageView.getContentDescription())){
                    imageView.setOnLongClickListener(view -> {
                        runOnUiThread(() -> ElementSender.createView(null, view.getContext(), ""));
                        return true;
                    });
                }
            });
        } catch (NoSuchMethodException e) {
            Logger.e(this.getItemName(), e);
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
                            hook();
                        }
                    });
        } catch (ClassNotFoundException e) {
            Logger.e("QQPacketHelper", e);
        }
    }

}