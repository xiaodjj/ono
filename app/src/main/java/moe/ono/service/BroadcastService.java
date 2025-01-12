package moe.ono.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import moe.ono.config.CacheConfig;
import moe.ono.util.Logger;

public class BroadcastService {
    public static void sendBroadcast(Context context, String action, String message) {
        Intent intent = new Intent(action);
        intent.putExtra("message", message);
        context.sendBroadcast(intent);
    }

    public static void registerReceiver(Context context, String action, BroadcastReceiver receiver) {
        if (CacheConfig.isReceiverRegistered(action)){
            return;
        }
        IntentFilter filter = new IntentFilter(action);
        context.registerReceiver(receiver, filter);
        CacheConfig.addReceiver(action);
        Logger.i("Receiver " + action + " registered.");
    }

    public static void unregisterReceiver(Context context, BroadcastReceiver receiver) {
        context.unregisterReceiver(receiver);
    }

}