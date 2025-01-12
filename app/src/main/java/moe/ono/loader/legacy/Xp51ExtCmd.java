package moe.ono.loader.legacy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import de.robv.android.xposed.XposedBridge;

import moe.ono.common.CheckUtils;
import moe.ono.common.ModuleLoader;

public class Xp51ExtCmd {

    private Xp51ExtCmd() {
    }

    public static Object handleQueryExtension(@NonNull String cmd, @Nullable Object[] arg) {
        CheckUtils.checkNonNull(cmd, "cmd");
        switch (cmd) {
            case "GetXposedBridgeClass":
                return XposedBridge.class;
            case "GetLoadPackageParam":
                return LegacyHookEntry.getLoadPackageParam();
            case "GetInitZygoteStartupParam":
                return LegacyHookEntry.getInitZygoteStartupParam();
            case "GetInitErrors":
                return ModuleLoader.getInitErrors();
            default:
                return null;
        }
    }

}
