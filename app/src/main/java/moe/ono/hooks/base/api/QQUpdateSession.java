package moe.ono.hooks.base.api;

import static moe.ono.constants.Constants.MethodCacheKey_AIOParam;
import static moe.ono.hooks._core.factory.HookItemFactory.getItem;

import android.os.Bundle;

import androidx.annotation.NonNull;

import moe.ono.dexkit.TargetManager;
import moe.ono.hooks._base.ApiHookItem;
import moe.ono.hooks._core.annotation.HookItem;
import moe.ono.hooks.item.chat.QQBubbleRedirect;
import moe.ono.reflex.XField;
import moe.ono.util.Initiator;
import moe.ono.util.Session;

@HookItem(path = "API/获取Session")
public class QQUpdateSession extends ApiHookItem {

    private void update(ClassLoader classLoader) {
        hookBefore(TargetManager.requireMethod(MethodCacheKey_AIOParam), param -> {
            Bundle bundle = (Bundle) param.args[0];
            Object cAIOParam = bundle.getParcelable("aio_param");

            Object AIOSession = XField.obj(cAIOParam).type(Initiator.loadClass("com.tencent.aio.data.AIOSession")).get();
            Object AIOContact = XField.obj(AIOSession).type(Initiator.loadClass("com.tencent.aio.data.AIOContact")).get();

            String cPeerUID = XField.obj(AIOContact).name("f").type(String.class).get();
            int cChatType = XField.obj(AIOContact).name("e").type(int.class).get();
            Session.setCurrentPeerID(cPeerUID);
            Session.setCurrentChatType(cChatType);

            // -------------------------------------------

            QQBubbleRedirect.invokeGetAioVasMsgData();
        });
    }

    @Override
    public void load(@NonNull ClassLoader classLoader) throws Throwable {
        update(classLoader);
    }
}