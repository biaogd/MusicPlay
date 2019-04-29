package com.example.myapp.self;

public class UserBean {
    private String id;
    private String userName;
    private int imageName;

    public UserBean(String id, String userName, int imageName) {
        this.id = id;
        this.userName = userName;
        this.imageName = imageName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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
}
