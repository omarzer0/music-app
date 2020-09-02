package com.azapps.musicplayer.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.azapps.musicplayer.R;
import com.azapps.musicplayer.ui.activity.HomeActivity;

public class SearchLocalStorageFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "SearchLocal";
    private Button searchAgainBtn;
    private TextView showDoneText;
    private ProgressBar progressBar;
    private int numberOfSongs;

    public SearchLocalStorageFragment() {

    }

    public static SearchLocalStorageFragment newInstance() {
        SearchLocalStorageFragment fragment = new SearchLocalStorageFragment();

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_local_audio_files, container, false);
        searchAgainBtn = view.findViewById(R.id.fragment_search_local_audio_files_search_again_btn);
        searchAgainBtn.setOnClickListener(this);
        showDoneText = view.findViewById(R.id.fragment_search_local_audio_files_done_tv);
        progressBar = view.findViewById(R.id.fragment_search_local_audio_files_progress_bar);
        ((HomeActivity) getActivity()).loadAudioFromTheDevice();
        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fragment_search_local_audio_files_search_again_btn) {
            ((HomeActivity) getActivity()).loadAudioFromTheDevice();
        }
    }

    public void getDataToTextView(int numberOfSongsLoaded) {
        numberOfSongs = numberOfSongsLoaded;
        progressBar.setVisibility(View.GONE);
        showDoneText.setVisibility(View.VISIBLE);
        searchAgainBtn.setVisibility(View.VISIBLE);
        Log.e(TAG, "getDataToTextView: " + numberOfSongs);
        showDoneText.setText(numberOfSongs + getString(R.string.noOfSongsFound));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (numberOfSongs > 0) {
            ConstraintLayout constraintLayoutFound = getActivity().findViewById(R.id.fragment_display_songs_root_constraint_found);
            constraintLayoutFound.setVisibility(View.VISIBLE);
        } else {
            ConstraintLayout constraintLayoutNotFound = getActivity().findViewById(R.id.fragment_display_songs_root_not_found);
            constraintLayoutNotFound.setVisibility(View.VISIBLE);
        }
    }
}
