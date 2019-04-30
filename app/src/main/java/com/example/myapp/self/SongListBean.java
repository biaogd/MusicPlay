package com.example.myapp.self;

import java.io.Serializable;

public class SongListBean implements Serializable {
    private static final long serialVersionUID = 3933676773302656646L;
    private int listId;
    private String listName;
    private int listCount;

    public SongListBean(int listId, String listName, int listCount) {
        this.listId = listId;
        this.listName = listName;
        this.listCount = listCount;
    }

    public int getListId() {
        return listId;
    }

    public void setListId(int listId) {
        this.listId = listId;
    }

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

    public int getListCount() {
        return listCount;
    }

    public void setListCount(int listCount) {
        this.listCount = listCount;
    }
}
