package com.example.myapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.myapp.self.Music;
import com.example.myapp.self.SongListBean;

import java.util.ArrayList;
import java.util.List;

public class MyDao {
    private Context context;
    private static SQLiteDatabase db;
    private static MyDao dao;
    public MyDao(Context context){
        this.context = context;
    }

    public SQLiteDatabase getSQLiteDB(){
        return context.openOrCreateDatabase("mydb.db", Context.MODE_PRIVATE,null);
    }

    public void initConnect(){
        db = context.openOrCreateDatabase("mydb.db", Context.MODE_PRIVATE,null);
    }
    public boolean isConnection(){
        return db.isOpen();
    }
    public void closeConnect(){
        if(isConnection()){
            db.close();
        }
    }

    public List<Music> findAll(String tableName){
        List<Music> list=new ArrayList<>();
//        db= getSQLiteDB();
        if(!isConnection()){
            initConnect();
        }
        Cursor cursor = db.query(tableName,null,null,null,null,null,null);
            while (cursor.moveToNext()){
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
            }
        //关闭数据库连接
        if(!cursor.isClosed()){
            cursor.close();
        }
//        if (db.isOpen()){
//            db.close();
//        }
        return list;
    }

    public List<Music> findAll(int listId,String tableName){
        List<Music> list=new ArrayList<>();
//        db= getSQLiteDB();
        if(!isConnection()){
            initConnect();
        }
        Cursor cursor = db.query(tableName,null,"list_id=?",new String[]{String.valueOf(listId)},null,null,null);
        while (cursor.moveToNext()){
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
        }
        //关闭数据库连接
        if(!cursor.isClosed()){
            cursor.close();
        }
        return list;
    }

    //清空一个自定义歌单的歌曲
    public void clearListSong(SongListBean bean){
        if(!isConnection()){
            initConnect();
        }
        db.delete("self_music_list","list_id=?",new String[]{String.valueOf(bean.getListId())});
    }

    //清空表
    public void clearTable(String tableName){
//        db = getSQLiteDB();
        if(!isConnection()){
            initConnect();
        }
        int i=db.delete(tableName,null,null);
        Log.i("清空了表"+tableName,i+"条数据");
    }

    public SQLiteDatabase newDB(){
        if(!isConnection()){
            initConnect();
        }
        return db;
    }

    //清空音乐列表的喜欢标记,设置love=0
    public int clearLove(String tableName){
        if(!isConnection()){
            initConnect();
        }
        ContentValues values=new ContentValues();
        values.put("love",0);
        int i = db.update(tableName,values,null,null);
        return i;
    }

    public long insertMusic(Music music,String tableName){
        if(!isConnection()){
            initConnect();
        }
        ContentValues values=new ContentValues();
        values.put("song_name",music.getSongName());
        values.put("song_author",music.getSongAuthor());
        values.put("all_time",music.getAlltime());
        values.put("path",music.getPath());
        values.put("song_size",music.getSongSize());
        values.put("flag",music.getFlag());
        values.put("love",music.getLove());
        long i=db.insert(tableName,null,values);
        System.out.println("歌曲:"+music.getSongName()+";插入"+tableName+"表成功");
        return i;
    }


    //自定义歌单，插入信息的方法
    public long insertMusic(int listId, Music music, String tableName){
//        db = getSQLiteDB();
        if(!isConnection()){
            initConnect();
        }
        ContentValues values=new ContentValues();
        values.put("list_id",listId);
        values.put("song_name",music.getSongName());
        values.put("song_author",music.getSongAuthor());
        values.put("all_time",music.getAlltime());
        values.put("path",music.getPath());
        values.put("song_size",music.getSongSize());
        values.put("flag",music.getFlag());
        values.put("love",music.getLove());
        long i=db.insert(tableName,null,values);
        return i;
    }

    public long deleteMusic(Music music, String tableName){
//        db = getSQLiteDB();
        if(!isConnection()){
            initConnect();
        }
        String dpath = music.getPath();
        long row = db.delete(tableName,"path=?",new String[]{dpath});
        return row;
    }
    //删除歌单的一个歌曲
    public long deleteMusic(Music music, String tableName, SongListBean bean){
        if(!isConnection()){
            initConnect();
        }
        int listId = bean.getListId();
        long row = db.delete(tableName,"list_id=? and song_name=? and song_author=?",new String[]{String.valueOf(listId),music.getSongName(),music.getSongAuthor()});
        return row;
    }
    public List<Music> findMusicByKeyword(String word){
        List<Music> musicList=new ArrayList<>();
        if(!isConnection()){
            initConnect();
        }
        String sql = "select *from local_music_list where path like ?";
        Cursor cursor=db.rawQuery(sql,new String[]{"%"+word+"%"});
        while (cursor.moveToNext()) {
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
        }
        if(!cursor.isClosed()){
            cursor.close();
        }
        return musicList;
    }

    public int allCount(String tableName,int listId){
        if(!isConnection()){
            initConnect();
        }
        int count = 0;
        //固定的歌单
        if(listId==0){
            String sql= "select count(*) as cou from "+tableName;
            Cursor cursor= db.rawQuery(sql,new String[]{});
            if(cursor.moveToFirst()){
                count = cursor.getInt(cursor.getColumnIndexOrThrow("cou"));
            }
            if(!cursor.isClosed()){
                cursor.close();
            }
        }else {
            String sql= "select count(*) as cou from "+tableName+" where list_id= ?";
            Cursor cursor= db.rawQuery(sql,new String[]{String.valueOf(listId)});
            if(cursor.moveToFirst()){
                count = cursor.getInt(cursor.getColumnIndexOrThrow("cou"));
            }
            if(!cursor.isClosed()){
                cursor.close();
            }
        }
        return count;
    }

    public void setFlagWithDeleteMusic(Music music, int flag){
        if(!isConnection()){
            initConnect();
        }
        ContentValues values=new ContentValues();
        values.put("flag",flag);
        db.update("near_music_list",values,"path=?",new String[]{music.getPath()});
        db.update("download_music_list",values,"path=?",new String[]{music.getPath()});
        db.update("love_music_list",values,"path=?",new String[]{music.getPath()});
        db.update("self_music_list",values,"path=?",new String[]{music.getPath()});
    }

    public void updateFlagWithLogin(Music music,int love){
        if(!isConnection()){
            initConnect();
        }
        ContentValues values=new ContentValues();
        values.put("love",love);
        db.update("near_music_list",values,"path=?",new String[]{music.getPath()});
        db.update("download_music_list",values,"path=?",new String[]{music.getPath()});
        db.update("local_music_list",values,"path=?",new String[]{music.getPath()});
        db.update("self_music_list",values,"path=?",new String[]{music.getPath()});
    }
}
