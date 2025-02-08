package moe.ono.hooks.base.api

import com.tencent.qphone.base.remote.FromServiceMsg
import com.tencent.qphone.base.remote.ToServiceMsg
import com.tencent.qqnt.kernel.nativeinterface.MsgElement
import com.tencent.qqnt.kernel.nativeinterface.MultiForwardMsgElement
import moe.ono.bridge.Nt_kernel_bridge
import moe.ono.builder.MsgBuilder
import moe.ono.config.CacheConfig.setRKeyGroup
import moe.ono.config.CacheConfig.setRKeyPrivate
import moe.ono.creator.ElementSender
import moe.ono.creator.QQMessageFetcherResultDialog
import moe.ono.hooks._base.ApiHookItem
import moe.ono.hooks._core.annotation.HookItem
import moe.ono.reflex.XField
import moe.ono.reflex.XMethod
import moe.ono.ui.CommonContextWrapper
import moe.ono.util.ContextUtils
import moe.ono.util.FunProtoData
import moe.ono.util.Logger
import moe.ono.util.SyncUtils
import org.json.JSONObject
import java.util.Arrays
import java.util.UUID

@HookItem(path = "API/QQMsgRespHandler")
class QQMsgRespHandler : ApiHookItem() {
    private fun update() {
        hookBefore(XMethod.clz("mqq.app.msghandle.MsgRespHandler").name("dispatchRespMsg").ignoreParam().get()) { param ->
            val serviceMsg: ToServiceMsg = XField.obj(param.args[1]).name("toServiceMsg").get()
            val fromServiceMsg: FromServiceMsg =
                XField.obj(param.args[1]).name("fromServiceMsg").get()

            val data = FunProtoData()
            data.fromBytes(
                getUnpPackage(
                    fromServiceMsg.wupBuffer
                )
            )

            val obj: JSONObject = data.toJSON()
            when (fromServiceMsg.serviceCmd) {
                "OidbSvcTrpcTcp.0x9067_202" -> {
                    Logger.d("on OidbSvcTrpcTcp.0x9067_202")

                    val rkeyGroup =
                        obj.getJSONObject("4")
                            .getJSONObject("4")
                            .getJSONArray("1")
                            .getJSONObject(0).getString("1")

                    val rkeyPrivate =
                        obj.getJSONObject("4")
                            .getJSONObject("4")
                            .getJSONArray("1")
                            .getJSONObject(1).getString("1")

                    setRKeyGroup(rkeyGroup)
                    setRKeyPrivate(rkeyPrivate)
                }
                "MessageSvc.PbGetGroupMsg" -> {
                    Logger.d("on MessageSvc.PbGetGroupMsg")

                    Logger.d("obj: " + obj.toString(4))
                    SyncUtils.runOnUiThread { QQMessageFetcherResultDialog.createView(
                        CommonContextWrapper.createAppCompatContext(ContextUtils.getCurrentActivity()), obj) }

                }
                "MessageSvc.PbGetOneDayRoamMsg" -> {
                    Logger.d("on MessageSvc.PbGetOneDayRoamMsg")

                    Logger.d("obj: " + obj.toString(4))
                    SyncUtils.runOnUiThread { QQMessageFetcherResultDialog.createView(
                        CommonContextWrapper.createAppCompatContext(ContextUtils.getCurrentActivity()), obj) }
                }

                "trpc.group.long_msg_interface.MsgService.SsoSendLongMsg" -> {
                    Logger.d("on trpc.group.long_msg_interface.MsgService.SsoSendLongMsg")
                    Logger.d("obj: " + obj.toString(4))

                    val resid = obj.getJSONObject("2").getString("3")

                    Logger.d("resid", resid)
                    val content = "{\n" +
                            "    \"37\": {\n" +
                            "        \"6\": 1,\n" +
                            "        \"7\": \"$resid\",\n" +
                            "        \"17\": 0,\n" +
                            "        \"19\": {\n" +
                            "            \"15\": 0,\n" +
                            "            \"31\": 0,\n" +
                            "            \"41\": 0\n" +
                            "        }\n" +
                            "    }\n" +
                            "}"

                    ElementSender.setContent(content)
                }
            }
        }

    }


    private fun getUnpPackage(b: ByteArray?): ByteArray? {
        if (b == null) {
            return null
        }
        if (b.size < 4) {
            return b
        }
        return if (b[0].toInt() == 0) {
            Arrays.copyOfRange(b, 4, b.size)
        } else {
            b
        }
    }

    @Throws(Throwable::class)
    override fun load(classLoader: ClassLoader) {
        update()
    }
}