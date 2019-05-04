package com.example.myapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myapp.self.MyLogin;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        editText=(EditText)findViewById(R.id.error_text);
        button=(Button)findViewById(R.id.error_submit);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = editText.getText().toString().trim();
                if(text.length()==0){
                    Toast.makeText(ReportActivity.this,"内容不能为空",Toast.LENGTH_LONG);
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
                Request request=new Request.Builder().post(body).url("").build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {

                    }
                });
            }
        });
    }
}
