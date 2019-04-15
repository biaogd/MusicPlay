package com.example.myapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.myapp.self.Music;

import java.util.ArrayList;
import java.util.List;

public class MyDao {
    private Context context;
    private SQLiteDatabase db;
    private static MyDao dao;
    public MyDao(Context context){
        this.context = context;
    }
    public MyDao getInstance(){
        if(dao == null){
            dao = new MyDao(context);
            return dao;
        }else {
            return dao;
        }
    }

    public SQLiteDatabase getSQLiteDB(){
        return context.openOrCreateDatabase("mydb.db", Context.MODE_PRIVATE,null);
    }
    public List<Music> findAll(String tableName){
        List<Music> list=new ArrayList<>();
        db= getSQLiteDB();
        Cursor cursor = db.query(tableName,null,null,null,null,null,null);
        if(cursor.moveToFirst()){
            do {
                Music music=new Music();
                music.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                music.setSongName(cursor.getString(cursor.getColumnIndexOrThrow("song_name")));
                music.setSongAuthor(cursor.getString(cursor.getColumnIndexOrThrow("song_author")));
                music.setAlltime(cursor.getInt(cursor.getColumnIndexOrThrow("all_time")));
                music.setPath(cursor.getString(cursor.getColumnIndexOrThrow("path")));
                music.setSongSize(cursor.getInt(cursor.getColumnIndexOrThrow("song_size")));
                music.setFlag(cursor.getInt(cursor.getColumnIndexOrThrow("flag")));
                music.setLove(cursor.getInt(cursor.getColumnIndexOrThrow("love")));
                list.add(music);
            }while (cursor.moveToNext());
        }
        //关闭数据库连接
        if(!cursor.isClosed()){
            cursor.close();
        }
        if (db.isOpen()){
            db.close();
        }
        return list;
    }

    public long insertMusic(Music music,String tableName){
        db = getSQLiteDB();
        ContentValues values=new ContentValues();
        values.put("song_name",music.getSongName());
        values.put("song_author",music.getSongAuthor());
        values.put("all_time",music.getAlltime());
        values.put("path",music.getPath());
        values.put("song_size",music.getSongSize());
        values.put("flag",music.getFlag());
        values.put("love",music.getLove());
        long i=db.insert(tableName,null,values);
        if(db.isOpen()){
            db.close();
        }
        return i;
    }

    public long deleteMusic(Music music, String tableName){
        db = getSQLiteDB();
        String dpath = music.getPath();
        long row = db.delete(tableName,"path=?",new String[]{dpath});
        if(db.isOpen()){
            db.close();
        }
        return row;
    }

    public List<Music> findMusicByKeyword(String word){
        List<Music> musicList=new ArrayList<>();
        db = getSQLiteDB();
        String sql = "select *from local_music_list where path like ?";
        Cursor cursor=db.rawQuery(sql,new String[]{"%"+word+"%"});
        if(cursor.moveToFirst()) {
            do {
                Music music = new Music();
                music.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                music.setSongName(cursor.getString(cursor.getColumnIndexOrThrow("song_name")));
                music.setSongAuthor(cursor.getString(cursor.getColumnIndexOrThrow("song_author")));
                music.setAlltime(cursor.getInt(cursor.getColumnIndexOrThrow("all_time")));
                music.setPath(cursor.getString(cursor.getColumnIndexOrThrow("path")));
                music.setSongSize(cursor.getInt(cursor.getColumnIndexOrThrow("song_size")));
                music.setFlag(cursor.getInt(cursor.getColumnIndexOrThrow("flag")));
                music.setLove(cursor.getInt(cursor.getColumnIndexOrThrow("love")));
                musicList.add(music);
            } while (cursor.moveToNext());
        }
        if(db.isOpen()){
            db.close();
        }
        return musicList;
    }
}
