package com.example.myapp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.myapp.self.Music;

import java.util.List;

/**
 * 这个适配器是用于适配播放列表的ListView，
 */
public class MyAdapter2 extends BaseAdapter {
    private List<Music> musicList;
    private Context context;
    private ViewHolder holder;
    public MyAdapter2(Context context, List<Music> list){
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
    public class ViewHolder{
        TextView songName;
        TextView songAuthor;
        ImageButton deleteBtn;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.song_manager,null);
            holder = new ViewHolder();
            holder.songName=(TextView)convertView.findViewById(R.id.song_about_name);
            holder.songAuthor=(TextView)convertView.findViewById(R.id.song_about_author);
            holder.deleteBtn=(ImageButton)convertView.findViewById(R.id.song_delete_button);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder)convertView.getTag();
        }
        final Music music=musicList.get(position);
        holder.songName.setText(music.getSongName());
        holder.songAuthor.setText(" - "+music.getSongAuthor());
        if(music!=null && music.getId()==-1000){
            Log.i("在适配器当中","执行了更新颜色操作"+music.getPath());
            holder.songName.setTextColor(context.getResources().getColor(R.color.beautiful));
            holder.songAuthor.setTextColor(context.getResources().getColor(R.color.beautiful));
        }else {
            holder.songName.setTextColor(context.getResources().getColor(R.color.base_color));
            holder.songAuthor.setTextColor(context.getResources().getColor(R.color.base_color));
        }
        if(music!=null && music.getId() == -1){
            Log.i("id等于-1","找到了");
            holder.songName.setTextColor(context.getResources().getColor(R.color.base_color));
            holder.songAuthor.setTextColor(context.getResources().getColor(R.color.base_color));
        }
        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //发送广播给service删除歌曲
                Intent intent=new Intent();
                intent.setAction("deleteMusicFromList");
                intent.putExtra("delete_music",music);
                context.sendBroadcast(intent);
                musicList.remove(position);
                notifyDataSetChanged();
            }
        });
        return convertView;
    }
}
