package moe.ono.creator

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.telecom.Call
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.ScrollView
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.core.BottomPopupView
import com.lxj.xpopup.util.XPopupUtils
import io.noties.markwon.Markwon
import kotlinx.io.errors.IOException
import moe.ono.R
import moe.ono.hooks.base.util.Toasts
import moe.ono.hooks.base.util.Toasts.TYPE_ERROR
import moe.ono.hooks.base.util.Toasts.TYPE_INFO
import moe.ono.ui.CommonContextWrapper
import moe.ono.ui.view.LoadingButton
import moe.ono.util.AppRuntimeHelper
import moe.ono.util.Logger
import moe.ono.util.SyncUtils
import moe.ono.util.SyncUtils.runOnUiThread
import moe.ono.util.Utils.jump
import moe.ono.util.analytics.ActionReporter
import moe.ono.util.api.ArkEnv
import moe.ono.util.api.ark.ArkRequest
import moe.ono.util.api.ark.ArkRequestCallback
import okhttp3.Callback
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.net.Inet4Address
import java.net.InetAddress
import java.security.MessageDigest

@SuppressLint("ResourceType")
class QQMessageTrackerResultDialog(context: Context) : BottomPopupView(context) {
    data class IpLocationResponse(
        val code: Int,
        val success: Boolean,
        val message: String,
        val data: IpData
    )

    data class IpData(
        val ip: String,
        val country: String,
        val province: String,
        val city: String,
        val districts: String,
        val isp: String,
        val net: String,
        val lng: String,
        val lat: String
    )

    @SuppressLint("SetTextI18n", "ServiceCast")
    override fun onCreate() {
        super.onCreate()
        Handler(Looper.getMainLooper()).postDelayed({
            val textView =
                findViewById<TextView>(R.id.tv_callback)
            val button = findViewById<LoadingButton>(R.id.btn_get)
            val btnMore = findViewById<MaterialButton>(R.id.btn_more)
            val scrollView = findViewById<ScrollView>(R.id.scrollview)

            button.setOnClickListener { get(textView, button) }

            val items = ArrayList<String>()
            items.add("滑动到顶部")
            items.add("滑动到底部")

            textView.setCustomSelectionActionModeCallback(object : ActionMode.Callback {
                override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                    menu?.clear()

                    menu?.add(0, 1, 0, "解析 IP")
                    menu?.add(0, 2, 1, "复制")

                    menu?.findItem(1)?.setOnMenuItemClickListener {
                        val selectedText = getSelectedText(textView)
                        if (selectedText?.matches(Regex("""\d{1,3}(\.\d{1,3}){3}""")) == true) {
                            jump(context, "https://iplark.com/$selectedText")
                        } else {
                            Toasts.show(context, TYPE_INFO, "请选择有效的 IP 地址")
                        }
                        mode?.finish()
                        true
                    }

                    menu?.findItem(2)?.setOnMenuItemClickListener {
                        val selectedText = getSelectedText(textView)
                        if (!selectedText.isNullOrEmpty()) {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Copied Text", selectedText))
                            Toasts.show(context, TYPE_INFO, "已复制")
                        }
                        mode?.finish()
                        true
                    }

                    return true
                }

                override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                    return false
                }

                override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                    return false
                }

                override fun onDestroyActionMode(mode: ActionMode?) {
                }
            })




