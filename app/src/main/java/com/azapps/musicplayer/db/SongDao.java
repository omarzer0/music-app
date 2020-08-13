package com.azapps.musicplayer.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.azapps.musicplayer.pojo.Song;

import java.util.List;

@Dao
public interface SongDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Song song);

    @Update
    void update(Song song);

    @Delete
    void delete(Song song);

    @Query("DELETE FROM song_table")
    void deleteAllSongs();

//    @Query("SELECT * FROM song_table ORDER BY lastModifyDate DESC")
//    LiveData<List<Song>> getAllSongsByLastModifyDate();

    @Query("SELECT * FROM song_table ORDER BY title DESC")
    LiveData<List<Song>> getAllSongsByTitle();
//
//    @Query("SELECT * FROM song_table ORDER BY duration DESC")
//    LiveData<List<Song>> getAllSongsByDuration();

}
