package com.azapps.musicplayer.adapter;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.azapps.musicplayer.R;

public class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private static final String TAG = "SongViewHolder";
    TextView titleTV, artistTV, albumTV;
    ImageView moreImageView;
    ConstraintLayout root;
    OnSongClickListener listener;

    public SongViewHolder(@NonNull View itemView, OnSongClickListener onSongClickListener) {
        super(itemView);
        titleTV = itemView.findViewById(R.id.song_item_tv_title);
        artistTV = itemView.findViewById(R.id.song_item_tv_artist);
        albumTV = itemView.findViewById(R.id.song_item_tv_album);
        moreImageView = itemView.findViewById(R.id.song_item_img_more);
        root = itemView.findViewById(R.id.song_item_root_layout);

        moreImageView.setOnClickListener(this);
        titleTV.setOnClickListener(this);
        artistTV.setOnClickListener(this);
        albumTV.setOnClickListener(this);
        listener = onSongClickListener;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.song_item_img_more) {
            listener.onSongMoreOptionClick(getAdapterPosition());
        } else {
            listener.onSongClick(getAdapterPosition());
        }
    }
}
