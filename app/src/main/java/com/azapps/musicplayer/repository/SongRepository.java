package com.azapps.musicplayer.repository;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.azapps.musicplayer.db.SongDao;
import com.azapps.musicplayer.db.SongDatabase;
import com.azapps.musicplayer.pojo.Song;

import java.util.List;

public class SongRepository {
    private SongDao songDao;
    private LiveData<List<Song>> allSongs;

    public SongRepository(Application application) {
        SongDatabase songDatabase = SongDatabase.getInstance(application);
        songDao = songDatabase.songDao();
//        allSongs = songDao.getAllSongsByTitle();
    }


    public void insert(Song song) {
        new InsertSongAsyncTask(songDao).execute(song);
    }

    public void delete(Song song) {
        new DeleteSongAsyncTask(songDao).execute(song);
    }

    public void update(Song song) {
        new UpdateSongAsyncTask(songDao).execute(song);
    }

    public void deleteAllSongs() {
        new DeleteAllSongAsyncTask(songDao).execute();
    }


//    public LiveData<List<Song>> getAllSongs() {
//        return allSongs;
//    }

    public LiveData<List<Song>> getAllSongsByAddedOrder() {
        allSongs = songDao.getAllSongsByLastModifyDate();
        return allSongs;
    }

    public LiveData<List<Song>> getAllSongsByAlphaOrder() {
        allSongs = songDao.getAllSongsByTitle();
        return allSongs;
    }

    private static class InsertSongAsyncTask extends AsyncTask<Song, Void, Void> {
        private SongDao songDao;

        public InsertSongAsyncTask(SongDao songDao) {
            this.songDao = songDao;
        }

        @Override
        protected Void doInBackground(Song... songs) {
            songDao.insert(songs[0]);
            return null;
        }
    }

    private static class UpdateSongAsyncTask extends AsyncTask<Song, Void, Void> {
        private SongDao songDao;

        public UpdateSongAsyncTask(SongDao songDao) {
            this.songDao = songDao;
        }

        @Override
        protected Void doInBackground(Song... songs) {
            songDao.update(songs[0]);
            return null;
        }
    }

    private static class DeleteSongAsyncTask extends AsyncTask<Song, Void, Void> {
        private SongDao songDao;

        public DeleteSongAsyncTask(SongDao songDao) {
            this.songDao = songDao;
        }

        @Override
        protected Void doInBackground(Song... songs) {
            songDao.delete(songs[0]);
            return null;
        }
    }

    private static class DeleteAllSongAsyncTask extends AsyncTask<Void, Void, Void> {
        private SongDao songDao;

        public DeleteAllSongAsyncTask(SongDao songDao) {
            this.songDao = songDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            songDao.deleteAllSongs();
            return null;
        }
    }

}
