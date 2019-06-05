package com.example.myapp.self;

import java.io.Serializable;

public class Music implements Serializable {

    private static final long serialVersionUID = 709373947980558027L;
    private int id=0;
    private String songName;
    private String songAuthor;
    private int alltime;
    private String path;
    private int songSize=0;
    //0为本地歌曲，1为网络歌曲
    private int flag = 0;
    //0为不在我喜欢的列表中，1是在我喜欢的列表中
    private int love;
    public Music(){}
    public Music(String songName, String songAuthor, String path) {
        this.songName = songName;
        this.songAuthor = songAuthor;
        this.path = path;
    }

    public Music(String songName, String songAuthor, int alltime, String path, int songSize) {
        this.songName = songName;
        this.songAuthor = songAuthor;
        this.alltime = alltime;
        this.path = path;
        this.songSize = songSize;
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "Music{" +
                "id=" + id +
                ", songName='" + songName + '\'' +
                ", songAuthor='" + songAuthor + '\'' +
                ", alltime=" + alltime +
                ", path='" + path + '\'' +
                ", songSize=" + songSize +
                ", flag=" + flag +
                ", love=" + love +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Music music = (Music) o;
        return this.path.equals(music.path);
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAlltime() {
        return alltime;
    }

    public void setAlltime(int alltime) {
        this.alltime = alltime;
    }

    public int getSongSize() {
        return songSize;
    }

    public void setSongSize(int songSize) {
        this.songSize = songSize;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public int getLove() {
        return love;
    }

    public void setLove(int love) {
        this.love = love;
    }
}
