package moe.ono.activity

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.CallSuper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import moe.ono.R
import moe.ono.config.ONOConf

class BiliLoginActivity : BaseActivity() {
    private var flag: Boolean = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun doOnCreate(savedInstanceState: Bundle?): Boolean {
        super.doOnCreate(savedInstanceState)
        setContentView(R.layout.activity_login_bili)

        val mWvLogin = findViewById<WebView>(R.id.wv_login)
        mWvLogin.settings.javaScriptEnabled = true
        mWvLogin.webViewClient = LoginWebViewClient()
        mWvLogin.loadUrl("https://passport.bilibili.com/login")
        return true
    }

    inner class LoginWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            val url = request.url.toString()
            view.loadUrl(url)
            if (url.contains("m.bilibili.com")) {
                val cookieManager = CookieManager.getInstance()
                val cookies = cookieManager.getCookie(url)
                onLoginSuccess(cookies)
            }
            return true
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
        }
    }

    fun onLoginSuccess(cookies: String?) {
        ONOConf.setString("global", "cookies", cookies)

        if (!flag) {
            MaterialAlertDialogBuilder(this)
                .setTitle("登录成功")
                .setMessage("已链接到您的第三方账户，点击确认后返回到上级页面。")
                .setNegativeButton(
                    "确定"
                ) { _: DialogInterface?, _: Int -> finish() }
                .setPositiveButton("切换账户") { _, _ -> logout() }
                .show()
            flag = true
        }
    }

    fun logout() {
        val cookieManager = CookieManager.getInstance()
        cookieManager.removeAllCookies(null)
        cookieManager.flush()

        val mWvLogin = findViewById<WebView>(R.id.wv_login)
        mWvLogin.clearCache(true)
        mWvLogin.clearHistory()
        mWvLogin.loadUrl("https://passport.bilibili.com/login")
    }

    @CallSuper
    override fun doOnEarlyCreate(savedInstanceState: Bundle?, isInitializing: Boolean) {
        super.doOnEarlyCreate(savedInstanceState, isInitializing)
        setTheme(R.style.Theme_Ono)
    }
}