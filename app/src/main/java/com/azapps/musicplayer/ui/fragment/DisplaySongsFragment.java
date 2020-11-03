package com.azapps.musicplayer.ui.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
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
import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.azapps.musicplayer.pojo.Constant.ACTION_NAME;
import static com.azapps.musicplayer.pojo.Constant.ACTION_NEXT;
import static com.azapps.musicplayer.pojo.Constant.ACTION_PLAY;
import static com.azapps.musicplayer.pojo.Constant.ACTION_PREVIOUS;
import static com.azapps.musicplayer.pojo.Constant.ADDED_TIME_ORDER;
import static com.azapps.musicplayer.pojo.Constant.ALPHA_ORDER;
import static com.azapps.musicplayer.pojo.Constant.CURRENT_SONG_IMAGE_DATA;
import static com.azapps.musicplayer.pojo.Constant.CURRENT_SONG_POSITION;
import static com.azapps.musicplayer.pojo.Constant.CURRENT_SONG_Title;
import static com.azapps.musicplayer.pojo.Constant.DELETE_BOTTOM_SHEET_TAG;
import static com.azapps.musicplayer.pojo.Constant.FRAGMENT_MUSIC_PLAYER_TAG;
import static com.azapps.musicplayer.pojo.Constant.FRAGMENT_SEARCH_LOCAL_STORAGE_TAG;
import static com.azapps.musicplayer.pojo.Constant.MORE_BOTTOM_SHEET_TAG;
import static com.azapps.musicplayer.pojo.Constant.MUSIC_BROADCAST_SEND_INTENT;
import static com.azapps.musicplayer.pojo.Constant.MY_PREFS_NAME;
import static com.azapps.musicplayer.pojo.Constant.REQUEST_PERMISSION_STORAGE;
import static com.azapps.musicplayer.pojo.Constant.SEND_IS_PLAYING_BOOLEAN_EXTRA;
import static com.azapps.musicplayer.pojo.Constant.SEND_SONG_DATA_STRING_EXTRA;
import static com.azapps.musicplayer.pojo.Constant.SEND_SONG_TITLE_STRING_EXTRA;

