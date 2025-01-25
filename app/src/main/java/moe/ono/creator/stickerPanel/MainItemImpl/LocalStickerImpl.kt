package moe.ono.creator.stickerPanel.MainItemImpl

import android.content.Context
import android.content.DialogInterface
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import moe.ono.R
import moe.ono.config.ONOConf
import moe.ono.creator.stickerPanel.Async
import moe.ono.creator.stickerPanel.ICreator
import moe.ono.creator.stickerPanel.ICreator.IMainPanelItem
import moe.ono.creator.stickerPanel.LocalDataHelper
import moe.ono.creator.stickerPanel.LocalDataHelper.LocalPath
import moe.ono.creator.stickerPanel.LocalDataHelper.LocalPicItems
import moe.ono.creator.stickerPanel.RecentStickerHelper
import moe.ono.hooks.base.util.Toasts
import moe.ono.hooks.message.MsgSender
import moe.ono.ui.SimpleDragSortView
import moe.ono.util.HostInfo
import moe.ono.util.ImageUtils
import moe.ono.util.LayoutHelper
import moe.ono.util.Logger
import moe.ono.util.Session
import moe.ono.util.ignoreResult
import java.io.File
import kotlin.concurrent.Volatile

class LocalStickerImpl(private var mPackInfo: LocalPath, private var mContext: Context) : IMainPanelItem {
    private var cacheView: ViewGroup =
        View.inflate(mContext, R.layout.sticker_panel_plus_pack_item, null) as ViewGroup
    private var panelContainer: LinearLayout = cacheView.findViewById(R.id.Sticker_Item_Container)
    private var cacheImageView: HashSet<ViewInfo> = HashSet()
    private var tvTitle: TextView = cacheView.findViewById(R.id.Sticker_Panel_Item_Name)
    private var mPicItems: MutableList<LocalPicItems>? = null

    private var setButton: View

    private var showControlType: Int = 0
    private var doNotAutoClose: Boolean = false
    private var isCreated: Boolean = false

    init {
        tvTitle.text = mPackInfo.Name

        setButton = cacheView.findViewById(R.id.Sticker_Panel_Set_Item)
        setButton.setOnClickListener { onSetButtonClick() }
    }

    private fun createMainView() {
        try {
            mPicItems = LocalDataHelper.getPicItems(mPackInfo.storePath)
            var itemLine: LinearLayout? = null
            for (i in mPicItems?.indices!!) {
                val item = mPicItems?.get(i)
                if (i % 5 == 0) {
                    itemLine = LinearLayout(mContext)
                    val params = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    params.bottomMargin = LayoutHelper.dip2px(mContext, 16f)
                    panelContainer.addView(itemLine, params)
                }
                itemLine!!.addView(
                    item?.let {
                        getItemContainer(
                            mContext,
                            LocalDataHelper.getLocalItemPath(mPackInfo, item),
                            i % 5,
                            it
                        )
                    }
                )
            }
            isCreated = true
        } catch (e: Exception) {
            Logger.e("LocalStickerImpl", e)
        }
    }

