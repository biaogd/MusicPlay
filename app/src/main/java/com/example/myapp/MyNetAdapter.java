package com.example.myapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.myapp.database.MyDao;
import com.example.myapp.self.Music;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MyNetAdapter extends BaseAdapter {
    private Context context;
    private List<Music> musicList;
    private ViewHolder holder;
    private MyDao myDao;
    public MyNetAdapter(Context context,List<Music> list){
        this.context=context;
        this.musicList=list;
        myDao = new MyDao(this.context);
    }
    @Override
    public int getCount() {
        return musicList.size();
    }

    @Override
    public Object getItem(int position) {
        return musicList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    private class ViewHolder{
        TextView songName;
        TextView songAuthor;
        ImageButton menuMusic;
        ImageButton flagBtn;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView==null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.net_item_music, null);
            holder = new ViewHolder();
            holder.songName = (TextView) convertView.findViewById(R.id.song_name_net);
            holder.songAuthor = (TextView) convertView.findViewById(R.id.song_author_net);
            holder.menuMusic = (ImageButton) convertView.findViewById(R.id.menu_music_net);
            holder.flagBtn = (ImageButton)convertView.findViewById(R.id.music_flag_btn);
            convertView.setTag(holder);
        }else{
            holder=(ViewHolder)convertView.getTag();
        }
        final Music music=musicList.get(position);
        holder.songName.setText(music.getSongName());
        holder.songAuthor.setText(music.getSongAuthor());
        final View finalConvertView = convertView;
        holder.menuMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View myView = LayoutInflater.from(context).inflate(R.layout.net_music_menu,null);
                final PopupWindow popupWindow=new PopupWindow(myView, WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
                popupWindow.setFocusable(true);
                popupWindow.setTouchable(true);
                popupWindow.setOutsideTouchable(true);
                popupWindow.showAtLocation(finalConvertView.findViewById(R.id.menu_music_net), Gravity.BOTTOM,0,0);
                TextView songNameAndAuthor = (TextView)myView.findViewById(R.id.on_song_about);
                songNameAndAuthor.setText("歌曲："+music.getSongName()+" - "+music.getSongAuthor());
                Button comeToLove= (Button)myView.findViewById(R.id.come_to_love);
                List<Music> music1=new MyDao(context).findAll("love_music_list");
                for (Music m:music1){
                    if(m.getPath().equals(music.getPath())){
                        comeToLove.setEnabled(false);
                        break;
                    }
                }
                comeToLove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        music.setLove(1);
                        new MyDao(context).insertMusic(music,"love_music_list");
                        Intent intent1=new Intent();
                        intent1.setAction("updatelove");
                        context.sendBroadcast(intent1);
                        updateLove(music,"near_music_list");
                        popupWindow.dismiss();
                    }
                });
                Button cancel = (Button)myView.findViewById(R.id.cancel_btn_net);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                    }
                });
                Button downloadBtn = (Button)myView.findViewById(R.id.download_music);
                List<Music> mList = myDao.findAll("local_music_list");
                //查找本地歌曲有没有这首歌
                for (Music m:mList){
                    if(m.getSongName().equals(music.getSongName())&&m.getSongAuthor().equals(music.getSongAuthor())){
                        downloadBtn.setVisibility(View.GONE);   //隐藏下载按钮并且不占空间
                        break;
                    }
                }
                downloadBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent("download_music");
                        intent.putExtra("music",music);
                        context.sendBroadcast(intent);
                        popupWindow.dismiss();
                    }
                });
            }
        });
        if(music.getFlag()==0){
            holder.flagBtn.setImageResource(R.mipmap.ic_phone_20);
        }else {
            holder.flagBtn.setImageResource(R.mipmap.ic_cloud_20);
        }
        return convertView;
    }

    private SQLiteDatabase getSQLiteDB(){
        return context.openOrCreateDatabase("mydb.db", Context.MODE_PRIVATE,null);
    }

    /**
     * 获取一个数据库当中是否包含正在操作的歌曲
     * @param music 正在操作的歌曲对象
     * @param tableName 要查询的数据库
     * @return  -1，数据库中不包含这个歌曲；0，包含，但该歌曲不是我喜欢的；1，这个歌曲在，并且是我喜欢的
     */
    private int selectByPath(Music music,String tableName){
        SQLiteDatabase db = getSQLiteDB();
        Cursor cursor=db.query(tableName,new String[]{"love"},"path=?",new String[]{music.getPath()},null,null,null);
        int size = cursor.getCount();
        Log.i(tableName,"size = "+size);
        int love=0;
        if(size >0) {
            cursor.moveToFirst();
            love = cursor.getInt(cursor.getColumnIndexOrThrow("love"));
        }
        if(db.isOpen()) {
            db.close();
        }
        if(size==0){
            return -1;
        }
        Log.i(tableName,"love="+love);
        return love;
    }

    /**
     * 更新一个数据库中的love列，并且在喜欢和不喜欢之间自由转化
     * @param music 要更新的歌曲
     * @param tableName 数据库表名
     */
    private int updateLove(Music music,String tableName){
        SQLiteDatabase db = getSQLiteDB();
        int i=selectByPath(music,tableName);
        int j=0;
        if(i>-1) {
            j = i>0?0:1;
            Log.i(tableName,""+j);
            ContentValues values = new ContentValues();
            values.put("love",j);
            db.update(tableName,values,"path=?",new String[]{music.getPath()} );
        }
        if(db.isOpen()) {
            db.close();
        }
        return j;
    }
    //开启一个线程下载音乐
    public void downloadMusic(final Music music, final String url){
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client=new OkHttpClient.Builder().connectTimeout(5, TimeUnit.SECONDS)
                        .build();
                Request request=new Request.Builder().url(url).build();
                try {
                    Response response=client.newCall(request).execute();
                    if(response.isSuccessful()) {
                        Intent intent1=new Intent("what_download");
                        intent1.putExtra("music",music);
                        context.sendBroadcast(intent1);
                        InputStream inputStream = response.body().byteStream();
                        String name = response.header("Content-Disposition");
                        long size = response.body().contentLength();
                        String []strs = name.split("=");
                        String fileName="";
                        if(strs.length>1){
                            fileName = strs[1];
                        }
                        Log.i("文件名",fileName);
                        FileOutputStream out=new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath()+"/downloadMusic/"+fileName);
                        int i=0,j=0;
                        byte[] bytes=new byte[1024];
                        while ((i = inputStream.read(bytes))!=-1){
                            out.write(bytes,0,i);
                            j = j+i;
                            Intent intent=new Intent("update_dw_progress");
                            intent.putExtra("progress",(int)(j*100/size));
                            intent.putExtra("music",music);
                            context.sendBroadcast(intent);
                        }
                        Log.i("音乐"+fileName,"下载完毕");
                    }
                } catch (Exception e) {
                    if(e instanceof ConnectException){

                    }
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
