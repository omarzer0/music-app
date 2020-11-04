package com.azapps.musicplayer.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.azapps.musicplayer.R;
import com.mikhaellopez.circularimageview.CircularImageView;

public class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private static final String TAG = "SongViewHolder";
    TextView titleTV, artistTV;
    ImageView moreImageView;
    ConstraintLayout root;
    OnSongClickListener listener;
    CircularImageView songImageView;

    public SongViewHolder(@NonNull View itemView, OnSongClickListener onSongClickListener) {
        super(itemView);
        titleTV = itemView.findViewById(R.id.song_item_tv_title);
        artistTV = itemView.findViewById(R.id.song_item_tv_artist);
        moreImageView = itemView.findViewById(R.id.song_item_img_more);
        songImageView = itemView.findViewById(R.id.song_item_img_song_image);
        root = itemView.findViewById(R.id.song_item_root_layout);

        moreImageView.setOnClickListener(this);
        root.setOnClickListener(this);
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
