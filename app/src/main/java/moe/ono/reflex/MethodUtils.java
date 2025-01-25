package moe.ono.reflex;

import static moe.ono.util.Initiator.loadClass;

import android.util.Log;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import moe.ono.dexkit.DexMethodDescriptor;
import moe.ono.reflex.base.BaseFinder;
import moe.ono.startup.StartupInfo;
import moe.ono.util.CheckClassType;

public class MethodUtils extends BaseFinder<Method> {

    private String methodName;
    private Class<?> returnType;
    private Class<?>[] methodParams;
    private Integer paramCount;

    /**
     * 通过方法签名获取方法对象
     */
    public static Method getMethodByDesc(String desc) throws NoSuchMethodException {
        Method method = new DexMethodDescriptor(desc).getMethodInstance(StartupInfo.getHostApp().getClassLoader());
        method.setAccessible(true);
        return method;
    }

    public static Method getMethodByDesc(String desc, ClassLoader classLoader) throws NoSuchMethodException {
        Method method = new DexMethodDescriptor(desc).getMethodInstance(classLoader);
        method.setAccessible(true);
        return method;
    }

    public static MethodUtils create(Object target) {
        return create(target.getClass());
    }

    public static MethodUtils create(Class<?> fromClass) {
        MethodUtils methodUtils = new MethodUtils();
        methodUtils.setDeclaringClass(fromClass);
        return methodUtils;
    }

    public static MethodUtils create(String formClassName) {
        return create(ClassUtils.findClass(formClassName));
    }

    public MethodUtils returnType(Class<?> returnType) {
        this.returnType = returnType;
        return this;
    }

    public MethodUtils methodName(String methodName) {
        this.methodName = methodName;
        return this;
    }

    public MethodUtils params(Class<?>... methodParams) {
        this.methodParams = methodParams;
        this.paramCount = methodParams.length;
        return this;
    }

    public MethodUtils paramCount(int paramCount) {
        this.paramCount = paramCount;
        return this;
    }

    @Override
    public BaseFinder<Method> find() {
        List<Method> cache = findMethodCache();
        if (cache != null && !cache.isEmpty()) {
            result = cache;
            return this;
        }
        Method[] methods = getDeclaringClass().getDeclaredMethods();
        result.addAll(Arrays.asList(methods));
        result.removeIf(method -> methodName != null && !method.getName().equals(methodName));
        result.removeIf(method -> returnType != null && !CheckClassType.checkType(method.getReturnType(), returnType));
        result.removeIf(method -> paramCount != null && method.getParameterCount() != paramCount);
        result.removeIf(method -> methodParams != null && !paramEquals(method.getParameterTypes()));
        writeToMethodCache(result);
        return this;
    }

    private boolean paramEquals(Class<?>[] methodParams) {
        for (int i = 0; i < methodParams.length; i++) {
            Class<?> type = methodParams[i];
            Class<?> findType = this.methodParams[i];
            if (findType == Ignore.class || CheckClassType.checkType(type, findType)) {
                continue;
            }
            return false;
        }
        return true;
    }

    @Override
    public String buildSign() {
        String build = "method:" +
                fromClassName +
                " " +
                returnType +
                " " +
                methodName +
                "(" +
                paramCount +
                Arrays.toString(methodParams) +
                ")";
        return build;
    }

    private <T> T tryCall(Method method, Object object, Object... args) {
        try {
            Log.d("MethodTool", "tryCall: " + method + " obj=" + object + " args " + Arrays.toString(args));
            return (T) method.invoke(object, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T callFirstStatic(Object... args) {
        Method method = first();
        return tryCall(method, null, args);
    }

    public <T> T callFirst(Object runTimeObj, Object... args) {
        Method method = first();
        return tryCall(method, runTimeObj, args);
    }

    public <T> T callLast(Object object, Object... args) {
        Method method = last();
        return tryCall(method, object, args);
    }

    private static final HashMap<String, Method> MethodCache = new HashMap<>();

    public static Method findMethod(String clazz, String methodName, Class<?> returnType, Class<?>[] paramTypes) throws ClassNotFoundException {
        return findMethod(loadClass(clazz), methodName, returnType, paramTypes);
    }

    public static Method findMethod(Class<?> clazz, String methodName, Class<?> returnType, Class<?>[] paramTypes) {
        if (clazz == null) return null;
        StringBuilder builder = new StringBuilder();
        builder.append(clazz.getName()).append(".").append(methodName).append("(");
        for (Class<?> clz : paramTypes) builder.append(clz.getName()).append(";");
        builder.append(")").append(returnType.getName());
        String SignText = builder.toString();
        if (MethodCache.containsKey(SignText)) return MethodCache.get(SignText);

        Class<?> Current_Find = clazz;
        while (Current_Find != null) {
            Loop:
            for (Method method : Current_Find.getDeclaredMethods()) {
                if ((method.getName().equals(methodName) || methodName == null) && method.getReturnType().equals(returnType)) {
                    Class<?>[] params = method.getParameterTypes();
                    if (params.length == paramTypes.length) {
                        for (int i = 0; i < params.length; i++) {
                            if (!Objects.equals(params[i], paramTypes[i])) continue Loop;
                        }
                        MethodCache.put(SignText, method);
                        method.setAccessible(true);
                        return method;
                    }

                }
            }
            Current_Find = Current_Find.getSuperclass();
        }


        return null;
    }
}
