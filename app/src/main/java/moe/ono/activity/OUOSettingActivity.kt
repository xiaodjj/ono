package moe.ono.activity

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.annotation.CallSuper
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.color.DynamicColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import moe.ono.R
import moe.ono.constants.PackageConstants
import moe.ono.databinding.ActivitySettingBinding
import moe.ono.dexkit.TargetManager
import moe.ono.dexkit.TargetManager.removeAllMethodSignature
import moe.ono.fragment.BaseSettingFragment
import moe.ono.hooks.base.util.Toasts
import moe.ono.hostInfo
import moe.ono.isInHostProcess
import moe.ono.ui.ModuleThemeManager
import moe.ono.ui.ThemeAttrUtils
import moe.ono.ui.view.BgEffectPainter
import mqq.app.AppRuntime
import java.lang.Integer.max

open class OUOSettingActivity : BaseActivity() {
    private val FRAGMENT_TAG = "OUOSettingActivity.FRAGMENT_TAG"
    private val FRAGMENT_SAVED_STATE_KEY = "OUOSettingActivity.FRAGMENT_SAVED_STATE_KEY"
    private val FRAGMENT_CLASS_KEY = "OUOSettingActivity.FRAGMENT_CLASS_KEY"
    private val FRAGMENT_ARGS_KEY = "OUOSettingActivity.FRAGMENT_ARGS_KEY"

    private var mAppBarLayoutHeight: Int = 0
    private val mPendingOnStartActions = ArrayList<Runnable>(4)
    private val mPendingOnResumeActions = ArrayList<Runnable>(4)
    private val mPendingActionsLock = Any()
    private val mFragmentStack = ArrayList<BaseSettingFragment>(4)

    private lateinit var mBgEffectView: View
    private lateinit var mBgEffectPainter: BgEffectPainter
    private val startTime = System.nanoTime().toFloat()
    private val mHandler = Handler(Looper.getMainLooper())
    private lateinit var binding: ActivitySettingBinding
    private lateinit var mAppBarLayout: AppBarLayout
    private lateinit var mAppToolBar: androidx.appcompat.widget.Toolbar

    private val mHostAppPackages = setOf(
        PackageConstants.PACKAGE_NAME_QQ,
    )

