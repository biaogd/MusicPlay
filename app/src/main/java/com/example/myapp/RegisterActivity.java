package com.example.myapp;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapp.self.SelfFinal;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText userNameEdit,emailEdit,passwordEdit1,passwordEdit2;
    private Button submit;
    private OkHttpClient client;
    private Handler handler;
    private TextView promptTv,successTv;
    private LinearLayout linearLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        userNameEdit = (EditText)findViewById(R.id.user_name_edit);
        emailEdit = (EditText)findViewById(R.id.user_email_edit);
        passwordEdit1 = (EditText)findViewById(R.id.user_password1_edit);
        passwordEdit2 = (EditText)findViewById(R.id.user_password2_edit);
        submit = (Button)findViewById(R.id.register_submit);
        handler = new MyHandler();
        promptTv =(TextView)findViewById(R.id.prompt_tv);
        linearLayout = (LinearLayout)findViewById(R.id.main_layout);
        successTv = (TextView)findViewById(R.id.success_tv);
        submit.setOnClickListener(listener);
    }

    View.OnClickListener listener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.register_submit:
                    promptTv.setVisibility(View.GONE);
                    if(userNameEdit.length()==0||emailEdit.length()==0||passwordEdit1.length()==0||passwordEdit2.length()==0){
                        Toast.makeText(RegisterActivity.this,"有字段为空，填写完整",Toast.LENGTH_SHORT).show();
                    }else {
                        String userName = userNameEdit.getText().toString().trim();
                        String email = emailEdit.getText().toString().trim();
                        String pw1 = passwordEdit1.getText().toString().trim();
                        String pw2 = passwordEdit2.getText().toString().trim();
                        //输入的两次密码不相同
                        if(!pw1.equals(pw2)){
                            Toast.makeText(RegisterActivity.this,"两次输入的密码不同",Toast.LENGTH_SHORT).show();
                        }else {
                            //与后端通信
                            client = new OkHttpClient();
                            RequestBody body=new FormBody.Builder().add("userName",userName)
                                    .add("email",email).add("pw",pw1)
                                    .build();
                            String urls = SelfFinal.host+SelfFinal.port +"/music/user/register";
                            Request request=new Request.Builder().post(body).url(urls).build();
                            client.newCall(request).enqueue(new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    handler.sendEmptyMessage(400);
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    String status = response.body().string();
                                    if(status.equals("failed")){
                                        handler.sendEmptyMessage(401);
                                    }else if(status.equals("successed")){
                                        handler.sendEmptyMessage(200);
                                    }else {
                                        handler.sendEmptyMessage(400);
                                    }
                                }
                            });

                        }
                    }

            }
        }
    };
    public class MyHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 400:
                    promptTv.setText("请求服务器错误，请重试");
                    promptTv.setVisibility(View.VISIBLE);
                    break;
                case 401:
                    promptTv.setText("该邮箱已经被注册，直接登录");
                    promptTv.setVisibility(View.VISIBLE);
                    break;
                case 200:
                    linearLayout.setVisibility(View.GONE);
                    successTv.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }
}
