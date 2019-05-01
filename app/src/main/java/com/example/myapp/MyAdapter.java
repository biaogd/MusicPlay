package com.example.myapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.app.Fragment;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.Toast;

import com.example.myapp.self.Music;
import com.example.myapp.self.MyLogin;
import com.example.myapp.self.SongListBean;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyAdapter extends BaseAdapter {

    private List<Music> mList;
    private LayoutInflater inflater;
    private Fragment fragment;
    private SQLiteDatabase db;
    private Activity context;
    //正在播放的歌曲在列表中的位置
    private int nowPosition;

    private static final String local_stable = "local_music_list";
    private static final String near_stable = "near_music_list";
    private static final String download_stable = "download_music_list";
    private static final String love_stable = "love_music_list";
    private int index=-1;
    public MyAdapter(Activity context,List<Music> list ,Fragment fragment){
        this.inflater = LayoutInflater.from(context);
        this.mList = list;
        this.context = context;
        this.fragment = fragment;
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
            ContentValues values = new ContentValues();
            values.put("love",j);
            db.update(tableName,values,"path=?",new String[]{music.getPath()} );
        }
        if(db.isOpen()) {
            db.close();
        }
        return j;
    }

    protected long insertMusic(Music music,String tableName){
        SQLiteDatabase db = getSQLiteDB();
        ContentValues values=new ContentValues();
        values.put("song_name",music.getSongName());
        values.put("song_author",music.getSongAuthor());
        values.put("all_time",music.getAlltime());
        values.put("path",music.getPath());
        values.put("song_size",music.getSongSize());
        values.put("flag",music.getFlag());
        values.put("love",1);
        long i=db.insert(tableName,null,values);
        if(db.isOpen()){
            db.close();
        }
        return i;
    }

    private void deleteLove(Music music,String tableName){
        SQLiteDatabase db=getSQLiteDB();
        db.delete(tableName,"path=?",new String[]{music.getPath()});
        if(db.isOpen()){
            db.close();
        }
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private int []images=new int[]{R.mipmap.ic_heart_48,R.mipmap.ic_heart_red_48};
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        if(convertView ==null){
            convertView = inflater.inflate(R.layout.song,null);
        }
        final Music m = mList.get(position);
        TextView name = (TextView) convertView.findViewById(R.id.songName);
        TextView author = (TextView)convertView.findViewById(R.id.songAuthor);
        final ImageButton loveBtn = (ImageButton)convertView.findViewById(R.id.loveImageButton);
        ImageButton moreBtn = (ImageButton)convertView.findViewById(R.id.moreImageButton);
        //id等于-1000的是当前正在播放的歌曲对象，设置他的颜色为绿色，出现了第一屏正常显示，滑动之后出现每隔几行
        //字体颜色也为绿色的问题，主要原因是使用了convertView，这个是实现了缓存的
        //对于之前的多个歌曲都是显示绿色重复的问题，原因是ListView的复用问题，没有设置id不等于-1000的行的颜色，复用之前的
        //行就可能会出现复用了刚好是绿色的哪一个View，所以出现多个“正在播放”
        //在第一屏之后会复用前面已经渲染的View，之前在这个if语句中没有加入else条件，造成滑动后颜色重复问题
        //对于解决这个问题，就是对所有的条件重写，解决复用的问题，加入else条件即可
        if(m!=null && m.getId()==-1000){
            Log.i("在适配器当中","执行了更新颜色操作"+m.getPath());
            name.setTextColor(context.getResources().getColor(R.color.beautiful));
            author.setTextColor(context.getResources().getColor(R.color.beautiful));
        }else {
            name.setTextColor(context.getResources().getColor(R.color.color1));
            author.setTextColor(context.getResources().getColor(R.color.black));
        }
        if(m!=null && m.getId() == -1){
            name.setTextColor(context.getResources().getColor(R.color.color1));
            author.setTextColor(context.getResources().getColor(R.color.black));
        }
        //在我喜欢的音乐列表中隐藏按钮
        if(fragment instanceof LoveMusicFragment){
            loveBtn.setVisibility(View.GONE);
        }
        loveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkNet(context)) {
                    if (!MyLogin.getMyLogin().isLogin()) {
                        //如果没有用户登录，直接跳转到用户登录界面
                        Intent intents = new Intent(context, Login_in.class);
                        context.startActivity(intents);
                    }else {
                        Intent intent = new Intent("update_service_love");
                        SongListBean bean=new SongListBean(MyLogin.getMyLogin().getLoveId(),null,0);
                        if (fragment instanceof LoveMusicFragment) {
                            updateLove(m, near_stable);
                            deleteLove(m, love_stable);
                            updateLove(m, local_stable);
                            updateLove(m, download_stable);
                            mList.remove(position);
                            notifyDataSetChanged();
                        } else if (fragment instanceof LocalMusicListFragment) {
                                int i = updateLove(m, local_stable);
                                loveBtn.setImageResource(images[i]);
                                updateLove(m, near_stable);
                                updateLove(m, download_stable);
                                if (m.getLove() == 0) {
                                    m.setLove(1);
                                } else {
                                    m.setLove(0);
                                }
                                if (i == 0) {
                                    deleteLove(m, love_stable);
                                }
                                if (i == 1) {
                                    insertMusic(m, love_stable);
                                    syncSongList(m,bean);
                                }
//                   intent.putExtra("fragment","localFragment");
                        } else if (fragment instanceof NearPlayListFragment) {
                            int i = updateLove(m, near_stable);
                            loveBtn.setImageResource(images[i]);
                            updateLove(m, download_stable);
                            if (i == 0) {
                                deleteLove(m, love_stable);
                            }
                            if (i == 1) {
                                insertMusic(m, love_stable);
                                syncSongList(m,bean);
                            }
                            if (m.getLove() == 0) {
                                m.setLove(1);
                            } else {
                                m.setLove(0);
                            }
                            updateLove(m, local_stable);
                        } else {
                            int i = updateLove(m, download_stable);
                            loveBtn.setImageResource(images[i]);
                            updateLove(m, local_stable);
                            updateLove(m, near_stable);
                            if (i == 0) {
                                deleteLove(m, love_stable);
                            }
                            if (i == 1) {
                                insertMusic(m, love_stable);
                                syncSongList(m,bean);
                            }
                            if (m.getLove() == 0) {
                                m.setLove(1);
                            } else {
                                m.setLove(0);
                            }
                        }
                        intent.putExtra("music", m);
                        context.sendBroadcast(intent);
                    }
                }else {
                    Toast.makeText(context,"网络无法连接",Toast.LENGTH_LONG).show();
                }
            }
        });
        loveBtn.setImageResource(images[m.getLove()]);
        name.setText(m.getSongName());
        author.setText(m.getSongAuthor());
        final View finalConvertView = convertView;
        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            final View view=LayoutInflater.from(context).inflate(R.layout.music_list_menu,null);
                final PopupWindow popupWindow=new PopupWindow(view, WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT,true);
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
                popupWindow.showAtLocation(finalConvertView.findViewById(R.id.moreImageButton), Gravity.BOTTOM,0,0);
                TextView nameAuthor = (TextView)view.findViewById(R.id.name_author);
                nameAuthor.setText(m.getSongName()+" - "+m.getSongAuthor());
                final Button deleteBtn=(Button)view.findViewById(R.id.delete_btn);

                deleteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(fragment instanceof LocalMusicListFragment){
                            popupWindow.dismiss();
                            index=-1;
                            String []arr={"同时删除本地歌曲文件"};
                            AlertDialog.Builder builder=new AlertDialog.Builder(context);
                            builder.setTitle("确定删除歌曲?");
                            builder.setSingleChoiceItems(arr, index, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    index = which;
                                }
                            });
                            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if(index==0){
                                        deleteLove(m,local_stable);
                                        mList.remove(position);
                                        notifyDataSetChanged();
                                        Toast.makeText(context,"删除成功",Toast.LENGTH_SHORT).show();
                                        File file=new File(m.getPath());
                                        if(file.exists()){
                                            file.delete();
                                        }
                                        System.out.println(m.getPath());
                                        File file1=new File(m.getPath().split("\\.")[0]+".lrc");
                                        if(file1.exists()){
                                            file1.delete();
                                        }
                                    }else{
                                        deleteLove(m,local_stable);
//                                        popupWindow.dismiss();
                                        mList.remove(position);
                                        notifyDataSetChanged();
                                    }
                                }
                            }).setNegativeButton("取消",null);
                            builder.create().show();
                        }else if(fragment instanceof NearPlayListFragment){
                            deleteLove(m,near_stable);
                            popupWindow.dismiss();
                            mList.remove(position);
                            notifyDataSetChanged();
                            Toast.makeText(context,"删除成功",Toast.LENGTH_SHORT).show();
                        }else if(fragment instanceof DownloadMusicFragment){
                            deleteLove(m,download_stable);
                            popupWindow.dismiss();
                            mList.remove(position);
                            notifyDataSetChanged();
                            Toast.makeText(context,"删除成功",Toast.LENGTH_SHORT).show();
                        }else {
                            popupWindow.dismiss();
                            updateLove(m,near_stable);
                            deleteLove(m,love_stable);
                            updateLove(m,local_stable);
                            updateLove(m,download_stable);
                            mList.remove(position);
                            notifyDataSetChanged();
                            Intent intent=new Intent("update_service_love");
                            intent.putExtra("music",m);
                            context.sendBroadcast(intent);
                            Toast.makeText(context,"删除成功",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                Button cancelBtn =(Button)view.findViewById(R.id.cancelBtn);
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                    }
                });
                Button aboutBtn = (Button)view.findViewById(R.id.about_btn);
                aboutBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                        View mv = LayoutInflater.from(context).inflate(R.layout.music_about,null);
                        final PopupWindow pw=showPopupWindow(mv,view.findViewById(R.id.about_btn));
                        TextView name=(TextView)mv.findViewById(R.id.about_song_name);
                        name.setText("歌名："+m.getSongName());
                        TextView author = (TextView)mv.findViewById(R.id.about_song_author);
                        author.setText("歌手："+m.getSongAuthor());
                        TextView time = (TextView)mv.findViewById(R.id.about_song_all_time);
                        time.setText("时长："+transforTime(m.getAlltime()));
                        TextView size = (TextView)mv.findViewById(R.id.about_song_size);
                        double sizes = (double)m.getSongSize()/(1024*1024);
                        Log.i("文件大小:",sizes+"");
                        DecimalFormat format=new DecimalFormat("#.00");
                        size.setText("大小："+format.format(sizes)+"M");
                        TextView path = (TextView)mv.findViewById(R.id.about_song_path);
                        path.setText("路径："+m.getPath());
                        Button sureBtn = (Button)mv.findViewById(R.id.sureBtn);
                        sureBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pw.dismiss();
                            }
                        });


                    }
                });
            }
        });
        ImageButton flagBtn = (ImageButton)convertView.findViewById(R.id.music_flag_btn);
        if(m.getFlag()==0){
            flagBtn.setImageResource(R.mipmap.ic_phone_20);
        }else {
            flagBtn.setImageResource(R.mipmap.ic_cloud_20);
        }
        return convertView;
    }


    public void syncSongList(Music m, SongListBean bean) {
        OkHttpClient client = new OkHttpClient();
        //用户id
        int userId = MyLogin.getMyLogin().getBean().getId();
        int listId = bean.getListId();
        int mId = m.getFlag();
        RequestBody body = new FormBody.Builder().add("user_id", String.valueOf(userId))
                .add("song_list_id", String.valueOf(listId))
                .add("music_id", String.valueOf(mId))
                .add("music_name", m.getSongName())
                .add("music_author", m.getSongAuthor())
                .add("music_path", m.getPath()).build();
        String url = "http://www.mybiao.top:8000/music/user/syncAddMusic";
        String urls = "http://192.168.0.106:8000/music/user/syncAddMusic";
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
    public void syncNetDelMusicFromSongList(Music music){
        OkHttpClient client=new OkHttpClient();



    }

    public boolean checkNet(Context context){
        if(context!=null){
            ConnectivityManager manager=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info=manager.getActiveNetworkInfo();
            if(info!=null){
                return info.isConnected();
            }
        }
        return false;
    }
    public void showDialog(final Music music, final int pos, final PopupWindow popup){
        index=-1;
        String []arr={"同时删除本地歌曲文件，包括歌词"};
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        builder.setTitle("确定删除歌曲?");
        builder.setSingleChoiceItems(arr, index, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                index = which;
            }
        });
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(index==0){
                    deleteLove(music,local_stable);
                    mList.remove(pos);
                    notifyDataSetChanged();
                    Toast.makeText(context,"删除成功",Toast.LENGTH_SHORT).show();
                    File file=new File(music.getPath());
                    if(file.exists()){
                        file.delete();
                    }
                    File file1=new File(music.getPath().split(".")[0]+".lrc");
                    if(file1.exists()){
                        file1.delete();
                    }
                }else{
                    deleteLove(music,local_stable);
//                    popup.dismiss();
                    mList.remove(pos);
                    notifyDataSetChanged();
                }
            }
        }).setNegativeButton("取消",null);
        builder.create().show();
    }

    /**
     * 创建一个弹窗
     * @param view  弹出的窗口的view对象
     * @param finalConvertView    菜单按钮的view对象
     */
    private PopupWindow showPopupWindow(View view,View finalConvertView){
        PopupWindow popupWindow=new PopupWindow(view,WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT,true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setTouchable(true);
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
        popupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        popupWindow.setOutsideTouchable(true);
        popupWindow.showAtLocation(finalConvertView, Gravity.BOTTOM,0,0);
        return popupWindow;
    }

    /**
     * 把时间毫秒转化为分钟
     * @param time
     * @return
     */
    public String transforTime(long time){
        long million = time/1000;
        int mill = (int)million%60; //获取秒
        int minute = (int)million/60;
        String allTime = String.valueOf(minute)+":"+String.valueOf(mill);
        return allTime;
    }
}
