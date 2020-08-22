package com.azapps.musicplayer.ui.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.azapps.musicplayer.R;
import com.azapps.musicplayer.pojo.Utils;
import com.azapps.musicplayer.ui.fragment.DisplayMusicFragment;

public class StarterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starter);
        Utils.replaceFragments(DisplayMusicFragment.newInstance(), getSupportFragmentManager(), R.id.activity_starter_frame_layout);
    }

}