public class DisplaySongsFragment extends Fragment implements OnSongClickListener, View.OnClickListener,
        OnAudioFocusChangeListener, OnPreparedListener, OnCompletionListener, DialogInterface.OnClickListener {

    private static final String TAG = "DisplaySongsActivity";
    // ui
    private Button previousBtn, nextBtn, moreOptionsBtn;
    private ImageView refreshImgView, favouriteImgView;
    private ImageButton playBtn;
    private CircularImageView nowPlayingImageView;
    private TextView nowPlayingTextView, clickToLoadDataTv;
    private ConstraintLayout bottomControlConstraintLayout;
    private MediaPlayer mp;
    private AudioManager audioManager;
    private ConstraintLayout constraintLayoutFound, constraintLayoutNotFound;

    // vars
    private ArrayList<Song> songList;
    private SongAdapter adapter;
    private SongViewModel songViewModel;
    private LiveData<List<Song>> listLiveDataSongs;
    private Song song;
    private int currentSongClickedPosition = -1;
    private boolean isLooping = false, cameFromAudioFocus = false;
    private static int orderOfAudioFiles = ADDED_TIME_ORDER;

    public static Fragment newInstance() {
        return new DisplaySongsFragment();
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_display_songs, container, false);
        initViews(view);
        setRecyclerView(view);
        checkIfThePermissionIsGranted();
        runSearchFun(true);
        getFromPreference();
        return view;
    }

    private void initViews(View view) {
        previousBtn = view.findViewById(R.id.fragment_display_songs_previous_btn);
        playBtn = view.findViewById(R.id.fragment_display_songs_play_btn);
        nextBtn = view.findViewById(R.id.fragment_display_songs_next_btn);
        refreshImgView = view.findViewById(R.id.fragment_display_songs_img_refresh_data);
        moreOptionsBtn = view.findViewById(R.id.fragment_display_songs_btn_more_options);
        nowPlayingImageView = view.findViewById(R.id.fragment_display_songs_now_playing_song_image_view);
        nowPlayingTextView = view.findViewById(R.id.fragment_display_songs_now_playing_song_title);
        bottomControlConstraintLayout = view.findViewById(R.id.fragment_display_songs_constraint_layout_bottom_play_control);
        clickToLoadDataTv = view.findViewById(R.id.fragment_display_songs_search_local_db_tv);
        constraintLayoutFound = view.findViewById(R.id.fragment_display_songs_root_constraint_found);
        constraintLayoutNotFound = view.findViewById(R.id.fragment_display_songs_root_not_found);
        audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        setListeners();
    }

    private void setListeners() {
        previousBtn.setOnClickListener(this);
        playBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
        refreshImgView.setOnClickListener(this);
        bottomControlConstraintLayout.setOnClickListener(this);
        moreOptionsBtn.setOnClickListener(this);
        clickToLoadDataTv.setOnClickListener(this);
    }

    private void checkIfThePermissionIsGranted() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // not granted show explanation for the required permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                alertMessageBuilder();
            } else {
                //request it again
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_STORAGE);
            }

        } else {
            // granted
            modelViewInstantiate();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                modelViewInstantiate();
            } else {
                getActivity().finishAffinity();
            }
        }
    }

    private void alertMessageBuilder() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.required_permission)
                .setMessage(R.string.storage_permission_alert_message)
                .setCancelable(false)
                .setPositiveButton(R.string.ok, this)
                .show();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                REQUEST_PERMISSION_STORAGE);
    }

    public void loadAudioFromTheDevice() {
        modelViewInstantiate();
        getMusic(true);
    }

    private void audioFilesWasFound() {
        if (songList.size() > 0) {
            constraintLayoutFound.setVisibility(View.VISIBLE);
            constraintLayoutNotFound.setVisibility(View.GONE);
        }
    }


    private void modelViewInstantiate() {
        songViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication())).get(SongViewModel.class);
        listLiveDataSongs = null;
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
        setSongListObserver();
    }

    private void setSongListObserver() {
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

            SearchLocalStorageFragment fragment = (SearchLocalStorageFragment) getActivity().getSupportFragmentManager().findFragmentByTag(FRAGMENT_SEARCH_LOCAL_STORAGE_TAG);
            if ((fragment != null && fragment.isVisible())) {
                fragment.getDataToTextView(songList.size());
                Log.e(TAG, "onChanged: " + songList.size());
            } else {
                audioFilesWasFound();
            }
        }
    };

    private void getMusic(boolean isFromOnCreate) {
        ContentResolver contentResolver = getActivity().getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " DESC";
        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);
        int counter = 0;
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                long lastDateModified = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED));
                String displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String year = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.YEAR));
