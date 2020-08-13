package com.azapps.musicplayer.ui;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.azapps.musicplayer.R;
import com.azapps.musicplayer.adapter.OnSongClickListener;
import com.azapps.musicplayer.adapter.SongAdapter;
import com.azapps.musicplayer.pojo.Song;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import static com.azapps.musicplayer.pojo.Constant.SEND_CLICKED_SONG_TO_MUSIC_ACTIVITY;

public class DisplaySongsActivity extends AppCompatActivity implements OnSongClickListener {

    private ArrayList<Song> songList ;
    private SongAdapter adapter;
    private SongViewModel songViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_songs);

        prepareFab();

        setRecyclerView();

        modelViewInstantiate();
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
                Toast.makeText(DisplaySongsActivity.this, "done! " + songList.size(), Toast.LENGTH_SHORT).show();
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
                long duration;
                try {
                    duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                } catch (Exception e) {
                    Log.e("error", "getMusic: " + e.getMessage());
                    duration = 0;
                }
                // Save to audioList
                Song song = new Song(title, displayName, artist, album, data, year, lastDateModified, size, duration);
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
        adapter = new SongAdapter(this);
//        adapter.submitList(songList);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onSongClick(int position) {
        Intent intent = new Intent(this, MusicPlayerActivity.class);
        intent.putExtra(SEND_CLICKED_SONG_TO_MUSIC_ACTIVITY, songList.get(position));
        startActivity(intent);
    }

    private void prepareFab() {
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                freeDateBase();
                try {
                    getMusic();
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
}