package com.azapps.musicplayer.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.azapps.musicplayer.R;

public class MusicPlayerFragment extends Fragment {


    // TODO: Rename and change types of parameters


    public MusicPlayerFragment() {

    }

    public static MusicPlayerFragment newInstance() {
        return new MusicPlayerFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_music_player, container, false);

        return view;
    }
}