package com.example.myapp;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.myapp.database.MyDao;
import com.example.myapp.self.Music;
import com.example.myapp.self.MyApp;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends Activity {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };

    private Fragment mainFragment;
    //歌曲总时间
    private int alltime;
    //当前的播放进度
    private int position;

    private ProgressBar progressBar;

    private MyBroadcast broadcast;

    private ImageButton playbtn;
    //用于判断该程序是不是第一次运行
    private SharedPreferences.Editor editor;
    private SharedPreferences ps;
    private int count=0;
    private TextView nameTv;
    private TextView authorTv;
    private SQLiteDatabase db;
    private String sqlCreate="create table local_music_list(id integer primary key autoincrement,song_name text,song_author text,all_time integer,path text,song_size integer,flag integer default 0,love integer default 0);";
    private String nearsql="create table near_music_list(id integer primary key autoincrement,song_name text,song_author text,all_time integer,path text,song_size integer,flag integer default 0,love integer default 0);";
    private String downloadsql="create table download_music_list(id integer primary key autoincrement,song_name text,song_author text,all_time integer,song_size integer,path text,flag integer default 0,love integer default 0);";
    private String lovesql="create table love_music_list(id integer primary key autoincrement,song_name text,song_author text,all_time integer, path text,song_size integer,flag integer default 0,love integer default 0)";
    //要播放的音乐
    private Music nowMusic;
    private boolean playing;
    private static final double MUSIC_V = 1.0d;

    //程序的版本号versionCode,用于更新软件
    private int versionCode;

    private int isNew = 0;
    private Handler handler;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder builder;
    //当前Service当中的音乐播放列表
    private List<Music> musicList;
    private ListView listView;
    private MyAdapter2 adapter2;
    private Gson gson=new Gson();
    private MyDao myDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //透明状态栏
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT){
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(this);
        //获取软件的版本好versionCode
        PackageInfo packageInfo= null;
        try {
            packageInfo = this.getApplicationContext().getPackageManager().getPackageInfo(this.getPackageName(),0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        versionCode = packageInfo.versionCode;
        Log.i("该应用的版本号",versionCode+"");

        ps = getSharedPreferences("isFirst",MODE_PRIVATE);
        this.count=ps.getInt("count",-1);
        //等于-1，说明第一次启动,创建所需要的数据库和表
        if(this.count == -1){
            db = this.openOrCreateDatabase("mydb.db",MODE_PRIVATE,null);
            db.execSQL(sqlCreate);
            db.execSQL(nearsql);
            db.execSQL(downloadsql);
            db.execSQL(lovesql);
        }
        this.count++;
        editor=getSharedPreferences("isFirst",MODE_PRIVATE).edit();
        editor.putInt("count",count);
        editor.commit();

        //创建程序目录
        File file=new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/downloadMusic");
        if(!file.exists()){
            file.mkdir();
        }

        FragmentManager manager=getFragmentManager();
        FragmentTransaction transaction=manager.beginTransaction();
        if(mainFragment == null){
            mainFragment = new MainFragment();
            transaction.add(R.id.other_frag,mainFragment);
        }
        transaction.show(mainFragment);
        transaction.commit();

        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        handler = new MyHandler();
        myDao=new MyDao(this);

        updateApp();
        nameTv = (TextView)findViewById(R.id.myname);
        authorTv = (TextView)findViewById(R.id.myauthor);
        progressBar = (ProgressBar)findViewById(R.id.progress);
        progressBar.setProgress(0);
        //动态注册广播
        broadcast=new MyBroadcast();
        IntentFilter filter=new IntentFilter();
        filter.addAction("currentposition");
        filter.addAction("updateMusic");
        filter.addAction("startorpause");
        filter.addAction("exitApp");
        filter.addAction("musicList");
        registerReceiver(broadcast,filter);
        //0是暂停播放时显示，1是正在播放时显示
        playbtn=(ImageButton) findViewById(R.id.palybtn);
        playbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent controll=new Intent();
                controll.setAction("sop");
                controll.putExtra("flag","click");
                sendBroadcast(controll);
            }

        });

        LinearLayout layout=(LinearLayout)findViewById(R.id.about_play_song);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, PlayActivity.class);
                if (nowMusic != null) {
                    startActivity(intent);
                }
            }
        });
        //在应用程序启动的时候启动播放歌曲的服务
        Intent intent1=new Intent(this,MyService.class);
        startService(intent1);


        //启动下载服务
        Intent intent=new Intent(this,DownloadService.class);
        startService(intent);
        musicList = new ArrayList<>();
        ImageButton musicListBtn=(ImageButton)findViewById(R.id.music_list_menu);
        musicListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击按钮时发送广播给Service获取当前的播放列表
                final Intent intent=new Intent();
                intent.setAction("getMusicList");
                sendBroadcast(intent);
                View view=LayoutInflater.from(MainActivity.this).inflate(R.layout.play_list,null);
                final PopupWindow popupWindow=new PopupWindow(view,WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
                popupWindow.setFocusable(true);
                popupWindow.setTouchable(true);
                popupWindow.setOutsideTouchable(true);
                popupWindow.showAtLocation(findViewById(R.id.music_list_menu), Gravity.BOTTOM,0,0);
                Button cancelBtn=(Button)view.findViewById(R.id.cancel_btn);
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                    }
                });
                listView = (ListView)view.findViewById(R.id.now_play_music_list);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Log.i("点击了歌曲",musicList.get(position).getSongName());
                        Intent intent2=new Intent();
                        intent2.setAction("playMusicOnList");
                        intent2.putExtra("what_play_index",position);
                        sendBroadcast(intent2);
                    }
                });
            }
        });
    }

    //检查是否有更新的版本,,
    public void updateApp(){
        Log.i("函数","更新版本的函数执行了");
       new Thread(new Runnable() {
           @Override
           public void run() {
               try {
                   Log.i("检查更新","程序执行了检查更新");
                   OkHttpClient client=new OkHttpClient();
                   Request req = new Request.Builder().url("http://www.mybiao.top:8000/checkUpdate?code="+versionCode).build();
                   Response res = client.newCall(req).execute();
                   if(res.isSuccessful()){
                       String body = res.body().string();
                       System.out.println("成功"+"检查新版本成功"+isNew);
                       MyApp myApp = gson.fromJson(body, MyApp.class);
                       if(myApp.getStatus().equals("ok")){
                           Log.i("有新版本",myApp.getName());
                           Message message=new Message();
                           message.what=10;
                           message.obj = myApp;
                           handler.sendMessage(message);
                       }
//                       if(isNew>(int)(MUSIC_V*10)) {
//                           System.out.println("正在执行更新程序");
//                           handler.sendEmptyMessage(10);
//                       }
                   }
               }catch (Exception e){
                   e.printStackTrace();
               }
           }
       }).start();

    }





