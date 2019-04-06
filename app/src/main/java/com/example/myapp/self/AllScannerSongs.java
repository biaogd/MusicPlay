package com.example.myapp.self;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class AllScannerSongs {

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
