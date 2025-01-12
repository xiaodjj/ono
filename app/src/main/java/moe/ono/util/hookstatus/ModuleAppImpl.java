package moe.ono.util.hookstatus;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import moe.ono.BuildConfig;
import moe.ono.HostInfo;
import moe.ono.loader.hookapi.IClassLoaderHelper;
import moe.ono.loader.hookapi.ILoaderService;
import moe.ono.startup.StartupInfo;
import moe.ono.util.Logger;

public class ModuleAppImpl extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        StartupInfo.setInHostProcess(false);
        // init host info, this is required for nearly all operations
        HostInfo.init(this);
        initStartupInfo();

        Logger.d("ModuleAppImpl onCreate");
    }

    private void initStartupInfo() {
        final String apkPath = getApplicationInfo().sourceDir;
        ILoaderService loaderService = new ILoaderService() {

            // not used, just for compatibility
            private IClassLoaderHelper mClassLoaderHelper;

            @NonNull
            @Override
            public String getEntryPointName() {
                return "ActivityThread";
            }

            @NonNull
            @Override
            public String getLoaderVersionName() {
                return BuildConfig.VERSION_NAME;
            }

            @Override
            public int getLoaderVersionCode() {
                return BuildConfig.VERSION_CODE;
            }

            @NonNull
            @Override
            public String getMainModulePath() {
                return apkPath;
            }

            @Override
            public void log(@NonNull String msg) {
                android.util.Log.i(BuildConfig.TAG, msg);
            }

            @Override
            public void log(@NonNull Throwable tr) {
                android.util.Log.e("ovom", tr.toString(), tr);
            }

            @Nullable
            @Override
            public Object queryExtension(@NonNull String key, @Nullable Object... args) {
                return null;
            }

            @Override
            public void setClassLoaderHelper(@Nullable IClassLoaderHelper helper) {
                mClassLoaderHelper = helper;
            }

            @Nullable
            @Override
            public IClassLoaderHelper getClassLoaderHelper() {
                return mClassLoaderHelper;
            }
        };
        StartupInfo.setModulePath(apkPath);
        StartupInfo.setLoaderService(loaderService);
    }

}
