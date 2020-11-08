package com.azapps.musicplayer.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import static com.azapps.musicplayer.pojo.Constant.BLUETOOTH_NEXT;
import static com.azapps.musicplayer.pojo.Constant.BLUETOOTH_PLAY;
import static com.azapps.musicplayer.pojo.Constant.BLUETOOTH_PREVIOUS;
import static com.azapps.musicplayer.pojo.Constant.BROADCAST_BLUETOOTH_HEADPHONE_INTENT;
import static com.azapps.musicplayer.pojo.Constant.HEADPHONE_BLUETOOTH_EXTRA;

public class MediaButtonIntentReceiver extends BroadcastReceiver {

    public MediaButtonIntentReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String intentAction = intent.getAction();
        if (!Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
            return;
        }
        KeyEvent event = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        if (event == null) {
            return;
        }
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            String sendText = "";
            int action = event.getKeyCode();
            switch (action) {
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                case KeyEvent.KEYCODE_HEADSETHOOK:
                    sendText = BLUETOOTH_PLAY;
                    break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    sendText = BLUETOOTH_NEXT;
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    sendText = BLUETOOTH_PREVIOUS;
                    break;
            }
            Intent sendIntent = new Intent(BROADCAST_BLUETOOTH_HEADPHONE_INTENT);
            sendIntent.putExtra(HEADPHONE_BLUETOOTH_EXTRA, sendText);
            context.sendBroadcast(sendIntent);
            abortBroadcast();
        }
    }
}