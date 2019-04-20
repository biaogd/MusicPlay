package com.example.myapp.self;

import java.io.Serializable;

public class LrcBean implements Serializable {
    private static final long serialVersionUID = -8931844425754525250L;
    private String lrc;
    private int beginTime;
//    private int allCount;       //连这个歌词总共的自动换行数

    public LrcBean(String lrc, int beginTime) {
        this.lrc = lrc;
        this.beginTime = beginTime;
    }

    @Override
    public String toString() {
        return "LrcBean{" +
                "lrc='" + lrc + '\'' +
                ", beginTime='" + beginTime + '\'' +
                '}';
    }

    public String getLrc() {
        return lrc;
    }

    public void setLrc(String lrc) {
        this.lrc = lrc;
    }

    public int getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(int beginTime) {
        this.beginTime = beginTime;
    }

}
