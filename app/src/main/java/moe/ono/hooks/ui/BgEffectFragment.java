package moe.ono.hooks.ui;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import moe.ono.R;
import moe.ono.ui.view.BgEffectPainter;

public class BgEffectFragment extends Fragment {
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBgEffectView = inflater.inflate(R.layout.layout_effect_bg, container, false);
        mBgEffectView.post(() -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mBgEffectPainter = new BgEffectPainter(requireContext());
                mBgEffectPainter.showRuntimeShader(requireContext(), mBgEffectView);
            }
            mHandler.post(runnableBgEffect);
        });
        return mBgEffectView;
    }
}