//android6.0以上需要动态申请权限
    public static void verifyStoragePermissions(Activity activity) {

        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public class MyBroadcast extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("currentposition")) {
                Bundle bundle = intent.getBundleExtra("current");
                alltime = bundle.getInt("alltime");
                position = bundle.getInt("position");
                progressBar.setMax(alltime);
                progressBar.setProgress(position);
            }
            if(intent.getAction().equals("updateMusic")) {
                Bundle bundle=intent.getBundleExtra("nowplay");
                Music music=(Music)bundle.getSerializable("nowplaymusic");
                nowMusic = music;
                String whichFragment = bundle.getString("whichFragment");
                nameTv.setText(music.getSongName());
                authorTv.setText(music.getSongAuthor());
                if(whichFragment == null){
                    //获取最近音乐列表的所有音乐
                    Log.i("whichFragment",whichFragment+"");
                    List<Music> list1 = new MyDao(MainActivity.this).findAll("near_music_list");
                    int i;
                    for(i=0;i<list1.size();i++){
                        Music m=list1.get(i);
                        if(m.getPath().equals(music.getPath())){      //列表中已经有这个歌曲
                            myDao.deleteMusic(music,"near_music_list");     //从数据库中删除
                            myDao.insertMusic(music,"near_music_list");
                            break;
                        }
                    }
                    if(i>=list1.size()){     //这首歌不再数据库当中，也就是不再最近播放列表中
                        myDao.insertMusic(music,"near_music_list");
                    }
                }
                if(whichFragment!=null&&!whichFragment.equals("nearFragment")){
                    List<Music> list1=myDao.findAll("near_music_list");
                    int i;
                    for(i=0;i<list1.size();i++){
                        Music m=list1.get(i);
                        if(m.getPath().equals(music.getPath())){      //列表中已经有这个歌曲
                            Log.i("在MainActivity","updateMusic 之ixng了");
                            myDao.deleteMusic(music,
                                    "near_music_list");     //从数据库中删除
                            myDao.insertMusic(music,"near_music_list");
                            break;
                        }
                    }
                    if(i>=list1.size()){     //这首歌不再数据库当中，也就是不再最近播放列表中
                        myDao.insertMusic(music,"near_music_list");
                    }
                }
                if(adapter2!=null) {
                    int j = 0;
                    for (j = 0; j < musicList.size(); j++) {
                        if (musicList.get(j).getId() == -1000) {
                            musicList.get(j).setId(-1);
                        }
                    }
                    int i = 0;
                    for (i = 0; i < musicList.size(); i++) {
                        if (musicList.get(i).getPath().equals(nowMusic.getPath())) {
                            musicList.get(i).setId(-1000);
                        }
                    }
                    adapter2.notifyDataSetChanged();
                }
            }
            if(intent.getAction().equals("startorpause")){
                int flags = intent.getIntExtra("flags",0);
                if(flags == 0){
                    playbtn.setImageResource(R.mipmap.ic_play64);
                    playing = false;
                }else{
                    playbtn.setImageResource(R.mipmap.ic_pause64);
                    playing = true;
                }
            }
            //接收到从通知栏退出程序按钮发出的广播，销毁当前的活动
            if(intent.getAction().equals("exitApp")){
                finish();
            }
            if(intent.getAction().equals("musicList")){
                musicList = (ArrayList<Music>)intent.getSerializableExtra("music_list");
                if (musicList!=null&&listView!=null) {
                    int j = 0;
                    for (j = 0; j < musicList.size(); j++) {
                        if (musicList.get(j).getId() == -1000) {
                            musicList.get(j).setId(-1);
                        }
                    }
                    int i = 0;
                    for (i = 0; i < musicList.size(); i++) {
                        if (musicList.get(i).getPath().equals(nowMusic.getPath())) {
                            musicList.get(i).setId(-1000);
                        }
                    }
                    adapter2 = new MyAdapter2(MainActivity.this, musicList);
                    listView.setAdapter(adapter2);
                    adapter2.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //每次从新进入到这个Activity，发送广播到Service，获取当前的播放状态
        Intent intent1=new Intent();
        intent1.setAction("updateActivity");
        sendBroadcast(intent1);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //程序退出时停止服务
        Intent intent=new Intent(this,MyService.class);
        stopService(intent);
        unregisterReceiver(broadcast);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent home = new Intent(Intent.ACTION_MAIN);
            home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            home.addCategory(Intent.CATEGORY_HOME);
            startActivity(home);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    private void initNotification(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            builder = new NotificationCompat.Builder(this,"update_app");
        }else {
            builder = new NotificationCompat.Builder(this,"default");
        }
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.app_icon_96))
                .setSmallIcon(R.mipmap.ic_small_64)
                .setContentTitle("正在更新")
                .setAutoCancel(true)
                .setContentText("下载进度:"+"0%")
                .setProgress(100,0,false);
    }

    private void downloadUpdate(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client=new OkHttpClient();
                try{
                    Request request=new Request.Builder().url("http://www.mybiao.top:8000/downloadApp").addHeader("Accept-Encoding","identity").build();
                    Response response=client.newCall(request).execute();
                    if(response.isSuccessful()){
                        String handers = response.header("Content-Disposition");
                        String []strs = handers.split("=");
                        String fileName="";
                        if(strs.length>1){
                            fileName = strs[1];
                        }
                        InputStream inputStream=response.body().byteStream();
                        long allSize = response.body().contentLength();
                        File file=new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+fileName);
                        if(!file.exists()){
                            file.createNewFile();
                        }
                        FileOutputStream out=new FileOutputStream(file);
                        byte []bytes=new byte[1024];
                        int i=0;
                        int j=0;
                        while ((i = inputStream.read(bytes))!=-1){
                            out.write(bytes,0,i);
                            j +=i;
//                            Log.i("j的值为",""+j);
                            Message message=new Message();
                            message.what=105;
                            message.arg1 = (int)(j*100/allSize);
                            message.obj = fileName;
                            handler.sendMessage(message);
                        }
                        out.close();
                        inputStream.close();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public class MyHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what==10){
                MyApp app=(MyApp) msg.obj;
                View view=LayoutInflater.from(MainActivity.this).inflate(R.layout.update_app,null);
                TextView content = (TextView)view.findViewById(R.id.update_content);
                content.setText("\n"+app.getContent());

                WindowManager wm = getWindowManager();
                DisplayMetrics metrics=new DisplayMetrics();
                wm.getDefaultDisplay().getMetrics(metrics);
                int width = metrics.widthPixels;
                final PopupWindow popupWindow=new PopupWindow(view,(int)(width*2/3),WindowManager.LayoutParams.WRAP_CONTENT);
                popupWindow.setTouchInterceptor(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if(event.getAction()==MotionEvent.ACTION_OUTSIDE) {
                            return true;
                        }
                        return false;
                    }
                });
                popupWindow.showAtLocation(nameTv,Gravity.CENTER,0,0);
                ImageButton backBtn = (ImageButton)view.findViewById(R.id.back_update);
                backBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    popupWindow.dismiss();
                    }
                });
                Button updateBtn = (Button)view.findViewById(R.id.update_btn);
                updateBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                            NotificationChannel channel=new NotificationChannel("update_app","myapp",NotificationManager.IMPORTANCE_LOW);
                            initNotification();
                            notificationManager.createNotificationChannel(channel);
                        }else{
                            initNotification();
                        }
                        notificationManager.notify(210,builder.build());
                        downloadUpdate();
                        popupWindow.dismiss();
                    }
                });
            }
            if(msg.what==105){

                builder.setProgress(100,msg.arg1,false);
                builder.setContentText("下载进度:"+msg.arg1+"%");
                notificationManager.notify(210,builder.build());
                if(msg.arg1==100){
                    builder.setContentTitle("下载完毕");
                    builder.setProgress(0,0,false);
                    Intent intent=new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    File file=new File(Environment.getExternalStorageDirectory().getAbsolutePath(),(String)msg.obj);
                    System.out.println(file.getName());
                    if(Build.VERSION.SDK_INT>=24){
                        Uri apkuri = FileProvider.getUriForFile(getApplicationContext(),"com.example.myapp.fileprovider",file);
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        intent.setDataAndType(apkuri,"application/vnd.android.package-archive");
                    }else {
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setDataAndType(Uri.fromFile(file),"application/vnd.android.package-archive");
                    }
                    notificationManager.notify(210,builder.build());
                    startActivity(intent);
                }
            }
        }
    }
}
