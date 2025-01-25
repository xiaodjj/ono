package moe.ono.creator.stickerPanel

import android.content.Context
import android.content.DialogInterface
import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.Button
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import moe.ono.R
import moe.ono.creator.stickerPanel.EmoPanel.EmoInfo
import moe.ono.creator.stickerPanel.LocalDataHelper.LocalPath
import moe.ono.creator.stickerPanel.LocalDataHelper.LocalPicItems
import moe.ono.hooks.base.util.Toasts
import moe.ono.util.FileUtils
import moe.ono.util.HostInfo
import moe.ono.util.ignoreResult
import java.io.File

object PanelUtils {
    private var choicePath: LocalPath? = null

    fun preSavePicToList(url: String, md5: String, context: Context) {
        choicePath = null
        val inflater = LayoutInflater.from(context)
        val mRoot = inflater.inflate(R.layout.sticker_pre_save, null) as ScrollView
        val preView = mRoot.findViewById<ImageView>(R.id.emo_pre_container)
        preView.scaleType = ImageView.ScaleType.FIT_CENTER
        val newInfo = EmoInfo()
        newInfo.URL = url
        newInfo.type = 2
        newInfo.MD5 = md5.uppercase()

        if (url.startsWith("http")) {
            EmoOnlineLoader.submit(newInfo) {
                Glide.with(HostInfo.getApplication())
                    .load(File(newInfo.Path))
                    .fitCenter()
                    .into(preView)
            }
        } else {
            newInfo.Path = url
            Glide.with(HostInfo.getApplication())
                .load(File(newInfo.Path))
                .fitCenter()
                .into(preView)
        }

        val paths = LocalDataHelper.readPaths()

        val group = mRoot.findViewById<RadioGroup>(R.id.emo_pre_list_choser)
        for (path in paths) {
            val button = RadioButton(context)
            button.text = path.Name
            button.textSize = 16f
            button.setTextColor(context.resources.getColor(R.color.global_font_color, null))
            button.setOnCheckedChangeListener { v: CompoundButton, isCheck: Boolean ->
                if (v.isPressed && isCheck) {
                    choicePath = path
                }
            }
            group.addView(button)
        }

        val btnCreate = mRoot.findViewById<Button>(R.id.createNew)
        btnCreate.setOnClickListener {
            createNewFolder(context, group)
        }

        MaterialAlertDialogBuilder(context)
            .setTitle("是否保存如下图片")
            .setView(mRoot)
            .setNegativeButton(
                "保存"
            ) { _: DialogInterface?, _: Int ->
                if (choicePath == null) {
                    Toasts.show(context, Toasts.TYPE_INFO, "没有选择任何的保存列表")
                } else if (TextUtils.isEmpty(newInfo.Path)) {
                    Toasts.show(context, Toasts.TYPE_ERROR, "图片尚未加载完毕,保存失败")
                } else {
                    FileUtils.copy(
                        newInfo.Path,
                        Env.app_save_path + "本地表情包/" + choicePath?.storePath + "/" + md5
                    )
                    val item = LocalPicItems()
                    item.id = md5
                    item.fileName = md5
                    item.addTime = System.currentTimeMillis()
                    LocalDataHelper.addPicItem(choicePath?.storePath, item)


                    Toasts.show(context, Toasts.TYPE_SUCCESS, "已保存")
                }
            }.setOnDismissListener {
                Glide.with(HostInfo.getApplication()).clear(preView)
            }.show()
    }

