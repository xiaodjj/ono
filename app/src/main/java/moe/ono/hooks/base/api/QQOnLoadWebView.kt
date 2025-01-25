package moe.ono.hooks.base.api

import de.robv.android.xposed.XposedHelpers.findMethodExact
import moe.ono.hooks._base.ApiHookItem
import moe.ono.hooks._core.annotation.HookItem
import moe.ono.hooks.clazz
import moe.ono.hooks.item.chat.QQBubbleRedirect.Companion.injectWebViewForBubble
import moe.ono.util.Logger
import moe.ono.util.SyncUtils
import java.lang.reflect.Method
import java.util.regex.Pattern


@HookItem(path = "API/OnWebViewLoad")
class QQOnLoadWebView : ApiHookItem() {
    @Throws(Throwable::class)
    override fun load(classLoader: ClassLoader) {
        val mLoadUrl: Method = findMethodExact(
            "com.tencent.qimei.webview.QmX5Webview".clazz,
            "loadUrl",
            String::class.java,
        )

        hookAfter(mLoadUrl) {
            val url = it.args[0] as String
            Logger.d("loadUrl: $url")

            if (url.startsWith("https://zb.vip.qq.com/mall/item-detail?appid=2&")) {
                val webView = it.thisObject
                val pattern = Pattern.compile("itemid=(\\d+)")
                val matcher = pattern.matcher(url)
                if (matcher.find()) {
                    val itemid = matcher.group(1)
                    Logger.d("itemid: $itemid")
                    itemid?.let { it1 -> injectWebViewForBubble(webView, it1) }
                } else {
                    Logger.e("itemid not found.")
                }
            }
        }
    }

    override fun targetProcess(): Int {
        return SyncUtils.PROC_TOOL
    }
}
