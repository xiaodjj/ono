package net.bytebuddy.android;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface IAndroidInjectableClassLoader {

    void injectDex(@NonNull byte[] dexBytes, @Nullable String dexName) throws IllegalArgumentException;

}
