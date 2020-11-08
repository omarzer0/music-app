package com.azapps.musicplayer.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.azapps.musicplayer.R;
import com.azapps.musicplayer.ui.activity.HomeActivity;
import com.mikhaellopez.circularimageview.CircularImageView;

import static com.azapps.musicplayer.pojo.Constant.ACTION_CLOSE;
import static com.azapps.musicplayer.pojo.Constant.ACTION_NAME;
import static com.azapps.musicplayer.pojo.Constant.ACTION_NEXT;
import static com.azapps.musicplayer.pojo.Constant.ACTION_PLAY;
import static com.azapps.musicplayer.pojo.Constant.ACTION_PREVIOUS;
import static com.azapps.musicplayer.pojo.Constant.BLUETOOTH_NEXT;
import static com.azapps.musicplayer.pojo.Constant.BLUETOOTH_PLAY;
import static com.azapps.musicplayer.pojo.Constant.BLUETOOTH_PREVIOUS;
import static com.azapps.musicplayer.pojo.Constant.BROADCAST_BLUETOOTH_HEADPHONE_INTENT;
import static com.azapps.musicplayer.pojo.Constant.HEADPHONE_BLUETOOTH_EXTRA;
import static com.azapps.musicplayer.pojo.Constant.MUSIC_BROADCAST_SEND_INTENT;

public class MusicPlayerFragment extends Fragment implements View.OnClickListener {


    private static final String TITLE_EXTRA = "title";
    private static final String ARTIST_EXTRA = "artist";
    private static final String DATA_EXTRA = "data";
    private static final String TOTAL_TIME_EXTRA = "total time";
    private static final String SONG_COVER_EXTRA = "song cover extra";

    private CircularImageView songCoverImage;
    private Button nextBtn, previousBtn;
    private ImageButton playBtn;
    private SeekBar positionSeekBar;
    private TextView elapsedTimeLabel, totalTimeLabel, songTitleTV, songArtistTV;
    private ImageView repeatBtn;

    private Handler mSeekBarUpdateHandler;
    private Runnable mUpdateSeekBar;

    //    private Song song;
    private int currentPosition;
    private String songTitle;
    private String songData;
    private String songArtist;
    private int totalTime;
    private String cover;
    private boolean isFirstTime = true;

    public MusicPlayerFragment() {
    }

