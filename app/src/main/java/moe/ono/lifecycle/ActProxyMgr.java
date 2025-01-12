package moe.ono.lifecycle;

import androidx.annotation.NonNull;

import org.intellij.lang.annotations.MagicConstant;

/**
 * This class is used to cope with Activity
 */
public class ActProxyMgr {

    public static final String STUB_DEFAULT_ACTIVITY = "com.tencent.mobileqq.activity.photo.CameraPreviewActivity";
    public static final String STUB_TRANSLUCENT_ACTIVITY = "cooperation.qlink.QlinkStandardDialogActivity";
    public static final String STUB_TOOL_ACTIVITY = "cooperation.qqindividuality.QQIndividualityProxyActivity";
    @MagicConstant
    public static final String ACTIVITY_PROXY_INTENT = "moe.ono.lifecycle.ActProxyMgr.ACTIVITY_PROXY_INTENT";

    private ActProxyMgr() {
        throw new AssertionError("No instance for you!");
    }

    // NOTICE: ** If you have created your own package, add it to proguard-rules.pro.**

    public static boolean isModuleProxyActivity(@NonNull String className) {
        return className.startsWith("moe.ono.activity.")
            || "moe.ono.util.consis.ShadowStartupAgentActivity".equals(className);
    }

    public static boolean isModuleBundleClassLoaderRequired(@NonNull String className) {
        return isModuleProxyActivity(className);
    }
}
