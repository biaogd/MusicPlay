package com.example.myapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Login_in extends Activity {

    private Button cancelBtn,loginInBtn;
    private EditText userIdEditText,userPwEditText;
    private TextView losePwTv,registerTv,errTv;
    private Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_in);
        cancelBtn=(Button)findViewById(R.id.login_cancel_btn);
        cancelBtn.setOnClickListener(listener);
        userIdEditText = (EditText)findViewById(R.id.user_id);
        userPwEditText = (EditText)findViewById(R.id.user_pw);
        loginInBtn=(Button)findViewById(R.id.login_in_btn);
        loginInBtn.setOnClickListener(listener);

        registerTv = (TextView)findViewById(R.id.user_register_tv);
        registerTv.setOnClickListener(listener);
        errTv = (TextView)findViewById(R.id.login_err_tv);
        handler=new MyHandler();
    }

    View.OnClickListener listener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.login_cancel_btn:
                    finish();
                    break;
                case R.id.login_in_btn:
                    errTv.setVisibility(View.GONE);
                    String userId = userIdEditText.getText().toString().trim();
                    String userPw = userPwEditText.getText().toString().trim();
                    if(userId.length()==0||userPw.length()==0){
                        errTv.setText("用户名或密码为空");
                        errTv.setVisibility(View.VISIBLE);
                    }else {
                        OkHttpClient client=new OkHttpClient();
                        RequestBody body=new FormBody.Builder().add("email",userId)
                                .add("password",userPw).build();
                        String url = "http://www.mybiao.top:8000/music/user/login";
                        String urls="http://192.168.0.106:8000/music/user/login";
                        Request request=new Request.Builder().url(urls).post(body).build();
                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                handler.sendEmptyMessage(400);
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                String str = response.body().string();
                                if(str.equals("unActivation")){
                                    //用户未激活
                                    handler.sendEmptyMessage(100);
                                }else if(str.equals("success")){
                                    handler.sendEmptyMessage(200);
                                }else if(str.equals("noPassword")){
                                    handler.sendEmptyMessage(101);
                                }else if(str.equals("unRegister")){
                                    handler.sendEmptyMessage(102);
                                }else {
                                    handler.sendEmptyMessage(404);
                                }
                            }
                        });
                    }
                    break;
                case R.id.user_register_tv:
                    Intent intent=new Intent(Login_in.this,RegisterActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    };
    public class MyHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 400:
                    errTv.setText("用户登录异常,稍后重试");
                    errTv.setVisibility(View.VISIBLE);
                    break;
                case 100:
                    errTv.setText("用户已注册，但未激活");
                    errTv.setVisibility(View.VISIBLE);
                    break;
                case 200:

                    finish();
                    break;
                case 101:
                    errTv.setText("密码错误");
                    errTv.setVisibility(View.VISIBLE);
                    userPwEditText.setText("");
                    break;
                case 102:
                    errTv.setText("用户不存在");
                    errTv.setVisibility(View.VISIBLE);
                    break;
                case 404:
                    errTv.setText("有未知的异常");
                    errTv.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }
}
