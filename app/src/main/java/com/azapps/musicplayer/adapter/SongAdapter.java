package com.azapps.musicplayer.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;

import com.azapps.musicplayer.R;
import com.azapps.musicplayer.pojo.Song;
import com.bumptech.glide.Glide;


public class SongAdapter extends ListAdapter<Song, SongViewHolder> {
    OnSongClickListener listener;
    Context context;

    private static final DiffUtilSongCallback CALLBACK = new DiffUtilSongCallback();

    public SongAdapter(OnSongClickListener onSongClickListener, Context context) {
        super(CALLBACK);
        listener = onSongClickListener;
        this.context = context;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.song_item, parent, false);
        return new SongViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song currentSong = getItem(position);
        holder.titleTV.setText(currentSong.getTitle());
        holder.artistTV.setText(currentSong.getArtist());
        holder.moreImageView.setImageResource(R.drawable.ic_more);
        Uri uri = Uri.parse(currentSong.getCover());
        Glide.with(context).load(uri).placeholder(R.drawable.default_image).into(holder.songImageView);
    }
}