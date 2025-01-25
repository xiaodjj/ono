package moe.ono.creator.stickerPanel;

import android.annotation.SuppressLint;

import moe.ono.util.HostInfo;
@SuppressLint("SdCardPath")
public class Env {
    public static String app_save_path = "/sdcard/Android/data/" + HostInfo.getPackageName() + "/files/.tool/";
    // /storage/emulated/0/Android/data/com.tencent.mobileqq/Tencent/MobileQQ/.emotionsm/
    public static String app_path = "/sdcard/Android/data/" + HostInfo.getPackageName() + "/Tencent/MobileQQ/";
}
