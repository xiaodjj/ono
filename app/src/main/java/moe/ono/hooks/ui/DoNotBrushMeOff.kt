package moe.ono.hooks.ui

import android.annotation.SuppressLint
import de.robv.android.xposed.XC_MethodHook
import moe.ono.hooks._base.BaseSwitchFunctionHookItem
import moe.ono.hooks._core.annotation.HookItem
import moe.ono.hooks._core.factory.HookItemFactory.getItem
import moe.ono.util.Logger
import top.artmoe.inao.entries.MsgPushOuterClass

@SuppressLint("DiscouragedApi")
@HookItem(path = "娱乐功能/禁止敷衍", description = "替换敷衍词汇\n* 此功能通过直接拦截MsgPush实现，会影响本地聊天记录，完美告别敷衍")
class DoNotBrushMeOff : BaseSwitchFunctionHookItem() {
    override fun load(classLoader: ClassLoader) {}

    fun filter(msgPush: MsgPushOuterClass.MsgPush, param: XC_MethodHook.MethodHookParam) {
        if (!getItem(this.javaClass).isEnabled){
            return
        }
        kotlin.runCatching {
            val oldMessage = msgPush.qqMessage

            val newMessageBody = oldMessage.messageBody.toBuilder().apply {
                if (hasRichMsg()) {
                    richMsg = richMsg.toBuilder().apply {
                        msgContentList.forEachIndexed { index, msgContent ->
                            if (msgContent.hasTextMsg()) {
                                val originalText = msgContent.textMsg.text
                                val replacementText = getReplacement(originalText)
                                if (replacementText != null) {
                                    setMsgContent(
                                        index,
                                        msgContent.toBuilder().apply {
                                            textMsg = textMsg.toBuilder().apply {
                                                text = replacementText
                                            }.build()
                                        }.build()
                                    )
                                }
                            }
                        }
                    }.build()
                }
            }.build()

            val newMessage = oldMessage.toBuilder().apply {
                messageBody = newMessageBody
            }.build()

            val newMsgPush = msgPush.toBuilder().apply {
                qqMessage = newMessage
            }.build()

            param.args[1] = newMsgPush.toByteArray()
        }.onFailure {
            Logger.e(it)
        }
    }

    private fun getReplacement(text: String?): String? {
        val replacementMap = mapOf(
            "哦" to "哦哦",
            "嗯" to "嗯嗯",
            "好的" to "好的呀",
            "行" to "行吧",
            "好" to "好的呀"
        )
        return text?.trim()?.let { replacementMap[it] }
    }
}