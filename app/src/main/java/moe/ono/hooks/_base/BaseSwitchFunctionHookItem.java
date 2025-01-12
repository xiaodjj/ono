package moe.ono.hooks._base;

import android.view.View;

import de.robv.android.xposed.XC_MethodHook;
import moe.ono.util.SyncUtils;

public abstract class BaseSwitchFunctionHookItem extends BaseHookItem {

    private boolean enabled = isAlwaysRun();
    private final int targetProcess = targetProcess();

    public String getTip() {
        return null;
    }

    public View.OnClickListener getOnClickListener() {
        return null;
    }

    /**
     * 是否默认加载
     */
    public boolean isAlwaysRun() {
        return true;
    }

    /**
     * 目标进程
     */
    public int targetProcess() {
        return SyncUtils.PROC_MAIN;
    }


    public int getTargetProcess() {
        return targetProcess;
    }


    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    protected final void tryExecute(XC_MethodHook.MethodHookParam param, HookAction hookAction) {
        //只有在开启了的情况下才执行
        if (isEnabled()) {
            super.tryExecute(param, hookAction);
        }
    }

}
