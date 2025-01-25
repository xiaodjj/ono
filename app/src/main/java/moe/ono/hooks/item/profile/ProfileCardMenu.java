package moe.ono.hooks.item.profile;

import static de.robv.android.xposed.XposedHelpers.findMethodExact;
import static moe.ono.util.Initiator.loadClass;
import static moe.ono.util.SyncUtils.postDelayed;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;

import java.lang.reflect.Method;

import moe.ono.hooks._base.BaseSwitchFunctionHookItem;
import moe.ono.hooks._core.annotation.HookItem;
import moe.ono.hooks.base.util.Toasts;
import moe.ono.ui.CommonContextWrapper;
import moe.ono.util.Logger;
import moe.ono.util.Utils;


@SuppressLint("DiscouragedApi")
@HookItem(path = "资料卡/我要更多的信息", description = "长按别人资料卡主页上的设置按钮呼出菜单")
public class ProfileCardMenu extends BaseSwitchFunctionHookItem {
    private static String QQ;
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    private void hookTargetActivity(Activity activity) {
        // TODO: 更改获取QQ号的方式
        postDelayed(() -> new Thread(() -> {
            String qqNumber = null;
            try {
                TextView tv = activity.findViewById(activity.getResources().getIdentifier("gmx", "id", activity.getPackageName()));
                String text = tv.getText().toString();
                qqNumber = text.replace("QQ号：", "").replaceAll("[^0-9]", "");
            } catch (Exception e) {
                try {
                    TextView tv = activity.findViewById(activity.getResources().getIdentifier("info", "id", activity.getPackageName()));
                    String text = tv.getText().toString();
                    qqNumber = text.replace("QQ号：", "").replaceAll("[^0-9]", "");
                } catch (Exception ex) {
                    Logger.e("无法获取QQ号: ", e);
                }
            }

            if (qqNumber != null) {
                QQ = qqNumber;
            }

            View setting = null;
            try {
                setting = Utils.getViewByDesc(activity, "设置", 200);
            } catch (InterruptedException e) {
                Logger.e("查找设置按钮时出错: ", e);
            }

            if (setting != null) {
                View finalSetting = setting;
                mainHandler.post(() -> {
                    try {
                        finalSetting.setOnLongClickListener(new onSettingLongClickListener());
                    } catch (Exception ignored) {}
                });
            } else {
                Logger.e("设置按钮未找到");
            }
        }).start(), 100);
    }

    @Override
    public void load(@NonNull ClassLoader classLoader) throws Throwable {
        try {
            Class<?> clazz = loadClass("com.tencent.mobileqq.profilecard.activity.FriendProfileCardActivity");
            Method m = findMethodExact(clazz, "doOnCreate", Bundle.class);
            hookAfter(m, param -> hookTargetActivity((Activity) param.thisObject));
        } catch (ClassNotFoundException e) {
            Logger.e(this.getItemName(), e);
        }
    }


    private static class onSettingLongClickListener implements View.OnLongClickListener {
        @Override
        public boolean onLongClick(View v) {
            Context fixContext = CommonContextWrapper.createAppCompatContext(v.getContext());
            new XPopup.Builder(fixContext)
                    .hasShadowBg(false)
                    .atView(v)
                    .asAttachList(new String[]{"跳转小世界", "查看等级"},
                        new int[]{},
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                switch (position) {
                                    case 0:
                                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("mqqapi://qcircle/openmainpage?uin=" + QQ));
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        v.getContext().startActivity(intent);
                                        break;
                                    case 1:
                                        try {
                                            Utils.jump(v, this.hashCode(), "https://club.vip.qq.com/card/friend?qq=" + QQ);
                                        } catch (Exception e) {
                                            Toasts.error(v.getContext(), "无法打开内置浏览器");
                                        }
                                        break;
                                }
                            }
                        })
                    .show();
            return true;
        }
    }

}