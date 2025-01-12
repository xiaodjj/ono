package moe.ono.hooks.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.List;

import moe.ono.HostInfo;
import moe.ono.R;
import moe.ono.startup.HookBase;
import moe.ono.startup.StartupInfo;
import moe.ono.ui.view.BgEffectPainter;
import moe.ono.util.Logger;

@SuppressLint("DiscouragedApi")
public class HyperBackground implements HookBase {
    public static String method_name = "Hyper-Background";
    public static String method_description = "花里胡哨";

    private View mBgEffectView;
    private BgEffectPainter mBgEffectPainter;
    private final float startTime = (float) System.nanoTime();
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    Runnable runnableBgEffect = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
        @Override
        public void run() {
            mBgEffectPainter.setAnimTime(((((float) System.nanoTime()) - startTime) / 1.0E9f) % 62.831852f);
            mBgEffectPainter.setResolution(new float[]{mBgEffectView.getWidth(), mBgEffectView.getHeight()});
            mBgEffectPainter.updateMaterials();
            mBgEffectView.setRenderEffect(mBgEffectPainter.getRenderEffect());
            mHandler.postDelayed(runnableBgEffect, 16L);
        }
    };



    @Override
    public void init(@NonNull ClassLoader cl, @NonNull ApplicationInfo ai) {
        setHyperBackground();
    }

    private void setHyperBackground() {
        Activity activity = StartupInfo.getSplashActivity();


        // 获取所有 ViewGroup
        List<ViewGroup> allViewGroups = getAllViewGroups(activity);
        Logger.i("Total ViewGroups: " + allViewGroups.size());

        for (int i = 0; i < allViewGroups.size(); i++) {
            Logger.i("ViewGroup[" + i + "]: " + allViewGroups.get(i));
        }

        // 查找目标 ViewGroup（倒数第二个全屏 ViewGroup）
        ViewGroup targetViewGroup = findSecondLastFullscreenView(activity);


        if (targetViewGroup != null) {
            Logger.i("Set hyper background using Fragment!");
            mBgEffectView = LayoutInflater.from(HostInfo.getHostInfo().getApplication()).inflate(R.layout.layout_effect_bg, targetViewGroup, false);
            targetViewGroup.addView(mBgEffectView, 0);
            mBgEffectView = targetViewGroup.findViewById(R.id.bgEffectView);


            mBgEffectView.post(() -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    mBgEffectPainter = new BgEffectPainter(HostInfo.getHostInfo().getApplication());
                    mBgEffectPainter.showRuntimeShader(HostInfo.getHostInfo().getApplication(), mBgEffectView);
                }

                mHandler.post(runnableBgEffect);
            });
        } else {
            Logger.i("No suitable ViewGroup found for setting hyper background.");
        }
    }

    private ViewGroup findSecondLastFullscreenView(Activity activity) {
        ViewGroup contentView = activity.getWindow().getDecorView().findViewById(android.R.id.content);

        // 获取 contentView 的宽高
        int contentWidth = contentView.getMeasuredWidth();
        int contentHeight = contentView.getMeasuredHeight();

        if (contentWidth == 0 || contentHeight == 0) {
            contentView.post(() -> findSecondLastFullscreenView(activity));
            return contentView;
        }

        // 存储符合条件的全屏视图
        List<ViewGroup> fullscreenViews = new ArrayList<>();
        collectFullscreenViews(contentView, contentWidth, contentHeight, fullscreenViews);

        if (fullscreenViews.size() > 1) {
            // 倒数第二个视图
            ViewGroup secondLastView = fullscreenViews.get(fullscreenViews.size() - 3);
            Logger.i("Found second last fullscreen view: " + secondLastView);
            return secondLastView;
        } else {
            Logger.i("Not enough fullscreen views to determine the second last one.");
        }
        return null;
    }


    private void collectFullscreenViews(ViewGroup parent, int contentWidth, int contentHeight, List<ViewGroup> fullscreenViews) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);

            // 如果子视图是 ViewGroup 且为全屏，添加到列表
            if (child instanceof ViewGroup && isFullscreenView(child, contentWidth, contentHeight)) {
                fullscreenViews.add((ViewGroup) child);
            }

            // 继续递归检查子 View
            if (child instanceof ViewGroup) {
                collectFullscreenViews((ViewGroup) child, contentWidth, contentHeight, fullscreenViews);
            }
        }
    }

    private List<ViewGroup> getAllViewGroups(Activity activity) {
        ViewGroup rootView = activity.getWindow().getDecorView().findViewById(android.R.id.content);
        List<ViewGroup> viewGroups = new ArrayList<>();

        // 调用递归方法收集所有 ViewGroup
        collectAllViewGroups(rootView, viewGroups);

        return viewGroups;
    }

    private void collectAllViewGroups(ViewGroup parent, List<ViewGroup> viewGroups) {
        // 添加当前 ViewGroup 到列表
        viewGroups.add(parent);

        // 遍历子视图
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);

            // 如果子视图是 ViewGroup，递归调用
            if (child instanceof ViewGroup) {
                collectAllViewGroups((ViewGroup) child, viewGroups);
            }
        }
    }

    private boolean isFullscreenView(View view, int contentWidth, int contentHeight) {
        // 检查视图的宽高是否与 contentView 相同
        return view.getMeasuredWidth() == contentWidth &&
                view.getMeasuredHeight() == contentHeight;
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