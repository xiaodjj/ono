package moe.ono.hooks.message;

import com.tencent.qqnt.kernel.nativeinterface.MsgElement;

import java.util.ArrayList;

import moe.ono.bridge.Nt_kernel_bridge;
import moe.ono.bridge.kernelcompat.ContactCompat;
import moe.ono.builder.MsgBuilder;
import moe.ono.hooks.message.bridge.Chat_facade_bridge;
import moe.ono.util.QAppUtils;

public class MsgSender {
    public static void send_pic_by_contact(ContactCompat contact, String picPath){
        if (QAppUtils.isQQnt()){
            ArrayList<MsgElement> newMsgArr = new ArrayList<>();
            if (contact.getChatType() == 4){
                newMsgArr.add(MsgBuilder.nt_build_pic_guild(picPath));
            }else {
                newMsgArr.add(MsgBuilder.nt_build_sticker(picPath));
            }

            Nt_kernel_bridge.send_msg(contact,newMsgArr);
        } else {
            Chat_facade_bridge.sendPic(contact,picPath);
        }
    }
}
