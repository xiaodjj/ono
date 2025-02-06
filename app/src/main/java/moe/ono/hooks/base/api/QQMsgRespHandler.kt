package moe.ono.hooks.base.api

import com.tencent.qphone.base.remote.FromServiceMsg
import com.tencent.qphone.base.remote.ToServiceMsg
import moe.ono.config.CacheConfig.setRKeyGroup
import moe.ono.config.CacheConfig.setRKeyPrivate
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

@HookItem(path = "API/QQMsgRespHandler")
class QQMsgRespHandler : ApiHookItem() {
    private fun update() {
        hookBefore(XMethod.clz("mqq.app.msghandle.MsgRespHandler").name("dispatchRespMsg").ignoreParam().get()) { param ->
            val serviceMsg: ToServiceMsg = XField.obj(param.args[1]).name("toServiceMsg").get()
            val fromServiceMsg: FromServiceMsg =
                XField.obj(param.args[1]).name("fromServiceMsg").get()
            if ("OidbSvcTrpcTcp.0x9067_202" == fromServiceMsg.serviceCmd) {
                val data = FunProtoData()
                data.fromBytes(
                    getUnpPackage(
                        fromServiceMsg.wupBuffer
                    )
                )

                val obj: JSONObject = data.toJSON()
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
            } else if ("MessageSvc.PbGetGroupMsg" == fromServiceMsg.serviceCmd) {
                Logger.d("on MessageSvc.PbGetGroupMsg")
                val data = FunProtoData()
                data.fromBytes(
                    getUnpPackage(
                        fromServiceMsg.wupBuffer
                    )
                )

                val obj: JSONObject = data.toJSON()

                Logger.d("obj: " + obj.toString(4))
                SyncUtils.runOnUiThread { QQMessageFetcherResultDialog.createView(
                    CommonContextWrapper.createAppCompatContext(ContextUtils.getCurrentActivity()), obj) }

            } else if ("MessageSvc.PbGetOneDayRoamMsg" == fromServiceMsg.serviceCmd) {
                Logger.d("on MessageSvc.PbGetOneDayRoamMsg")
                val data = FunProtoData()
                data.fromBytes(
                    getUnpPackage(
                        fromServiceMsg.wupBuffer
                    )
                )

                val obj: JSONObject = data.toJSON()

                Logger.d("obj: " + obj.toString(4))
                SyncUtils.runOnUiThread { QQMessageFetcherResultDialog.createView(
                    CommonContextWrapper.createAppCompatContext(ContextUtils.getCurrentActivity()), obj) }
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