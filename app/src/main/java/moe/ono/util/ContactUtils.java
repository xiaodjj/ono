package moe.ono.util;

import static moe.ono.constants.Constants.MethodCacheKey_getBuddyName;
import static moe.ono.constants.Constants.MethodCacheKey_getDiscussionMemberShowName;
import static moe.ono.dexkit.TargetManager.getMethod;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tencent.common.app.AppInterface;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

import de.robv.android.xposed.XposedHelpers;
import moe.ono.bridge.ManagerHelper;
import moe.ono.bridge.ntapi.RelationNTUinAndUidApi;
import moe.ono.reflex.Reflex;
import mqq.app.AppRuntime;

public class ContactUtils {

    private ContactUtils() {
    }

    private static final String UNICODE_RLO = "\u202E";

    @NonNull
        public static String getTroopMemberNick(long troopUin, long memberUin) {
        return getTroopMemberNick(String.valueOf(troopUin), String.valueOf(memberUin));
    }

    @NonNull
    public static String getTroopMemberNick(@NonNull String troopUin, @NonNull String memberUin) {
        Objects.requireNonNull(troopUin);
        Objects.requireNonNull(memberUin);
        AppRuntime app = AppRuntimeHelper.getQQAppInterface();
        assert app != null;
        if (!HostInfo.requireMinQQVersion(QQVersion.QQ_9_0_25)) {
            try {
                Object mTroopManager = ManagerHelper.getTroopManager();
                Object troopMemberInfo = Reflex.invokeVirtualDeclaredOrdinal(mTroopManager, 0, 3, false,
                        troopUin, memberUin,
                        String.class, String.class,
                        Initiator._TroopMemberInfo());
                if (troopMemberInfo != null) {
                    String troopnick = (String) XposedHelpers.getObjectField(troopMemberInfo, "troopnick");
                    if (troopnick != null) {
                        String ret = troopnick.replace(UNICODE_RLO, "");
                        if (!ret.trim().isEmpty()) {
                            return ret;
                        }
                    }
                }
            } catch (Exception | LinkageError e) {
                Logger.e(e);
            }
        }
        try {
            String ret = getDiscussionMemberShowName(app, troopUin, memberUin);
            if (ret != null) {
                ret = ret.replace(UNICODE_RLO, "");
                if (!ret.trim().isEmpty()) {
                    return ret;
                }
            }
        } catch (Exception | LinkageError e) {
            Logger.e(e);
        }
        try {
            String ret;
            String nickname = getBuddyName(app, memberUin);
            if (nickname != null && !(ret = nickname.replace(UNICODE_RLO, "")).trim().isEmpty()) {
                return ret;
            }
        } catch (Exception | LinkageError e) {
            Logger.e(e);
        }
        //**sigh**
        return memberUin;
    }

    public static String getDiscussionMemberShowName(@NonNull AppRuntime app, @NonNull String troopUin, @NonNull String memberUin) {
        Objects.requireNonNull(app, "app is null");
        Objects.requireNonNull(troopUin, "troopUin is null");
        Objects.requireNonNull(memberUin, "memberUin is null");
        Method getDiscussionMemberShowName = getMethod(MethodCacheKey_getDiscussionMemberShowName);
        if (getDiscussionMemberShowName == null) {
            Logger.e("getDiscussionMemberShowName but N_ContactUtils_getDiscussionMemberShowName not found");
            return null;
        }
        try {
            return (String) getDiscussionMemberShowName.invoke(null, app, troopUin, memberUin);
        } catch (IllegalAccessException e) {
            // should not happen
            Logger.e(e);
            return null;
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            Logger.e(Objects.requireNonNullElse(cause, e));
            return null;
        }
    }

    @Nullable
    public static String getBuddyName(@NonNull AppRuntime app, @NonNull String uin) {
        Objects.requireNonNull(app, "app is null");
        Objects.requireNonNull(uin, "uin is null");
        Method getBuddyName = getMethod(MethodCacheKey_getBuddyName);
        if (getBuddyName == null) {
            Logger.w("getBuddyName but N_ContactUtils_getBuddyName not found");
            return null;
        }
        try {
            return (String) getBuddyName.invoke(null, app, uin, false);
        } catch (IllegalAccessException e) {
            // should not happen
            Logger.e(e);
            return null;
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            Logger.e(Objects.requireNonNullElse(cause, e));
            return null;
        }
    }

    @NonNull
    public static String getTroopName(@NonNull String troopUin) {
        if (TextUtils.isEmpty(troopUin)) {
            return "";
        }
        try {
            return (String) Reflex.invokeStatic(Initiator.loadClass("com.tencent.mobileqq.utils.ContactUtils"), "a",
                    AppRuntimeHelper.getQQAppInterface(), troopUin, true,
                    AppInterface.class, String.class, boolean.class, String.class);
        } catch (ReflectiveOperationException e) {
            Logger.e(e);
            return troopUin;
        }
    }

    @NonNull
    public static String getDisplayNameForUid(@NonNull String peerUid) {
        return getDisplayNameForUid(peerUid, 0);
    }

    @NonNull
    public static String getDisplayNameForUid(@NonNull String peerUid, long groupNumber) {
        try {
            String uin = RelationNTUinAndUidApi.getUinFromUid(peerUid);
            if (TextUtils.isEmpty(uin)) {
                return peerUid;
            }
            return getDisplayNameForUin(uin, groupNumber);
        } catch (RuntimeException e) {
            return peerUid;
        }
    }

    @NonNull
    public static String getDisplayNameForUid(@NonNull String peerUid, @Nullable String groupNumber) {
        if (TextUtils.isEmpty(groupNumber)) {
            return getDisplayNameForUid(peerUid);
        }
        return getDisplayNameForUid(peerUid, Long.parseLong(groupNumber));
    }

    @NonNull
    public static String getDisplayNameForUin(@NonNull String uin) {
        return getDisplayNameForUin(uin, 0);
    }

    @NonNull
    public static String getDisplayNameForUin(@NonNull String uin, long groupNumber) {
        if (groupNumber > 0) {
            return getTroopMemberNick(String.valueOf(groupNumber), uin);
        }
        String ret = getBuddyName(Objects.requireNonNull(AppRuntimeHelper.getQQAppInterface()), uin);
        if (ret != null) {
            return ret;
        }
        return uin;
    }
}
