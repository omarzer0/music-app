package com.azapps.musicplayer.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.azapps.musicplayer.R;
import com.azapps.musicplayer.ui.activity.DisplaySongsActivity;

import static com.azapps.musicplayer.pojo.Constant.ACTION_NAME;
import static com.azapps.musicplayer.pojo.Constant.ACTION_NEXT;
import static com.azapps.musicplayer.pojo.Constant.ACTION_PLAY;
import static com.azapps.musicplayer.pojo.Constant.ACTION_PREVIOUS;
import static com.azapps.musicplayer.pojo.Constant.MUSIC_BROADCAST_SEND_INTENT;

public class MusicPlayerFragment extends Fragment implements View.OnClickListener {


    private static final String TITLE_EXTRA = "title";
    private static final String ARTIST_EXTRA = "artist";
    private static final String DATA_EXTRA = "data";
    private static final String TOTAL_TIME_EXTRA = "total time";

    private ImageView songCoverImage;
    private Button playBtn, nextBtn, previousBtn;
    private SeekBar positionSeekBar;
    private TextView elapsedTimeLabel, totalTimeLabel, songTitleTV, songArtistTV;

    private Handler mSeekBarUpdateHandler;
    private Runnable mUpdateSeekBar;

    //    private Song song;
    private int currentPosition;
    private String songTitle;
    private String songData;
    private String songArtist;
    private int totalTime;

    public MusicPlayerFragment() {
    }

    public static Fragment newInstance(String title, String artist, String data, int duration) {
        MusicPlayerFragment fragment = new MusicPlayerFragment();
        Bundle args = new Bundle();
        args.putString(TITLE_EXTRA, title);
        args.putString(ARTIST_EXTRA, artist);
        args.putString(DATA_EXTRA, data);
        args.putInt(TOTAL_TIME_EXTRA, duration);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            songTitle = getArguments().getString(TITLE_EXTRA);
            songArtist = getArguments().getString(ARTIST_EXTRA);
            songData = getArguments().getString(DATA_EXTRA);
            totalTime = getArguments().getInt(TOTAL_TIME_EXTRA);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View view = inflater.inflate(R.layout.fragment_music_player, container, false);
        initViews(view);
        getSongExtra();
        prepareAndListenToPositionSeekBarChanges();
        updateSeekBar();
        play();

        return view;
    }


    private void initViews(View view) {
        playBtn = view.findViewById(R.id.fragment_music_player_playBtn);
        playBtn.setOnClickListener(this);
        nextBtn = view.findViewById(R.id.fragment_music_player_next);
        nextBtn.setOnClickListener(this);
        previousBtn = view.findViewById(R.id.fragment_music_player_previous);
        previousBtn.setOnClickListener(this);

        songCoverImage = view.findViewById(R.id.fragment_music_player_song_img_cover);
        elapsedTimeLabel = view.findViewById(R.id.fragment_music_player_elapsedTimeLabel);
        totalTimeLabel = view.findViewById(R.id.fragment_music_player_totalTimeLabel);
        positionSeekBar = view.findViewById(R.id.fragment_music_player_positionSeekBar);
        songTitleTV = view.findViewById(R.id.fragment_music_player_song_title);
        songArtistTV = view.findViewById(R.id.fragment_music_player_song_artist);
        setTotalTimeLabel_TimeElapsed_SongTitleAndSongArtist();

    }

    private void setTotalTimeLabel_TimeElapsed_SongTitleAndSongArtist() {
        currentPosition = ((DisplaySongsActivity) getActivity()).getCurrentSongPosition();
        totalTimeLabel.setText(createTimeLabel(totalTime));
        elapsedTimeLabel.setText(createTimeLabel(((DisplaySongsActivity) getActivity()).getCurrentSongPosition()));
        positionSeekBar.setMax(totalTime);
        positionSeekBar.setProgress(currentPosition);
        songTitleTV.setText(songTitle);
        songArtistTV.setText(songArtist);
    }

