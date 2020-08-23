package com.azapps.musicplayer.ui;

import android.content.Context;
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

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.azapps.musicplayer.R;
import com.azapps.musicplayer.pojo.Song;

public class MusicPlayerFragment extends Fragment {


    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private ImageView songCoverImage;
    private Button playBtn;
    private SeekBar positionSeekBar;
    private TextView elapsedTimeLabel, totalTimeLabel;
    private int totalTime;

    private Handler mSeekBarUpdateHandler;
    private Runnable mUpdateSeekBar;

    private Song song;
    private int currentPosition;

    public MusicPlayerFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {

        super.onAttach(context);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public static MusicPlayerFragment newInstance(Song song, int totalTime) {
        MusicPlayerFragment fragment = new MusicPlayerFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, song);
        args.putInt(ARG_PARAM2, totalTime);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            song = (Song) getArguments().getSerializable(ARG_PARAM1);
            totalTime = getArguments().getInt(ARG_PARAM2);
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

//    private void initMediaPlayer(String sUri) {
//        Uri uri = Uri.parse(sUri);
//        mp = MediaPlayer.create(this, uri);
//        mp.setLooping(true);
//        mp.seekTo(0);
//        mp.setVolume(1.0f, 1.0f);
//        totalTime = mp.getDuration();

//        remainingTimeLabel.setText(createTimeLabel(totalTime));
//
//        prepareAndListenToPositionSeekBarChanges();
//    }

    private void prepareAndListenToPositionSeekBarChanges() {
        // position Bar
        positionSeekBar.setMax(totalTime);
        positionSeekBar.setPadding(0, 0, 0, 0);
        positionSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    positionSeekBar.setProgress(progress);
                    ((DisplaySongsActivity)getActivity()).setCurrentSongPosition(progress);
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
                int currentPosition = ((DisplaySongsActivity) getActivity()).getCurrentSongPosition();
                positionSeekBar.setProgress(currentPosition);
                Log.e("TAG", "run: "+ currentPosition );
//                ((DisplaySongsActivity)getActivity()).setCurrentSongPosition(progress);
                mSeekBarUpdateHandler.postDelayed(this, 50);

                String elapsedTime = createTimeLabel(currentPosition);
                elapsedTimeLabel.setText(elapsedTime);

            }
        };
    }

    private void initViews(View view) {
        playBtn = view.findViewById(R.id.fragment_music_player_playBtn);
        songCoverImage = view.findViewById(R.id.fragment_music_player_song_img_cover);
        elapsedTimeLabel = view.findViewById(R.id.fragment_music_player_elapsedTimeLabel);
        elapsedTimeLabel.setText(R.string.zero_start_time);
        totalTimeLabel = view.findViewById(R.id.fragment_music_player_totalTimeLabel);
        totalTimeLabel.setText(createTimeLabel(totalTime));

        positionSeekBar = view.findViewById(R.id.fragment_music_player_positionSeekBar);
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((DisplaySongsActivity)getActivity()).playBtnClicked();
                play();
            }
        });
    }

    private void getSongExtra() {

        try {
            String data = song.getData();
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(data);
            byte[] coverBytes = retriever.getEmbeddedPicture();
            Bitmap songCover;

            songCover = BitmapFactory.decodeByteArray(coverBytes, 0, coverBytes.length);
            songCoverImage.setImageBitmap(songCover);
        } catch (Exception e) {
            e.getMessage();
            songCoverImage.setImageResource(R.drawable.song_not_found_background_image);
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
        if (((DisplaySongsActivity)getActivity()).getIsPlaying()) {
            // mediaPlayer is in pause state
            mSeekBarUpdateHandler.postDelayed(mUpdateSeekBar, 0);
            playBtn.setBackgroundResource(R.drawable.ic_pause);
        } else {
            // mediaPlayer is in play state
            playBtn.setBackgroundResource(R.drawable.ic_play_button);
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mSeekBarUpdateHandler.removeCallbacks(mUpdateSeekBar);
        ConstraintLayout constraintLayout = getActivity().findViewById(R.id.activity_display_songs_root_constraint);
        constraintLayout.setVisibility(View.VISIBLE);
    }
}