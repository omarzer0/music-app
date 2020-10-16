package com.azapps.musicplayer.pojo;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "song_table")
//TODO: remember>>> change Serializable to Parcelable later
public class Song  {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String displayName;
    private String artist;
    private String album;
    private String data;
    private String year;
    private long lastModifyDate;
    private long size;
    private boolean isFavourite;


    @Ignore
    public Song(String title, String displayName, String artist, String album, String data, String year, long lastModifyDate, long size,boolean isFavourite) {
        this.title = checkIfEmpty(title);
        this.displayName = checkIfEmpty(displayName);
        this.artist = checkIfEmpty(artist);
        this.album = checkIfEmpty(album);
        this.data = checkIfEmpty(data);
        this.year = checkIfEmpty(year);
        this.size = size;
        this.lastModifyDate = lastModifyDate;
        this.isFavourite = isFavourite;
    }

    public Song(int id, String title, String displayName, String artist, String album, String data, String year, long lastModifyDate, long size, boolean isFavourite) {
        this.id = id;
        this.title = checkIfEmpty(title);
        this.displayName = checkIfEmpty(displayName);
        this.artist = checkIfEmpty(artist);
        this.album = checkIfEmpty(album);
        this.data = checkIfEmpty(data);
        this.year = checkIfEmpty(year);
        this.size = size;
        this.lastModifyDate = lastModifyDate;
        this.isFavourite = isFavourite;
    }

    public long getLastModifyDate() {
        return lastModifyDate;
    }

    public void setLastModifyDate(long lastModifyDate) {
        this.lastModifyDate = lastModifyDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public boolean isFavourite() {
        return isFavourite;
    }

    public void setFavourite(boolean favourite) {
        isFavourite = favourite;
    }


    private String checkIfEmpty(String s) {
        if (s == null || s.equals("<unknown>")) return "unknown";
        else return s;
    }
}
