package com.azapps.musicplayer.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.azapps.musicplayer.pojo.Song;

public class DiffUtilSongCallback extends DiffUtil.ItemCallback<Song> {
    @Override
    public boolean areItemsTheSame(@NonNull Song oldItem, @NonNull Song newItem) {
        return oldItem.getId() == (newItem.getId());
    }

    @Override
    public boolean areContentsTheSame(@NonNull Song oldItem, @NonNull Song newItem) {
        return (oldItem.getTitle().equals(newItem.getTitle())
                && oldItem.getData().equals(newItem.getData())
                && oldItem.getDuration() == (newItem.getDuration())
                && oldItem.getLastModifyDate() == (newItem.getLastModifyDate())
        );
    }
}
