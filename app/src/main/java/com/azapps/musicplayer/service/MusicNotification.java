package com.azapps.musicplayer.service;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.support.v4.media.session.MediaSessionCompat;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.azapps.musicplayer.R;
import com.azapps.musicplayer.broadcast.MusicBroadcast;
import com.azapps.musicplayer.ui.activity.HomeActivity;

import static com.azapps.musicplayer.pojo.Constant.ACTION_CLOSE;
import static com.azapps.musicplayer.pojo.Constant.ACTION_NEXT;
import static com.azapps.musicplayer.pojo.Constant.ACTION_PLAY;
import static com.azapps.musicplayer.pojo.Constant.ACTION_PREVIOUS;
import static com.azapps.musicplayer.pojo.Constant.CHANNEL_ID;


public class MusicNotification {

    public static NotificationCompat.Builder createNotificationCompat(Context context, boolean isPlaying, String title, String imageData) {
        Intent notificationIntent = new Intent(context, HomeActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        Intent broadcastIntentPrevious = new Intent(context, MusicBroadcast.class);
        broadcastIntentPrevious.setAction(ACTION_PREVIOUS);
        PendingIntent actionIntentPrevious = PendingIntent.getBroadcast(context, 0, broadcastIntentPrevious, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent broadcastIntentPlay = new Intent(context, MusicBroadcast.class);
        broadcastIntentPlay.setAction(ACTION_PLAY);
        PendingIntent actionIntentPlay = PendingIntent.getBroadcast(context, 0, broadcastIntentPlay, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent broadcastIntentNext = new Intent(context, MusicBroadcast.class);
        broadcastIntentNext.setAction(ACTION_NEXT);
        PendingIntent actionIntentNext = PendingIntent.getBroadcast(context, 0, broadcastIntentNext, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent broadcastIntentClose = new Intent(context, MusicBroadcast.class);
        broadcastIntentClose.setAction(ACTION_CLOSE);
        PendingIntent actionIntentClose = PendingIntent.getBroadcast(context, 0, broadcastIntentClose, PendingIntent.FLAG_UPDATE_CURRENT);



        RemoteViews collapsedView = new RemoteViews(context.getPackageName(), R.layout.notification_collapsed);
        MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(context, "media session");
        if (isPlaying) {
            collapsedView.setImageViewResource(R.id.notification_collapsed_play_btn, R.drawable.ic_play_button);
        } else {
            collapsedView.setImageViewResource(R.id.notification_collapsed_play_btn, R.drawable.ic_pause);
        }
        collapsedView.setTextViewText(R.id.notification_collapsed_song_title_tv, title);

        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(imageData);
            byte[] coverBytes = retriever.getEmbeddedPicture();
            Bitmap songCover;

            songCover = BitmapFactory.decodeByteArray(coverBytes, 0, coverBytes.length);
            collapsedView.setImageViewBitmap(R.id.notification_collapsed_song_image_image_view, songCover);
        } catch (Exception e) {
            e.getMessage();
            collapsedView.setImageViewResource(R.id.notification_collapsed_song_image_image_view, R.drawable.default_image);
        }

        collapsedView.setOnClickPendingIntent(R.id.notification_collapsed_previous_btn, actionIntentPrevious);
        collapsedView.setOnClickPendingIntent(R.id.notification_collapsed_play_btn, actionIntentPlay);
        collapsedView.setOnClickPendingIntent(R.id.notification_collapsed_next_btn, actionIntentNext);
        collapsedView.setOnClickPendingIntent(R.id.notification_collapsed_close_btn, actionIntentClose);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setCustomContentView(collapsedView)
                .setCustomBigContentView(collapsedView)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())

                .setNotificationSilent()
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentTitle("Music Service")
                .setSmallIcon(R.mipmap.ic_launcher_foreground);
//                .setContentIntent(pendingIntent);

        return builder;
    }


}
