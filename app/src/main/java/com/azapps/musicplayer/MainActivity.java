package com.azapps.musicplayer;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private ImageView playBtn;
    private SeekBar positionSeekBar, volumeSeekBar;
    private TextView elapsedTimeLabel, remainingTimeLabel;
    private MediaPlayer mp;
    private int totalTime;

    private Handler mSeekBarUpdateHandler;
    private Runnable mUpdateSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playBtn = findViewById(R.id.playBtn);


        // Media Player
        mp = MediaPlayer.create(this, R.raw.sound);
        mp.setLooping(true);
        mp.seekTo(0);
        mp.setVolume(0.5f, 0.5f);
        totalTime = mp.getDuration();


        // timers
        elapsedTimeLabel = findViewById(R.id.elapsedTimeLabel);
        elapsedTimeLabel.setText(R.string.zero_start_time);

        remainingTimeLabel = findViewById(R.id.remainingTimeLabel);
        remainingTimeLabel.setText(createTimeLabel(totalTime));

        // position Bar
        positionSeekBar = findViewById(R.id.positionSeekBar);
        positionSeekBar.setMax(totalTime);
        positionSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    positionSeekBar.setProgress(progress);
                    mp.seekTo(progress);
                    elapsedTimeLabel.setText(createTimeLabel(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // volume Bar
        volumeSeekBar = findViewById(R.id.volumeSeekBar);
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float volumeNum = progress / 100f;
                mp.setVolume(volumeNum, volumeNum);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        mSeekBarUpdateHandler = new Handler();
        mUpdateSeekBar = new Runnable() {
            @Override
            public void run() {
                int currentPosition = mp.getCurrentPosition();
                positionSeekBar.setProgress(currentPosition);
                mSeekBarUpdateHandler.postDelayed(this,50);

                String elapsedTime = createTimeLabel(currentPosition);
                elapsedTimeLabel.setText(elapsedTime);

            }
        };
    }

    private String createTimeLabel(int time) {
        String label = "";
        int min = time / 1000 / 60;
        int sec = time / 1000 % 60;
        label = min +":";
        if (sec <10) label +="0";
        label +=sec;

        return label;
    }


    public void play(View view) {
        if (!mp.isPlaying()) {
            // mediaPlayer is in pause state
            mp.start();
            mSeekBarUpdateHandler.postDelayed(mUpdateSeekBar, 0);
            playBtn.setImageResource(R.drawable.ic_pause);
        } else {
            // mediaPlayer is in play state
            mp.pause();
            playBtn.setImageResource(R.drawable.ic_play_button);
        }
    }
}