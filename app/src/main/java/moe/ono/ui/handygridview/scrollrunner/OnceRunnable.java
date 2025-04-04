package moe.ono.ui.handygridview.scrollrunner;

import android.view.View;

public abstract class OnceRunnable implements Runnable {
    private boolean mScheduled;

    public final void run() {
        onRun();
        mScheduled = false;
    }

    public abstract void onRun();

    public void postSelf(View carrier) {
        postDelaySelf(carrier, 0);
    }

    public void postDelaySelf(View carrier, int delay) {
        if (!mScheduled) {
            carrier.postDelayed(this, delay);
            mScheduled = true;
        }
    }

    public void removeSelf(View carrier) {
        mScheduled = false;
        carrier.removeCallbacks(this);
    }
}
