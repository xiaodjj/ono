package net.bytebuddy.android;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import dalvik.system.BaseDexClassLoader;
import moe.ono.hookimpl.InMemoryClassLoaderHelper;

import java.io.File;
import java.util.Objects;

public class InjectableDexClassLoader extends BaseDexClassLoader implements IAndroidInjectableClassLoader {

    /**
     * Constructs an instance. Note that all the *.jar and *.apk files from {@code dexPath} might be first extracted in-memory before the code is loaded. This
     * can be avoided by passing raw dex files (*.dex) in the {@code dexPath}.
     *
     * @param dexPath            the list of jar/apk files containing classes and resources, delimited by {@code File.pathSeparator}, which defaults to
     *                           {@code ":"} on Android.
     * @param optimizedDirectory this parameter is deprecated and has no effect since API level 26.
     * @param librarySearchPath  the list of directories containing native libraries, delimited by {@code File.pathSeparator}; may be {@code null}
     * @param parent             the parent class loader
     */
    public InjectableDexClassLoader(String dexPath, File optimizedDirectory, String librarySearchPath, ClassLoader parent) {
        super(dexPath, optimizedDirectory, librarySearchPath, parent == null ? System.class.getClassLoader() : parent);
    }

    public InjectableDexClassLoader(ClassLoader parent) {
        super("", null, null, parent);
    }

    @Override
    public void injectDex(@NonNull byte[] dexBytes, @Nullable String dexName) throws IllegalArgumentException {
        Objects.requireNonNull(dexBytes, "dexBytes");
        InMemoryClassLoaderHelper.INSTANCE.injectDexToClassLoader(this, dexBytes, dexName);
    }

}
