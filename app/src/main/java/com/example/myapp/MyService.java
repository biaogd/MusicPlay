package com.example.myapp;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;


import com.danikula.videocache.CacheListener;
import com.danikula.videocache.HttpProxyCacheServer;
import com.example.myapp.self.Music;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class MyService extends Service {

    private String path;
    private MediaPlayer player;
    private int alltime;       //歌曲总时间
    private int position;      //歌曲当前的播放进度
    private Timer timer=new Timer();
    private Timer timer1=new Timer();
    //正在播放的音乐
    private Music music;
    //定义正在播放的音乐列表
    private List<Music> mList;
    //正在播放的音乐在MusicList中的下标
    private int listp;
    //当前的播放顺序
    private static final int list_loop=0;
    private static final int random_play=1;
    private static final int one_loop = 2;
    private int order=list_loop;
    private PlayBroadcast broadcast;
    private NotificationManager manager;
    private  NotificationCompat.Builder builder;
    private String whichFragment;
    private RemoteViews remoteViews;

    private int bufferFlag=0;
    private int cacheFlag=0;
    private App app;

    public MyService() {
    }

    public String getPath() {
        return path;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    /**
     * 播放上一首音乐
     */
    public void last(){
        if(this.listp == 0){
            listp =mList.size()-1;
        }else {
            this.listp--;
        }
        this.music = mList.get(this.listp);
        this.player.reset();
//        timer.cancel();
        play();
    }
    //随机播放一首歌
    public void randowPlay(){
        Random random=new Random(this.listp);
        this.listp = random.nextInt(mList.size());
        this.music = mList.get(this.listp);
        this.player.reset();
//        timer.cancel();
        play();
    }

    /**
     * 播放下一首音乐
     */
    public void next(){
        if(this.listp == (mList.size()-1)){
            this.listp=0;
        }else {
            this.listp++;
        }
        this.music = mList.get(this.listp);
        this.player.reset();
//        timer.cancel();
        play();
    }

    //播放指定的歌曲
    public void playByIndex(int pos){
        if(pos <=mList.size()&&pos>=0){
            this.listp = pos;
            this.music = mList.get(this.listp);
            this.player.reset();
            play();
        }
    }

    //播放音乐
    public void play(){
        try {
            app.getProxy().unregisterCacheListener(cacheListener);
            app.getProxy().registerCacheListener(cacheListener,music.getPath());
            bufferFlag =0;
            cacheFlag = 0;
            if(music.getFlag() == 0) {
                player.setDataSource(music.getPath());
            }
            if(music.getFlag()==1) {
                String proxyUrl = app.getProxy().getProxyUrl(music.getPath());
                player.setDataSource(proxyUrl);
            }
            if(music.getFlag()==1&&app.getProxy().isCached(music.getPath())){
                cacheFlag = 1;
            }
//          异步加载网络歌曲
            player.prepareAsync();
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    music.setAlltime(mp.getDuration());
                    Log.i("歌曲"+music.getSongName()+"总时长",music.getAlltime()+"");
                    startMusic(mp);
                    bufferFlag = 1;
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
        //开始播放，更新通知栏的歌曲信息
        remoteViews.setTextViewText(R.id.notifi_song_name,music.getSongName());
        remoteViews.setTextViewText(R.id.notifi_song_author,music.getSongAuthor());
        remoteViews.setImageViewResource(R.id.pause_music,R.mipmap.ic_pause_48);
        manager.notify(123,builder.build());
        //每次播放新的音乐，发送广播，更新Activity的控制界面，更新歌曲名字和作者等信息
        Intent intent1=new Intent();
        intent1.setAction("updateMusic");
        Bundle bundle=new Bundle();
        bundle.putSerializable("nowplaymusic",this.music);
        bundle.putString("whichFragment",whichFragment);
        intent1.putExtra("nowplay",bundle);
        sendBroadcast(intent1);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(player!=null && player.isPlaying()) {
                    Intent myIntent = new Intent();
                    Bundle bundle = new Bundle();
//                    Log.i("music总时间",""+music.getAlltime());
                    bundle.putInt("alltime", music.getAlltime());
                    bundle.putInt("position", player.getCurrentPosition());
                    bundle.putInt("cacheFlag",cacheFlag);
                    myIntent.putExtra("current", bundle);
                    myIntent.setAction("currentposition");
                    sendBroadcast(myIntent);
//                    Log.i("广播", "已经发送");
                }
            }
        },0,1000);
        //用于更新歌词，每0.5秒发送一次广播
        timer1.schedule(new TimerTask() {
            @Override
            public void run() {
                if(player!=null && player.isPlaying()) {
//                    Log.i("广播线程", "开始执行了");
                    Intent myIntent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putInt("alltime", music.getAlltime());
                    bundle.putInt("position", player.getCurrentPosition());
                    myIntent.putExtra("current", bundle);
                    myIntent.setAction("currentpositionper");
                    sendBroadcast(myIntent);
                }
            }
        },0,500);
    }
    @Override
    public void onCreate() {
        super.onCreate();
        //注册接收控制暂停或播放的广播
        broadcast=new PlayBroadcast();
        IntentFilter filter=new IntentFilter();
        filter.addAction("startMusic");
        filter.addAction("sop");
        filter.addAction("lastAndNext");
        filter.addAction("musicOrder");
        filter.addAction("status");
        filter.addAction("pauseMusic");
        filter.addAction("updateActivity");
        filter.addAction("exitApp");
        filter.addAction("seekTo");
        filter.addAction("getnowplaymusic");
        filter.addAction("getMusicList");
        filter.addAction("deleteMusicFromList");
        filter.addAction("playMusicOnList");
        filter.addAction("play_net_music");
        registerReceiver(broadcast,filter);
        manager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        player=new MediaPlayer();
//        Log.i("服务中oncreate方法","执行了");
        app = new App();
    }

    public class App{
        private HttpProxyCacheServer proxyCacheServer;
        private HttpProxyCacheServer getProxy() {
            if (proxyCacheServer == null) {
                proxyCacheServer=new HttpProxyCacheServer.Builder(MyService.this).maxCacheFilesCount(200).build();
            }
            return proxyCacheServer;
        }
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {

        //Android8.0开始增加了通知渠道
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel channel=new NotificationChannel("fore_service","前台服务", NotificationManager.IMPORTANCE_LOW);
            initNotification();
            manager.createNotificationChannel(channel);
            startForeground(123,builder.build());
        }else {
            initNotification();
            startForeground(123,builder.build());
        }

        player.setOnCompletionListener(completionListener);
        return super.onStartCommand(intent, flags, startId);
    }

    public CacheListener cacheListener=new CacheListener() {
        @Override
        public void onCacheAvailable(File cacheFile, String url, int percentsAvailable) {
            Intent intent=new Intent();
            intent.setAction("getBufferProgress");
            intent.putExtra("bufferPos",percentsAvailable*music.getAlltime()/100);
            sendBroadcast(intent);
            Log.i("缓冲百分比",percentsAvailable+"");
        }
    };
    private void initNotification(){
        remoteViews = new RemoteViews(getPackageName(),R.layout.notification_layout);
        //播放暂停按钮的点击事件
        PendingIntent pIntent1=PendingIntent.getBroadcast(getApplicationContext(),1,new Intent("pauseMusic").putExtra("keys","pause"),PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.pause_music,pIntent1);
        //上一首按钮的点击事件
        PendingIntent pIntent2 = PendingIntent.getBroadcast(getApplicationContext(),2,new Intent("pauseMusic").putExtra("keys","last"),PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.last_music,pIntent2);
        //下一首按钮的点击事件
        PendingIntent pIntent3 = PendingIntent.getBroadcast(getApplicationContext(),3,new Intent("pauseMusic").putExtra("keys","next"),PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.next_music,pIntent3);
        //停止按钮的点击事件
        PendingIntent pIntent4=PendingIntent.getBroadcast(getApplicationContext(),4,new Intent("exitApp"),PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.exitMusic,pIntent4);

        Intent intent1=new Intent(this,MainActivity.class);
        intent1.putExtra("notifi_music",music);
        PendingIntent pendingIntent=PendingIntent.getActivity(getApplicationContext(),0,intent1,PendingIntent.FLAG_UPDATE_CURRENT);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            builder = new NotificationCompat.Builder(this, "fore_service");
        }else {
            builder = new NotificationCompat.Builder(this,"default");
        }
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.app_icon_96))
                .setSmallIcon(R.mipmap.ic_small_64)
                .setContentTitle("正在播放音乐")
                .setContentText("开始播放音乐吧")
                .setContentIntent(pendingIntent)      //设置转到的Activity
                .setSound(null)
                .setContent(remoteViews);   //设置布局到通知栏
    }


    MediaPlayer.OnCompletionListener completionListener=new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            if(order == list_loop) {
                next();
            }else if(order == random_play){
                randowPlay();
            }else {
                player.reset();
                play();
            }
        }
    };


    //暂停音乐的播放
    public void pauseMusic(){
        if(player!=null&&player.isPlaying()){
            Intent intent1=new Intent();
            intent1.setAction("startorpause");
            intent1.putExtra("flags",0);
            sendBroadcast(intent1);
            remoteViews.setImageViewResource(R.id.pause_music,R.mipmap.ic_play_48);
            manager.notify(123,builder.build());
            player.pause();
        }
    }
    //从暂停的地方继续播放音乐
    public void startMusic(MediaPlayer player){
        if (player!=null&&!player.isPlaying()){
            Intent intent1=new Intent();
            intent1.setAction("startorpause");
            intent1.putExtra("flags",1);
            sendBroadcast(intent1);
            remoteViews.setImageViewResource(R.id.pause_music,R.mipmap.ic_pause_48);
            manager.notify(123,builder.build());
            player.start();
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(player.isPlaying()){
            player.stop();
            timer.cancel();
            timer1.cancel();
        }
        unregisterReceiver(broadcast);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            stopForeground(true);
        }else {
            stopForeground(true);
        }
    }
    public class PlayBroadcast extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("startMusic")){
                Bundle bundle=intent.getBundleExtra("mList");
                listp = bundle.getInt("position");
                mList = (ArrayList<Music>)bundle.getSerializable("musicList");
                whichFragment = bundle.getString("whichFragment");
                music = mList.get(listp);
                player.reset();
                play();
            }
            if(intent.getAction().equals("play_net_music")){
                whichFragment =null;
                Bundle bundle=intent.getBundleExtra("music_data");
                listp = bundle.getInt("pos");
                mList = (ArrayList<Music>)bundle.getSerializable("musicList");
                music = mList.get(listp);
                player.reset();
                play();
            }
            if(intent.getAction().equals("sop")) {
                if (player.isPlaying()&&bufferFlag==1) {
                    pauseMusic();
                } else {
                    //music对象不为空，为暂停状态
                    if (music != null&&bufferFlag==1) {
                        startMusic(player);
                    }
                }
            }
            //play页面点击上一首下一首，收到这个广播
            if(intent.getAction().equals("lastAndNext")){
                String lastNext = intent.getStringExtra("lastnext");
                if(lastNext.equals("last")){
                    if(order == list_loop || order == one_loop) {
                        last();
                    }else{
                        randowPlay();
                    }
                }
                if(lastNext.equals("next")){
                    if(order == random_play){
                        randowPlay();
                    }else {
                        next();
                    }
                }
            }
            if(intent.getAction().equals("musicOrder")){
                order = (order+1)%3;
                Intent intent2=new Intent();
                intent2.setAction("getOrder");
                intent2.putExtra("orderKey",order);
                sendBroadcast(intent2);
            }
            if(intent.getAction().equals("status")){
                Intent intent1 = new Intent();
                intent1.setAction("returnOrder");
                Bundle bundle=new Bundle();
                bundle.putInt("orderKey",order);
                bundle.putInt("positions",player.getCurrentPosition());
                intent1.putExtra("orderKeys",bundle);
                sendBroadcast(intent1);
            }
            if(intent.getAction().equals("pauseMusic")){
                String keys = intent.getStringExtra("keys");
                if(keys.equals("pause")) {
                    if (player.isPlaying()&&bufferFlag==1) {
                        pauseMusic();
                    } else {
                        //music对象不为空，为暂停状态
                        if (music != null&&bufferFlag==1) {
                            startMusic(player);
                        }
                    }
                }
                if(keys.equals("last")){
                    if(order == random_play){
                        randowPlay();
                    }else {
                        last();
                    }

                }
                if(keys.equals("next")){
                    if(order == random_play){
                        randowPlay();
                    }else {
                        next();
                    }
                }
            }
            if(intent.getAction().equals("updateActivity")){
                if (music!=null) {
                    //获取正在播放音乐的信息
                    Intent intent1 = new Intent();
                    intent1.setAction("updateMusic");
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("nowplaymusic", music);
                    bundle.putString("whichFragment", whichFragment);
                    intent1.putExtra("nowplay", bundle);
                    sendBroadcast(intent1);
                    //获取播放或暂停的播放状态
                    if (player.isPlaying()) {
                        Intent intent3 = new Intent();
                        intent1.setAction("startorpause");
                        intent1.putExtra("flags", 1);
                        sendBroadcast(intent1);
                    } else {
                        //music对象不为空，为暂停状态
                        if (music != null) {
                            Intent intent2 = new Intent();
                            intent1.setAction("startorpause");
                            intent1.putExtra("flags", 0);
                            sendBroadcast(intent1);
                        }
                    }
                    //获取进度条的状态
                    Intent myIntent = new Intent();
                    Bundle bundle2 = new Bundle();
                    bundle2.putInt("alltime", music.getAlltime());
                    bundle2.putInt("position", player.getCurrentPosition());
                    myIntent.putExtra("current", bundle2);
                    myIntent.setAction("currentposition");
                    sendBroadcast(myIntent);
                }
            }
            if(intent.getAction().equals("exitApp")){

            }
            if(intent.getAction().equals("seekTo")){
                position = intent.getIntExtra("progress",position);
                player.seekTo(position);
            }
            if(intent.getAction().equals("getnowplaymusic")){
                if(music != null) {
                    Intent intent1 = new Intent();
                    intent1.setAction("updateMusic");
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("nowplaymusic", music);
                    bundle.putString("whichFragment", whichFragment);
                    intent1.putExtra("nowplay", bundle);
                    sendBroadcast(intent1);
                }
            }
            if(intent.getAction().equals("getMusicList")){
                Intent intent1=new Intent();
                intent1.setAction("musicList");
                intent1.putExtra("music_list",(ArrayList<Music>)mList);
                sendBroadcast(intent1);
            }
            if(intent.getAction().equals("deleteMusicFromList")){
                Music nowMusic = (Music)intent.getSerializableExtra("delete_music");
                int i=0;
                for(i=0;i<mList.size();i++){
                    if(mList.get(i).getPath().equals(nowMusic.getPath())){
                        mList.remove(i);
                        Log.i("删除歌曲播放列表","成功");
                        break;
                    }
                }
            }
            if(intent.getAction().equals("playMusicOnList")){
                int pos = intent.getIntExtra("what_play_index",-1);
                if(pos!=-1){
                    playByIndex(pos);
                }
            }
        }
    }
}
