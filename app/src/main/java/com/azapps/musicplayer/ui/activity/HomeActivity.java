package com.azapps.musicplayer.ui.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.azapps.musicplayer.R;
import com.azapps.musicplayer.pojo.Utils;
import com.azapps.musicplayer.ui.fragment.Test;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Utils.replaceFragments(Test.newInstance(),getSupportFragmentManager(),R.id.home_activity_root_view,"display song fragment");
    }
}