    private fun onSetButtonClick() {
        MaterialAlertDialogBuilder(mContext)
            .setTitle("选择你的操作").setItems(
                arrayOf(
                    "删除该表情包", "修改表情包名字", "排序表情包"
                )
            ) { _: DialogInterface?, which: Int ->
                when (which) {
                    0 -> {
                        MaterialAlertDialogBuilder(mContext)
                            .setTitle("提示")
                            .setMessage("是否删除该表情包(" + tvTitle.text + "),该表情包内的本地表情将被删除并不可恢复")
                            .setNeutralButton("确定删除") { _: DialogInterface?, _: Int ->
                                LocalDataHelper.deletePath(mPackInfo)
                                ICreator.dismissAll()
                            }
                            .setNegativeButton(
                                "取消"
                            ) { _: DialogInterface?, _: Int -> }
                            .show()
                    }
                    1 -> {
                        MaterialDialog(mContext).show {
                            title(text = "表情包命名")
                            input(
                                hint = "新名称",
                                prefill = "",
                                waitForPositiveButton = false
                            ) { dialog, _ ->
                                val inputField = dialog.getInputField()
                                dialog.setActionButtonEnabled(
                                    WhichButton.POSITIVE,
                                    inputField.text.isNotEmpty()
                                )
                            }.ignoreResult()
                            positiveButton(text = "确定修改") {
                                val text = getInputField().text
                                if (text.isEmpty()) {
                                    Toasts.show(mContext, Toasts.TYPE_ERROR, "输入的名字不能为空")
                                    return@positiveButton
                                }
                                mPackInfo.Name = text.toString()
                                LocalDataHelper.setPathName(mPackInfo, text.toString())
                            }
                            negativeButton(text = "取消") {}
                        }
                    }
                    2 -> {
                        Async.runAsyncLoading(
                            mContext,
                            "正在处理图片中..."
                        ) {
                            val fileList = ArrayList<String>()
                            val wh = LayoutHelper.getScreenWidth(mContext) / 6
                            for (item in mPicItems!!) {
                                fileList.add(
                                    ImageUtils.getResizePicPath(
                                        LocalDataHelper.getLocalItemPath(
                                            mPackInfo,
                                            item
                                        ), wh
                                    )
                                )
                            }
                            val sourceInfo = ArrayList<String>()
                            for (item in mPicItems!!) {
                                sourceInfo.add(item.id)
                            }
                            Async.runOnUi {
                                SimpleDragSortView.createDrag(
                                    mContext, fileList, sourceInfo
                                ) {
                                    for (item in mPicItems!!) {
                                        LocalDataHelper.deletePicLog(mPackInfo, item)
                                    }
                                    for (i in sourceInfo.indices) {
                                        for (item in mPicItems!!) {
                                            if (item.id == sourceInfo[i]) {
                                                LocalDataHelper.addPicItem(mPackInfo.storePath, item)
                                            }
                                        }
                                    }
                                    ICreator.dismissAll()
                                }
                            }
                        }
                    }
                }
            }.show()
    }

    override fun getView(): View {
        if (!isCreated) {
            createMainView()
        }
        showControlType = ONOConf.getInt("global", "sticker_panel_set_rb_show_anim", 1)
        doNotAutoClose = ONOConf.getBoolean("global", "sticker_panel_set_dont_close_panel", false)
        onViewDestroy()
        return cacheView
    }

    private fun getItemContainer(
        context: Context,
        coverView: String,
        count: Int,
        item: LocalPicItems
    ): View {
        val wh = LayoutHelper.getScreenWidth(context) / 6
        val itemDistance = (LayoutHelper.getScreenWidth(context) - wh * 5) / 4

        val img = ImageView(context)
        val info = ViewInfo()
        info.view = img
        info.status = 0

        cacheImageView.add(info)

        val params = LinearLayout.LayoutParams(wh, wh)
        if (count > 0) params.leftMargin = itemDistance
        img.layoutParams = params

        img.tag = coverView
        img.setOnClickListener {
            MsgSender.send_pic_by_contact(
                Session.getContact(),
                LocalDataHelper.getLocalItemPath(mPackInfo, item)
            )
            RecentStickerHelper.addPicItemToRecentRecord(mPackInfo, item)
            if (!doNotAutoClose) {
                ICreator.dismissAll()
            }
        }

        img.setOnLongClickListener {
            val preView = ImageView(context)
            preView.scaleType = ImageView.ScaleType.FIT_CENTER
            preView.layoutParams = ViewGroup.LayoutParams(
                LayoutHelper.getScreenWidth(HostInfo.getApplication()) / 2,
                LayoutHelper.getScreenWidth(HostInfo.getApplication()) / 2
            )
            Glide.with(HostInfo.getApplication()).load(coverView).fitCenter()
                .into(preView)
            MaterialAlertDialogBuilder(mContext)
                .setTitle("选择你对该表情的操作")
                .setView(preView)
                .setOnDismissListener {
                    Glide.with(HostInfo.getApplication()).clear(preView)
                }
                .setNegativeButton("删除该表情") { _: DialogInterface?, _: Int ->
                    LocalDataHelper.deletePicItem(mPackInfo, item)
                    mPicItems!!.remove(item)

                    cacheImageView.clear()
                    panelContainer.removeAllViews()

                    onViewDestroy()
                    createMainView()
                    Async.runOnUi { this.notifyViewUpdate0() }
                }
                .setNeutralButton("设置为标题预览") { _: DialogInterface?, _: Int ->
                    LocalDataHelper.setPathCover(mPackInfo, item)
                    ICreator.dismissAll()
                }.show()
            true
        }

        return img
    }

