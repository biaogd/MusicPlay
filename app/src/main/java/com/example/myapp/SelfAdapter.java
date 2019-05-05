package com.example.myapp;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapp.database.MyDao;
import com.example.myapp.self.Music;
import com.example.myapp.self.MyLogin;
import com.example.myapp.self.SelfFinal;
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
    private List<Music> musicList;
    private MyDao myDao;
    private ViewHolder holder;
    private SongListBean songListBean;
    private SQLiteDatabase db;
    private static final String local_stable = "local_music_list";
    private static final String near_stable = "near_music_list";
    private static final String download_stable = "download_music_list";
    private static final String love_stable = "love_music_list";
    public SelfAdapter(Activity activity,List<Music> musicList,SongListBean songListBean){
        this.context = activity;
        this.musicList = musicList;
        this.songListBean = songListBean;
        myDao=new MyDao(activity);
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
        final Music music=musicList.get(position);
        if(music.getId()==-1000){
            if(music.getFlag()!=-1) {
                holder.songName.setTextColor(context.getResources().getColor(R.color.beautiful));
                holder.songAuthor.setTextColor(context.getResources().getColor(R.color.beautiful));
            }else {
//                holder.flagBtn.setImageResource(R.mipmap.ic_phone_20);
                holder.songName.setTextColor(context.getResources().getColor(R.color.gray));
                holder.songAuthor.setTextColor(context.getResources().getColor(R.color.gray));
            }
        }else {
            if(music.getFlag()!=-1) {
                holder.songName.setTextColor(context.getResources().getColor(R.color.color1));
                holder.songAuthor.setTextColor(context.getResources().getColor(R.color.black));
            }else {
                holder.songName.setTextColor(context.getResources().getColor(R.color.gray));
                holder.songAuthor.setTextColor(context.getResources().getColor(R.color.gray));
            }
        }
        if(music.getId() == -1){
            if(music.getFlag()!=-1) {
                holder.songName.setTextColor(context.getResources().getColor(R.color.color1));
                holder.songAuthor.setTextColor(context.getResources().getColor(R.color.black));
            }else {
                holder.songName.setTextColor(context.getResources().getColor(R.color.gray));
                holder.songAuthor.setTextColor(context.getResources().getColor(R.color.gray));
            }
        }
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
                        musicList.remove(position);
                        notifyDataSetChanged();
                        //删除本地歌曲
                        long ii = myDao.deleteMusic(music,"self_music_list",songListBean);
                        //删除服务器歌曲
                        if(ii>0) {
                            syncNetDelMusicFromSongList(music, songListBean);
                            Toast.makeText(context, "已删除", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(context, "删除失败", Toast.LENGTH_SHORT).show();

                        }
                        window.dismiss();

                    }
                });
                final Button comeToList=(Button)view.findViewById(R.id.come_to_list);
                if(music.getFlag()==-1){
                    comeToList.setEnabled(false);
                }
                comeToList.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        window.dismiss();
                        showAddListWindow(context,comeToList,music);
                    }
                });
            }
        });
        ImageButton flagBtn = (ImageButton)convertView.findViewById(R.id.music_flag_btn);
        if(music.getFlag()==1){
            flagBtn.setImageResource(R.mipmap.ic_cloud_20);
        }else{
            flagBtn.setImageResource(R.mipmap.ic_phone_20);
        }
        return convertView;
    }
    private void updatelocalLove(Music music,String tableName,int loved){
        db = myDao.newDB();
        ContentValues values=new ContentValues();
        values.put("love",loved);
        db.update(tableName,values,"path=?",new String[]{music.getPath()});
    }

    //修改数据库love的值为1
    private void updateAllLove(Music music,int loved){
        //把本地歌单love修改为1
        updatelocalLove(music,local_stable,loved);
        updatelocalLove(music,near_stable,loved);
        updatelocalLove(music,download_stable,loved);
        updatelocalLove(music,love_stable,loved);
        //把自定义歌单love改为1
        updatelocalLove(music,"self_music_list",loved);
    }
    public void showAddListWindow(final Activity context, View p, final Music music){
        List<SongListBean> songListBeanList=MyLogin.bean.getSongList();
        View views = LayoutInflater.from(context).inflate(R.layout.add_song_list,null);
        LinearLayout body_layout = (LinearLayout)views.findViewById(R.id.song_list_body);
        final PopupWindow popupWindow=new PopupWindow(views,WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
        for(final SongListBean bean:songListBeanList){
            View view1=LayoutInflater.from(context).inflate(R.layout.button_layout,null);
            Button button=(Button)view1.findViewById(R.id.self_button);
            button.setText(bean.getListName());
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    List<Music> allMusic;
                    //获得这个歌单的所有歌曲
                    if(bean.getListId()==MyLogin.loveId) {
                        allMusic = myDao.findAll("love_music_list");
                    }else {
                        allMusic = myDao.findAll(bean.getListId(),"self_music_list");
                    }
                    int i=0;
                    for(i=0;i<allMusic.size();i++){
                        Music mm = allMusic.get(i);
                        if(mm.getSongName().equals(music.getSongName())&&mm.getSongAuthor().equals(music.getSongAuthor())){
                            //歌曲已经在这个歌单中
                            break;
                        }
                    }
                    if(i>=allMusic.size()) {
                            //歌曲不再这个歌单中
                            int listId = bean.getListId();
                            //将这个歌曲加入到本地数据库
                            long iii=0;
                            if(bean.getListId()==MyLogin.loveId){
                                music.setLove(1);
                                iii=myDao.insertMusic(music,"love_music_list");
                                updateAllLove(music,1);
                            }else {
                                iii = myDao.insertMusic(listId, music, "self_music_list");
                            }
                            if(iii>0) {
                                Log.i("歌曲" + music.getSongName(), "已加入到数据库中");
                                //在把这个歌曲同步到服务器
                                syncSongList(music, bean);
                                Toast.makeText(context, "歌曲已加入歌单中", Toast.LENGTH_LONG).show();
                            }
                    }else {
                        Toast.makeText(context,"已存在",Toast.LENGTH_LONG).show();
                    }
                    popupWindow.dismiss();
                }
            });
            body_layout.addView(view1);
        }
        popupWindow.setFocusable(true);
        popupWindow.setTouchable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0x000000));
        popupWindow.setOutsideTouchable(true);
        //设置弹出窗口背景变半透明，来高亮弹出窗口
        WindowManager.LayoutParams lp =context.getWindow().getAttributes();
        lp.alpha=0.5f;
        context.getWindow().setAttributes(lp);

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                //恢复透明度
                WindowManager.LayoutParams lp =context.getWindow().getAttributes();
                lp.alpha=1f;
                context.getWindow().setAttributes(lp);
            }
        });
        popupWindow.showAtLocation(p,Gravity.BOTTOM,0,0);
    }

    private void syncSongList(Music m, SongListBean bean) {
        OkHttpClient client = new OkHttpClient();
        //用户id
        int userId = MyLogin.bean.getId();
        int listId = bean.getListId();
        int mId = m.getFlag();
        RequestBody body = new FormBody.Builder().add("user_id", String.valueOf(userId))
                .add("song_list_id", String.valueOf(listId))
                .add("music_id", String.valueOf(mId))
                .add("music_name", m.getSongName())
                .add("music_author", m.getSongAuthor())
                .add("music_path", m.getPath()).build();
        String urls = SelfFinal.host+SelfFinal.port +"/music/user/syncAddMusic";
        Request request = new Request.Builder().post(body).url(urls).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i("上传新的歌曲","成功");
            }
        });
    }

    private void syncNetDelMusicFromSongList(Music music,SongListBean bean){
        OkHttpClient client=new OkHttpClient();
        int listId = bean.getListId();
        String songName = music.getSongName();
        String songAuthor = music.getSongAuthor();
        RequestBody body=new FormBody.Builder().add("listId",String.valueOf(listId))
                .add("songName",songName)
                .add("songAuthor",songAuthor).build();
        String url = "http://www.mybiao.top:8000/music/user/syncDelMusic";
        String urls = "http://192.168.0.106:8000/music/user/syncDelMusic";
        Request request=new Request.Builder().url(urls).post(body).build();
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
