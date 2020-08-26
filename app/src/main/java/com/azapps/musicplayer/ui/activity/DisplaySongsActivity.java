package com.azapps.musicplayer.ui.activity;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.azapps.musicplayer.R;
import com.azapps.musicplayer.adapter.OnSongClickListener;
import com.azapps.musicplayer.adapter.SongAdapter;
import com.azapps.musicplayer.pojo.Song;
import com.azapps.musicplayer.pojo.Utils;
import com.azapps.musicplayer.service.MusicService;
import com.azapps.musicplayer.ui.fragment.MoreBottomSheetDialog;
import com.azapps.musicplayer.ui.fragment.MusicPlayerFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.azapps.musicplayer.pojo.Constant.ACTION_NAME;
import static com.azapps.musicplayer.pojo.Constant.ACTION_NEXT;
import static com.azapps.musicplayer.pojo.Constant.ACTION_PLAY;
import static com.azapps.musicplayer.pojo.Constant.ACTION_PREVIOUS;
import static com.azapps.musicplayer.pojo.Constant.ADDED_TIME_ORDER;
import static com.azapps.musicplayer.pojo.Constant.ALPHA_ORDER;
import static com.azapps.musicplayer.pojo.Constant.FRAGMENT_TAG;
import static com.azapps.musicplayer.pojo.Constant.MORE_BOTTOM_SHEET_TAG;
import static com.azapps.musicplayer.pojo.Constant.MUSIC_BROADCAST_SEND_INTENT;
import static com.azapps.musicplayer.pojo.Constant.SEND_IS_PLAYING_BOOLEAN_EXTRA;
import static com.azapps.musicplayer.pojo.Constant.SEND_SONG_DATA_STRING_EXTRA;
import static com.azapps.musicplayer.pojo.Constant.SEND_SONG_TITLE_STRING_EXTRA;

public class DisplaySongsActivity extends AppCompatActivity implements OnSongClickListener, View.OnClickListener,
        OnAudioFocusChangeListener, OnPreparedListener, OnCompletionListener {

    private static final String TAG = "DisplaySongsActivity";
    // ui
    private ArrayList<Song> songList;
    private ArrayList<Song> filteredArrayList;
    private SongAdapter adapter;
    private SongViewModel songViewModel;

    private Button previousBtn, playBtn, nextBtn, moreOptionsBtn;
    private EditText searchEditText;
    private ImageView nowPlayingImageView;
    private TextView nowPlayingTextView;
    private ConstraintLayout bottomControlConstraintLayout;
    private MediaPlayer mp;
    private AudioManager audioManager;

    // vars
    private Song song;
    private int currentSongClickedPosition = -1;
    private boolean isLooping = false;
    private static int orderOfAudioFiles = ADDED_TIME_ORDER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_songs);

        initViews();
//        prepareMoreOptionImg();
        setRecyclerView();
        modelViewInstantiate();
        initEditTextSearchFunction();
    }

    private void initViews() {
        previousBtn = findViewById(R.id.activity_display_songs_previous_btn);
        playBtn = findViewById(R.id.activity_display_songs_play_btn);
        nextBtn = findViewById(R.id.activity_display_songs_next_btn);
        moreOptionsBtn = findViewById(R.id.activity_display_songs_btn_more_options);
        searchEditText = findViewById(R.id.activity_display_songs_ed_search_edit_text);
        nowPlayingImageView = findViewById(R.id.activity_display_songs_now_playing_song_image_view);
        nowPlayingTextView = findViewById(R.id.activity_display_songs_now_playing_song_title);
        bottomControlConstraintLayout = findViewById(R.id.activity_display_songs_constraint_layout_bottom_play_control);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        setListeners();
    }

    private void setListeners() {
        previousBtn.setOnClickListener(this);
        playBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
        bottomControlConstraintLayout.setOnClickListener(this);
        moreOptionsBtn.setOnClickListener(this);
    }

    private void modelViewInstantiate() {
        songViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication()))
                .get(SongViewModel.class);
        LiveData<List<Song>> listLiveDataSongs = null;
        if (orderOfAudioFiles == ADDED_TIME_ORDER) {
            songViewModel.viewModelOrder = ADDED_TIME_ORDER;
            listLiveDataSongs = songViewModel.getAllSongsByAddedOrder();
            Log.e(TAG, "modelViewInstantiate: " + 1);
        } else if (orderOfAudioFiles == ALPHA_ORDER) {
            songViewModel.viewModelOrder = ALPHA_ORDER;
            listLiveDataSongs = songViewModel.getAllSongsByAlphaOrder();
            Log.e(TAG, "modelViewInstantiate: " + 2);
        }

        // switch on order by function
        if (songListObserver != null) {
            listLiveDataSongs.removeObserver(songListObserver);
        }
        listLiveDataSongs.observe(this, songListObserver);
    }

    private Observer<List<Song>> songListObserver = new Observer<List<Song>>() {
        @Override
        public void onChanged(List<Song> songs) {
            songList = new ArrayList<>();
            songList.addAll(songs);
            adapter.submitList(songs);
            Log.e(TAG, "onChanged: " + orderOfAudioFiles);
        }
    };

    private void getMusic() {
        ContentResolver contentResolver = getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " DESC";
        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String year = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.YEAR));
                long lastDateModified = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED));
                long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
                // Save to audioList
                Song song = new Song(title, displayName, artist, album, data, year, lastDateModified, size);
