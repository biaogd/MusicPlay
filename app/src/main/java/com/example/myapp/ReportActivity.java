package com.example.myapp;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myapp.self.MyLogin;
import com.example.myapp.self.SelfFinal;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ReportActivity extends AppCompatActivity {
    private EditText editText;
    private Button button;
    private Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        editText=(EditText)findViewById(R.id.error_text);
        button=(Button)findViewById(R.id.error_submit);
        handler=new MyHandler();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button.setEnabled(false);
                String text = editText.getText().toString().trim();
                if(text.length()==0){
                    Toast.makeText(ReportActivity.this,"内容不能为空",Toast.LENGTH_LONG).show();
                }
                String userEmail;
                if(MyLogin.userEmail!=null){
                    userEmail=MyLogin.userEmail;
                }else {
                    userEmail = "null";
                }
                OkHttpClient client=new OkHttpClient();
                SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String now = format.format(new Date());
                RequestBody body=new FormBody.Builder()
                        .add("userEmail",userEmail)
                        .add("time",now)
                        .add("text",text)
                        .build();
                String url = SelfFinal.host+SelfFinal.port+"/errorReport";
                Request request=new Request.Builder().post(body).url(url).build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        handler.sendEmptyMessage(400);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String str = response.body().string();
                        if(str.equals("success")){
                            handler.sendEmptyMessage(200);
                        }else {
                            handler.sendEmptyMessage(400);
                        }
                    }
                });
            }
        });
    }
    public class MyHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what==200){
                Toast.makeText(ReportActivity.this,"提交成功",Toast.LENGTH_LONG).show();
                button.setEnabled(false);
            }
            if(msg.what==400){
                Toast.makeText(ReportActivity.this,"提交失败",Toast.LENGTH_LONG).show();
                button.setEnabled(true);
            }
        }
    }
}
