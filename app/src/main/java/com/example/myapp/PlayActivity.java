package com.example.myapp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapp.database.MyDao;
import com.example.myapp.self.DealLrc;
import com.example.myapp.self.LrcBean;
import com.example.myapp.self.Music;
import com.example.myapp.self.MyLogin;
import com.example.myapp.self.NetMusicBean;
import com.example.myapp.self.SelfFinal;
import com.example.myapp.self.SongListBean;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PlayActivity extends Activity {

    private Music music;
    private MyBroadcast broadcast;
    private SeekBar seekBar;
    private TextView nowTime;
    private TextView allTime;
    private int nowPosition;
    private ImageButton playPause, playOrder;
    private TextView songName, songAuthor;
    private List<LrcBean> lrcBeanList;
    //当前播放列表
    private List<Music> musicList;
    private ListView listView;
    private MyAdapter2 adapter2;

    private TextView lrcTextView;
    private ScrollView scrollView;
    private int line;
    private static final int list_loop = 0;
    private static final int random_play = 1;
    private static final int one_loop = 2;
    private int order = list_loop;
    private int[] orderImages = new int[]{R.mipmap.ic_repeat_48, R.mipmap.ic_shuffle_48, R.mipmap.ic_repeat_one_48};

    private MyHandlers handler;

    private ImageButton loveBtn,menuBtn;

    private MyDao myDao;
    private DealLrc dealLrc;
    private Gson gson;
    private int []images=new int[]{R.mipmap.ic_heart_48,R.mipmap.ic_heart_red_48};

    private PopupMenu popupMenu;
    private boolean haveLrc=false;
    //截止到播放的歌词，总共换行的次数
//    private int allCount=0;
    //一个歌曲歌词自动换行的个数

    private int spaces=0;
    private int noewpos=0;

    private static final String local_stable = "local_music_list";
    private static final String near_stable = "near_music_list";
    private static final String download_stable = "download_music_list";
    private static final String love_stable = "love_music_list";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        //动态注册广播
        broadcast = new MyBroadcast();
        IntentFilter filter = new IntentFilter();
        filter.addAction("currentposition");
        filter.addAction("updateMusic");
        filter.addAction("startorpause");
        filter.addAction("returnOrder");
        filter.addAction("currentpositionper");
        filter.addAction("getOrder");
        filter.addAction("exitApp");
        filter.addAction("musicList");
        filter.addAction("getBufferProgress");
        filter.addAction("getMusic");
        registerReceiver(broadcast, filter);


        gson=new Gson();
        dealLrc=new DealLrc();
        ImageButton downBtn = (ImageButton) findViewById(R.id.downBtn);
        downBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        songName = (TextView) findViewById(R.id.this_song_title);
        songAuthor = (TextView) findViewById(R.id.this_song_author);
        nowTime = (TextView) findViewById(R.id.now_time);
        allTime = (TextView) findViewById(R.id.all_time);
        seekBar = (SeekBar) findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Intent intent1 = new Intent();
                intent1.setAction("seekTo");
                intent1.putExtra("progress", seekBar.getProgress());
                sendBroadcast(intent1);
            }
        });
        playPause = (ImageButton) findViewById(R.id.play_pause_45);

        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent controll = new Intent();
                controll.setAction("sop");
                controll.putExtra("flag", "click");
                sendBroadcast(controll);
            }

        });
        ImageButton lastBtn = (ImageButton) findViewById(R.id.last_play);
        lastBtn.setOnClickListener(listener);
        ImageButton nextBtn = (ImageButton) findViewById(R.id.next_play);
        nextBtn.setOnClickListener(listener);

        playOrder = (ImageButton) findViewById(R.id.play_sort);
        //发送广播获取现在的播放顺序
