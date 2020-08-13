package com.azapps.musicplayer.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.azapps.musicplayer.R;

public class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    TextView titleTV, artistTV, albumTV;
    ImageView moreImageView;
    OnSongClickListener listener;

    public SongViewHolder(@NonNull View itemView, OnSongClickListener onSongClickListener) {
        super(itemView);
        titleTV = itemView.findViewById(R.id.song_item_tv_title);
        artistTV = itemView.findViewById(R.id.song_item_tv_artist);
        albumTV = itemView.findViewById(R.id.song_item_tv_album);
        moreImageView = itemView.findViewById(R.id.song_item_img_more);

        itemView.setOnClickListener(this);
        listener = onSongClickListener;
    }

    @Override
    public void onClick(View v) {
        listener.onSongClick(getAdapterPosition());
    }
}
