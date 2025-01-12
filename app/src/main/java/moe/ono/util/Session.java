package moe.ono.util;

import java.util.Objects;

import moe.ono.bridge.kernelcompat.ContactCompat;

public class Session {
    public static String cPeerUID;
    public static int cChatType = -1;

    private Session() {
        throw new AssertionError("No instance for you!");
    }


    public static String getCurrentPeerID() {
        if (cPeerUID == null) {
            throw new IllegalStateException("cPeerUID is null");
        }
        return cPeerUID;
    }
    public static int getCurrentChatType() {
        if (cChatType == -1) {
            throw new IllegalStateException("cChatType is null");
        }
        return cChatType;
    }

    public static void setCurrentPeerID(String cPeerUID) {
        Objects.requireNonNull(cPeerUID);
        Session.cPeerUID = cPeerUID;
    }
    public static void setCurrentChatType(int type) {
        Session.cChatType = type;
    }

    public static ContactCompat getContact() {
        try {
            ContactCompat contact = new ContactCompat();
            contact.setPeerUid(getCurrentPeerID());

            int chatType = getCurrentChatType();
            contact.setChatType(chatType);

//            if (chatType == 4){
//                contact.setGuildId(getCurrentGuildIDByAIOContact(AIOContact));
//            }
            return contact;

        } catch (Exception e){
            Logger.e("Session.getContact", e);
            return null;
        }

    }
}
