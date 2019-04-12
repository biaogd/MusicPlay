package com.example.myapp;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

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
    private Queue<Music> downQueue=new ArrayBlockingQueue<Music>(200);
    private Thread t1,t2,t3;
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
        listenThread();
    }

    public class DownloadBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("download_music")) {
                Music music = (Music) intent.getSerializableExtra("music");
                downloadList.add(music);
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
                    }
                }
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcast);
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
                    String []strs = name.split("=");
                    String fileName="";
                    if(strs.length>1){
                        fileName = strs[1];
                    }
                    Log.i("文件名",fileName);
                    FileOutputStream out=new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath()+"/downloadMusic/"+fileName);
                    int i=0,j=0;
                    byte[] bytes=new byte[1024];
                    while ((i = inputStream.read(bytes))!=-1){
                        out.write(bytes,0,i);
                        j = j+i;
                        Intent intent=new Intent("update_dw_progress");
                        intent.putExtra("progress",(int)(j*100/size));
                        intent.putExtra("music",music);
                        sendBroadcast(intent);
                    }
                    Log.i("音乐"+fileName,"下载完毕");
                }
            } catch (Exception e) {
                if(e instanceof ConnectException){

                }
                e.printStackTrace();
            }
        }
        }

}
