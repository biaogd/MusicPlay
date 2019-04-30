package com.example.myapp.self;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SelfSongBean implements Serializable {
    private static final long serialVersionUID = 3927772714349960752L;
    @SerializedName("ID")
    private int id;
    @SerializedName("ListID")
    private int listId;
    @SerializedName("SongID")
    private int songId;
    @SerializedName("SongName")
    private String songName;
    @SerializedName("SongAuthor")
    private String songAuthor;
    @SerializedName("SongPath")
    private String songPath;

    public SelfSongBean(int id, int listId, int songId, String songName, String songAuthor, String songPath) {
        this.id = id;
        this.listId = listId;
        this.songId = songId;
        this.songName = songName;
        this.songAuthor = songAuthor;
        this.songPath = songPath;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getListId() {
        return listId;
    }

    public void setListId(int listId) {
        this.listId = listId;
    }

    public int getSongId() {
        return songId;
    }

    public void setSongId(int songId) {
        this.songId = songId;
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

    public String getSongPath() {
        return songPath;
    }

    public void setSongPath(String songPath) {
        this.songPath = songPath;
    }
}
