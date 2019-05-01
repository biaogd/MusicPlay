package com.example.myapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.myapp.self.Music;
import com.example.myapp.self.SongIdBean;

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

    public List<SongIdBean> findAll(int listId,String tableName){
        List<SongIdBean> list=new ArrayList<>();
        db= getSQLiteDB();
        Cursor cursor = db.query(tableName,null,"list_id=?",new String[]{String.valueOf(listId)},null,null,null);
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
                int songId = cursor.getInt(cursor.getColumnIndexOrThrow("song_id"));
                SongIdBean idBean=new SongIdBean(songId,music);
                list.add(idBean);
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

    //清空表
    public void clearTable(String tableName){
        db = getSQLiteDB();
        int i=db.delete(tableName,null,null);
        Log.i("清空了表"+tableName,i+"条数据");
        if(db.isOpen()){
            db.close();
        }
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


    //自定义歌单，插入信息的方法,线程安全的
    public synchronized long insertMusic(int listId, SongIdBean bean, String tableName){
        db = getSQLiteDB();
        Music music=bean.getMusic();
        ContentValues values=new ContentValues();
        values.put("song_id",bean.getId());
        values.put("list_id",listId);
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

    public int allCount(String tableName,int listId){
        db = getSQLiteDB();
        int count = 0;
        //固定的歌单
        if(listId==0){
            String sql= "select count(*) as cou from "+tableName;
            Cursor cursor= db.rawQuery(sql,new String[]{});
            if(cursor.moveToFirst()){
                count = cursor.getInt(cursor.getColumnIndexOrThrow("cou"));
            }
            cursor.close();
        }else {
            String sql= "select count(*) as cou from "+tableName+" where list_id= ?";
            Cursor cursor= db.rawQuery(sql,new String[]{String.valueOf(listId)});
            if(cursor.moveToFirst()){
                count = cursor.getInt(cursor.getColumnIndexOrThrow("cou"));
            }
            cursor.close();
        }

        if(db.isOpen()){
            db.close();
        }
        return count;
    }
}
