# 保持模块自身的所有类和成员
-keep class moe.ono.** { *; }

# 保持 Kotlin 函数类及其所有成员
-keep class kotlin.jvm.functions.** { *; }

# 保持所有包含原生方法的类
-keepclasseswithmembernames class * {
    native <methods>;
}


# Xposed
-adaptresourcefilecontents META-INF/xposed/java_init.list
-keepattributes RuntimeVisibleAnnotations
-keep,allowobfuscation,allowoptimization public class * extends io.github.libxposed.api.XposedModule {
    public <init>(...);
    public void onPackageLoaded(...);
    public void onSystemServerLoaded(...);
}
-keep,allowoptimization,allowobfuscation @io.github.libxposed.api.annotations.* class * {
    @io.github.libxposed.api.annotations.BeforeInvocation <methods>;
    @io.github.libxposed.api.annotations.AfterInvocation <methods>;
}

# 保持必要的属性和注解
-keepattributes LineNumberTable,SourceFile,RuntimeVisibleAnnotations,AnnotationDefault

# 保持可序列化类的 Companion 对象字段
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

# 保持 Companion 对象的 serializer() 方法
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

# 保持 Serializable 对象的 INSTANCE 和 serializer() 方法
-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# 保持 ByteBuddy 相关的所有类和成员
-keep class net.bytebuddy.** { *; }

# 保持 Xposed API 相关的类
-keep class android.view.animation.PathInterpolator { *; }

# 忽略 Xposed 和 ByteBuddy 相关的警告
-dontwarn de.robv.android.xposed.**
-dontwarn io.github.libxposed.api.**

# Kotlin Intrinsics 方法没有副作用，可以安全移除
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static void check*(...);
    public static void throw*(...);
}

# Java Objects.requireNonNull 方法没有副作用，可以安全移除
-assumenosideeffects class java.util.Objects {
    public static ** requireNonNull(...);
}

# 移除 Android 日志方法
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
}

-keep class com.google.protobuf.**
# protobuf
-keepclassmembers public class * extends com.google.protobuf.MessageLite {*;}
-keepclassmembers public class * extends com.google.protobuf.MessageOrBuilder {*;}


-dontoptimize
-dontobfuscate