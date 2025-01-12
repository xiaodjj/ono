package moe.ono.util.consis;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import moe.ono.ui.WindowIsTranslucent;
import mqq.app.AppActivity;

public class ShadowStartupAgentActivity extends AppActivity implements WindowIsTranslucent {

    @SuppressLint("ObsoleteSdkInt")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(android.R.style.Theme_Translucent_NoTitleBar);
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.flags &= ~WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        params.flags |= WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        window.setAttributes(params);
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        window.setElevation(0);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setBackgroundDrawable(null);
        View decorView = window.getDecorView();
        int option = decorView.getSystemUiVisibility()
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(option);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected boolean doOnCreate(Bundle savedInstanceState) {
        super.doOnCreate(savedInstanceState);
        StartupDirectorBridge.getInstance().notifyStartupFinished();
        finish();
        return false;
    }
}
