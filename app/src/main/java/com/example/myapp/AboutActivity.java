package com.example.myapp;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {

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
        TextView textView=(TextView)findViewById(R.id.version_id);
        textView.setText("版本："+versionName);

    }
}
