package moe.ono.startup;

import android.content.pm.ApplicationInfo;

import androidx.annotation.NonNull;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public interface HookBase {
    void init(@NonNull ClassLoader cl, @NonNull ApplicationInfo ai);
    String getName();
    String getDescription();
    Boolean isEnable();
}
