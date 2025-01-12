package moe.ono.util;

import moe.ono.constants.Constants;
import moe.ono.reflex.XClass;
import moe.ono.reflex.XMethod;

public class QAppUtils {
    public static long getServiceTime(){
        try {
            return XMethod.clz("com.tencent.mobileqq.msf.core.NetConnInfoCenter").name("getServerTimeMillis").ret(long.class).invoke();
        } catch (Exception e) {
            return 0;
        }
    }
    public static String UserUinToPeerID(String UserUin){
        try {
            Object convertHelper = XClass.newInstance(Initiator.loadClass("com.tencent.qqnt.kernel.api.impl.UixConvertAdapterApiImpl"));
            return XMethod.obj(convertHelper).name("getUidFromUin").ret(String.class).param(long.class).invoke(Long.parseLong(UserUin));
        }catch (Exception e){
            return "";
        }
    }
    public static boolean isQQnt(){
        try {
            return Initiator.load("com.tencent.qqnt.base.BaseActivity") != null;
        }catch (Exception e){
            return false;
        }

    }
    public static String getCurrentUin(){
        try {
            Object AppRuntime = getAppRuntime();
            return XMethod.obj(AppRuntime).name("getCurrentAccountUin").ret(String.class).invoke();
        } catch (Exception e) {
            return "";
        }
    }
    public static Object getAppRuntime() throws Exception {
            Object sApplication = XMethod.clz(Constants.CLAZZ_BASE_APPLICATION_IMPL).name("getApplication").ret(Initiator.load(Constants.CLAZZ_BASE_APPLICATION_IMPL)).invoke();
        return XMethod.obj(sApplication).name("getRuntime").ret(Initiator.loadClass("mqq.app.AppRuntime")).invoke();
    }
}
