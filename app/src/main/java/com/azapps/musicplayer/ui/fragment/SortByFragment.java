package com.azapps.musicplayer.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.azapps.musicplayer.R;
import com.azapps.musicplayer.ui.activity.DisplaySongsActivity;

import static com.azapps.musicplayer.pojo.Constant.ADDED_TIME_ORDER;
import static com.azapps.musicplayer.pojo.Constant.ALPHA_ORDER;

public class SortByFragment extends Fragment implements RadioGroup.OnCheckedChangeListener, View.OnClickListener {
    private static final String TAG = "SortByFragment";

    public SortByFragment() {

    }

    public static Fragment newInstance() {
        return new SortByFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sort_by, container, false);
        RadioGroup radioGroup = view.findViewById(R.id.fragment_sort_by_radio_group_rg);
        radioGroup.setOnCheckedChangeListener(this);
        TextView cancelBtn = view.findViewById(R.id.fragment_sort_by_cancel_tv);
        cancelBtn.setOnClickListener(this);
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ConstraintLayout constraintLayout = getActivity().findViewById(R.id.activity_display_songs_root_constraint_found);
        constraintLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.fragment_sort_by_radio_button_rb_sort_by_alpha_order:
                Log.e(TAG, "onCheckedChanged: alpha order");
                ((DisplaySongsActivity) getActivity()).setOrderOfAudioFiles(ALPHA_ORDER);
                break;

            case R.id.fragment_sort_by_radio_button_rb_sort_by_time_added:
                Log.e(TAG, "onCheckedChanged: time order");
                ((DisplaySongsActivity) getActivity()).setOrderOfAudioFiles(ADDED_TIME_ORDER);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_sort_by_cancel_tv:
                getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                break;
        }
    }
}