    private val runnableBgEffect = object : Runnable {
        override fun run() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mBgEffectPainter.setAnimTime(((System.nanoTime() - startTime) / 1.0E9f) % 62.831852f)
                mBgEffectPainter.setResolution(floatArrayOf(mBgEffectView.width.toFloat(), mBgEffectView.height.toFloat()))
                mBgEffectPainter.updateMaterials()
                mBgEffectView.setRenderEffect(mBgEffectPainter.renderEffect)
                mHandler.postDelayed(this, 16L)
            }
        }
    }

    @CallSuper
    override fun doOnEarlyCreate(savedInstanceState: Bundle?, isInitializing: Boolean) {
        super.doOnEarlyCreate(savedInstanceState, isInitializing)
        setTheme(ModuleThemeManager.getCurrentStyleId())
    }

    /**
     * Handle saved instance state ourselves
     */
    override fun shouldRetainActivitySavedInstanceState() = false

    override fun doOnCreate(savedInstanceState: Bundle?): Boolean {
        // we don't want the Fragment to be recreated
        setTheme(R.style.Theme_MaiTungTMDesign_DayNight)

        super.doOnCreate(null)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // update window background, I don't know why, but it's necessary
        val bgColor = ThemeAttrUtils.resolveColorOrDefaultColorInt(this, android.R.attr.windowBackground, 0)
        window.setBackgroundDrawable(ColorDrawable(bgColor))
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED
                    or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )
        requestTranslucentStatusBar()

        if (isInHostProcess) {
            val isThemeLightMode = resources.getBoolean(R.bool.is_not_night_mode)
            if (isThemeLightMode) {
                // QQ 8.9.78(4548)+
                // We need to tell system we are using a light title bar and we want a dark status bar text
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val insetsController = window.decorView.windowInsetsController
                    val flags = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
                            WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                    insetsController?.setSystemBarsAppearance(flags, flags)
                } else {
                    window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or
                            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
            }
        }

        setHyperBackground()

        mAppBarLayout = findViewById(R.id.topAppBarLayout)
        mAppToolBar = findViewById(R.id.topAppBar)
        mAppBarLayout.background = mAppToolBar.background
        setSupportActionBar(mAppToolBar)
        requestTranslucentStatusBar()
        mAppBarLayout.addOnLayoutChangeListener { _, _, top, _, bottom, _, _, _, _ ->
            mAppBarLayoutHeight = bottom - top
            for (fragment in mFragmentStack) {
                fragment.notifyLayoutPaddingsChanged()
            }
        }


        if (isInHostProcess) {
            val isThemeLightMode = resources.getBoolean(R.bool.is_not_night_mode)
            if (isThemeLightMode) {
                // QQ 8.9.78(4548)+
                // We need to tell system we are using a light title bar and we want a dark status bar text
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val insetsController = window.decorView.windowInsetsController
                    val flags = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
                            WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                    insetsController?.setSystemBarsAppearance(flags, flags)
                } else {
                    window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or
                            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
            }
        }

        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.ouo_setting_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.menu_remove_all_method_signature) {

            MaterialAlertDialogBuilder(this)
                .setTitle("确定重新适配宿主吗？")
                .setMessage("这可能会花费一些时间。")
                .setPositiveButton("确定") { _: DialogInterface?, _: Int ->
                    Toasts.info(this, "重启QQ生效")
                    removeAllMethodSignature()
                    TargetManager.setIsNeedFindTarget(true)
                }
                .setNegativeButton("取消") { _: DialogInterface, _: Int -> }
                .show()

        } else {
            return super@OUOSettingActivity.onOptionsItemSelected(item)
        }
        return true
    }


    open val layoutPaddingTop: Int
        get() = max(mAppBarLayoutHeight, statusBarLayoutInsect)

    open val layoutPaddingBottom: Int
        get() = navigationBarLayoutInsect

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // notifies the fragment that it is attached to the window
        for (fragment in mFragmentStack) {
            fragment.notifyLayoutPaddingsChanged()
        }
    }

    override fun doOnDestroy() {
        super.doOnDestroy()
        synchronized(mPendingActionsLock) {
            mPendingOnStartActions.clear()
            mPendingOnResumeActions.clear()
        }
    }

    

    @CallSuper
    override fun doOnResume() {
        super.doOnResume()
        synchronized(mPendingActionsLock) {
            // on start actions
            for (action in mPendingOnStartActions) {
                action.run()
            }
            mPendingOnStartActions.clear()
            // on resume actions
            for (action in mPendingOnResumeActions) {
                action.run()
            }
            mPendingOnResumeActions.clear()
        }
    }

    @CallSuper
    override fun doOnStart() {
        super.doOnStart()
        // on start actions
        synchronized(mPendingActionsLock) {
            for (action in mPendingOnStartActions) {
                action.run()
            }
            mPendingOnStartActions.clear()
        }
    }

    /*
   * Hyper OS 2.0 !
   * */
    private fun setHyperBackground(){
        DynamicColors.applyToActivitiesIfAvailable(application)

        val contentView = findViewById<ViewGroup>(android.R.id.content)

        mBgEffectView = LayoutInflater.from(this).inflate(R.layout.layout_effect_bg, contentView, false)
        contentView.addView(mBgEffectView, 0)
        mBgEffectView = contentView.findViewById(R.id.bgEffectView)

        mBgEffectView.post {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mBgEffectPainter = BgEffectPainter(hostInfo.application)
                val isThemeLightMode = resources.getBoolean(R.bool.is_night_mode)
                mBgEffectPainter.showRuntimeShader(hostInfo.application, mBgEffectView, binding.topAppBar, isThemeLightMode)
            }

            mHandler.post(runnableBgEffect)
        }

    }

    companion object {
        const val TARGET_FRAGMENT_KEY: String = "OUOSettingActivity.TARGET_FRAGMENT_KEY"
        const val TARGET_FRAGMENT_ARGS_KEY: String = "OUOSettingActivity.TARGET_FRAGMENT_ARGS_KEY"

        @JvmStatic
        @JvmOverloads
        fun createStartActivityForFragmentIntent(
            context: Context,
            fragmentClass: Class<out BaseSettingFragment>,
            args: Bundle? = null
        ): Intent {
            val intent = Intent(context, OUOSettingActivity::class.java)
            intent.putExtra(TARGET_FRAGMENT_KEY, fragmentClass.name)
            intent.putExtra(TARGET_FRAGMENT_ARGS_KEY, args)
            return intent
        }
    }
}