package com.example.myapp;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.example.myapp.database.MyDao;
import com.example.myapp.self.Music;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadService extends Service {
    private BroadcastReceiver broadcast;
    private List<Music> downloadList;
    private Thread t1;
    private MyDao myDao;
    public DownloadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        broadcast=new DownloadBroadcastReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction("download_music");
        filter.addAction("get_download_list");
        registerReceiver(broadcast,filter);
        downloadList = new ArrayList<>();
        myDao=new MyDao(getApplicationContext());
        myDao.initConnect();
        listenThread();
    }

    public class DownloadBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("download_music")) {
                Music music = (Music) intent.getSerializableExtra("music");
                int i=0;
                //判断该歌曲是否正在下载中
                for(i=0;i<downloadList.size();i++){
                    if(downloadList.get(i).getPath().equals(music.getPath())){
                        Toast.makeText(getApplicationContext(),"该歌曲已在下载列表",Toast.LENGTH_SHORT).show();
                        break;
                    }

                }
                if(i>=downloadList.size()) {
                    downloadList.add(music);
                    Toast.makeText(getApplicationContext(),"该歌曲加入下载列表成功",Toast.LENGTH_SHORT).show();
                    Log.i("DownloadService", "添加新的歌曲到下载列表中" + music.getSongName());
                }
            }
            if(intent.getAction().equals("get_download_list")){
                Intent intent1=new Intent("return_download_list");
                intent1.putExtra("list",(ArrayList)downloadList);
                sendBroadcast(intent1);
            }
        }
    }


    public void listenThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                int flag = 0;
                while (true){
                    if(downloadList.size()>0) {
                        if(t1!=null&&t1.isAlive()){
                            continue;
                        }
                        if(flag == 1){
                            downloadList.remove(0);
                        }
                        if(downloadList.size()>0) {
                            t1 = new MyThread(downloadList.get(0));
                            t1.start();
                            flag = 1;
                        }
                    }else {
                        flag=0;
                    }
                }
            }
        }).start();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcast);
        if(myDao.isConnection()){
            myDao.closeConnect();
        }
    }
    public class MyThread extends Thread{
        private Music music;
        public MyThread(Music music){
            this.music=music;
        }
        @Override
        public void run() {
            super.run();
            OkHttpClient client=new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS)
                    .build();
            Request request=new Request.Builder().url(music.getPath()).build();
            try {
                Response response=client.newCall(request).execute();
                if(response.isSuccessful()) {
                    InputStream inputStream = response.body().byteStream();
                    String name = response.header("Content-Disposition");
                    long size = response.body().contentLength();
                    String[] strs = name.split("=");
                    String fileName = "";
                    if (strs.length > 1) {
                        fileName = strs[1];
                    }
                    Log.i("文件名", fileName);
                    //文件保存的路径
                    String newPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/downloadMusic/" + fileName;
                    FileOutputStream out = new FileOutputStream(newPath);
                    int i = 0, j = 0;
                    byte[] bytes = new byte[1024];
                    while ((i = inputStream.read(bytes)) != -1) {
                        out.write(bytes, 0, i);
                        j = j + i;
                        Intent intent = new Intent("update_dw_progress");
                        intent.putExtra("progress", (int) (j * 100 / size));
                        intent.putExtra("music", music);
                        intent.putExtra("newPath", newPath);
                        sendBroadcast(intent);
                    }
                    if (j == size) {
                        music.setPath(newPath);
                        music.setFlag(0);
                        myDao.insertMusic(music, "download_music_list");
                        myDao.insertMusic(music, "local_music_list");
                        Log.i("音乐" + fileName, "下载完毕");
                    }
                }
            } catch (Exception e) {
                if(e instanceof ConnectException){

                }
                e.printStackTrace();
            }
        }
        }

}