//                songList.add(song);
                songViewModel.insert(song);

            }
        }
        cursor.close();
        adapter.submitList(songList);
    }

    private void setRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.activity_display_songs_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.hasFixedSize();
        recyclerView.setItemAnimator(null);
        adapter = new SongAdapter(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onSongClick(int position) {
        currentSongClickedPosition = position;
        song = detectFromWhichList(position);
        if (checkOnAudioFocus()) {
            playMusic(song);
        }
    }

    private Song detectFromWhichList(int position) {
        Song song;
        if (filteredArrayList == null) {
            song = songList.get(position);
        } else {
            song = filteredArrayList.get(position);
        }
        return song;
    }

//    private void prepareMoreOptionImg() {
//        Button moreOptionsImg = findViewById(R.id.activity_display_songs_img_more_options);
//        moreOptionsImg.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                freeDateBase();
//                try {
//                    getMusic();
//                    if (songList.size() == 0)
//                        Toast.makeText(DisplaySongsActivity.this, "oooops! no music was found", Toast.LENGTH_SHORT).show();
//                } catch (Exception e) {
//                    Log.e("error", "onCreate: " + e.getMessage());
//                    Toast.makeText(DisplaySongsActivity.this, "Error can't load any song", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//    }

    private void freeDateBase() {
        try {
            songViewModel.deleteAllSongs();
        } catch (Exception e) {
            e.getMessage();
        }
    }

    private void initEditTextSearchFunction() {

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filterItems(s.toString());
            }
        });
    }

    private void filterItems(String text) {
        filteredArrayList = new ArrayList<>();


        for (Song song : songList) {
            if (song.getTitle().toLowerCase().contains(text.toLowerCase())) {
                filteredArrayList.add(song);
            }
        }

        adapter.submitList(filteredArrayList);
    }

    private void setImageToPlayerControl(String data) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(data);
            byte[] coverBytes = retriever.getEmbeddedPicture();
            Bitmap songCover;

            songCover = BitmapFactory.decodeByteArray(coverBytes, 0, coverBytes.length);
            nowPlayingImageView.setImageBitmap(songCover);
        } catch (Exception e) {
            e.getMessage();
            nowPlayingImageView.setImageResource(R.drawable.default_image);
        }
    }

    private void setTitleToPlayerControl(String songTitle) {
        nowPlayingTextView.setText(songTitle);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_display_songs_previous_btn:
                previousBtnClicked();
                break;
            case R.id.activity_display_songs_play_btn:
                playBtnClicked();
                break;
            case R.id.activity_display_songs_next_btn:
                nextBtnClicked();
                break;
            case R.id.activity_display_songs_constraint_layout_bottom_play_control:
                controlBodyClicked();
                break;
            case R.id.activity_display_songs_btn_more_options:
                moreOptionsBtnClicked();
                break;
        }
    }

    private void moreOptionsBtnClicked() {
        MoreBottomSheetDialog moreBottomSheetDialog = new MoreBottomSheetDialog();
        moreBottomSheetDialog.show(getSupportFragmentManager(), MORE_BOTTOM_SHEET_TAG);
    }

    private void initMediaPlayer() {
        //isLooping = true;
        // here we will change it later when ask the user to go on or repeat
        mp = new MediaPlayer();
        mp.setLooping(isLooping);
        mp.seekTo(0);
        mp.setVolume(1.0f, 1.0f);


    }

    private void playMusic(Song song) {
        releaseMediaPlayer();
        String songData = song.getData();
        String songTitle = song.getTitle();
        setImageToPlayerControl(songData);
        setTitleToPlayerControl(songTitle);
        Uri uri = Uri.parse(songData);
        initMediaPlayer();
        try {
            mp.setDataSource(this, uri);
            mp.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mp.setOnPreparedListener(this);
    }


    public void nextBtnClicked() {
        if (mp != null) {
            startMyService();
            if (currentSongClickedPosition >= songList.size() - 1) {
                currentSongClickedPosition = 0;
            } else {
                currentSongClickedPosition++;
            }
            song = songList.get(currentSongClickedPosition);
            if (checkOnAudioFocus()) {
                playMusic(song);
            }
        }
    }

    public void previousBtnClicked() {
        if (mp != null) {
            startMyService();
            if (currentSongClickedPosition - 1 < 0) {
                currentSongClickedPosition = songList.size() - 1;
            } else {
                currentSongClickedPosition--;
            }
            song = songList.get(currentSongClickedPosition);
            if (checkOnAudioFocus()) {
                playMusic(song);
            }
        }
    }

    private void controlBodyClicked() {
        if (currentSongClickedPosition != -1) {
            Song song = detectFromWhichList(currentSongClickedPosition);
            ConstraintLayout constraintLayout = findViewById(R.id.activity_display_songs_root_constraint);
            Utils.replaceFragments(MusicPlayerFragment.newInstance(song.getTitle(), song.getArtist(), song.getData(), mp.getDuration()), getSupportFragmentManager(), R.id.activity_display_songs_root_view);
            constraintLayout.setVisibility(View.GONE);

        }
    }


    public void playBtnClicked() {
        if (mp != null) {
            startMyService();
            if (!mp.isPlaying() && currentSongClickedPosition != -1) {
                mp.start();
                mp.setOnCompletionListener(this);
                playBtn.setBackgroundResource(R.drawable.ic_pause);
            } else {
                mp.pause();
                playBtn.setBackgroundResource(R.drawable.ic_play_button);
            }
        }
    }

    public void startMyService() {
        if (mp != null && currentSongClickedPosition != -1) {
            Intent serviceIntent = new Intent(DisplaySongsActivity.this, MusicService.class);
            serviceIntent.putExtra(SEND_IS_PLAYING_BOOLEAN_EXTRA, mp.isPlaying());
            serviceIntent.putExtra(SEND_SONG_DATA_STRING_EXTRA, getSongData());
            serviceIntent.putExtra(SEND_SONG_TITLE_STRING_EXTRA, getSongTitle());
            startService(serviceIntent);
        }
    }

    public int getCurrentSongPosition() {
        return mp.getCurrentPosition();
    }

    public void setCurrentSongPosition(int position) {
        mp.seekTo(position);
    }

    public boolean getIsPlaying() {
        return mp.isPlaying();
    }

    public String getSongData() {
        return song.getData();
    }

    public String getSongTitle() {
        return song.getTitle();
    }

    public String getSongArtist() {
        return song.getArtist();
    }

    public int getSongTotalTime() {
        return mp.getDuration();
    }

    public int getOrderOfAudioFiles() {
        return orderOfAudioFiles;
    }

    public void setOrderOfAudioFiles(int order) {
        orderOfAudioFiles = order;
        modelViewInstantiate();
    }


    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getExtras().getString(ACTION_NAME);
            MusicPlayerFragment fragment = (MusicPlayerFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
            if (!(fragment != null && fragment.isVisible()))
                switch (action) {
                    case ACTION_PREVIOUS:
                        previousBtnClicked();
                        break;

                    case ACTION_PLAY:
                        playBtnClicked();
                        break;

                    case ACTION_NEXT:
                        nextBtnClicked();
                        break;

                }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(broadcastReceiver, new IntentFilter(MUSIC_BROADCAST_SEND_INTENT));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
        unregisterReceiver(broadcastReceiver);
        Intent serviceIntent = new Intent(this, MusicService.class);
        stopService(serviceIntent);
    }

    public void releaseMediaPlayer() {
        if (mp != null) {
            mp.release();
            mp = null;
            audioManager.abandonAudioFocus(this);
            playBtn.setBackgroundResource(R.drawable.ic_play_button);
        }
    }

    public boolean checkOnAudioFocus() {
        int audioFocusResult = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        return audioFocusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }


    @Override
    public void onAudioFocusChange(int focusChange) {
        MusicPlayerFragment fragment = (MusicPlayerFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
            playBtnClicked();
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            playBtnClicked();
            releaseMediaPlayer();
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            playBtnClicked();
        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            playBtnClicked();
        }
        if (fragment != null && fragment.isVisible()) {
            fragment.play();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        playBtnClicked();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.e("TAG", "onCompletion: ");
        playBtn.setBackgroundResource(R.drawable.ic_play_button);
        releaseMediaPlayer();
        initMediaPlayer();
        nextBtnClicked();
    }

}