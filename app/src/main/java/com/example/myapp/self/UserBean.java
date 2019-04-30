package com.example.myapp.self;

import java.io.Serializable;
import java.util.List;

public class UserBean implements Serializable {

    private static final long serialVersionUID = -8146579401403692045L;
    private int id;
    private String userName;
    private int imageName;
    private List<SongListBean> songList;
    public UserBean(int id, String userName, int imageName) {
        this.id = id;
        this.userName = userName;
        this.imageName = imageName;
    }

    public UserBean(int id, String userName, int imageName, List<SongListBean> songList) {
        this.id = id;
        this.userName = userName;
        this.imageName = imageName;
        this.songList = songList;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getImageName() {
        return imageName;
    }

    public void setImageName(int imageName) {
        this.imageName = imageName;
    }

    public List<SongListBean> getSongList() {
        return songList;
    }

    public void setSongList(List<SongListBean> songList) {
        this.songList = songList;
    }
}