    private void prepareAndListenToPositionSeekBarChanges() {
        // position Bar
        positionSeekBar.setMax(totalTime);
        positionSeekBar.setPadding(0, 0, 0, 0);
        positionSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    positionSeekBar.setProgress(progress);
                    ((DisplaySongsActivity) getActivity()).setCurrentSongPosition(progress);
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
                currentPosition = ((DisplaySongsActivity) getActivity()).getCurrentSongPosition();
                positionSeekBar.setProgress(currentPosition);
                mSeekBarUpdateHandler.postDelayed(this, 50);
                String elapsedTime = createTimeLabel(currentPosition);
                elapsedTimeLabel.setText(elapsedTime);

            }
        };
    }

    private void getSongExtra() {
        try {
//            String data = song.getData();
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(songData);
            byte[] coverBytes = retriever.getEmbeddedPicture();
            Bitmap songCover;

            songCover = BitmapFactory.decodeByteArray(coverBytes, 0, coverBytes.length);
            songCoverImage.setImageBitmap(songCover);
        } catch (Exception e) {
            e.getMessage();
            songCoverImage.setImageResource(R.drawable.default_image);
        }
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
        if (((DisplaySongsActivity) getActivity()).getIsPlaying()) {
            // mediaPlayer is in pause state
            mSeekBarUpdateHandler.postDelayed(mUpdateSeekBar, 0);
            playBtn.setBackgroundResource(R.drawable.ic_pause);
        } else {
            // mediaPlayer is in play state
            playBtn.setBackgroundResource(R.drawable.ic_play_button);
        }
    }

    public void reversePlay() {
        if (((DisplaySongsActivity) getActivity()).getIsPlaying()) {
            // mediaPlayer is in pause state
            mSeekBarUpdateHandler.postDelayed(mUpdateSeekBar, 0);
            playBtn.setBackgroundResource(R.drawable.ic_play_button);
        } else {
            // mediaPlayer is in play state
            playBtn.setBackgroundResource(R.drawable.ic_pause);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_music_player_previous:
                ((DisplaySongsActivity) getActivity()).previousBtnClicked();
                reversePlay();
                getSongChanged();
                break;

            case R.id.fragment_music_player_playBtn:
                ((DisplaySongsActivity) getActivity()).playBtnClicked();
                play();
                break;

            case R.id.fragment_music_player_next:
                ((DisplaySongsActivity) getActivity()).nextBtnClicked();
                reversePlay();
                getSongChanged();
        }
    }


    private IntentFilter filter = new IntentFilter(MUSIC_BROADCAST_SEND_INTENT);
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getExtras().getString(ACTION_NAME);
            Log.e("TAG", "onReceive: " + action);
            switch (action) {

                case ACTION_PREVIOUS:
                    ((DisplaySongsActivity) getActivity()).previousBtnClicked();
                    reversePlay();
                    getSongChanged();
                    break;

                case ACTION_PLAY:
                    ((DisplaySongsActivity) getActivity()).playBtnClicked();
                    play();
                    break;

                case ACTION_NEXT:
                    ((DisplaySongsActivity) getActivity()).nextBtnClicked();
                    reversePlay();
                    getSongChanged();
                    break;


            }
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        getActivity().registerReceiver(broadcastReceiver, filter);
    }

    private void getSongChanged() {
        songData = ((DisplaySongsActivity) getActivity()).getSongData();
        songTitle = ((DisplaySongsActivity) getActivity()).getSongTitle();
        songArtist = ((DisplaySongsActivity) getActivity()).getSongArtist();
        totalTime = ((DisplaySongsActivity) getActivity()).getSongTotalTime();
        getSongExtra();
        setTotalTimeLabel_TimeElapsed_SongTitleAndSongArtist();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        getActivity().unregisterReceiver(broadcastReceiver);
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mSeekBarUpdateHandler.removeCallbacks(mUpdateSeekBar);
        ConstraintLayout constraintLayout = getActivity().findViewById(R.id.activity_display_songs_root_constraint);
        constraintLayout.setVisibility(View.VISIBLE);
    }
}