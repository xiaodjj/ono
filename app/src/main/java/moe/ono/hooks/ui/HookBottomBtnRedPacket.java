package moe.ono.hooks.ui;

import static moe.ono.constants.Constants.CLAZZ_ACTIVITY_SPLASH;
import static moe.ono.constants.Constants.CLAZZ_PANEL_ICON_LINEAR_LAYOUT;
import static moe.ono.util.Initiator.loadClass;
import static moe.ono.util.SyncUtils.runOnUiThread;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.lxj.xpopup.XPopup;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import moe.ono.config.CacheConfig;
import moe.ono.creator.ElementSender;
import moe.ono.creator.FakeFileSender;
import moe.ono.hooks.XHook;
import moe.ono.hooks._base.BaseSwitchFunctionHookItem;
import moe.ono.hooks._core.annotation.HookItem;
import moe.ono.hooks.base.util.Toasts;
import moe.ono.reflex.XMethod;
import moe.ono.startup.HookBase;
import moe.ono.ui.CommonContextWrapper;
import moe.ono.util.Initiator;
import moe.ono.util.Logger;

@SuppressLint("DiscouragedApi")
@HookItem(path = "聊天与消息/快捷菜单", description = "长按红包按钮调出快捷菜单")
public class HookBottomBtnRedPacket extends BaseSwitchFunctionHookItem {
    private final List<String> classNames = Arrays.asList(
            "com.tencent.mobileqq.aio.msglist.holder.component.avatar.AIOAvatarContentComponent",
            "com.tencent.mobileqq.aio.msglist.holder.component.nick.block.MainNickNameBlock"
    );

    private static final ArrayList<Object> instanceForComponent = new ArrayList<>();
    private static final ArrayList<Object> instancesForMainNickNameBlock = new ArrayList<>();
    private static String componentMethodName;
    private static String componentMethodNameForMainNickNameBlock;
    private static final int MAX_SIZE = 15;

    private void hookTargetActivity(Activity activity) {
        try {
            Method method = XMethod.clz(CLAZZ_PANEL_ICON_LINEAR_LAYOUT).ret(ImageView.class).ignoreParam().get();

            XHook.hookAfter(method, param -> {
                ImageView imageView = (ImageView) param.getResult();
                if ("红包".contentEquals(imageView.getContentDescription())){
                    imageView.setOnLongClickListener(view -> {
                        Context fixContext = CommonContextWrapper.createAppCompatContext(imageView.getContext());
                        popMenu(fixContext, activity, view);
                        return true;
                    });
                }
            });
            processForInstance();
        } catch (NoSuchMethodException e) {
            Logger.e(e, true);
        }

    }

    private void popMenu(Context fixCtx, Activity activity, View view){
        new XPopup.Builder(fixCtx)
            .asCenterList("", new String[]{"PacketHelper", "匿名化", "假文件"},
                (position, text) -> {
                    switch (position) {
                        case 0:
                            runOnUiThread(() -> ElementSender.createView(null, view.getContext(), ""));
                            break;
                        case 1:
                            autoMosaicNameNT();
                            break;
                        case 2:
                            try {
                                runOnUiThread(() -> FakeFileSender.createView(view.getContext()));
                            } catch (Exception e) {
                                Toasts.error(view.getContext(), "请求失败");
                            }

                            break;
                        case 3:
                            break;
                    }
                })
            .show();
    }



    private void processForInstance(){
        // com.tencent.mobileqq.aio.msglist.holder.component.avatar.AIOAvatarContentComponent
        try {
            Class<?> clazz = Initiator.load(classNames.get(0));
            assert clazz != null;
            for (Method method : clazz.getDeclaredMethods()) {
                try {
                    if (method.getParameterCount() == 1 && method.getParameterTypes()[0] == Boolean.TYPE) {
                        componentMethodName = method.getName();

                        XHook.hookBefore(method, param -> {
                            if (instanceForComponent.size() >= MAX_SIZE) {
                                instanceForComponent.remove(0);
                            }
                            instanceForComponent.add(param.thisObject);
                            if (CacheConfig.isAutoMosaicNameNT()){
                                param.args[0] = true;
                            }
                        });
                    }
                } catch (ArrayIndexOutOfBoundsException ignored) {}
            }
        } catch (Exception e) {
            Logger.e(e);
        }

        // com.tencent.mobileqq.aio.msglist.holder.component.nick.block.MainNickNameBlock
        try {
            Class<?> clazz = Initiator.load(classNames.get(1));
            assert clazz != null;
            for (Method method : clazz.getDeclaredMethods()) {
                try {
                    if (method.getParameterCount() == 1 && method.getParameterTypes()[0] == Boolean.TYPE) {
                        componentMethodNameForMainNickNameBlock = method.getName();
                        XHook.hookBefore(method, param -> {
                            if (instancesForMainNickNameBlock.size() >= MAX_SIZE) {
                                instancesForMainNickNameBlock.remove(0);
                            }
                            instancesForMainNickNameBlock.add(param.thisObject);
                            if (CacheConfig.isAutoMosaicNameNT()){
                                param.args[0] = true;
                            }
                        });
                    }
                } catch (ArrayIndexOutOfBoundsException ignored) {}
            }
        } catch (Exception e) {
            Logger.e(e);
        }
    }




    private void autoMosaicNameNT() {
        CacheConfig.setAutoMosaicNameNT(!CacheConfig.isAutoMosaicNameNT());

        List<Object> instancesCopy = new ArrayList<>(instanceForComponent);

        for (Object instance : instancesCopy) {
            try {
                Method matchingMethod = instance.getClass().getDeclaredMethod(componentMethodName, boolean.class);
                matchingMethod.setAccessible(true);
                matchingMethod.invoke(instance, CacheConfig.isAutoMosaicNameNT());
            } catch (Exception e) {
                Logger.e("Error invoking method on instance: " + e.getMessage());
            }
        }

        List<Object> instancesCopyForMainNickNameBlock = new ArrayList<>(instancesForMainNickNameBlock);

        for (Object instance : instancesCopyForMainNickNameBlock) {
            try {
                Method matchingMethod = instance.getClass().getDeclaredMethod(componentMethodNameForMainNickNameBlock, boolean.class);
                matchingMethod.setAccessible(true);
                matchingMethod.invoke(instance, CacheConfig.isAutoMosaicNameNT());
            } catch (Exception e) {
                Logger.e("Error invoking method on instance: " + e.getMessage());
            }
        }
    }

    @Override
    public void load(@NonNull ClassLoader classLoader) throws Throwable {
        try {
            Class<?> clazz = loadClass(CLAZZ_ACTIVITY_SPLASH);
            XposedHelpers.findAndHookMethod(clazz, "doOnCreate", Bundle.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            hookTargetActivity((Activity) param.thisObject);
                        }
                    });
        } catch (ClassNotFoundException e) {
            Logger.e("Failed to hook target method: " + e, true);
        }
    }
}