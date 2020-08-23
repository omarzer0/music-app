package com.azapps.musicplayer.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static com.azapps.musicplayer.pojo.Constant.ACTION_NAME;
import static com.azapps.musicplayer.pojo.Constant.MUSIC_BROADCAST_SEND_INTENT;

public class MusicBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.sendBroadcast(new Intent(MUSIC_BROADCAST_SEND_INTENT).putExtra(ACTION_NAME, intent.getAction()));
    }
}
