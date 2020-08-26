package com.azapps.musicplayer.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.azapps.musicplayer.R;
import com.azapps.musicplayer.pojo.Utils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class MoreBottomSheetDialog extends BottomSheetDialogFragment implements View.OnClickListener {
    private static final String TAG = "MoreBottomSheetDialog" ;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.more_bottom_sheet_layout, container, false);
        TextView sortByTv = view.findViewById(R.id.more_bottom_sheet_layout_sort_by_tv);
        TextView loadAudio = view.findViewById(R.id.more_bottom_sheet_layout_search_local_audio_tv);
        TextView cancel = view.findViewById(R.id.more_bottom_sheet_layout_cancel);
        sortByTv.setOnClickListener(this);
        loadAudio.setOnClickListener(this);
        cancel.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.more_bottom_sheet_layout_sort_by_tv:
                Log.e(TAG, "onClick: sort by" );
                ConstraintLayout constraintLayout = getActivity().findViewById(R.id.activity_display_songs_root_constraint);
                Utils.replaceFragments(SortByFragment.newInstance(),getActivity().getSupportFragmentManager(),R.id.activity_display_songs_root_view);
                constraintLayout.setVisibility(View.GONE);
                dismiss();
                break;
            case R.id.more_bottom_sheet_layout_search_local_audio_tv:
                Log.e(TAG, "onClick: load audio" );
                dismiss();
                break;
            case R.id.more_bottom_sheet_layout_cancel:
                dismiss();
                break;
        }
    }
}
