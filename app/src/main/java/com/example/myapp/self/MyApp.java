package com.example.myapp.self;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class MyApp implements Serializable {
    private static final long serialVersionUID = 4380123940110964099L;
    @SerializedName("Content")
    private String content;     //更新的内容
    @SerializedName("Name")
    private String name;        //软件包的名字
    @SerializedName("Status")
    private String status;      //状态，ok，有最新版，no，无最新版

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
