package com.azapps.musicplayer.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.azapps.musicplayer.R;
import com.azapps.musicplayer.ui.activity.HomeActivity;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.File;

public class DeleteSongBottomSheetDialog extends BottomSheetDialogFragment implements View.OnClickListener {

    private String removedPath;
    private int id;

    public DeleteSongBottomSheetDialog(String path, int id) {
        removedPath = path;
        this.id = id;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.delete_bottom_sheet_layout, container, false);
        TextView delete = view.findViewById(R.id.delete_bottom_sheet_layout_delete_tv);
        TextView cancel = view.findViewById(R.id.delete_bottom_sheet_layout_cancel_tv);
        delete.setOnClickListener(this);
        cancel.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.delete_bottom_sheet_layout_delete_tv:
                if (new File(removedPath).delete()) {
                    Toast.makeText(getActivity(), "Deleted", Toast.LENGTH_SHORT).show();
                    ((HomeActivity) getActivity()).submitListChanges(id);
                } else if (removedPath.equals("")) {
                    Toast.makeText(getActivity(), "cannot delete this file, it is being played!", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(getActivity(), "cannot delete this file, plz delete it from file manger", Toast.LENGTH_SHORT).show();
                }
                dismiss();
                break;
            case R.id.delete_bottom_sheet_layout_cancel_tv:
                dismiss();
                break;
        }
    }
}