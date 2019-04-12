package com.example.myapp.self;

import java.io.Serializable;

public class DownloadBean implements Serializable {
    private static final long serialVersionUID = 1804898122583580974L;

    private Music music;
    private int progress;

    public DownloadBean(Music music, int progress) {
        this.music = music;
        this.progress = progress;
    }

    @Override
    public String toString() {
        return "downloadBean{" +
                "music=" + music +
                ", progress=" + progress +
                '}';
    }

    public Music getMusic() {
        return music;
    }

    public void setMusic(Music music) {
        this.music = music;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}
