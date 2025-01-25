package moe.ono.hooks.item.chat

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.lxj.xpopup.XPopup
import de.robv.android.xposed.XposedHelpers.findMethodExact
import moe.ono.hooks._base.BaseSwitchFunctionHookItem
import moe.ono.hooks._core.annotation.HookItem
import moe.ono.hooks._core.factory.HookItemFactory.getItem
import moe.ono.hooks.base.util.Toasts
import moe.ono.hooks.clazz
import moe.ono.util.AppRuntimeHelper
import moe.ono.util.Logger
import moe.ono.util.SystemServiceUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.reflect.Method


@SuppressLint("DiscouragedApi")
@HookItem(path = "聊天与消息/气泡重定向", description = "重定向后的气泡对 iOS 设备无效\n开启后去商城任意找一个气泡点击应用即可\n* 重启生效")
class QQBubbleRedirect : BaseSwitchFunctionHookItem() {
    @Throws(Throwable::class)
    override fun load(classLoader: ClassLoader) {
        try {
            val mI: Method = findMethodExact("com.tencent.mobileqq.bubble.BubbleManager".clazz,
                "I", String::class.java)

            hookBefore(mI) {
                val cacheFile = File(CACHE_FILE)
                if (cacheFile.exists()) {
                    it.args[0] = CACHE_FILE
                }
            }

        } catch (t: Throwable) {
            Logger.e("QQBubbleRedirect", t)
        }

    }



    companion object {
        @JvmStatic
        fun invokeGetAioVasMsgData() {
            if (!getItem(QQBubbleRedirect::class.java).isEnabled){
                return
            }
            try {
                val vasAioDataImplClass =
                    Class.forName("com.tencent.mobileqq.vas.api.impl.VasAioDataImpl")
                val vasAioDataImplInstance = vasAioDataImplClass.getDeclaredConstructor().newInstance()
                val getAioVasMsgDataMethod = vasAioDataImplClass.getMethod(
                    "getAioVasMsgData",
                    String::class.java
                )

                getAioVasMsgDataMethod.invoke(vasAioDataImplInstance, AppRuntimeHelper.getAccount())
            } catch (e: Exception) {
                Logger.e("InvokeGetAioVasMsgData", e)
            }
        }

        @JvmStatic
        fun injectWebViewForBubble(webView: WebView, itemId: String) {
            webView.addJavascriptInterface(WebAppInterfaceForBubble(webView.context), "obj")

            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    super.onPageFinished(view, url)
                    val jsCode = "(function() {" +
                            "var buttons = document.querySelectorAll('button[vt-itemid][vt-itemtype][vt-actionid]');" + // 匹配所有符合条件的按钮
                            "if (typeof obj !== 'undefined') {" +
                            "    console.log(\"obj is defined\");" +
                            "} else {" +
                            "    console.log(\"obj is not defined\");" +
                            "}" +
                            "buttons.forEach(function(button) {" +
                            "   if (button.innerText.includes('分享')) {" +
                            "       var customButton = document.createElement('button');" +
                            "       customButton.innerText = '应用';" +
                            "       customButton.style.marginLeft = '10px';" +
                            "       customButton.onclick = function() { " +
                            "       alert(\"气泡 $itemId 已尝试应用，重启生效\");" +
                            "       obj.onCustomButtonClicked(\"$itemId\");" +
                            "       " +
                            " };" +
                            "       button.parentNode.insertBefore(customButton, button.nextSibling);" +
                            "   }" +
                            "});" +
                            "})();"

                    webView.evaluateJavascript(jsCode, null)
                }
            }

            webView.webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                    Logger.d(("WebView console: " + consoleMessage.message() +
                            " -- From line " + consoleMessage.lineNumber() +
                            " of " + consoleMessage.sourceId()))
                    return true
                }
            }
        }
        private fun createCacheFile(id: String) {
            val jsonContent =
                "{\"animations\":{\"stc1\":{\"align\":\"BL\",\"alpha\":\"false\",\"count\":\"12\",\"cycle_count\":\"3\",\"rect\":[\"0\",\"-80\",\"56\",\"80\"],\"time\":\"100\",\"type\":\"static\",\"zip_name\":\"voice\"}},\"color\":\"0xFFffffff\",\"id\":2727,\"key_animations\":[{\"align\":\"BL\",\"animation\":\"stc1\",\"count\":\"12\",\"cycle_count\":\"3\",\"key_word\":[\"我\",\"你\",\"他\",\"她\",\"它\",\"爱\",\"乐\",\"美\",\"高兴\",\"笑\",\"好\",\"的\",\"那\",\"拜拜\",\"么么\",\"晚安\",\"乖\",\"帅\",\"睡\",\"想\",\"是\",\"88\",\"不\",\"啊\",\"哈\",\"嗯\",\"呵\"],\"rect\":[\"0\",\"-80\",\"56\",\"80\"],\"time\":\"100\",\"version\":1516189158,\"zip_name\":\"voice\"}],\"link_color\":\"0xFF04018d\",\"loopList\":[#>id<#],\"name\":\"七彩心情\",\"version\":1516189158,\"voice_animation\":{\"align\":\"BL\",\"animation\":\"stc1\",\"count\":\"12\",\"rect\":[\"0\",\"-80\",\"56\",\"80\"],\"time\":\"100\"},\"zoom_point\":[\"65\",\"56\"]}"
            val cacheFile = File(CACHE_FILE)
            cacheFile.delete()

            try {
                if (!cacheFile.parentFile?.exists()!!) {
                    cacheFile.parentFile?.mkdirs()
                }
                FileOutputStream(cacheFile).use { fos ->
                    fos.write(jsonContent.replace("#>id<#", id).toByteArray())
                }
                Logger.i("[QQBubbleRedirect] 缓存文件创建成功: $CACHE_FILE")
            } catch (e: IOException) {
                Logger.i("[QQBubbleRedirect] 缓存文件创建失败: ", e)
            }
        }

        class WebAppInterfaceForBubble(private val context: Context) {
            @JavascriptInterface
            fun onCustomButtonClicked(itemId: String) {
                Logger.d("itemID: $itemId")
                createCacheFile(itemId)
                Toasts.show(context, Toasts.TYPE_SUCCESS, "done")

            }
        }


        @SuppressLint("SdCardPath")
        const val CACHE_FILE =
            "/data/user/0/com.tencent.mobileqq/files/files/ono_bubble_cache.json"
    }
}

