package com.azapps.musicplayer.ui.activity;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.azapps.musicplayer.R;
import com.azapps.musicplayer.pojo.Utils;
import com.azapps.musicplayer.ui.fragment.DisplaySongsFragment;

import static com.azapps.musicplayer.pojo.Constant.FRAGMENT_DISPLAY_SONG;

public class HomeActivity extends AppCompatActivity {
    private DisplaySongsFragment displaySongsFragment;
    public static final String TAG = "HomeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Utils.replaceFragments(DisplaySongsFragment.newInstance(), getSupportFragmentManager(), R.id.fragment_home_root_frame_layout, FRAGMENT_DISPLAY_SONG);

    }

    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count == 1){
            moveTaskToBack(true);
        }else {
            getSupportFragmentManager().popBackStack();
        }
    }

    private void getDisplaySongFragmentInstance() {
        if (displaySongsFragment == null) {
            displaySongsFragment = (DisplaySongsFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_DISPLAY_SONG);
        }
    }


    public int getCurrentSongPosition() {
        getDisplaySongFragmentInstance();
        return displaySongsFragment.getCurrentSongPosition();
    }

    public void setCurrentSongPosition(int progress) {
        getDisplaySongFragmentInstance();
        displaySongsFragment.setCurrentSongPosition(progress);
    }

    public boolean getIsPlaying() {
        getDisplaySongFragmentInstance();
        return displaySongsFragment.getIsPlaying();
    }

    public void previousBtnClicked() {
        getDisplaySongFragmentInstance();
        displaySongsFragment.previousBtnClicked();
    }

    public void playBtnClicked() {
        getDisplaySongFragmentInstance();
        displaySongsFragment.playBtnClicked();
    }

    public void nextBtnClicked() {
        getDisplaySongFragmentInstance();
        displaySongsFragment.nextBtnClicked();
    }

    public String getSongData() {
        getDisplaySongFragmentInstance();
        return displaySongsFragment.getSongData();
    }

    public String getSongTitle() {
        getDisplaySongFragmentInstance();
        return displaySongsFragment.getSongTitle();
    }

    public String getSongArtist() {
        getDisplaySongFragmentInstance();
        return displaySongsFragment.getSongArtist();
    }

    public int getSongTotalTime() {
        getDisplaySongFragmentInstance();
        return displaySongsFragment.getSongTotalTime();
    }

    public void loadAudioFromTheDevice() {
        getDisplaySongFragmentInstance();
        displaySongsFragment.loadAudioFromTheDevice();
    }

    public void setOrderOfAudioFiles(int alphaOrder) {
        getDisplaySongFragmentInstance();
        displaySongsFragment.setOrderOfAudioFiles(alphaOrder);
    }
}