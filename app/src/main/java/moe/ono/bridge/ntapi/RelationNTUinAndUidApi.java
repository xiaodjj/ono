package moe.ono.bridge.ntapi;

import com.tencent.mobileqq.qroute.QRoute;
import com.tencent.mobileqq.qroute.QRouteApi;

import java.lang.reflect.Method;

import moe.ono.util.Initiator;

public class RelationNTUinAndUidApi {

    private RelationNTUinAndUidApi() {
    }

    private static Object sImpl = null;
    private static Method sGetUidFromUin = null;
    private static Method sGetUinFromUid = null;

    private static Object getRelationNTUinAndUidApiImpl() throws ReflectiveOperationException, LinkageError {
        if (sImpl == null) {
            Class<? extends QRouteApi> klass = (Class<? extends QRouteApi>) Initiator.loadClass("com.tencent.relation.common.api.IRelationNTUinAndUidApi");
            sGetUidFromUin = klass.getMethod("getUidFromUin", String.class);
            sGetUinFromUid = klass.getMethod("getUinFromUid", String.class);
            sImpl = QRoute.api(klass);
        }
        return sImpl;
    }

    public static boolean isAvailable() {
        try {
            getRelationNTUinAndUidApiImpl();
            return true;
        } catch (ReflectiveOperationException | LinkageError e) {
            return false;
        }
    }

    public static String getUidFromUin(String str) {
        try {
            Object impl = getRelationNTUinAndUidApiImpl();
            return (String) sGetUidFromUin.invoke(impl, str);
        } catch (ReflectiveOperationException | LinkageError e) {
            throw new RuntimeException("RelationNTUinAndUidApi not available", e);
        }
    }


    public static String getUinFromUid(String str) {
        try {
            Object impl = getRelationNTUinAndUidApiImpl();
            return (String) sGetUinFromUid.invoke(impl, str);
        } catch (ReflectiveOperationException | LinkageError e) {
            throw new RuntimeException("RelationNTUinAndUidApi not available", e);
        }
    }

}
