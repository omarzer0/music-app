package com.azapps.musicplayer.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;

import com.azapps.musicplayer.R;
import com.azapps.musicplayer.pojo.Song;


public class SongAdapter extends ListAdapter<Song, SongViewHolder> {
    OnSongClickListener listener;

    private static final DiffUtilSongCallback CALLBACK = new DiffUtilSongCallback();

    public SongAdapter(OnSongClickListener onSongClickListener) {
        super(CALLBACK);
        listener = onSongClickListener;
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
        holder.albumTV.setText(currentSong.getAlbum());
        holder.moreImageView.setImageResource(R.drawable.ic_more);
    }
}
