package com.example.myapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import com.example.myapp.self.SongIdBean;
import com.example.myapp.self.SongListBean;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SelfAdapter extends BaseAdapter {
    private Activity context;
    private List<SongIdBean> songIdBeanList;
    private MyDao myDao;
    private ViewHolder holder;
    public SelfAdapter(Activity activity,List<SongIdBean> songIdBeanList){
        this.context = activity;
        this.songIdBeanList = songIdBeanList;
        myDao=new MyDao(activity);
    }

    @Override
    public int getCount() {
        return songIdBeanList.size();
    }

    @Override
    public Object getItem(int position) {
        return songIdBeanList.get(position);
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
    public View getView(final int position, View convertView, ViewGroup parent) {
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
        final SongIdBean idBean=songIdBeanList.get(position);
        final Music music=idBean.getMusic();
        //获取歌曲的id
        final int id = idBean.getId();
        holder.songName.setText(music.getSongName());
        holder.songAuthor.setText(music.getSongAuthor());
        final View finalConvertView = convertView;
        holder.menuMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view=LayoutInflater.from(context).inflate(R.layout.self_list_menu,null);
                final PopupWindow window=new PopupWindow(view, WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
                window.setFocusable(true);
                window.setTouchable(true);
                window.setOutsideTouchable(true);
                WindowManager.LayoutParams lp =context.getWindow().getAttributes();
                lp.alpha=0.5f;
                context.getWindow().setAttributes(lp);

                window.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        //恢复透明度
                        WindowManager.LayoutParams lp =context.getWindow().getAttributes();
                        lp.alpha=1f;
                        context.getWindow().setAttributes(lp);
                    }
                });
                window.showAtLocation(finalConvertView.findViewById(R.id.menu_music_net), Gravity.BOTTOM,0,0);
                TextView songNameAndAuthor = (TextView)view.findViewById(R.id.self_song_about);
                songNameAndAuthor.setText("歌曲："+music.getSongName()+" - "+music.getSongAuthor());
                Button cancel = (Button)view.findViewById(R.id.self_cancel_btn);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        window.dismiss();
                    }
                });
                Button downloadBtn = (Button)view.findViewById(R.id.self_download_music);
                if(music.getFlag()==1) {
                    List<Music> mList = myDao.findAll("local_music_list");
                    //查找本地歌曲有没有这首歌
                    int i=0;
                    for (i=0;i<mList.size();i++) {
                        Music m=mList.get(i);
                        if (m.getSongName().equals(music.getSongName()) && m.getSongAuthor().equals(music.getSongAuthor())) {
                            downloadBtn.setEnabled(false);
                            break;
                        }
                    }
                    if(i>=mList.size()){
                        downloadBtn.setEnabled(true);
                    }
                    downloadBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent("download_music");
                            intent.putExtra("music", music);
                            context.sendBroadcast(intent);
                            window.dismiss();
                        }
                    });
                }else {
                    downloadBtn.setEnabled(false);
                }
                Button deleteBtn = (Button)view.findViewById(R.id.self_delete_music);
                deleteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        songIdBeanList.remove(position);
                        notifyDataSetChanged();
                        syncNetDelMusicFromSongListById(music,idBean);
                        Log.i("要删除的歌曲的id=",idBean.getId()+"");
                        window.dismiss();

                    }
                });
            }
        });
        if(music.getFlag()==0){
            holder.flagBtn.setImageResource(R.mipmap.ic_phone_20);
            holder.songName.setTextColor(context.getResources().getColor(R.color.color1));
            holder.songAuthor.setTextColor(context.getResources().getColor(R.color.color1));
        }else if(music.getFlag()==1){
            holder.flagBtn.setImageResource(R.mipmap.ic_cloud_20);
            holder.songName.setTextColor(context.getResources().getColor(R.color.color1));
            holder.songAuthor.setTextColor(context.getResources().getColor(R.color.color1));
        }else {
            holder.flagBtn.setImageResource(R.mipmap.ic_phone_20);
            holder.songName.setTextColor(context.getResources().getColor(R.color.gray));
            holder.songAuthor.setTextColor(context.getResources().getColor(R.color.gray));
        }
        return convertView;
    }

    private void syncNetDelMusicFromSongListById(Music music, SongIdBean bean){
        OkHttpClient client=new OkHttpClient();
//        String songName = music.getSongName();
//        String songAuthor = music.getSongAuthor();
        int songId = bean.getId();
        String url = "http://www.mybiao.top:8000/music/user/syncDelMusicById?songId="+songId;
        String urls = "http://192.168.43.119:8000/music/user/syncDelMusicById?songId="+songId;
        Request request=new Request.Builder().url(urls).get().build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });

    }
}
