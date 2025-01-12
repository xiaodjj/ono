package moe.ono.hooks.ui;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import de.robv.android.xposed.XC_MethodHook;
import moe.ono.startup.HookBase;
import moe.ono.util.Logger;

@SuppressLint("DiscouragedApi")
public class TelegramAvatarMode implements HookBase {
    public static String method_name = "Telegram模式";
    public static String method_description = "隐藏自己的头像";

    public void hideSelfAvatar(ClassLoader classLoader) {
        findAndHookMethod("com.tencent.mobileqq.aio.msglist.holder.component.avatar.AIOAvatarContentComponent$avatarContainer$2",
                classLoader, "invoke", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                RelativeLayout relativeLayout = (RelativeLayout) param.getResult();
                ImageView view = (ImageView) relativeLayout.getChildAt(0);

                DisplayMetrics displayMetrics = new DisplayMetrics();
                WindowManager windowManager = (WindowManager) view.getContext().getSystemService(Context.WINDOW_SERVICE);
                windowManager.getDefaultDisplay().getMetrics(displayMetrics);

                view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        int screenWidth = displayMetrics.widthPixels;
                        int[] location = new int[2];
                        view.getLocationOnScreen(location);
                        int absoluteX = location[0];

                        if (absoluteX < screenWidth / 2) {/* nothing to do */} else {
                            relativeLayout.setVisibility(View.GONE);
                        }

                        view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });

                relativeLayout.post(() -> {
                    int screenWidth = displayMetrics.widthPixels;
                    int[] location = new int[2];
                    view.getLocationOnScreen(location);
                    int absoluteX = location[0];

                    if (absoluteX < screenWidth / 2) {
                        Logger.d("ImageView is on the left side");
                    } else {
                        Logger.d("ImageView is on the right side");
                        relativeLayout.setVisibility(View.GONE);
                    }
                });
            }
        });

    }


    @Override
    public void init(@NonNull ClassLoader cl, @NonNull ApplicationInfo ai) {
        hideSelfAvatar(cl);
    }

    @Override
    public String getName() {
        return method_name;
    }

    @Override
    public String getDescription() {
        return method_description;
    }

    @Override
    public Boolean isEnable() {
        return null;
    }
}