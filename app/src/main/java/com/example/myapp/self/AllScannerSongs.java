package com.example.myapp.self;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.example.myapp.database.MyDao;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AllScannerSongs {
    private static final String dir = Environment.getExternalStorageDirectory().getAbsolutePath();
    private static List<String> songs=new ArrayList<>();
    private static int getAll(String direct){
        File file=new File(direct);
        if(file.isDirectory()){
            File []files = file.listFiles();
            for (File f:files){
                getAll(f.getAbsolutePath());
            }
        }else {
            if(file.getName().endsWith(".mp3")){
                songs.add(file.getAbsolutePath());
            }
        }
        return 0;
    }
    //自定义的sd卡歌曲扫描
    public static List<Music> getMusicFromSdcard(Context context){
        MediaPlayer player=new MediaPlayer();
        List<Music> loveList=new MyDao(context).findAll("love_music_list");
        List<Music> list=new ArrayList<>();
        String name;
        String author;
        int alltime=0;
        String path;
        int songSize;
        int flag=0;
        int love=0;
        Music music;
        songs.clear();
        File file;
        getAll(dir);
        for(String p:songs){
            love=0;
            file=new File(p);
            String []strs=file.getName().split("-");
            if(strs.length>1){
                author = strs[0].trim();
                name = strs[1].split("\\.")[0].trim();
                try {
                    player.reset();
                    player.setDataSource(p);
                    player.prepare();
                    alltime = player.getDuration();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                path = p;
                songSize=(int)file.length();
                for(Music ms:loveList){
                    if(ms.getFlag()==0&&ms.getLove()==1&&ms.getPath().equals(path)){
                        love=1;
                        break;
                    }
                }
                music=new Music(name,author,alltime,path,songSize);
                music.setFlag(flag);
                music.setLove(love);
                list.add(music);
            }
        }
        return list;
    }

    public static List<Music> getAllMusicFromSdcard(Context context){
        List<Music> list=new ArrayList<>();
        String name;
        String author;
        int alltime;
        String path;
        int songSize;
        Music music;
        ContentResolver resolver=context.getContentResolver();
        Cursor cursor=resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,null,null,null,MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if(cursor.moveToFirst()){
            do {
                name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                author = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                alltime = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                String []ps=path.split("\\.");
                Log.i(path+";后缀：",ps[ps.length-1]);
                if(!ps[ps.length-1].equals("mp3")){
                    continue;
                }
                songSize  =cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                music = new Music(name,author,alltime,path,songSize);
                list.add(music);
            }while (cursor.moveToNext());
        }

        return list;
    }
}
