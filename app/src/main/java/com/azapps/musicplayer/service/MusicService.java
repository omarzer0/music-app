package com.azapps.musicplayer.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import static com.azapps.musicplayer.pojo.Constant.SEND_IS_PLAYING_BOOLEAN_EXTRA;
import static com.azapps.musicplayer.pojo.Constant.SEND_SONG_DATA_STRING_EXTRA;
import static com.azapps.musicplayer.pojo.Constant.SEND_SONG_TITLE_STRING_EXTRA;
import static com.azapps.musicplayer.service.MusicNotification.createNotificationCompat;

public class MusicService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean isPlaying = intent.getBooleanExtra(SEND_IS_PLAYING_BOOLEAN_EXTRA, false);
        String songData = intent.getStringExtra(SEND_SONG_DATA_STRING_EXTRA);
        String songTitle = intent.getStringExtra(SEND_SONG_TITLE_STRING_EXTRA);
        NotificationCompat.Builder builder = createNotificationCompat(this, isPlaying, songTitle, songData);
        startForeground(1, builder.build());

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
    }
}
