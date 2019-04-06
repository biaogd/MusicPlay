package com.example.myapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
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
import android.widget.Toast;

import com.example.myapp.database.MyDao;
import com.example.myapp.self.Music;

import java.io.File;
import java.io.FileOutputStream;
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
                    Bundle bundle =new Bundle();
                    bundle.putSerializable("playMusic",nowMusic);
                    bundle.putBoolean("playing",playing);
                    intent.putExtra("playIntent",bundle);
                    startActivity(intent);
                }
            }
        });
        //在应用程序启动的时候启动播放歌曲的服务
        Intent intent1=new Intent(this,MyService.class);
        startService(intent1);

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

    //检查是否有更新的版本
    public void updateApp(){
        Log.i("函数","更新版本的函数执行了");
       new Thread(new Runnable() {
           @Override
           public void run() {
               try {
                   Log.i("检查更新","程序执行了检查更新");
                   OkHttpClient client=new OkHttpClient();
                   Request req = new Request.Builder().url("http://www.mybiao.top:8080/checkUpdate").build();
                   Response res = client.newCall(req).execute();
                   if(res.isSuccessful()){
                       isNew = Integer.parseInt(res.body().string());
                       System.out.println("成功"+"检查新版本成功"+isNew);
                       if(isNew>(int)(MUSIC_V*10)) {
                           System.out.println("正在执行更新程序");
                           handler.sendEmptyMessage(10);
                       }
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
                            new MyDao(MainActivity.this).deleteMusic(music,"near_music_list");     //从数据库中删除
                            new MyDao(MainActivity.this).insertMusic(music,"near_music_list");
                            break;
                        }
                    }
                    if(i>=list1.size()){     //这首歌不再数据库当中，也就是不再最近播放列表中
                        new MyDao(MainActivity.this).insertMusic(music,"near_music_list");
                    }
                }
                if(whichFragment!=null&&!whichFragment.equals("nearFragment")){
                    List<Music> list1=new MyDao(MainActivity.this).findAll("near_music_list");
                    int i;
                    for(i=0;i<list1.size();i++){
                        Music m=list1.get(i);
                        if(m.getPath().equals(music.getPath())){      //列表中已经有这个歌曲
                            new MyDao(MainActivity.this).deleteMusic(music,
                                    "near_music_list");     //从数据库中删除
                            new MyDao(MainActivity.this).insertMusic(music,"near_music_list");
                            break;
                        }
                    }
                    if(i>=list1.size()){     //这首歌不再数据库当中，也就是不再最近播放列表中
                        new MyDao(MainActivity.this).insertMusic(music,"near_music_list");
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
                    Request request=new Request.Builder().url("http://www.mybiao.top:8080/app").addHeader("Accept-Encoding","identity").build();
                    Response response=client.newCall(request).execute();
                    if(response.isSuccessful()){
                        String handers = response.header("Content-Disposition");
                        String []strs = handers.split("=");
                        String fileName="";
                        if(strs.length>1){
                            fileName = strs[1];
                        }
                        InputStream inputStream=response.body().byteStream();
                        //无法获取文件大小位数
                        long allSize = response.body().contentLength();
                        Log.i("总的大小",""+allSize);
                        File file=new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+fileName);
                        if(!file.exists()){
                            file.createNewFile();
                        }
                        FileOutputStream out=new FileOutputStream(file);
                        byte []bytes=new byte[4096];
                        int i=0;
                        int j=0;
                        while ((i = inputStream.read(bytes))!=-1){
                            out.write(bytes,0,i);
                            j +=i;
                            Message message=new Message();
                            message.what=105;
                            message.arg1 = (int)(j/allSize*100);
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

                Toast.makeText(MainActivity.this,"有新版本可以更新",Toast.LENGTH_LONG).show();
                AlertDialog dialog=new AlertDialog.Builder(MainActivity.this).create();
                dialog.setTitle("版本更新");
                dialog.setMessage("检测到新版本，是否更新?");
                dialog.setButton(DialogInterface.BUTTON_POSITIVE, "立即更新", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //有新版本时调用浏览器进行下载
//                        Intent intent1=new Intent();
//                        intent1.setAction(Intent.ACTION_VIEW);
//                        intent1.setData(Uri.parse("http://www.mybiao.top:8080/app"));
//                        startActivity(intent1);
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                            NotificationChannel channel=new NotificationChannel("update_app","myapp",NotificationManager.IMPORTANCE_LOW);
                            initNotification();
                            notificationManager.createNotificationChannel(channel);
                        }else{
                            initNotification();
                        }
                        notificationManager.notify(210,builder.build());
                        downloadUpdate();
                    }
                });
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "暂不更新", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this,"已取消更新",Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.show();
            }
            if(msg.what==105){
                builder.setProgress(100,msg.arg1,false);
                builder.setContentText("下载进度:"+msg.arg1+"%");
                notificationManager.notify(210,builder.build());
            }
        }
    }
}
