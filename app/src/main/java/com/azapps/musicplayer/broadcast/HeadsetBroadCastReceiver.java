package com.azapps.musicplayer.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static com.azapps.musicplayer.pojo.Constant.BROADCAST_HEADPHONE_INTENT;
import static com.azapps.musicplayer.pojo.Constant.HEADPHONE_ACTION;


public class HeadsetBroadCastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.sendBroadcast(new Intent(BROADCAST_HEADPHONE_INTENT).putExtra(HEADPHONE_ACTION, intent.getAction()));
    }
}