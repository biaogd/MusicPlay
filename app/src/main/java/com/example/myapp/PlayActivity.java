package com.example.myapp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.myapp.self.DealLrc;
import com.example.myapp.self.LrcBean;
import com.example.myapp.self.Music;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
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
        registerReceiver(broadcast, filter);
        Intent intent = getIntent();
        Bundle bundle1 = intent.getBundleExtra("playIntent");
        music = (Music) bundle1.getSerializable("playMusic");
        boolean playing = bundle1.getBoolean("playing");
        ImageButton downBtn = (ImageButton) findViewById(R.id.downBtn);
        downBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        songName = (TextView) findViewById(R.id.this_song_title);
        songName.setText(music.getSongName());
        songAuthor = (TextView) findViewById(R.id.this_song_author);
        songAuthor.setText(music.getSongAuthor());
        nowTime = (TextView) findViewById(R.id.now_time);
        allTime = (TextView) findViewById(R.id.all_time);
        allTime.setText(String.valueOf(transforTime(music.getAlltime())));
        seekBar = (SeekBar) findViewById(R.id.seek_bar);
        seekBar.setMax(music.getAlltime());
        if(music.getFlag()==0){
            seekBar.setSecondaryProgress(music.getAlltime());
        }
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
        if (playing) {
            playPause.setImageResource(R.mipmap.ic_pause_88);
        }
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
        Intent intentq = new Intent();
        intentq.setAction("status");
        sendBroadcast(intentq);
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

        lrcBeanList = new DealLrc().getLrcList(music);
        if (lrcBeanList != null) {
            String lrc = "";
            lrc = lrc + "\n\n\n\n";
            for (LrcBean bean : lrcBeanList) {
                lrc = lrc + bean.getLrc() + "\n";
            }
            lrc = lrc + "\n\n\n\n";
            lrcTextView.setText(lrc);
            scrollView.post(new Runnable() {
                @Override
                public void run() {
                    scrollView.scrollTo(0, line);
                }
            });
        } else {
            getMusicLrc();
        }
    }
    public void getMusicLrc(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                String[] strs = music.getPath().split("=");
                List<LrcBean> lrcList = new ArrayList<>();
                Log.i("请求歌词线程","正在运行");
                if (strs.length >= 2) {
                    Request request = new Request.Builder().url("http://www.mybiao.top:8000/lrc?id=" + strs[1]).build();
                    try {
                        Response response = client.newCall(request).execute();
                        if (response.isSuccessful()) {
                            Log.i("歌词请求成功","ok");
                            InputStream inputStream = response.body().byteStream();
                            //gbk编码，中文不会乱码
                            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "gbk"));
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
                    } catch (IOException e) {
                        e.printStackTrace();
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

    private class MyBroadcast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("currentposition")) {
                Bundle bundle = intent.getBundleExtra("current");
                nowPosition = bundle.getInt("position");
                int all_time = music.getAlltime();
                if(all_time==0){
                    all_time = bundle.getInt("alltime");
                    seekBar.setMax(all_time);
                }
                allTime.setText(String.valueOf(transforTime(all_time)));
                nowTime.setText(String.valueOf(transforTime(nowPosition)));
                seekBar.setProgress(nowPosition);
            }
            if (intent.getAction().equals("currentpositionper")) {
                if (lrcBeanList != null) {
                    Bundle bundle = intent.getBundleExtra("current");
                    int myPosition = bundle.getInt("position");
                    for (int i = 0; i < lrcBeanList.size(); i++) {
                        if (i == lrcBeanList.size() - 1) {
                            if (myPosition >= lrcBeanList.get(i).getBeginTime()) {
                                line = i;
                                scrollView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        scrollView.scrollTo(0, getLrcY(line));
                                    }
                                });
                            }
                        } else {
                            if (myPosition >= lrcBeanList.get(i).getBeginTime() && myPosition <= lrcBeanList.get(i + 1).getBeginTime()) {
                                line = i;
                                scrollView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        scrollView.scrollTo(0, getLrcY(line));
                                    }
                                });
                            }
                        }
                    }
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
                Bundle bundle = intent.getBundleExtra("nowplay");
                music = (Music) bundle.getSerializable("nowplaymusic");
                songName.setText(music.getSongName());
                songAuthor.setText(music.getSongAuthor());
                allTime.setText(String.valueOf(transforTime(music.getAlltime())));
                seekBar.setMax(music.getAlltime());
                if(music.getFlag()==0){
                    seekBar.setSecondaryProgress(music.getAlltime());
                }
                lrcBeanList = new DealLrc().getLrcList(music);
                lrcTextView.setText("");
                if (lrcBeanList != null) {
                    String lrc = "";
                    lrc = lrc + "\n\n\n\n";
                    for (LrcBean bean : lrcBeanList) {
                        lrc = lrc + bean.getLrc() + "\n";
                    }
                    lrc = lrc + "\n\n\n\n";
                    lrcTextView.setText(lrc);
                } else {
                    if (music.getFlag()==1) {
                        getMusicLrc();
                    }
                    lrcTextView.setText("\n\n\n\n此歌曲暂无歌词");
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
            if (intent.getAction().equals("returnOrder")) {
                Bundle bundle = intent.getBundleExtra("orderKeys");
                int time1 = bundle.getInt("positions");
                nowTime.setText(String.valueOf(transforTime(time1)));
                seekBar.setProgress(time1);
                int orders = bundle.getInt("orderKey");
                Log.i("收到广播", "获取到了播放顺序");
                if (orders != -1) {
                    playOrder.setImageResource(orderImages[orders]);
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
                if(bufferPos!=-1){
                    seekBar.setSecondaryProgress(bufferPos);
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
        layout.getLineBounds(line, rect);
        int top = rect.centerY();
        return top;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcast);
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
                            lrc = lrc + "\n\n\n\n";
                            for (LrcBean bean : lrcBeanList) {
                                lrc = lrc + bean.getLrc() + "\n";
                            }
                            lrc = lrc + "\n\n\n\n";
                            lrcTextView.setText(lrc);
                            scrollView.post(new Runnable() {
                                @Override
                                public void run() {
                                    scrollView.scrollTo(0, line);
                                }
                            });
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