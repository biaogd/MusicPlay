package com.example.myapp;

import android.content.Context;
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

import java.util.List;

public class MyNetAdapter extends BaseAdapter {
    private Context context;
    private List<Music> musicList;
    private ViewHolder holder;
    public MyNetAdapter(Context context,List<Music> list){
        this.context=context;
        this.musicList=list;
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
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView==null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.net_item_music, null);
            holder = new ViewHolder();
            holder.songName = (TextView) convertView.findViewById(R.id.song_name_net);
            holder.songAuthor = (TextView) convertView.findViewById(R.id.song_author_net);
            holder.menuMusic = (ImageButton) convertView.findViewById(R.id.menu_music_net);
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
            }
        });
        return convertView;
    }
}
