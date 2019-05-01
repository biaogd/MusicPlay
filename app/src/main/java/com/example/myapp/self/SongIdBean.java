package com.example.myapp.self;

import java.io.Serializable;

public class SongIdBean implements Serializable {
    private static final long serialVersionUID = 9096789177412440722L;
    private int id=0;
    private Music music;

    public SongIdBean(int id, Music music) {
        this.id = id;
        this.music = music;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Music getMusic() {
        return music;
    }

    public void setMusic(Music music) {
        this.music = music;
    }
}
