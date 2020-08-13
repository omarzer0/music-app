package com.azapps.musicplayer.ui;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.azapps.musicplayer.R;
import com.azapps.musicplayer.pojo.Song;

import static com.azapps.musicplayer.pojo.Constant.SEND_CLICKED_SONG_TO_MUSIC_ACTIVITY;

public class MusicPlayerActivity extends AppCompatActivity {

    private ImageView playBtn;
    private SeekBar positionSeekBar;
    private TextView elapsedTimeLabel, remainingTimeLabel;
    private MediaPlayer mp;
    private int totalTime;

    private Handler mSeekBarUpdateHandler;
    private Runnable mUpdateSeekBar;
    private Song song;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        initViews();
        getSongExtra();
        initMediaPlayer(song.getData());
        updateSeekBar();
        play();

    }

    private void initMediaPlayer(String sUri) {
        Uri uri = Uri.parse(sUri);
        mp = MediaPlayer.create(this, uri);
        mp.setLooping(true);
        mp.seekTo(0);
        mp.setVolume(1.0f, 1.0f);
        totalTime = mp.getDuration();
        elapsedTimeLabel.setText(R.string.zero_start_time);
        remainingTimeLabel.setText(createTimeLabel(totalTime));

        prepareAndListenToPositionSeekBarChanges();
    }

    private void prepareAndListenToPositionSeekBarChanges() {
        // position Bar
        positionSeekBar.setMax(totalTime);
        positionSeekBar.setPadding(0,0,0,0);
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
    }

    private void updateSeekBar() {
        mSeekBarUpdateHandler = new Handler();
        mUpdateSeekBar = new Runnable() {
            @Override
            public void run() {
                int currentPosition = mp.getCurrentPosition();
                positionSeekBar.setProgress(currentPosition);
                mSeekBarUpdateHandler.postDelayed(this, 50);

                String elapsedTime = createTimeLabel(currentPosition);
                elapsedTimeLabel.setText(elapsedTime);

            }
        };
    }

    private void initViews() {
        playBtn = findViewById(R.id.playBtn);
        elapsedTimeLabel = findViewById(R.id.elapsedTimeLabel);
        remainingTimeLabel = findViewById(R.id.remainingTimeLabel);
        positionSeekBar = findViewById(R.id.positionSeekBar);
        playBtn = findViewById(R.id.playBtn);
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play();
            }
        });
    }

    private void getSongExtra() {
        song = (Song) getIntent().getSerializableExtra(SEND_CLICKED_SONG_TO_MUSIC_ACTIVITY);
    }

    private String createTimeLabel(int time) {
        String label = "";
        int min = time / 1000 / 60;
        int sec = time / 1000 % 60;
        label = min + ":";
        if (sec < 10) label += "0";
        label += sec;

        return label;
    }

    public void play() {
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