package com.example.myapp;


import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapp.self.MyLogin;
import com.example.myapp.self.UserBean;


/**
 * A simple {@link Fragment} subclass.
 */
public class LeftFragment extends Fragment{


    private Fragment localFragment,nearFragment,downloadFragment,loveFragment;
    private LinearLayout local,near,download,love;
    private SQLiteDatabase db;
    private BroadcastReceiver broadcast;
    private TextView userNameTv;
    private ImageView userImage;
    public LeftFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_left, container, false);
        local=(LinearLayout) view.findViewById(R.id.localmusicbtn);
        near = (LinearLayout) view.findViewById(R.id.nearplay);
        download = (LinearLayout) view.findViewById(R.id.downloadlist);
        love = (LinearLayout) view.findViewById(R.id.lovelist);
        TextView localtv=(TextView)view.findViewById(R.id.localmusictv);
        TextView neartv = (TextView)view.findViewById(R.id.nearlisttv);
        TextView downloadtv = (TextView)view.findViewById(R.id.downloadlisttv);
        TextView loveltv = (TextView)view.findViewById(R.id.lovelisttv);
        localtv.setText(localtv.getText()+"("+getCount("local_music_list")+")");
        neartv.setText(neartv.getText()+"("+getCount("near_music_list")+")");
        downloadtv.setText(downloadtv.getText()+"("+getCount("download_music_list")+")");
        loveltv.setText(loveltv.getText()+"("+getCount("love_music_list")+")");
        LinearLayout userLayout=(LinearLayout)view.findViewById(R.id.music_user);
        userNameTv = (TextView)view.findViewById(R.id.music_user_name);
        userImage = (ImageView)view.findViewById(R.id.music_user_image);

        userLayout.setOnClickListener(listener);

//        MyLogin.getMyLogin().setLogin(true);

        local.setOnClickListener(listener);
        near.setOnClickListener(listener);
        download.setOnClickListener(listener);
        love.setOnClickListener(listener);
        broadcast=new MyBroadcast();
        IntentFilter filter=new IntentFilter();
        filter.addAction("login_success");
        filter.addAction("login_out");
        getActivity().registerReceiver(broadcast,filter);

        SharedPreferences sp = getActivity().getSharedPreferences("user_data",Context.MODE_PRIVATE);
        String name = sp.getString("name",null);
        int id = sp.getInt("id",-1);
        if(name!=null&&id!=-1){
            userNameTv.setText(name);
            UserBean userBean = new UserBean(id, name, 0);
            MyLogin.getMyLogin().setBean(userBean);
            MyLogin.getMyLogin().setLogin(true);
            userImage.setImageResource(R.mipmap.ic_male_64);
            userNameTv.setText(name);
        }else {
            MyLogin.getMyLogin().setLogin(false);
            userImage.setImageResource(R.mipmap.ic_login_64);
            userNameTv.setText("立即登录");
        }


        return view;
    }

    View.OnClickListener listener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FragmentManager manager=getFragmentManager();
            FragmentTransaction transaction=manager.beginTransaction();
            switch (v.getId()){
                case R.id.localmusicbtn:
                    if(localFragment == null){
                        localFragment=new LocalMusicListFragment();
                    }
                    transaction.replace(R.id.other_frag,localFragment);
                    transaction.commit();
                    break;
                case R.id.nearplay:
                    if(nearFragment == null){
                        nearFragment=new NearPlayListFragment();
                    }
                    transaction.replace(R.id.other_frag,nearFragment);
                    transaction.commit();
                    break;
                case R.id.downloadlist:
                    if(downloadFragment==null)
                        downloadFragment = new DownloadMusicFragment();
                    transaction.replace(R.id.other_frag,downloadFragment);
                    transaction.commit();
                    break;
                case R.id.lovelist:
                    if(loveFragment==null)
                        loveFragment = new LoveMusicFragment();
                    transaction.replace(R.id.other_frag,loveFragment);
                    transaction.commit();
                    break;
                case R.id.music_user:
                    boolean logined = MyLogin.getMyLogin().isLogin();
                    if(checkNet(getActivity())) {
                        if (!logined) {
                            Intent intent = new Intent(getActivity(), Login_in.class);
                            startActivity(intent);
                        }
                    }else {
                        Toast.makeText(getActivity(),"未连接到网络",Toast.LENGTH_LONG).show();
                    }
                    break;
                default:
                    break;
            }
        }
    };

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
    public SQLiteDatabase getSQLiteDB(){
        return getActivity().openOrCreateDatabase("mydb.db", Context.MODE_PRIVATE,null);
    }

    public int getCount(String tableName){
        db=getSQLiteDB();
        Cursor cursor=db.query(tableName,new String[]{"id"},null,null,null,null,null);
        return cursor.getCount();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(broadcast);
    }

    public class MyBroadcast extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("login_success")){
                int id = intent.getIntExtra("id",-1);
                String name = intent.getStringExtra("name");
                if(id!=-1&&name!=null) {
                    userNameTv.setText(name);
                    userImage.setImageResource(R.mipmap.ic_male_64);
                    UserBean userBean = new UserBean(id, name, 0);
                    MyLogin.getMyLogin().setBean(userBean);
                    MyLogin.getMyLogin().setLogin(true);
                    SharedPreferences.Editor editor = getActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE).edit();
                    editor.putInt("id", id);
                    editor.putString("name", name);
                    editor.apply();
                }else {
                    Toast.makeText(getActivity(),"登录异常",Toast.LENGTH_LONG);
                }
            }
            if(intent.getAction().equals("login_out")){
                userImage.setImageResource(R.mipmap.ic_login_64);
                userNameTv.setText("立即登录");

            }
        }
    }

}