//                long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
                long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
                Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, size);
                String cover = albumArtUri.toString();
                // Save to audioList

                if (!checkIfSongExists(data)) {
                    Song song = new Song(title, displayName, artist, album, data, year, lastDateModified, size, false, cover);
                    songViewModel.insert(song);
                    counter++;
                }
            }
        }
        cursor.close();
        if (!isFromOnCreate)
            Toast.makeText(getActivity(), counter + " Songs is added", Toast.LENGTH_SHORT).show();
    }

    private void runSearchFun(final boolean isFromOnCreate) {

        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (currentSongClickedPosition != -1) {
                    scanForAddOrDeletedSongs(isFromOnCreate);
                }
            }
        }, 1000);

    }

    private void scanForAddOrDeletedSongs(boolean isFromOnCreate) {
        getMusic(isFromOnCreate);
        refreshSongs();
        if (song != null)
            findTheCorrectPosition(song.getData());
        Log.e("TAG", "run: ");
    }

    boolean checkIfSongExists(String data) {
        if (songList == null) return false;
        for (Song song : songList) {
            if (data.equals(song.getData()))
                return true;
        }
        return false;
    }

    private void refreshSongs() {
        if (songList != null && songList.size() != 0) {
            for (Song temp_song : songList) {
                File temp_file = new File(temp_song.getData());
                if (!temp_file.exists()) {
                    broadCastToMediaScanner(getActivity(), temp_file);
                    songViewModel.delete(temp_song);
                }
            }
        }
    }

    private void setRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.fragment_display_songs_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.hasFixedSize();
        recyclerView.setItemAnimator(null);
        adapter = new SongAdapter(this, getActivity());
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onSongClick(int position) {
        currentSongClickedPosition = position;
        song = songList.get(position);
        if (checkOnAudioFocus()) {
            playMusic(song);
        }
    }


    private void setImageToPlayerControl(String cover) {
        Uri uri = Uri.parse(cover);
        nowPlayingImageView.setImageURI(uri);
        if (nowPlayingImageView.getDrawable() == null)
            nowPlayingImageView.setImageResource(R.drawable.default_image);
    }

    private void setTitleToPlayerControl(String songTitle) {
        nowPlayingTextView.setText(songTitle);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_display_songs_previous_btn:
                previousBtnClicked();
                break;
            case R.id.fragment_display_songs_play_btn:
                playBtnClicked();
                break;
            case R.id.fragment_display_songs_next_btn:
                nextBtnClicked();
                break;
            case R.id.fragment_display_songs_constraint_layout_bottom_play_control:
                controlBodyClicked();
                break;
            case R.id.fragment_display_songs_btn_more_options:
                moreOptionsBtnClicked();
                break;
            case R.id.fragment_display_songs_search_local_db_tv:
                constraintLayoutNotFound.setVisibility(View.GONE);
                Utils.replaceFragments(SearchLocalStorageFragment.newInstance(), getActivity().getSupportFragmentManager(), R.id.fragment_display_songs_root_view, FRAGMENT_SEARCH_LOCAL_STORAGE_TAG);
                break;
            case R.id.fragment_display_songs_img_refresh_data:
                scanForAddOrDeletedSongs(false);
                break;
        }
    }

    private void moreOptionsBtnClicked() {
        MoreBottomSheetDialog moreBottomSheetDialog = new MoreBottomSheetDialog();
        moreBottomSheetDialog.show(getActivity().getSupportFragmentManager(), MORE_BOTTOM_SHEET_TAG);
    }

    @Override
    public void onSongMoreOptionClick(int position) {
        String path = "";
        Song deletedSong = songList.get(position);
        if (song == null || !song.getData().equals(deletedSong.getData())) {
            path = deletedSong.getData();
        }
        DeleteSongBottomSheetDialog deleteSongBottomSheetDialog = new DeleteSongBottomSheetDialog(path, position);
        deleteSongBottomSheetDialog.show(getActivity().getSupportFragmentManager(), DELETE_BOTTOM_SHEET_TAG);
    }

    public void submitListChanges(int position) {
        Song temp_song = songList.get(position);
        songViewModel.delete(temp_song);
        try {
            String data = song.getData();
            findTheCorrectPosition(data);
        } catch (NullPointerException e) {
            Log.e(TAG, "submitListChanges: " + e.getMessage());
        }
        broadCastToMediaScanner(getActivity(), new File(temp_song.getData()));
        currentSongClickedPosition--;
    }

    private void findTheCorrectPosition(String data) {
        try {
            for (int i = 0; i < songList.size(); i++) {
                Song temp = songList.get(i);
                if (temp.getData().equals(data)) {
                    currentSongClickedPosition = i;
                    song = temp;
                }
            }
        } catch (Exception e) {
            e.getMessage();
        }
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
        String cover = song.getCover();
        setImageToPlayerControl(cover);
        setTitleToPlayerControl(songTitle);
        Uri uri = Uri.parse(songData);
        initMediaPlayer();
        try {
            mp.setDataSource(getActivity(), uri);
            mp.prepare();
        } catch (IOException e) {
            Toast.makeText(getActivity(), "This Audio does not exist anymore", Toast.LENGTH_SHORT).show();
            broadCastToMediaScanner(getActivity(), new File(songData));
            songViewModel.delete(songList.get(currentSongClickedPosition));
            nextBtnClicked();
            e.printStackTrace();
        }
        mp.setOnPreparedListener(this);
    }

    public static void broadCastToMediaScanner(Context context, File file) {
        Uri contentUri = Uri.fromFile(file);
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
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
        if (song != null) {
            Utils.replaceFragments(MusicPlayerFragment.newInstance(song.getTitle(),
                    song.getArtist(), song.getData(), mp.getDuration(), song.getCover()),
                    getActivity().getSupportFragmentManager(), R.id.fragment_display_songs_root_view, FRAGMENT_MUSIC_PLAYER_TAG);
            constraintLayoutFound.setVisibility(View.GONE);
        }
    }

    public void playBtnClicked() {
        if (mp != null) {
            startMyService();
            if (!mp.isPlaying() && currentSongClickedPosition != -1) {
                mp.start();
                Log.e(TAG, "playBtnClicked: start");
                mp.setOnCompletionListener(this);
                playBtn.setImageResource(R.drawable.ic_pause);
            } else {
                mp.pause();
                playBtn.setImageResource(R.drawable.ic_play_button);
            }
        } else if (currentSongClickedPosition != -1) {
            onSongClick(currentSongClickedPosition);
        } else {
            onSongClick(0);
        }
        Log.e(TAG, "playBtnClicked: " + currentSongClickedPosition);
    }

    public void startMyService() {
        if (mp != null && currentSongClickedPosition != -1) {
            Intent serviceIntent = new Intent(getActivity(), MusicService.class);
            serviceIntent.putExtra(SEND_IS_PLAYING_BOOLEAN_EXTRA, mp.isPlaying());
            serviceIntent.putExtra(SEND_SONG_DATA_STRING_EXTRA, getSongData());
            serviceIntent.putExtra(SEND_SONG_TITLE_STRING_EXTRA, getSongTitle());
            getActivity().startService(serviceIntent);
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

    public String getSongCover() {
        return song.getCover();
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
            MusicPlayerFragment fragment = (MusicPlayerFragment) getActivity().getSupportFragmentManager().findFragmentByTag(FRAGMENT_MUSIC_PLAYER_TAG);
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
    public void onStart() {
        super.onStart();
        getActivity().registerReceiver(broadcastReceiver, new IntentFilter(MUSIC_BROADCAST_SEND_INTENT));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
        getActivity().unregisterReceiver(broadcastReceiver);
        Intent serviceIntent = new Intent(getActivity(), MusicService.class);
        getActivity().stopService(serviceIntent);
        saveToPreference();
    }

    private void saveToPreference() {
        SharedPreferences.Editor editor = getActivity().getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putInt(CURRENT_SONG_POSITION, currentSongClickedPosition);
        editor.putString(CURRENT_SONG_IMAGE_DATA, song.getCover());
        editor.putString(CURRENT_SONG_Title, song.getTitle());
        Log.e(TAG, "" + song.getData());
        editor.apply();
    }

    private void getFromPreference() {
        SharedPreferences preferences = getActivity().getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        currentSongClickedPosition = preferences.getInt(CURRENT_SONG_POSITION, -1);
        String cover = preferences.getString(CURRENT_SONG_IMAGE_DATA, "");
        String title = preferences.getString(CURRENT_SONG_Title, getString(R.string.choose_a_song));
        if (cover.equals("")) nowPlayingImageView.setImageResource(R.drawable.default_image);
        else setImageToPlayerControl(cover);
        nowPlayingTextView.setText(title);
        Log.e(TAG, "getFromPreference: " + currentSongClickedPosition);
    }

    public void releaseMediaPlayer() {
        if (mp != null) {
            mp.release();
            mp = null;
            audioManager.abandonAudioFocus(this);
            playBtn.setImageResource(R.drawable.ic_play_button);
        }
    }

    public boolean checkOnAudioFocus() {
        int audioFocusResult = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        return audioFocusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }


    @Override
    public void onAudioFocusChange(int focusChange) {
        MusicPlayerFragment fragment = (MusicPlayerFragment) getActivity().getSupportFragmentManager().findFragmentByTag(FRAGMENT_MUSIC_PLAYER_TAG);
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT && mp.isPlaying()) {
            playBtnClicked();
            cameFromAudioFocus = true;
            Log.e("TAG", "onAudioFocusChange: AUDIOFOCUS_LOSS_TRANSIENT");
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            playBtnClicked();
            releaseMediaPlayer();
            Log.e("TAG", "onAudioFocusChange: AUDIOFOCUS_LOSS");
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            playBtnClicked();
            Log.e("TAG", "onAudioFocusChange: AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN && cameFromAudioFocus) {
            playBtnClicked();
            cameFromAudioFocus = false;
            Log.e("TAG", "onAudioFocusChange: AUDIOFOCUS_GAIN");
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
        playBtn.setImageResource(R.drawable.ic_play_button);
        releaseMediaPlayer();
        initMediaPlayer();
        nextBtnClicked();
        MusicPlayerFragment fragment = (MusicPlayerFragment) getActivity().getSupportFragmentManager().findFragmentByTag(FRAGMENT_MUSIC_PLAYER_TAG);
        if ((fragment != null && fragment.isVisible())) {
            fragment.getSongChanged();
        }
    }

}