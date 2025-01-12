package moe.ono.fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import moe.ono.activity.OUOSettingActivity;


public abstract class BaseSettingFragment extends Fragment {

    private OUOSettingActivity mSettingsHostActivity = null;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mSettingsHostActivity = (OUOSettingActivity) requireActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mSettingsHostActivity = null;
    }

    @Nullable
    protected OUOSettingActivity getSettingsHostActivity() {
        return mSettingsHostActivity;
    }

    public void notifyLayoutPaddingsChanged() {
        onLayoutPaddingsChanged();
    }


    /**
     * @deprecated use {@link #doOnCreateView(LayoutInflater, ViewGroup, Bundle)} instead
     */
    @Nullable
    @Deprecated
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return doOnCreateView(inflater, container, savedInstanceState);
    }

    @Nullable
    public View doOnCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                               @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    protected void onLayoutPaddingsChanged() {
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        onLayoutPaddingsChanged();
    }

}
