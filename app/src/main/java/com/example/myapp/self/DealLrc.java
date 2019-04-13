package com.example.myapp.self;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DealLrc {

    private List<LrcBean> list;

    public DealLrc(){
        this.list = new ArrayList<>();
    }

    public List<LrcBean> getLrcList(Music mc){
        String lrcPath = mc.getPath().split("\\.")[0]+".lrc";
        File file=new File(lrcPath);
        if(!file.exists()){
            return null;
        }
        try {

            FileInputStream in = new FileInputStream(file);
            BufferedReader reader=new BufferedReader(new InputStreamReader(in,"gbk"));
            String line;
            while ((line=reader.readLine())!=null){
                String []lrcs = line.split("\\]");
                if(lrcs.length == 1){
                    continue;
                }
                //获取到歌词
                String lrc = lrcs[1];
                String time = lrcs[0].split("\\[")[1];
                String mintue = time.split(":")[0];
                String million = time.split(":")[1];
                //转化为毫秒数
                int allTime = Integer.parseInt(mintue)*60*1000+(int)(Double.parseDouble(million)*1000);
                LrcBean lrcBean=new LrcBean(lrc,allTime);
                list.add(lrcBean);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return this.list;
    }
}
