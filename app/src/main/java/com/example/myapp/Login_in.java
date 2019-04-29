package com.example.myapp;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Login_in extends Activity {

    private Button cancelBtn,loginInBtn;
    private EditText userIdEditText,userPwEditText;
    private TextView losePwTv,registerTv;
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

    }

    View.OnClickListener listener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.login_cancel_btn:
                    finish();
                    break;
                case R.id.login_in_btn:
                    String userId = userIdEditText.getText().toString().trim();
                    String userPw = userPwEditText.getText().toString().trim();

                    break;
                case R.id.user_register_tv:
                    Intent intent=new Intent(Login_in.this,RegisterActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    };
}
