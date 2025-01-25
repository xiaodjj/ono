package moe.ono.hooks.base.api

import android.webkit.WebView
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
class OnLoadWebView : ApiHookItem() {
    @Throws(Throwable::class)
    override fun load(classLoader: ClassLoader) {
        val mLoadUrl: Method = findMethodExact("android.webkit.WebView".clazz,"loadUrl",String::class.java)

        hookAfter(mLoadUrl) {
            val url = it.args[0] as String
            Logger.d("loadUrl: $url")

            if (url.startsWith("https://zb.vip.qq.com/mall/item-detail?appid=2&")) {
                val webView = it.thisObject as WebView
                val pattern = Pattern.compile("itemid=(\\d+)")
                val matcher = pattern.matcher(url)
                if (matcher.find()) {
                    val itemid = matcher.group(1)
                    Logger.d("itemid: $itemid")
                    injectWebViewForBubble(webView, itemid)
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
