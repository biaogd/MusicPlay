package com.example.myapp.self;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class NetMusicBean implements Serializable {
    private static final long serialVersionUID = -3942457338267208464L;
    @SerializedName("ID")
    private int id;
    @SerializedName("SongName")
    private String songName;
    @SerializedName("SongAuthor")
    private String songAuthor;
    @SerializedName("AllTime")
    private int allTime;
    @SerializedName("SongSize")
    private int songSize;
    @SerializedName("URL")
    private String url;

    public NetMusicBean(int id, String songName, String songAuthor, int allTime, int songSize, String url) {
        this.id = id;
        this.songName = songName;
        this.songAuthor = songAuthor;
        this.allTime = allTime;
        this.songSize = songSize;
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getSongAuthor() {
        return songAuthor;
    }

    public void setSongAuthor(String songAuthor) {
        this.songAuthor = songAuthor;
    }

    public int getAllTime() {
        return allTime;
    }

    public void setAllTime(int allTime) {
        this.allTime = allTime;
    }

    public int getSongSize() {
        return songSize;
    }

    public void setSongSize(int songSize) {
        this.songSize = songSize;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "NetMusicBean{" +
                "id=" + id +
                ", songName='" + songName + '\'' +
                ", songAuthor='" + songAuthor + '\'' +
                ", allTime=" + allTime +
                ", songSize=" + songSize +
                ", url='" + url + '\'' +
                '}';
    }
}
