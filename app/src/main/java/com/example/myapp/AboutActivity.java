package com.example.myapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapp.self.MyApp;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AboutActivity extends AppCompatActivity {

    private TextView checkUpdateTV;
    private NotificationCompat.Builder builder;
    private Handler handler;
    private NotificationManager notificationManager;
    private Gson gson;
    private int versionCode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        //获取软件的版本好versionCode
        PackageInfo packageInfo= null;
        try {
            packageInfo = this.getApplicationContext().getPackageManager().getPackageInfo(this.getPackageName(),0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String versionName = packageInfo.versionName;
        versionCode = packageInfo.versionCode;
        TextView textView=(TextView)findViewById(R.id.version_id);
        textView.setText("版本："+versionName);

        handler=new MyHandler();
        gson=new Gson();
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        checkUpdateTV=(TextView)findViewById(R.id.check_update_tv);
        checkUpdateTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkNet(AboutActivity.this)){
                    updateApp();
                }else {
                    Toast.makeText(AboutActivity.this,"网络无法连接，稍后重试",Toast.LENGTH_LONG).show();
                }
            }
        });

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
                        MyApp myApp = gson.fromJson(body, MyApp.class);
                        if(myApp.getStatus().equals("ok")){
                            Log.i("有新版本",myApp.getName());
                            Message message=new Message();
                            message.what=10;
                            message.obj = myApp;
                            handler.sendMessage(message);
                        }else {
                            handler.sendEmptyMessage(123);
                        }
                    }
                }catch (Exception e){
                    handler.sendEmptyMessage(404);
                }
            }
        }).start();

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

    public class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what==10){
                MyApp app=(MyApp) msg.obj;
                View view= LayoutInflater.from(AboutActivity.this).inflate(R.layout.update_app,null);
                TextView content = (TextView)view.findViewById(R.id.update_content);
                content.setText(app.getContent().replace("\\n","\n"));
                WindowManager wm = getWindowManager();
                DisplayMetrics metrics=new DisplayMetrics();
                wm.getDefaultDisplay().getMetrics(metrics);
                int width = metrics.widthPixels;
                final PopupWindow popupWindow=new PopupWindow(view,(int)(width*3/4),WindowManager.LayoutParams.WRAP_CONTENT);
                //设置弹出窗口背景变半透明，来高亮弹出窗口
                WindowManager.LayoutParams lp =getWindow().getAttributes();
                lp.alpha=0.5f;
                getWindow().setAttributes(lp);

                popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        //恢复透明度
                        WindowManager.LayoutParams lp =getWindow().getAttributes();
                        lp.alpha=1f;
                        getWindow().setAttributes(lp);
                    }
                });
                popupWindow.setTouchInterceptor(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if(event.getAction()==MotionEvent.ACTION_OUTSIDE) {
                            return true;
                        }
                        return false;
                    }
                });
                popupWindow.showAtLocation(checkUpdateTV, Gravity.CENTER,0,0);
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
                            NotificationChannel channel=new NotificationChannel("update_app","myapp", NotificationManager.IMPORTANCE_LOW);
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
            if(msg.what==404){
                Toast.makeText(AboutActivity.this,"无法连接到服务器，请稍后重试",Toast.LENGTH_LONG).show();
            }
            if(msg.what==123){
                Toast.makeText(AboutActivity.this,"已更新到最新版本",Toast.LENGTH_LONG).show();
            }
        }
    }
}
