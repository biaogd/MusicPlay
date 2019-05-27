package com.example.myapp;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapp.self.SelfFinal;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoseActivity extends AppCompatActivity {
    private TextView options;
    private EditText emailET,verifyCodeET,pwET1,pwET2;
    private Button button;
    private Handler handler;
    private String myEmail;
    private static int flag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lose);
        options=(TextView)findViewById(R.id.tishi_tv);
        emailET=(EditText)findViewById(R.id.input_email);
        verifyCodeET=(EditText)findViewById(R.id.input_verifyCode);
        pwET1 = (EditText)findViewById(R.id.input_pw1);
        pwET2 = (EditText)findViewById(R.id.input_pw2);
        button= (Button)findViewById(R.id.submit_btn);
        options.setText("输入邮箱地址以验证邮箱");
        handler=new MyHandler();
        flag = 0;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flag == 0) {
                    final String email = emailET.getText().toString().trim();
                    if (emailET.length() == 0 || email.length() == 0) {
                        Toast.makeText(LoseActivity.this, "邮箱不能为空", Toast.LENGTH_LONG).show();
                        return;
                    }
                    OkHttpClient client = new OkHttpClient();
                    String url = SelfFinal.host + SelfFinal.port + "/music/user/losePassword?email=" + email;
                    Request request = new Request.Builder().get().url(url).build();
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {

                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String str = response.body().string();
                            if (str.equals("noUser")) {
                                handler.sendEmptyMessage(404);
                            } else if (str.equals("next")) {
                                Message message = new Message();
                                message.what = 200;
                                message.obj = email;
                                handler.sendMessage(message);
                            }
                        }
                    });
                }else if(flag==1){
                    String code = verifyCodeET.getText().toString().trim();
                    if(verifyCodeET.length()==0||code.length()==0){
                        Toast.makeText(LoseActivity.this, "验证码不能为空", Toast.LENGTH_LONG).show();
                        return;
                    }
                    OkHttpClient client = new OkHttpClient();
                    String url = SelfFinal.host + SelfFinal.port + "/music/user/verifyCode?email=" + myEmail+"&code="+code;
                    Request request = new Request.Builder().get().url(url).build();
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {

                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String str = response.body().string();
                            if (str.equals("notCorrect")) {
                                handler.sendEmptyMessage(405);
                            } else if (str.equals("correct")) {
                                handler.sendEmptyMessage(201);
                            }
                        }
                    });
                }else if(flag==2){
                    button.setEnabled(false);
                    String pw1 = pwET1.getText().toString().trim();
                    String pw2 = pwET2.getText().toString().trim();
                    if(pw1.length()==0||pw2.length()==0){
                        Toast.makeText(LoseActivity.this, "密码不能为空", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if(!pw1.equals(pw2)){
                        Toast.makeText(LoseActivity.this, "两次输入的密码不同", Toast.LENGTH_LONG).show();
                        return;
                    }
                    OkHttpClient client = new OkHttpClient();
                    String url = SelfFinal.host + SelfFinal.port + "/music/user/modifyPW";
                    RequestBody body = new FormBody.Builder().add("email",myEmail)
                            .add("pw",pw1).build();
                    Request request=new Request.Builder().post(body).url(url).build();
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {

                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String str = response.body().string();
                            if (str.equals("falied")) {
                                handler.sendEmptyMessage(406);
                            } else if (str.equals("success")) {
                                handler.sendEmptyMessage(202);
                            }
                        }
                    });
                }
            }
        });
    }
    private class MyHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 404:
                    Toast.makeText(LoseActivity.this,"账号不存在",Toast.LENGTH_LONG).show();
                    break;
                case 200:
                    myEmail = (String)msg.obj;
                    options.setText("验证码已发送到:"+myEmail+"请注意查收");
                    emailET.setVisibility(View.GONE);
                    verifyCodeET.setVisibility(View.VISIBLE);
                    flag = 1;
                    break;
                case 405:
                    Toast.makeText(LoseActivity.this,"验证码错误",Toast.LENGTH_LONG).show();
                    break;
                case 201:
                    verifyCodeET.setVisibility(View.GONE);
                    pwET1.setVisibility(View.VISIBLE);
                    pwET2.setVisibility(View.VISIBLE);
                    options.setText("输入新的密码");
                    flag=2;
                    break;
                case 406:
                    Toast.makeText(LoseActivity.this,"密码修改失败",Toast.LENGTH_LONG).show();
                    break;
                case 202:
                    Toast.makeText(LoseActivity.this,"密码修改成功",Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }
}