//        Intent intentq = new Intent();
//        intentq.setAction("status");
//        sendBroadcast(intentq);
        playOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent3 = new Intent();
                intent3.setAction("musicOrder");
                sendBroadcast(intent3);
            }
        });
        ImageButton playMenuBtn = (ImageButton) findViewById(R.id.play_menu);
        playMenuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击按钮时发送广播给Service获取当前的播放列表
                final Intent intent = new Intent();
                intent.setAction("getMusicList");
                sendBroadcast(intent);
                View view = LayoutInflater.from(PlayActivity.this).inflate(R.layout.play_list, null);
                final PopupWindow popupWindow = new PopupWindow(view, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
                popupWindow.setFocusable(true);
                popupWindow.setTouchable(true);
                popupWindow.setOutsideTouchable(true);
                popupWindow.showAtLocation(findViewById(R.id.play_menu), Gravity.BOTTOM, 0, 0);
                Button cancelBtn = (Button) view.findViewById(R.id.cancel_btn);
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                    }
                });
                listView = (ListView) view.findViewById(R.id.now_play_music_list);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent2 = new Intent();
                        intent2.setAction("playMusicOnList");
                        intent2.putExtra("what_play_index", position);
                        sendBroadcast(intent2);
                    }
                });
            }
        });

        lrcTextView = (TextView) findViewById(R.id.lrc_textView);
        scrollView = (ScrollView) findViewById(R.id.lrc_scrollView);

        handler = new MyHandlers();

        myDao=new MyDao(this);
        loveBtn = (ImageButton)findViewById(R.id.play_love_btn);

        loveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkNet(PlayActivity.this)) {
                    if (!MyLogin.logined) {
                        //如果没有用户登录，直接跳转到用户登录界面
                        Intent intents = new Intent(PlayActivity.this, Login_in.class);
                        startActivity(intents);
                    }else {
                        int j = music.getLove();
                        SongListBean bean = new SongListBean(MyLogin.loveId, null, 0);
                        if (j == 1) {
                            music.setLove(0);
                            myDao.deleteMusic(music, "love_music_list");
                            updateAllLove(music, 0);
                            syncNetDelMusicFromSongList(music,bean);
                        } else {
                            music.setLove(1);
                            myDao.insertMusic(music, "love_music_list");
                            updateAllLove(music, 1);
                            syncSongList(music,bean);
                        }
                        Intent intent1 = new Intent();
                        intent1.setAction("update_service_love");
                        intent1.putExtra("loved", j);
                        intent1.putExtra("music", music);
                        sendBroadcast(intent1);
                        int i = j == 0 ? 1 : 0;
                        loveBtn.setImageResource(images[i]);
                    }
                }else {
                    Toast.makeText(PlayActivity.this,"网络无法连接",Toast.LENGTH_SHORT).show();
                }
            }
        });
        final ImageButton lrcMenuBtn=(ImageButton)findViewById(R.id.play_lrc_btn);
        lrcMenuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu=new PopupMenu(PlayActivity.this,lrcMenuBtn);
                popupMenu.getMenuInflater().inflate(R.menu.lrc_menu,popupMenu.getMenu());
                if(haveLrc){
                    popupMenu.getMenu().findItem(R.id.get_net_lrc).setEnabled(false);
                }
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.get_net_lrc:
                                lrcTextView.setText("\n\n\n\n正在搜索歌词...");
                                getMusicLrc();
                                break;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });
        menuBtn=(ImageButton)findViewById(R.id.play_menu_main);
        menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View myView = LayoutInflater.from(PlayActivity.this).inflate(R.layout.play_menu,null);
                final PopupWindow window=new PopupWindow(myView, WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
                window.setFocusable(true);
                window.setTouchable(true);
                window.setOutsideTouchable(true);
                WindowManager.LayoutParams lp =getWindow().getAttributes();
                lp.alpha=0.5f;
                getWindow().setAttributes(lp);

                window.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        //恢复透明度
                        WindowManager.LayoutParams lp =getWindow().getAttributes();
                        lp.alpha=1f;
                        getWindow().setAttributes(lp);
                    }
                });
                window.showAtLocation(findViewById(R.id.play_menu_main), Gravity.BOTTOM,0,0);
                Button playDwBtn = (Button)myView.findViewById(R.id.play_download);
                Button cancelBtn = (Button)myView.findViewById(R.id.play_cancel);
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        window.dismiss();
                    }
                });
                playDwBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent("download_music");
                        intent.putExtra("music",music);
                        sendBroadcast(intent);
                        window.dismiss();
                    }
                });
                if(music.getFlag()==0){
                    playDwBtn.setEnabled(false);
                }else {
                    int i=0;
                    List<Music> musicList1=myDao.findAll("local_music_list");
                    for(i=0;i<musicList1.size();i++){
                        if(musicList1.get(i).getSongName().equals(music.getSongName())&&musicList1.get(i).getSongAuthor().equals(music.getSongAuthor())){
                            playDwBtn.setEnabled(false);
                            break;
                        }
                    }
                    if(i>=musicList1.size()){
                        playDwBtn.setEnabled(true);
                    }
                }
            }
        });
