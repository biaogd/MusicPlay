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
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapp.database.MyDao;
import com.example.myapp.self.Music;
import com.example.myapp.self.MyLogin;
import com.example.myapp.self.SelfSongBean;
import com.example.myapp.self.SongIdBean;
import com.example.myapp.self.SongListBean;
import com.example.myapp.self.UserBean;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class LeftFragment extends Fragment{


    private Fragment localFragment,nearFragment,downloadFragment,loveFragment,fragment;
    private LinearLayout local,near,download,love;
    private SQLiteDatabase db;
    private BroadcastReceiver broadcast;
    private TextView userNameTv,selfListTv,loveTv;
    private ImageView userImage,downForward;
    private LinearLayout layout1;
    private int []images=new int[]{R.mipmap.ic_right_32,R.mipmap.ic_bottom_32};
    private Handler handler;
    private MyDao myDao;
    private Gson gson;

    private static final String selfTable="self_music_list";
    public LeftFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view=inflater.inflate(R.layout.fragment_left, container, false);
        myDao=new MyDao(getActivity());

        local=(LinearLayout) view.findViewById(R.id.localmusicbtn);
        near = (LinearLayout) view.findViewById(R.id.nearplay);
        download = (LinearLayout) view.findViewById(R.id.downloadlist);
        love = (LinearLayout) view.findViewById(R.id.lovelist);
        TextView localtv=(TextView)view.findViewById(R.id.localmusictv);
        TextView neartv = (TextView)view.findViewById(R.id.nearlisttv);
        TextView downloadtv = (TextView)view.findViewById(R.id.downloadlisttv);
        loveTv = (TextView)view.findViewById(R.id.lovelisttv);

        localtv.setText(localtv.getText()+"("+myDao.allCount("local_music_list",0)+")");
        neartv.setText(neartv.getText()+"("+myDao.allCount("near_music_list",0)+")");
        downloadtv.setText(downloadtv.getText()+"("+myDao.allCount("download_music_list",0)+")");
//        loveltv.setText(loveltv.getText()+"("+myDao.allCount("love_music_list",0)+")");

        LinearLayout userLayout=(LinearLayout)view.findViewById(R.id.music_user);
        userNameTv = (TextView)view.findViewById(R.id.music_user_name);
        userImage = (ImageView)view.findViewById(R.id.music_user_image);
        downForward = (ImageView)view.findViewById(R.id.image_down_forward);
        userLayout.setOnClickListener(listener);

        local.setOnClickListener(listener);
        near.setOnClickListener(listener);
        download.setOnClickListener(listener);
        love.setOnClickListener(listener);
        layout1=(LinearLayout)view.findViewById(R.id.body);
        selfListTv = (TextView)view.findViewById(R.id.create_list_tv);
        selfListTv.setOnClickListener(listener);
        broadcast=new MyBroadcast();
        IntentFilter filter=new IntentFilter();
        filter.addAction("login_success");
        filter.addAction("login_out");
        getActivity().registerReceiver(broadcast,filter);
        handler=new MyHandler();
        gson=new Gson();


        SharedPreferences sp = getActivity().getSharedPreferences("user_data",Context.MODE_PRIVATE);
        String name = sp.getString("name",null);
        int id = sp.getInt("id",-1);
        int loveId = sp.getInt("loveId",0);
        if(name!=null&&id!=-1&&loveId!=0){
            userNameTv.setText(name);
            String str=sp.getString("song_bean_list",null);
            List<SongListBean> songBeanList=new ArrayList<>();
            if(str!=null){
                songBeanList = gson.fromJson(str,new TypeToken<List<SongListBean>>(){}.getType());
                for(SongListBean bean:songBeanList){
                    if(bean.getListName().equals("我喜欢")){
                        continue;
                    }
                    View myView = LayoutInflater.from(getActivity()).inflate(R.layout.song_list,null);
                    TextView tv=(TextView)myView.findViewById(R.id.song_list_name);
                    tv.setText(bean.getListName());
                    TextView tv1=(TextView)myView.findViewById(R.id.song_list_count);
                    tv1.setText(myDao.allCount(selfTable,bean.getListId())+"首");
                    final  SongListBean bean1=bean;
                    myView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            System.out.println("正在启动一个新的页面");
                            FragmentTransaction transaction=getFragmentManager().beginTransaction();
                            if(fragment==null){
                                fragment=SelfFragment.newInstance(bean1);
                            }
                            transaction.replace(R.id.other_frag,fragment);
                            transaction.commit();
                        }
                    });
                    layout1.addView(myView);
                }
            }
            UserBean userBean = new UserBean(id, name, 0,songBeanList);
            MyLogin.getMyLogin().setBean(userBean);
            MyLogin.getMyLogin().setLogin(true);
            MyLogin.getMyLogin().setLoveId(loveId);
            userImage.setImageResource(R.mipmap.ic_male_64);
            loveTv.setText(loveTv.getText()+"("+myDao.allCount("love_music_list",0)+")");
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
                case R.id.create_list_tv:
                    if(layout1.getVisibility()==View.GONE){
                        layout1.setVisibility(View.VISIBLE);
                        downForward.setImageResource(images[1]);
                    }else {
                        layout1.setVisibility(View.GONE);
                        downForward.setImageResource(images[0]);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public void getListFromNet(final SongListBean beans){
        System.out.println("正在同步歌单");
        OkHttpClient client=new OkHttpClient();
        String url="http://192.168.43.119:8000/music/user/getSongs?list_id="+beans.getListId();
        Request request=new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.sendEmptyMessage(400);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String js = response.body().string();
//                List<Music> musicList=new ArrayList<>();
                List<SongIdBean> songIdBeans=new ArrayList<>();
                if(!js.equals("null")){
                    JsonParser jsonParser=new JsonParser();
                    JsonArray array=jsonParser.parse(js).getAsJsonArray();
                    List<Music> loveList = myDao.findAll("love_music_list");
                    List<Music> localList = myDao.findAll("local_music_list");
                    for(JsonElement element:array){
                        SelfSongBean bean=gson.fromJson(element,SelfSongBean.class);
                        Music music=new Music(bean.getSongName(),bean.getSongAuthor(),0,bean.getSongPath(),0);
                        if(beans.getListName().equals("我喜欢")){
                            music.setLove(1);
                        }else {
                            int i = 0;
                            for (i = 0; i < loveList.size(); i++) {
                                Music m = loveList.get(i);
                                if (music.getSongName().equals(m.getSongName()) && music.getSongAuthor().equals(m.getSongAuthor())) {
                                    music.setLove(1);
                                    break;
                                }
                            }
                            if(i>=loveList.size()){
                                music.setLove(0);
                            }
                        }

                        music.setFlag(bean.getSongId());
                        if(bean.getSongId()==0){
                            int j = 0;
                            for(j=0;j<localList.size();j++){
                                Music music1=localList.get(j);
                                if(music.getSongName().equals(music1.getSongName())&&music.getSongAuthor().equals(music1.getSongAuthor())){
                                    music.setFlag(0);
                                    break;
                                }
                            }
                            //歌曲不再本地,也不在服务器上，无法播放
                            if(j>=localList.size()){
                                music.setFlag(-1);
                            }
                        }
//                        musicList.add(music);
                        SongIdBean songIdBean=new SongIdBean(bean.getId(),music);
                        songIdBeans.add(songIdBean);
                    }

                }
                NewBean newBean=new NewBean(songIdBeans,beans);
                Message msg = new Message();
                msg.what=200;
                msg.obj = newBean;
                handler.sendMessage(msg);
            }
        });

    }
    public class NewBean implements Serializable{

        private static final long serialVersionUID = 9147966199655146746L;
        private List<SongIdBean> songIdBeans;
        private SongListBean bean;

        public NewBean(List<SongIdBean> songIdBeans, SongListBean bean) {
            this.songIdBeans = songIdBeans;
            this.bean = bean;
        }

        public List<SongIdBean> getSongIdBeans() {
            return songIdBeans;
        }

        public void setSongIdBeans(List<SongIdBean> songIdBeans) {
            this.songIdBeans = songIdBeans;
        }

        public SongListBean getBean() {
            return bean;
        }

        public void setBean(SongListBean bean) {
            this.bean = bean;
        }
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
    public SQLiteDatabase getSQLiteDB(){
        return getActivity().openOrCreateDatabase("mydb.db", Context.MODE_PRIVATE,null);
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
                String str = intent.getStringExtra("name");
                if(id!=-1&&str!=null) {
                    String []strs = str.split("-");
                    String name = strs[0];
                    String []songList=strs[1].split(";");
                    Log.i("歌单",strs[1]);
                    List<SongListBean> songBeanList=new ArrayList<>();
                    for(String s:songList){
                        String []ss=s.split("\\*");
                        if(ss[1].equals("我喜欢")){
                            final SongListBean bean=new SongListBean(Integer.parseInt(ss[0]),ss[1],Integer.parseInt(ss[2]));
                            songBeanList.add(bean);
                            getListFromNet(bean);
                            continue;
                        }
                        View myView = LayoutInflater.from(getActivity()).inflate(R.layout.song_list,null);
                        TextView tv=(TextView)myView.findViewById(R.id.song_list_name);
                        tv.setText(ss[1]);
                        TextView tv1=(TextView)myView.findViewById(R.id.song_list_count);
                        tv1.setText(ss[2]+"首");
                        final SongListBean bean=new SongListBean(Integer.parseInt(ss[0]),ss[1],Integer.parseInt(ss[2]));
                        songBeanList.add(bean);
                        myView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                FragmentTransaction transaction=getFragmentManager().beginTransaction();
                                if(fragment==null){
                                    fragment=SelfFragment.newInstance(bean);
                                }
                                transaction.replace(R.id.other_frag,fragment);
                                transaction.commit();
                            }
                        });
                        layout1.addView(myView);
                        //完成登录后，加载自定义的歌单
                        getListFromNet(bean);
                    }

                    userNameTv.setText(name);
                    userImage.setImageResource(R.mipmap.ic_male_64);
                    UserBean userBean = new UserBean(id, name, 0,songBeanList);
                    int loveId=0;
                    for(SongListBean songListBean:songBeanList){
                        if(songListBean.getListName().equals("我喜欢")){
                            loveId = songListBean.getListId();
                            break;
                        }
                    }
                    MyLogin.getMyLogin().setBean(userBean);
                    MyLogin.getMyLogin().setLogin(true);
                    MyLogin.getMyLogin().setLoveId(loveId);
                    String userJson = gson.toJson(songBeanList);
                    SharedPreferences.Editor editor = getActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE).edit();
                    editor.putInt("id", id);
                    editor.putString("name", name);
                    editor.putString("song_bean_list",userJson);
                    editor.putInt("loveId",loveId);
                    editor.apply();
                }else {
                    Toast.makeText(getActivity(),"登录异常",Toast.LENGTH_LONG).show();
                }
            }
            if(intent.getAction().equals("login_out")){
                userImage.setImageResource(R.mipmap.ic_login_64);
                userNameTv.setText("立即登录");
                layout1.removeAllViews();
                loveTv.setText("我喜欢");

            }
        }
    }
public class MyHandler extends Handler{
    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what){
            case 200:
                System.out.println("保存歌单到本地");
                NewBean newBean = (NewBean) msg.obj;
                List<SongIdBean> songIdBeans=newBean.getSongIdBeans();
                SongListBean bean=newBean.getBean();
                if(bean.getListName().equals("我喜欢")){
                    myDao.clearTable("love_music_list");
                    for (SongIdBean songIdBean:songIdBeans) {
                        myDao.insertMusic(songIdBean.getMusic(),"love_music_list");
                    }
                }else {
                    //将歌曲信息插入到数据库当中
                    for (SongIdBean bean1:songIdBeans) {
                        myDao.insertMusic(bean.getListId(), bean1, selfTable);
                    }
                }
                Log.i("获取到列表", songIdBeans.toString());
                break;
            case 400:
                Toast.makeText(getActivity(),"获取列表异常",Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
}
