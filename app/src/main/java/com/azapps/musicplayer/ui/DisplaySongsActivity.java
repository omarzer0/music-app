package com.azapps.musicplayer.ui;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
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
import android.widget.Toast;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.azapps.musicplayer.pojo.Constant.SEND_CLICKED_SONG_TO_MUSIC_ACTIVITY;

public class DisplaySongsActivity extends AppCompatActivity implements OnSongClickListener, View.OnClickListener {

    // ui
    private ArrayList<Song> songList;
    private ArrayList<Song> filteredArrayList;
    private SongAdapter adapter;
    private SongViewModel songViewModel;

    private Button previousBtn, playBtn, nextBtn;
    private EditText searchEditText;
    private ImageView nowPlayingImageView;
    private TextView nowPlayingTextView;
    private ConstraintLayout bottomControlConstraintLayout;

    private MediaPlayer mp;

    // vars
    private int currentSongClickedPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_songs);

        initViews();
        initMediaPlayer();

        prepareMoreOptionImg();
        setRecyclerView();
        modelViewInstantiate();
        initEditTextSearchFunction();
    }

    private void initViews() {
        mp = new MediaPlayer();
        previousBtn = findViewById(R.id.activity_display_songs_previous_btn);
        playBtn = findViewById(R.id.activity_display_songs_play_btn);
        nextBtn = findViewById(R.id.activity_display_songs_next_btn);
        searchEditText = findViewById(R.id.activity_display_songs_ed_search_edit_text);
        nowPlayingImageView = findViewById(R.id.activity_display_songs_now_playing_song_image_view);
        nowPlayingTextView = findViewById(R.id.activity_display_songs_now_playing_song_title);
        bottomControlConstraintLayout = findViewById(R.id.activity_display_songs_constraint_layout_bottom_play_control);

        setListeners();
    }

    private void setListeners() {
        previousBtn.setOnClickListener(this);
        playBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
        bottomControlConstraintLayout.setOnClickListener(this);
    }

    private void modelViewInstantiate() {
        songViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication()))
                .get(SongViewModel.class);
        LiveData<List<Song>> listLiveDataSongs = songViewModel.getAllSongs();
        // switch on order by function
        listLiveDataSongs.observe(this, new Observer<List<Song>>() {
            @Override
            public void onChanged(List<Song> songs) {
                songList = new ArrayList<>();
                songList.addAll(songs);
                adapter.submitList(songs);
            }
        });
    }

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
        Song song = detectFromWhichList(position);
        playMusic(song);
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

    private void playMusic(Song song) {
        String songData = song.getData();
        String songTitle = song.getTitle();
        setImageToPlayerControl(songData);
        setTitleToPlayerControl(songTitle);
        Uri uri = Uri.parse(songData);
        mp.reset();
        initMediaPlayer();
        try {
            mp.setDataSource(this, uri);
            mp.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        playBtnClicked();
        mp.start();
    }

    private void prepareMoreOptionImg() {
        Button moreOptionsImg = findViewById(R.id.activity_display_songs_img_more_options);
        moreOptionsImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                freeDateBase();
                try {
                    getMusic();
                    if (songList.size() == 0)
                        Toast.makeText(DisplaySongsActivity.this, "oooops! no music was found", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e("error", "onCreate: " + e.getMessage());
                    Toast.makeText(DisplaySongsActivity.this, "Error can't load any song", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

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
            nowPlayingImageView.setImageResource(R.drawable.song_not_found_background_image);
        }
    }

    private void setTitleToPlayerControl(String songTitle) {
        nowPlayingTextView.setText(songTitle);
    }

    private void initMediaPlayer() {
        mp.setLooping(true);
        mp.seekTo(0);
        mp.setVolume(1.0f, 1.0f);
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
        }
    }

    private void nextBtnClicked() {
        if (currentSongClickedPosition >= songList.size() - 1) {
            currentSongClickedPosition = 0;
        } else {
            currentSongClickedPosition++;
        }
        playMusic(songList.get(currentSongClickedPosition));
    }

    private void previousBtnClicked() {
        if (currentSongClickedPosition - 1 < 0) {
            currentSongClickedPosition = songList.size() - 1;
        } else {
            currentSongClickedPosition--;
        }
        playMusic(songList.get(currentSongClickedPosition));
    }

    private void controlBodyClicked() {
        if (currentSongClickedPosition != -1) {
        Intent intent = new Intent(this, MusicPlayerActivity.class);
        Song song = detectFromWhichList(currentSongClickedPosition);
        intent.putExtra(SEND_CLICKED_SONG_TO_MUSIC_ACTIVITY, song);
        startActivity(intent);
        }
    }

    private void playBtnClicked() {
        if (!mp.isPlaying()) {
            mp.start();
            playBtn.setBackgroundResource(R.drawable.ic_pause);
        } else {
            mp.pause();
            playBtn.setBackgroundResource(R.drawable.ic_play_button);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
    }

    private void releaseMediaPlayer() {
        if (mp != null) {
            mp.release();
            mp = null;
        }
    }
}