//        发送广播到 service，获取音乐播放信息
        Intent intent=new Intent("update_play_message");
        sendBroadcast(intent);
//        getMusicLrc();
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
        String url =  SelfFinal.host+SelfFinal.port+ "/music/user/syncAddMusic";
        String urls =  SelfFinal.host+SelfFinal.port+ "/music/user/syncAddMusic";
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
        String url = SelfFinal.host+SelfFinal.port+ "/music/user/syncDelMusic";
        String urls = SelfFinal.host+SelfFinal.port+"/music/user/syncDelMusic";
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

    private void updatelocalLove(Music music,String tableName,int loved){
        ContentValues values=new ContentValues();
        values.put("love",loved);
        myDao.newDB().update(tableName,values,"path=?",new String[]{music.getPath()});
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

    //md5算法加密
    public String getMD5(String s){
        String md5s="";
        try {
            MessageDigest digest=MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte[] bytes = digest.digest();
            md5s = new BigInteger(1,bytes).toString(16);
            for (;md5s.length()<32;md5s = "0"+md5s){

            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return md5s;
    }
    final String cachePath = Environment.getExternalStorageDirectory().getAbsolutePath()+
            "/Android/data/com.example.myapp/cache/lrc-cache";

    //获取本地歌词缓存的文件
    public void getCacheMusicLrc(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<LrcBean> lrcList = new ArrayList<>();
                //播放网络歌曲 先从缓存中查找歌词
                String lrcName = music.getSongAuthor()+" - "+music.getSongName()+".lrc";
                String lrcMd5Name = getMD5(lrcName);
                Log.i("Md5",lrcMd5Name);
                File fs1 = new File(cachePath);

                File file2=new File(cachePath+"/"+lrcMd5Name);
                if(file2.exists()){
                    Log.i("在缓存中查找到歌词","是的");
                    FileInputStream stream= null;
                    try {
                        stream = new FileInputStream(file2);
                        //gbk编码，中文不会乱码
                        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "gbk"));
                        String line;
                        while ((line = reader.readLine()) != null) {

                            String[] lrcs = line.split("\\]");
                            if (lrcs.length == 1) {
                                continue;
                            }
                            //获取到歌词
                            String lrc = lrcs[1];
                            String time = lrcs[0].split("\\[")[1];
                            String mintue = time.split(":")[0];
                            String million = time.split(":")[1];
                            //转化为毫秒数
                            int allTime = Integer.parseInt(mintue) * 60 * 1000 + (int) (Double.parseDouble(million) * 1000);
                            LrcBean lrcBean = new LrcBean(lrc, allTime);
                            lrcList.add(lrcBean);
                        }
                        Message message = new Message();
                        message.what = 101;
                        message.obj = lrcList;
                        handler.sendMessage(message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else {
                    Message msg = new Message();
                    msg.what = 102;
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }

    /**
     * 开启一个线程，查找歌词
     * 先从缓存中通过md5算法转换名称，到制定目录查找是否已经存在缓存歌词文件，
     * 如果已经存在歌词文件，就直接通过handler通知主线程加载歌词
     * 如果缓存中不存在，就从服务其中查找
     */
    public void getMusicLrc(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<LrcBean> lrcList = new ArrayList<>();
//                //播放网络歌曲 先从缓存中查找歌词
                String lrcName = music.getSongAuthor()+" - "+music.getSongName()+".lrc";
                String lrcMd5Name = getMD5(lrcName);
                File file2=new File(cachePath+"/"+lrcMd5Name);
                if(file2.exists()){
                    FileInputStream stream= null;
                    try {
                        stream = new FileInputStream(file2);
                        //gbk编码，中文不会乱码
                        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "gbk"));
                        String line;
                        while ((line = reader.readLine()) != null) {

                            String[] lrcs = line.split("\\]");
                            if (lrcs.length == 1) {
                                continue;
                            }
                            //获取到歌词
                            String lrc = lrcs[1];
                            String time = lrcs[0].split("\\[")[1];
                            String mintue = time.split(":")[0];
                            String million = time.split(":")[1];
                            //转化为毫秒数
                            int allTime = Integer.parseInt(mintue) * 60 * 1000 + (int) (Double.parseDouble(million) * 1000);
                            LrcBean lrcBean = new LrcBean(lrc, allTime);
                            lrcList.add(lrcBean);
                        }
                        Message message = new Message();
                        message.what = 101;
                        message.obj = lrcList;
                        handler.sendMessage(message);
                        } catch (Exception e) {
                            Message msg = new Message();
                            msg.what = 102;
                            handler.sendMessage(msg);
                        }
                }else {
                    OkHttpClient client = new OkHttpClient.Builder().connectTimeout(5, TimeUnit.SECONDS).build();
                    String url = "";
                    if (music.getFlag() == 1) {
                        String[] strs = music.getPath().split("=");
                        url = "http://www.mybiao.top:8000/lrc?id=" + strs[1];
                    } else {
                        List<NetMusicBean> netMusicBeans = new ArrayList<>();
                        Request req = new Request.Builder().url("http://www.mybiao.top:8000/search?word=" + music.getSongAuthor() + " - " + music.getSongName()).build();
                        try {
                            Response res = client.newCall(req).execute();
                            if (res.isSuccessful()) {
                                String mulist = res.body().string();
                                if (!mulist.equals("null")) {
                                    Log.i("muList", mulist);
                                    JsonParser jsonParser = new JsonParser();
                                    JsonArray jsonElements = jsonParser.parse(mulist).getAsJsonArray();
                                    for (JsonElement element : jsonElements) {
                                        NetMusicBean bean = gson.fromJson(element, NetMusicBean.class);
                                        Log.i("搜索到", bean.toString());
                                        netMusicBeans.add(bean);
                                    }
                                }
                            }

                        } catch (Exception e) {
                            Message msg = new Message();
                            msg.what = 102;
                            handler.sendMessage(msg);
                        }
                        if (netMusicBeans.size() == 1) {
                            String path = "http://www.mybiao.top:8000/song?id=" + netMusicBeans.get(0).getId();
                            String[] strs = path.split("=");
                            url = "http://www.mybiao.top:8000/lrc?id=" + strs[1];
                        }
                    }
                    if (!url.equals("")) {
                        Request request = new Request.Builder().url(url).build();
                        try {
                            Response response = client.newCall(request).execute();
                            if (response.isSuccessful()) {
                                Log.i("歌词请求成功", "ok");
                                InputStream inputStream = response.body().byteStream();
                                String handers = response.header("Content-Disposition");
                                String[] strs = handers.split("=");
                                String fileName = "";
                                if (strs.length > 1) {
                                    fileName = strs[1];
                                }
                                File file = new File(cachePath);
                                if (!file.exists()) {
                                    file.mkdirs();
                                }
                                File file1 = new File(cachePath + "/" + getMD5(fileName));
                                if (!file1.exists()) {
                                    file1.createNewFile();
                                }
                                FileOutputStream out = new FileOutputStream(file1);
                                byte[] bytes = new byte[1024];
                                int p = 0;
                                while ((p = inputStream.read(bytes)) != -1) {
                                    out.write(bytes, 0, p);
                                }
                                FileInputStream stream = new FileInputStream(file1);
                                //gbk编码，中文不会乱码
                                BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "gbk"));
                                String line;
                                while ((line = reader.readLine()) != null) {

                                    String[] lrcs = line.split("\\]");
                                    if (lrcs.length == 1) {
                                        continue;
                                    }
                                    //获取到歌词
                                    String lrc = lrcs[1];
                                    String time = lrcs[0].split("\\[")[1];
                                    String mintue = time.split(":")[0];
                                    String million = time.split(":")[1];
                                    //转化为毫秒数
                                    int allTime = Integer.parseInt(mintue) * 60 * 1000 + (int) (Double.parseDouble(million) * 1000);
                                    LrcBean lrcBean = new LrcBean(lrc, allTime);
                                    lrcList.add(lrcBean);
                                }
                                Message message = new Message();
                                message.what = 101;
                                message.obj = lrcList;
                                handler.sendMessage(message);
                            } else {
                                Message msg = new Message();
                                msg.what = 102;
                                handler.sendMessage(msg);
                            }
                        } catch (Exception e) {
                            Log.i("在主播放页面", "未找到歌词");
                            Message msg = new Message();
                            msg.what = 102;
                            handler.sendMessage(msg);
                        }
                    }
                }
            }
        }).start();
    }
    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent2 = new Intent();
            intent2.setAction("lastAndNext");
            if (v.getId() == R.id.last_play) {
                intent2.putExtra("lastnext", "last");
            } else {
                intent2.putExtra("lastnext", "next");
            }
            sendBroadcast(intent2);
        }
    };

    public String transforTime(long time) {
        long million = time / 1000;
        int mill = (int) million % 60; //获取秒
        int minute = (int) million / 60;
        String allTime = String.valueOf(minute) + ":" + String.valueOf(mill);
        return allTime;
    }

    /**
     * 从歌词文件读取的歌词一行可能过长，在textView中，会自动分行
     * 而歌词的滚动是根据从文件中读取的一行，滚动的，自动换行会造成歌词显示位置偏下，
     * 原因是换行占用了textView的一行空间，所以歌词偏下
     * 获取到正在播放的歌词，前面的歌词自动换行所另外占用的行数
     * 在歌词滚动是根据lrcBeanList的下标滚动制定行数时，在滚动时加上自动换行
     * 所占用的行数，纠正歌词的偏移
     * @param width     textiew的宽度
     * @param i         lrcBeanList的下标
     * @return          返回另外占用的行数
     */
    public int getAllCount(int width,int i){
        int allCount = 0;
        String lrcs;
        float textWidth;
        double count;
        int beishu ;
        TextPaint paint=lrcTextView.getPaint();
        for (int j = 0; j < lrcBeanList.size(); j++) {
            if (j <= i) {
                lrcs = lrcBeanList.get(j).getLrc();
                textWidth = paint.measureText(lrcs);
                count = Math.ceil(textWidth / width);
                beishu = (int) Math.round(count);
                allCount = allCount + beishu - 1;
            }
        }
        return allCount;
    }

    //修改正在播放的歌词的颜色
    public void playingLrcColor(int line) {
        int start = 0, end = 0;
        Layout layout = lrcTextView.getLayout();
        final int height = scrollView.getHeight();
        int h1 = getLrcY(1)-getLrcY(0);
        line = line+spaces;
        end = layout.getLineEnd(line);
        start = layout.getLineStart(line);
        SpannableString span=new SpannableString(lrcTextView.getText().toString());
        span.setSpan(new ForegroundColorSpan(Color.BLUE),start,end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        lrcTextView.setText(span);
    }

    public void scrollToY(int pos2,final int height){
        final int h1 = getLrcY(1)-getLrcY(0);
        final int width = lrcTextView.getMeasuredWidth();
        for (int i = 0; i < lrcBeanList.size(); i++) {
            if (i == lrcBeanList.size() - 1) {
                if (pos2 >= lrcBeanList.get(i).getBeginTime()) {
                    line = i;
                    String lrcs = lrcBeanList.get(i).getLrc();
                    //获取到目前的总换行个数
                    final int allCount = getAllCount(width,i);
                    playingLrcColor(i+allCount);
                    scrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            scrollView.scrollTo(0, getLrcY(line+allCount)+h1/2-height%h1/2);
                        }
                    });
                }
            } else {
                if (pos2 >= lrcBeanList.get(i).getBeginTime() && pos2 <= lrcBeanList.get(i + 1).getBeginTime()) {
                    line = i;
                    String lrcs = lrcBeanList.get(i).getLrc();
                    //获取到目前的总换行个数
                    final int allCount = getAllCount(width,i);
                    playingLrcColor(line+allCount);
                    scrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            scrollView.scrollTo(0, getLrcY(line+allCount)+h1/2-height%h1/2);
                        }
                    });
                }
            }
        }
    }
    private class MyBroadcast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            final int height = scrollView.getHeight();
            if (intent.getAction().equals("currentposition")) {
                Bundle bundle = intent.getBundleExtra("current");
                nowPosition = bundle.getInt("position");
                int all_time=0;
                if(music!=null)
                all_time = music.getAlltime();
                if(all_time==0){
                    all_time = bundle.getInt("alltime");
                    seekBar.setMax(all_time);
                }
                allTime.setText(String.valueOf(transforTime(all_time)));
                nowTime.setText(String.valueOf(transforTime(nowPosition)));
                seekBar.setProgress(nowPosition);
                if(bundle.getInt("cacheFlag")==1){
                    seekBar.setSecondaryProgress(seekBar.getMax());
                }
            }

            if (intent.getAction().equals("currentpositionper")) {
                if (lrcBeanList != null) {
                    Bundle bundle = intent.getBundleExtra("current");
                    int myPosition = bundle.getInt("position");
                    scrollToY(myPosition,height);
                }
            }
            if (intent.getAction().equals("startorpause")) {
                int flags = intent.getIntExtra("flags", 0);
                if (flags == 0) {
                    playPause.setImageResource(R.mipmap.ic_play_88);
                } else {
                    playPause.setImageResource(R.mipmap.ic_pause_88);
                }
            }
            if (intent.getAction().equals("updateMusic")) {
                //切换歌曲，重置换行个数
                haveLrc = false;
                Bundle bundle = intent.getBundleExtra("nowplay");
                music = (Music) bundle.getSerializable("nowplaymusic");
                songName.setText(music.getSongName());
                songAuthor.setText(music.getSongAuthor());
                allTime.setText(String.valueOf(transforTime(music.getAlltime())));
                seekBar.setMax(music.getAlltime());
                //重置进度条的进度数
                seekBar.setProgress(0);
                seekBar.setSecondaryProgress(0);
                if(music.getLove()==1){
                    loveBtn.setImageResource(images[1]);
                }else {
                    loveBtn.setImageResource(images[0]);
                }
                if(music.getFlag()==0){
                    seekBar.setSecondaryProgress(music.getAlltime());
                }
                if(music.getFlag()==0) {
                    lrcBeanList = dealLrc.getLrcList(music);
                    lrcTextView.setText("");
                    if (lrcBeanList != null) {
                        String lrc = "";
//                        lrc = lrc + "\n\n\n\n";
                        for (LrcBean bean : lrcBeanList) {
                            lrc = lrc + bean.getLrc() + "\n";
                        }
//                        lrc = lrc + "\n\n\n\n";
                        lrcTextView.setText(lrc);
//                        final int height = scrollView.getHeight();
                        int h1 = getLrcY(1)-getLrcY(0);
                        spaces = (height/h1)/2+1;
                        for(int i=0;i<(height/h1)/2+1;i++){
                            lrc = "\n"+lrc;
                            lrc = lrc+"\n";
                        }
                        lrcTextView.setText(lrc);
                        haveLrc = true;
                    } else {
//                        getMusicLrc();
//                        lrcTextView.setText("\n\n\n\n该歌曲暂无歌词");
                        getCacheMusicLrc();
                    }
                }else {
                    getMusicLrc();
                }

                if (adapter2 != null) {
                    int j = 0;
                    for (j = 0; j < musicList.size(); j++) {
                        if (musicList.get(j).getId() == -1000) {
                            musicList.get(j).setId(-1);
                        }
                    }
                    int i = 0;
                    for (i = 0; i < musicList.size(); i++) {
                        if (musicList.get(i).getPath().equals(music.getPath())) {
                            musicList.get(i).setId(-1000);
                        }
                    }
                    adapter2.notifyDataSetChanged();
                }
            }
            if (intent.getAction().equals("getOrder")) {
                int order1 = intent.getIntExtra("orderKey", -1);
                if (order1 != -1) {
                    playOrder.setImageResource(orderImages[order1]);
                }
            }
            if (intent.getAction().equals("exitApp")) {
                finish();
            }
            if (intent.getAction().equals("musicList")) {
                musicList = (ArrayList<Music>) intent.getSerializableExtra("music_list");
                if (musicList != null) {
                    int j = 0;
                    for (j = 0; j < musicList.size(); j++) {
                        if (musicList.get(j).getId() == -1000) {
                            musicList.get(j).setId(-1);
                        }
                    }
                    int i = 0;
                    for (i = 0; i < musicList.size(); i++) {
                        if (musicList.get(i).getPath().equals(music.getPath())) {
                            musicList.get(i).setId(-1000);
                        }
                    }
                    adapter2 = new MyAdapter2(PlayActivity.this, musicList);
                    listView.setAdapter(adapter2);
                    adapter2.notifyDataSetChanged();
                }
            }
            if(intent.getAction().equals("getBufferProgress")){
                int bufferPos = intent.getIntExtra("bufferPos",-1);
                    if (bufferPos != -1) {
                        seekBar.setSecondaryProgress(bufferPos);
                }
            }
            if(intent.getAction().equals("getMusic")){
                music=(Music)intent.getSerializableExtra("nowplaymusic");
                songName.setText(music.getSongName());
                songAuthor.setText(music.getSongAuthor());
                allTime.setText(String.valueOf(transforTime(music.getAlltime())));
                int pos = intent.getIntExtra("pos",0);
                noewpos = pos;
                seekBar.setMax(music.getAlltime());
                seekBar.setProgress(pos);
                nowTime.setText(transforTime(pos));
                int orders = intent.getIntExtra("order",-1);
                if(orders!=-1){
                    playOrder.setImageResource(orderImages[orders]);
                }
                if(music.getFlag()==0){
                    seekBar.setSecondaryProgress(music.getAlltime());
                }
                if(music.getFlag()==0) {
                    lrcBeanList = dealLrc.getLrcList(music);
                    if (lrcBeanList != null) {
                        String lrc = "";
                        for (LrcBean bean : lrcBeanList) {
                            lrc = lrc + bean.getLrc() + "\n";
                        }
                        lrcTextView.setText(lrc);
//                        final int height = scrollView.getHeight();
                        int h1 = getLrcY(1)-getLrcY(0);
                        spaces = (height/h1)/2+1;
                        for(int i=0;i<(height/h1)/2+1;i++){
                            lrc = "\n"+lrc;
                            lrc = lrc+"\n";
                        }
                        lrcTextView.setText(lrc);
                        //歌词恢复到播放的地方
                        scrollToY(pos,height);
                        haveLrc = true;
                        System.out.println("本地有歌词");
                    } else {
                        getCacheMusicLrc();
                    }
                }else {
                    getMusicLrc();
                }

                if(music.getLove() == 1){
                    loveBtn.setImageResource(images[1]);
                }
                loveBtn.setImageResource(images[0]);
                List<Music> musicList=myDao.findAll("love_music_list");
                for(Music m:musicList){
                    if(m.getPath().equals(music.getPath())){
                        loveBtn.setImageResource(images[1]);
                        break;
                    }
                }
            }
        }
    }
    //获取textview某一行的y坐标

    /*****************X坐标
     *
     *
     *
     *
     *
     * Y坐标*/
    private int getLrcY(int line) {
        Rect rect = new Rect();
        Layout layout = lrcTextView.getLayout();
        if(line>=0&&line<=layout.getLineCount()) {
            layout.getLineBounds(line, rect);
        }
        int top = rect.centerY();
        return top;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcast);
        if (myDao.isConnection()){
            myDao.closeConnect();
        }
    }

    public class MyHandlers extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 101:
                    lrcBeanList = (ArrayList<LrcBean>) msg.obj;
                    if(lrcBeanList.size()==0){
                        lrcTextView.setText("\n\n\n\n此歌曲暂无歌词");
                    }else {
                        if (lrcBeanList != null) {
                            String lrc = "";
                            for (LrcBean bean : lrcBeanList) {
                                lrc = lrc + bean.getLrc() + "\n";
                            }
                            lrcTextView.setText(lrc);
                            final int height = scrollView.getHeight();
                            int h1 = getLrcY(1)-getLrcY(0);
                            spaces = (height/h1)/2+1;
                            for(int i=0;i<(height/h1)/2+1;i++){
                                lrc = "\n"+lrc;
                                lrc = lrc+"\n";
                            }
                            lrcTextView.setText(lrc);
                            scrollToY(noewpos,height);
                        }
                    }
                    break;
                case 102:
                        lrcTextView.setText("\n\n\n\n此歌曲暂无歌词");
                    break;
            }
        }
    }
}