    public static Fragment newInstance(String title, String artist, String data, int duration, String cover) {
        MusicPlayerFragment fragment = new MusicPlayerFragment();
        Bundle args = new Bundle();
        args.putString(TITLE_EXTRA, title);
        args.putString(ARTIST_EXTRA, artist);
        args.putString(DATA_EXTRA, data);
        args.putInt(TOTAL_TIME_EXTRA, duration);
        args.putString(SONG_COVER_EXTRA, cover);
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
            cover = getArguments().getString(SONG_COVER_EXTRA);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View view = inflater.inflate(R.layout.fragment_music_player, container, false);
        initViews(view);
        try {
            getSongExtra();
            getIsLooping();
        } catch (Exception e) {
            Toast.makeText(getActivity(), "getSongExtra Error\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        prepareAndListenToPositionSeekBarChanges();
        updateSeekBar();
        try {
            play();
        } catch (Exception e) {
            Toast.makeText(getActivity(), "play error\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return view;
    }


    private void initViews(View view) {
        playBtn = view.findViewById(R.id.fragment_music_player_playBtn);
        playBtn.setOnClickListener(this);
        nextBtn = view.findViewById(R.id.fragment_music_player_next);
        nextBtn.setOnClickListener(this);
        previousBtn = view.findViewById(R.id.fragment_music_player_previous);
        previousBtn.setOnClickListener(this);
        repeatBtn = view.findViewById(R.id.fragment_music_player_img_repeat_image_view);
        repeatBtn.setOnClickListener(this);

        songCoverImage = view.findViewById(R.id.fragment_music_player_song_img_cover);
        elapsedTimeLabel = view.findViewById(R.id.fragment_music_player_elapsedTimeLabel);
        totalTimeLabel = view.findViewById(R.id.fragment_music_player_totalTimeLabel);
        positionSeekBar = view.findViewById(R.id.fragment_music_player_positionSeekBar);
        songTitleTV = view.findViewById(R.id.fragment_music_player_song_title);
        songArtistTV = view.findViewById(R.id.fragment_music_player_song_artist);
        setTotalTimeLabel_TimeElapsed_SongTitleAndSongArtist();

    }

    private void setTotalTimeLabel_TimeElapsed_SongTitleAndSongArtist() {
        currentPosition = ((HomeActivity) getActivity()).getCurrentSongPosition();
        totalTimeLabel.setText(createTimeLabel(totalTime));
        elapsedTimeLabel.setText(createTimeLabel(((HomeActivity) getActivity()).getCurrentSongPosition()));
        positionSeekBar.setMax(totalTime);
        positionSeekBar.setProgress(currentPosition);
        songTitleTV.setText(songTitle);
        songArtistTV.setText(songArtist);
    }

    private void prepareAndListenToPositionSeekBarChanges() {
        // position Bar
        positionSeekBar.setMax(totalTime);
        positionSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    positionSeekBar.setProgress(progress);
                    ((HomeActivity) getActivity()).setCurrentSongPosition(progress);
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
                try {
                    currentPosition = ((HomeActivity) getActivity()).getCurrentSongPosition();
                    positionSeekBar.setProgress(currentPosition);
                    mSeekBarUpdateHandler.postDelayed(this, 50);
                    String elapsedTime = createTimeLabel(currentPosition);
                    elapsedTimeLabel.setText(elapsedTime);
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "updateSeekBar\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        };
    }

    private void getSongExtra() {
        Uri uri = Uri.parse(cover);
        // do not use glide here because it first loads the place holder then loads the image
        // with glide the user will see the place holder first
        songCoverImage.setImageURI(uri);
        if (songCoverImage.getDrawable() == null)
            songCoverImage.setImageResource(R.drawable.default_image);
    }

    private void getIsLooping() {
        if (((HomeActivity) getActivity()).getLoopingState()) {
            repeatBtn.setImageResource(R.drawable.ic_repeat_once);
        } else {
            repeatBtn.setImageResource(R.drawable.ic_repeat);
        }
    }

    private String createTimeLabel(int time) {
        String label;
        int min = time / 1000 / 60;
        int sec = time / 1000 % 60;
        label = min + ":";
        if (sec < 10) label += "0";
        label += sec;

        return label;
    }

    public void play() {
        if (((HomeActivity) getActivity()).getIsPlaying()) {
            // mediaPlayer is in pause state
            mSeekBarUpdateHandler.postDelayed(mUpdateSeekBar, 0);
            playBtn.setImageResource(R.drawable.ic_pause);
        } else {
            // mediaPlayer is in play state
            playBtn.setImageResource(R.drawable.ic_play_button);
        }
    }

    public void reversePlay() {
        if (((HomeActivity) getActivity()).getIsPlaying()) {
            // mediaPlayer is in pause state
            mSeekBarUpdateHandler.postDelayed(mUpdateSeekBar, 0);
            playBtn.setImageResource(R.drawable.ic_play_button);
        } else {
            // mediaPlayer is in play state
            playBtn.setImageResource(R.drawable.ic_pause);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_music_player_previous:
                ((HomeActivity) getActivity()).previousBtnClicked();
                reversePlay();
                getSongChanged();
                break;

            case R.id.fragment_music_player_playBtn:
                ((HomeActivity) getActivity()).playBtnClicked();
                play();
                break;

            case R.id.fragment_music_player_next:
                ((HomeActivity) getActivity()).nextBtnClicked();
                reversePlay();
                getSongChanged();
                break;

            case R.id.fragment_music_player_img_repeat_image_view:
                if (((HomeActivity) getActivity()).getLoopingState()) { // looping is true make it false
                    ((HomeActivity) getActivity()).setLoopingState(false);
                    repeatBtn.setImageResource(R.drawable.ic_repeat);
                    Toast.makeText(getActivity(), R.string.play_all_songs_in_the_list, Toast.LENGTH_SHORT).show();
                } else {
                    ((HomeActivity) getActivity()).setLoopingState(true); // looping is false make it true
                    repeatBtn.setImageResource(R.drawable.ic_repeat_once);
                    Toast.makeText(getActivity(), R.string.repeat_the_current_song, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    private BroadcastReceiver serviceClicksBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getExtras().getString(ACTION_NAME);
            Log.e("TAG", "onReceive: " + action);
            switch (action) {

                case ACTION_PREVIOUS:
                    ((HomeActivity) getActivity()).previousBtnClicked();
                    reversePlay();
                    getSongChanged();
                    break;

                case ACTION_PLAY:
                    ((HomeActivity) getActivity()).playBtnClicked();
                    play();
                    break;

                case ACTION_NEXT:
                    ((HomeActivity) getActivity()).nextBtnClicked();
                    reversePlay();
                    getSongChanged();
                    break;

                case ACTION_CLOSE:
                    ((HomeActivity) getActivity()).closeBtnClicked();
                    play();
                    break;
            }
        }
    };


    private BroadcastReceiver headPhoneBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
//                if (state == 0) {
//                    if (!isFirstTime) {
//                        ((HomeActivity) getActivity()).playBtnClicked();
//                        play();
//                    } else {
//                        isFirstTime = false;
//                    }
//                }
            }
        }
    };


    private BroadcastReceiver bluetoothHeadPhonesBroadCastReceivers = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String code = intent.getStringExtra(HEADPHONE_BLUETOOTH_EXTRA);
            if ((code == null)) return;
            switch (code) {
                case BLUETOOTH_PLAY:
                    ((HomeActivity) getActivity()).playBtnClicked();
                    play();
                    break;
                case BLUETOOTH_NEXT:
                    ((HomeActivity) getActivity()).nextBtnClicked();
                    reversePlay();
                    getSongChanged();
                    break;
                case BLUETOOTH_PREVIOUS:
                    ((HomeActivity) getActivity()).previousBtnClicked();
                    reversePlay();
                    getSongChanged();
                    break;
            }
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        try {
            getActivity().registerReceiver(serviceClicksBroadcastReceiver, new IntentFilter(MUSIC_BROADCAST_SEND_INTENT));
            getActivity().registerReceiver(headPhoneBroadCastReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
            getActivity().registerReceiver(bluetoothHeadPhonesBroadCastReceivers, new IntentFilter(BROADCAST_BLUETOOTH_HEADPHONE_INTENT));
        } catch (Exception e) {
            Toast.makeText(getActivity(), "onStart\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void getSongChanged() {
        songData = ((HomeActivity) getActivity()).getSongData();
        songTitle = ((HomeActivity) getActivity()).getSongTitle();
        songArtist = ((HomeActivity) getActivity()).getSongArtist();
        totalTime = ((HomeActivity) getActivity()).getSongTotalTime();
        cover = ((HomeActivity) getActivity()).getSongCover();
        getSongExtra();
        setTotalTimeLabel_TimeElapsed_SongTitleAndSongArtist();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        try {
            getActivity().unregisterReceiver(serviceClicksBroadcastReceiver);
            getActivity().unregisterReceiver(headPhoneBroadCastReceiver);
            getActivity().unregisterReceiver(bluetoothHeadPhonesBroadCastReceivers);
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            mSeekBarUpdateHandler.removeCallbacks(mUpdateSeekBar);
            ConstraintLayout constraintLayout = getActivity().findViewById(R.id.fragment_display_songs_root_constraint_found);
            constraintLayout.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.getMessage();
        }
    }
}