    // 商店表情保存
    fun preSaveMarketPicToList(mpath: String?, id: String, context: Context) {
        choicePath = null
        val inflater = LayoutInflater.from(context)
        val mRoot = inflater.inflate(R.layout.sticker_pre_save, null) as ScrollView
        val preView = mRoot.findViewById<ImageView>(R.id.emo_pre_container)
        preView.scaleType = ImageView.ScaleType.FIT_CENTER
        val newInfo = EmoInfo()
        newInfo.Path = mpath
        newInfo.type = 2
        newInfo.ID = id.uppercase()

        Glide.with(HostInfo.getApplication())
            .load(File(newInfo.Path))
            .fitCenter()
            .into(preView)

        val paths = LocalDataHelper.readPaths()

        val group = mRoot.findViewById<RadioGroup>(R.id.emo_pre_list_choser)
        for (path in paths) {
            val button = RadioButton(context)
            button.text = path.Name
            button.textSize = 16f
            button.setTextColor(context.resources.getColor(R.color.global_font_color, null))
            button.setOnCheckedChangeListener { v: CompoundButton, isCheck: Boolean ->
                if (v.isPressed && isCheck) {
                    choicePath = path
                }
            }
            group.addView(button)
        }


        val btnCreate = mRoot.findViewById<Button>(R.id.createNew)
        btnCreate.setOnClickListener {
            createNewFolder(context, group)
        }
        MaterialAlertDialogBuilder(context)
            .setTitle("是否保存如下图片")
            .setMessage("商店表情请注意版权问题")
            .setView(mRoot)
            .setNegativeButton(
                "保存"
            ) { _: DialogInterface?, _: Int ->
                if (choicePath == null) {
                    Toasts.show(context, Toasts.TYPE_INFO, "没有选择任何的保存列表")
                } else if (TextUtils.isEmpty(newInfo.Path)) {
                    Toasts.show(context, Toasts.TYPE_ERROR, "图片尚未加载完毕,保存失败")
                } else {
                    FileUtils.copy(
                        newInfo.Path,
                        Env.app_save_path + "本地表情包/" + choicePath?.storePath + "/market_" + newInfo.ID
                    )
                    val item = LocalPicItems()
                    item.id = newInfo.ID
                    item.fileName = "market_"+newInfo.ID
                    item.addTime = System.currentTimeMillis()
                    LocalDataHelper.addPicItem(choicePath?.storePath, item)


                    Toasts.show(context, Toasts.TYPE_SUCCESS, "已保存")
                }
            }.setOnDismissListener {
                Glide.with(HostInfo.getApplication()).clear(preView)
            }.show()
    }

    // 如果要保存的是多张图片则弹出MD5选择, 选择后才弹出确认图片保存框
    fun preSaveMultiPicList(url: ArrayList<String>, md5: ArrayList<String>, context: Context) {
        MaterialAlertDialogBuilder(context)
            .setTitle("选择需要保存的图片")
            .setItems(
                md5.toTypedArray<String>()
            ) { _: DialogInterface?, which: Int ->
                preSavePicToList(
                    url[which], md5[which], context
                )
            }.setOnDismissListener { }.show()
    }

    private fun createNewFolder(context: Context, group: RadioGroup) {
        MaterialDialog(context).show {
            title(text = "创建新目录")
            input(
                hint = "目录名称",
                prefill = "",
                waitForPositiveButton = false
            ) { dialog, _ ->
                val inputField = dialog.getInputField()
                dialog.setActionButtonEnabled(
                    WhichButton.POSITIVE,
                    inputField.text.isNotEmpty()
                )
            }.ignoreResult()
            positiveButton(text = "确定创建") {
                val newName = getInputField().text.toString()
                if (TextUtils.isEmpty(newName)) {
                    Toasts.show(context, Toasts.TYPE_INFO, "名字不能为空")
                    return@positiveButton
                }
                val path = LocalPath()
                path.Name = newName
                path.storePath = RandomUtils.getRandomString(16)
                LocalDataHelper.addPath(path)

                val allPaths = LocalDataHelper.readPaths()
                group.removeAllViews()
                //确认添加列表后会重新扫描列表并显示
                for (pathItem in allPaths) {
                    val button = RadioButton(context)
                    button.text = pathItem.Name
                    button.textSize = 16f
                    button.setTextColor(
                        context.resources.getColor(R.color.global_font_color, null)
                    )
                    button.setOnCheckedChangeListener { vaa: CompoundButton, isCheck: Boolean ->
                        if (vaa.isPressed && isCheck) {
                            choicePath = pathItem
                        }
                    }
                    group.addView(button)
                }
            }
            negativeButton(text = "取消") {}
        }
    }
}