            btnMore.setOnClickListener { v ->
                XPopup.Builder(context)
                    .hasShadowBg(false)
                    .atView(v)
                    .asAttachList(
                        items.toTypedArray<String>(),
                        intArrayOf()
                    ) { _: Int, text: String? ->
                        when (text) {
                            "滑动到顶部" -> scrollView.fullScroll(ScrollView.FOCUS_UP)
                            "滑动到底部" -> scrollView.fullScroll(ScrollView.FOCUS_DOWN)
                        }
                    }
                    .show()
            }
            get(textView, button)
        }, 100)
    }


    private fun getSelectedText(textView: TextView): String? {
        val start = textView.selectionStart
        val end = textView.selectionEnd
        return if (start in 0..<end) {
            textView.text.substring(start, end)
        } else {
            null
        }
    }

    private fun get(textView: TextView, button: LoadingButton) {
        button.setLoading(true)
        button.isEnabled = false
        val url = ArkEnv.getAuthAPI() + String.format(
            ArkEnv.GET_CARD_DATA_ENDPOINT,
            "${Companion.id}.txt?t=${System.currentTimeMillis()}"
        )

        Thread {
            ArkRequest.retrieveArkListenerData(url, context, true, object : ArkRequestCallback {
                override fun onSuccess(result: String) {
                    runOnUiThread {
                        val markwon = Markwon.create(context)
                        markwon.setMarkdown(textView, result)
                        button.setLoading(false)
                        button.isEnabled = true
                    }
                }

                override fun onFailure(e: Exception) {
                    runOnUiThread {
                        val markwon = Markwon.create(context)
                        markwon.setMarkdown(
                            textView,
                            "### 无法获取监听数据\n\n- 请勿快速拉取数据\n------\n\n```\n$e\n```"
                        )
                        button.setLoading(false)
                        button.isEnabled = true
                    }
                }
            })
        }.start()

    }

    fun fetchIpData(context: Context, ip: String, callback: (IpLocationResponse?) -> Unit) {
        val url = "https://ipv4.ip.mir6.com/api/api_json.php?ip=$ip&token=mir6.com"

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                runOnUiThread {
                    Toasts.show(context, TYPE_ERROR,"请求失败，请检查网络连接")
                }

                callback(null)
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val ipLocationResponse = Gson().fromJson(responseBody, IpLocationResponse::class.java)
                    callback(ipLocationResponse)
                } else {
                    runOnUiThread {
                        Toasts.show(context, TYPE_ERROR,"请求失败，请稍后再试")
                    }

                    callback(null)
                }
            }
        })
    }

    fun showIpInfoDialog(context: Context, ipDataJson: IpLocationResponse?) {
        ipDataJson?.let { ipData ->
            val ipLocation = "${ipData.data.country} ${ipData.data.province} ${ipData.data.city} ${ipData.data.districts}"
            val net = ipData.data.net
            val isp = ipData.data.isp
            val lngLat = "${ipData.data.lng}, ${ipData.data.lat}"

            val infoText = """
            IP: ${ipData.data.ip}
            地址位置: $ipLocation
            网络类型: $net
            ISP: $isp
            经纬度: $lngLat
            """.trimIndent()

            val textView = TextView(context).apply {
                text = infoText
                isFocusableInTouchMode = true
                isClickable = true
                setTextIsSelectable(true)
                setPadding(32, 32, 32, 32)
            }

            // Create a MaterialDialog
            MaterialAlertDialogBuilder(context)
                .setTitle("IP 信息")
                .setView(textView)
                .setPositiveButton("复制") { _, _ ->
                    // Copy to clipboard when the button is clicked
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("IP Info", infoText)
                    clipboard.setPrimaryClip(clip)
                    Toasts.show(context, TYPE_INFO,"已复制 IP 信息")
                }
                .show()
        } ?: run {
            Toasts.show(context, TYPE_ERROR,"未能获取 IP 信息")
        }


    }

    fun md5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }





    override fun getImplLayoutId(): Int {
        return R.layout.layout_qq_message_tracker_result
    }

    companion object {
        private var popupView: BasePopupView? = null
        private var id: String? = null

        fun createView(context: Context, id_: String?) {
            val fixContext = CommonContextWrapper.createAppCompatContext(context)
            val newPop = XPopup.Builder(fixContext).moveUpToKeyboard(true).isDestroyOnDismiss(true)
            newPop.maxHeight((XPopupUtils.getScreenHeight(context) * .90f).toInt())
            newPop.popupHeight((XPopupUtils.getScreenHeight(context) * .90f).toInt())
            id = id_


            ActionReporter.reportVisitor(
                AppRuntimeHelper.getAccount(),
                "CreateView-QQMessageTrackerResultDialog"
            )

            popupView = newPop.asCustom(QQMessageTrackerResultDialog(fixContext))
            popupView?.show()
        }
    }
}