    override fun onViewDestroy() {
        for (img in cacheImageView) {
            img.view!!.setImageBitmap(null)
            img.status = 0
            Glide.with(HostInfo.getApplication()).clear(img.view!!)
        }
    }

    override fun getID(): Long {
        return mPackInfo.storePath.hashCode().toLong()
    }

    override fun notifyViewUpdate0() {
        for (v in cacheImageView) {
            Logger.d("NotifyUpdate", "update->" + LayoutHelper.isSmallWindowNeedPlay(v.view))
            if (LayoutHelper.isSmallWindowNeedPlay(v.view)) {
                if (v.status != 1) {
                    v.status = 1

                    val coverView = v.view!!.tag as String
                    if (File(coverView + "_thumb").exists()) {
                        if (showControlType == 0) {
                            Glide.with(HostInfo.getApplication()).load(coverView + "_thumb")
                                .skipMemoryCache(true).fitCenter().into(
                                    v.view!!
                                )
                        } else if (showControlType == 1) {
                            if (File(coverView + "_thumb").length() > 2 * 1024 * 1024) {
                                Glide.with(HostInfo.getApplication()).load(coverView + "_thumb")
                                    .dontAnimate().skipMemoryCache(true).fitCenter().into(
                                        v.view!!
                                    )
                            } else {
                                Glide.with(HostInfo.getApplication()).load(coverView + "_thumb")
                                    .skipMemoryCache(true).fitCenter().into(
                                        v.view!!
                                    )
                            }
                        } else if (showControlType == 2) {
                            Glide.with(HostInfo.getApplication()).load(coverView + "_thumb")
                                .dontAnimate().skipMemoryCache(true).fitCenter().into(
                                    v.view!!
                                )
                        }
                    } else {
                        if (showControlType == 0) {
                            Glide.with(HostInfo.getApplication()).load(coverView)
                                .skipMemoryCache(true).fitCenter().into(
                                    v.view!!
                                )
                        } else if (showControlType == 1) {
                            if (File(coverView).length() > 2 * 1024 * 1024) {
                                Glide.with(HostInfo.getApplication()).load(coverView).dontAnimate()
                                    .skipMemoryCache(true).fitCenter().into(
                                        v.view!!
                                    )
                            } else {
                                Glide.with(HostInfo.getApplication()).load(coverView)
                                    .skipMemoryCache(true).fitCenter().into(
                                        v.view!!
                                    )
                            }
                        } else if (showControlType == 2) {
                            Glide.with(HostInfo.getApplication()).load(coverView).dontAnimate()
                                .skipMemoryCache(true).fitCenter().into(
                                    v.view!!
                                )
                        }
                    }
                }
            } else {
                if (v.status != 0) {
                    Glide.with(HostInfo.getApplication()).clear(v.view!!)
                    v.status = 0
                }
            }
        }
    }

    class ViewInfo {
        var view: ImageView? = null

        @Volatile
        var status: Int = 0
    }
}
