package com.example.myapp.self;

import java.io.Serializable;

public class DownloadBean implements Serializable {
    private static final long serialVersionUID = 1804898122583580974L;

    private Music music;
    private int progress;
    private int flag;
    public DownloadBean(Music music, int progress) {
        this.music = music;
        this.progress = progress;
    }

    public DownloadBean(Music music, int progress, int flag) {
        this.music = music;
        this.progress = progress;
        this.flag = flag;
    }

    @Override
    public String toString() {
        return "DownloadBean{" +
                "music=" + music +
                ", progress=" + progress +
                ", flag=" + flag +
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

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }
}
