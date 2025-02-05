package moe.ono.creator

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.core.BottomPopupView
import com.lxj.xpopup.util.XPopupUtils
import moe.ono.R
import moe.ono.ui.CommonContextWrapper
import moe.ono.util.AppRuntimeHelper
import moe.ono.util.analytics.ActionReporter
import org.json.JSONObject

@SuppressLint("ResourceType")
class QQMessageFetcherResultDialog(context: Context) : BottomPopupView(context) {
    @SuppressLint("SetTextI18n", "ServiceCast")
    override fun onCreate() {
        super.onCreate()
        Handler(Looper.getMainLooper()).postDelayed({
            val tvContent = findViewById<TextView>(R.id.tv_content)
            val jsonString = content?.toString(4)
            tvContent.text = jsonString
        }, 100)
    }




    override fun getImplLayoutId(): Int {
        return R.layout.layout_pb_decode
    }

    companion object {
        private var popupView: BasePopupView? = null
        private var content: JSONObject? = null

        fun createView(context: Context, content: JSONObject) {
            val fixContext = CommonContextWrapper.createAppCompatContext(context)
            val newPop = XPopup.Builder(fixContext).moveUpToKeyboard(true).isDestroyOnDismiss(true)
            newPop.maxHeight((XPopupUtils.getScreenHeight(context) * .90f).toInt())
            newPop.popupHeight((XPopupUtils.getScreenHeight(context) * .90f).toInt())
            Companion.content = content


            ActionReporter.reportVisitor(
                AppRuntimeHelper.getAccount(),
                "CreateView-QQMessageFetcherResultDialog"
            )

            popupView = newPop.asCustom(QQMessageFetcherResultDialog(fixContext))
            popupView?.show()
        }
    }
}



