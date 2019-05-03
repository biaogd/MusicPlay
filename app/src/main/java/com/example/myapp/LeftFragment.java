package com.example.myapp;


import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapp.database.MyDao;
import com.example.myapp.self.Music;
import com.example.myapp.self.MyLogin;
import com.example.myapp.self.SelfFinal;
import com.example.myapp.self.SelfSongBean;
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
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
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
    private ImageButton addListBtn;
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
        myDao.initConnect();
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


        addListBtn=(ImageButton)view.findViewById(R.id.add_list_btn);
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
        addListBtn.setOnClickListener(listener);
        broadcast=new MyBroadcast();
        IntentFilter filter=new IntentFilter();
        filter.addAction("login_success");
        filter.addAction("login_in_success");
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
                for(final SongListBean bean:songBeanList){
                    if(bean.getListName().equals("我喜欢")){
                        continue;
                    }
                    final View myView = LayoutInflater.from(getActivity()).inflate(R.layout.song_list,null);
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
                    final ImageButton songListManagerBtn = (ImageButton)myView.findViewById(R.id.song_list_manager_btn);
                    songListManagerBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!checkNet(getActivity())) {
                                //网络无连接
                                Toast.makeText(getActivity(),"未连接到网络",Toast.LENGTH_LONG).show();
                            } else {
                                View view1 = LayoutInflater.from(getActivity()).inflate(R.layout.song_list_menu_window,null);
                                showWindow(myView,view1,songListManagerBtn,bean);
                            }
                        }
                    });
                    layout1.addView(myView);
                }
            }
            UserBean userBean = new UserBean(id, name, 0,songBeanList);
            MyLogin.bean=userBean;
            MyLogin.logined=true;
            MyLogin.loveId=loveId;
            userImage.setImageResource(R.mipmap.ic_male_64);
            loveTv.setText(loveTv.getText()+"("+myDao.allCount("love_music_list",0)+")");
        }else {
            MyLogin.logined=false;
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
                    boolean logined = MyLogin.logined;
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
                case R.id.add_list_btn:
                    if(!checkNet(getActivity())){
                        Toast.makeText(getActivity(),"未连接到网络",Toast.LENGTH_SHORT).show();
                    }else {
                        if (!MyLogin.logined) {
                            Intent intent = new Intent(getActivity(), Login_in.class);
                            getActivity().startActivity(intent);
                        } else {
                            final View myview = LayoutInflater.from(getActivity()).inflate(R.layout.add_list_layout, null);
                            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                                    .setTitle("添加歌单").setView(myview)
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            EditText editText = (EditText) myview.findViewById(R.id.list_name_edit);
                                            String listName = editText.getText().toString().trim();
                                            if (listName.length() == 0) {
                                                Toast.makeText(getActivity(), "名称为空", Toast.LENGTH_SHORT).show();
                                            } else {
                                                List<SongListBean> listBeans = MyLogin.bean.getSongList();
                                                int i = 0;
                                                for (i = 0; i < listBeans.size(); i++) {
                                                    if (listBeans.get(i).getListName().equals(listName)) {
                                                        break;
                                                    }
                                                }
                                                if (i >= listBeans.size()) {
                                                    //歌单不存在
                                                    SongListBean bean=new SongListBean(0,listName,0);
                                                    listBeans.add(bean);
                                                    MyLogin.bean.setSongList(listBeans);
                                                    //写入到本地
                                                    SharedPreferences.Editor editor=getActivity().getSharedPreferences("user_data",Context.MODE_PRIVATE).edit();
                                                    String beanJson = gson.toJson(listBeans);
                                                    editor.putString("song_bean_list",beanJson);
                                                    editor.apply();
                                                    //同步到服务器
                                                    syncAddSongList(bean);

                                                } else {
                                                    //歌单已存在
                                                    Toast.makeText(getActivity(), "歌单已存在", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }
                                    }).setNegativeButton("取消", null);
                            builder.show();
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public void syncAddSongList(SongListBean bean){
        //获取用户id
        int userId = MyLogin.bean.getId();
        String listName = bean.getListName();
        OkHttpClient client=new OkHttpClient();
        String urls = SelfFinal.host+SelfFinal.port+"/music/user/addSongList";
        RequestBody body=new FormBody.Builder().add("userId",String.valueOf(userId))
                .add("listName",listName).build();
        Request request=new Request.Builder().post(body).url(urls).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String myJson = response.body().string();
                SongListBean bean1 = gson.fromJson(myJson,new TypeToken<SongListBean>(){}.getType());
                Message message=new Message();
                message.what=20;
                message.obj = bean1;
                handler.sendMessage(message);
            }
        });
    }

    public void getListFromNet(final SongListBean beans){
        System.out.println("正在同步歌单");
        OkHttpClient client=new OkHttpClient();
        String url=SelfFinal.host+SelfFinal.port+"/music/user/getSongs?list_id="+beans.getListId();
        Request request=new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.sendEmptyMessage(400);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String js = response.body().string();
                List<Music> musicList=new ArrayList<>();
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
                        musicList.add(music);
                    }

                }
                NewBean newBean=new NewBean(musicList,beans);
                Message msg = new Message();
                msg.what=200;
                msg.obj = newBean;
                handler.sendMessage(msg);
            }
        });

    }
    public class NewBean implements Serializable{

        private static final long serialVersionUID = 9147966199655146746L;
        private List<Music> musicList;
        private SongListBean bean;

        public NewBean(List<Music> musicList, SongListBean bean) {
            this.musicList = musicList;
            this.bean = bean;
        }

        public List<Music> getMusicList() {
            return musicList;
        }

        public void setMusicList(List<Music> musicList) {
            this.musicList = musicList;
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(broadcast);
        if(myDao.isConnection()){
            myDao.closeConnect();
        }
    }

    public class MyBroadcast extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("login_in_success")){
                    System.out.println("接收到登录成功的广播");
                    int id = intent.getIntExtra("id", -1);
                    String str = intent.getStringExtra("name");
                    if (id != -1 && str != null) {
                        String[] strs = str.split("-");
                        String name = strs[0];
                        String[] songList = strs[1].split(";");
                        Log.i("歌单", strs[1]);
                        final List<SongListBean> songBeanList = new ArrayList<>();
                        for (String s : songList) {
                            String[] ss = s.split("\\*");
                            if (ss[1].equals("我喜欢")) {
                                final SongListBean bean = new SongListBean(Integer.parseInt(ss[0]), ss[1], Integer.parseInt(ss[2]));
                                songBeanList.add(bean);
                                getListFromNet(bean);
                                continue;
                            }
                            System.out.println("添加歌单:"+ss[1]);
                            final View myView = LayoutInflater.from(getActivity()).inflate(R.layout.song_list, null);
                            TextView tv = (TextView) myView.findViewById(R.id.song_list_name);
                            tv.setText(ss[1]);
                            TextView tv1 = (TextView) myView.findViewById(R.id.song_list_count);
                            tv1.setText(ss[2] + "首");
                            final SongListBean bean = new SongListBean(Integer.parseInt(ss[0]), ss[1], Integer.parseInt(ss[2]));
                            songBeanList.add(bean);
                            myView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                                    if (fragment == null) {
                                        fragment = SelfFragment.newInstance(bean);
                                    }
                                    transaction.replace(R.id.other_frag, fragment);
                                    transaction.commit();
                                }
                            });
                            final ImageButton songListManagerBtn = (ImageButton)myView.findViewById(R.id.song_list_manager_btn);
                            songListManagerBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (!checkNet(getActivity())) {
                                        //网络无连接
                                        Toast.makeText(getActivity(),"未连接到网络",Toast.LENGTH_LONG).show();
                                    } else {
                                        View view1 = LayoutInflater.from(getActivity()).inflate(R.layout.song_list_menu_window,null);
                                        showWindow(myView,view1,songListManagerBtn,bean);
                                    }
                                }
                            });
                            layout1.addView(myView);
                            //完成登录后，加载自定义的歌单
                            getListFromNet(bean);
                        }

                        userNameTv.setText(name);
                        userImage.setImageResource(R.mipmap.ic_male_64);
                        UserBean userBean = new UserBean(id, name, 0, songBeanList);
                        int loveId = 0;
                        for (SongListBean songListBean : songBeanList) {
                            if (songListBean.getListName().equals("我喜欢")) {
                                loveId = songListBean.getListId();
                                break;
                            }
                        }
                        MyLogin.bean = userBean;
                        MyLogin.logined = true;
                        MyLogin.loveId = loveId;
                        String userJson = gson.toJson(songBeanList);
                        SharedPreferences.Editor editor = getActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE).edit();
                        editor.putInt("id", id);
                        editor.putString("name", name);
                        editor.putString("song_bean_list", userJson);
                        editor.putInt("loveId", loveId);
                        editor.apply();
                    } else {
                        Toast.makeText(getActivity(), "登录异常", Toast.LENGTH_LONG).show();
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

    public void showWindow(final View myview, View view, View parent, final SongListBean bean){
        PopupWindow window=new PopupWindow(view, WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
        window.setFocusable(true);
        window.setTouchable(true);
        window.setBackgroundDrawable(new ColorDrawable(0x000000));
        window.setOutsideTouchable(true);
        //设置弹出窗口背景变半透明，来高亮弹出窗口
        WindowManager.LayoutParams lp =getActivity().getWindow().getAttributes();
        lp.alpha=0.5f;
        getActivity().getWindow().setAttributes(lp);

        window.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                //恢复透明度
                WindowManager.LayoutParams lp =getActivity().getWindow().getAttributes();
                lp.alpha=1f;
                getActivity().getWindow().setAttributes(lp);
            }
        });
        window.showAtLocation(parent, Gravity.BOTTOM,0,0);
        TextView songListNameTv = (TextView)view.findViewById(R.id.song_list_name_tv);
        songListNameTv.setText(bean.getListName());
        Button button=(Button)view.findViewById(R.id.delete_song_list);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //删除歌单
                //删除本地对应歌曲
                myDao.clearListSong(bean);
                //修改MyLogin
                List<SongListBean> listBeans=MyLogin.bean.getSongList();
                for (int i=0;i<listBeans.size();i++){
                    if (listBeans.get(i).getListId()==bean.getListId()){
                        listBeans.remove(i);
                        break;
                    }
                }
                MyLogin.bean.setSongList(listBeans);
                //修改本地歌单列表
                SharedPreferences.Editor editor=getActivity().getSharedPreferences("user_data",Context.MODE_PRIVATE).edit();
                String listJson = gson.toJson(listBeans);
                editor.putString("song_bean_list",listJson);
                editor.apply();
                //同时删除服务器歌曲和歌单
                deleteSongList(bean);
                //修改ui
                layout1.removeView(myview);
            }
        });
    }


    private void deleteSongList(SongListBean bean){
        int listId = bean.getListId();
        int userId = MyLogin.bean.getId();
        OkHttpClient client=new OkHttpClient();
        RequestBody body=new FormBody.Builder()
                .add("listId",String.valueOf(listId))
                .build();
        String url = SelfFinal.host+SelfFinal.port+"/music/user/deleteSongList";
        Request request=new Request.Builder().post(body).url("").build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.sendEmptyMessage(223);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String str=response.body().string();
                if(str.equals("success")){
                    //服务器歌单同步成功
                    handler.sendEmptyMessage(222);
                }
            }
        });
    }


public class MyHandler extends Handler{
    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what){
            case 200:
                NewBean newBean = (NewBean) msg.obj;
                List<Music> musicList=newBean.getMusicList();
                SongListBean bean=newBean.getBean();
                System.out.println("保存歌单到本地:"+bean.getListName());
                if(bean.getListName().equals("我喜欢")){
                    myDao.clearTable("love_music_list");
                    for (Music music:musicList) {
                        myDao.insertMusic(music,"love_music_list");
                    }
                }else {
                    //将歌曲信息插入到数据库当中
                    myDao.clearListSong(bean);
                    for (Music music:musicList) {
                        myDao.insertMusic(bean.getListId(), music, selfTable);
                    }
                }
                Log.i("获取到列表", musicList.toString());
                break;
            case 400:
                Toast.makeText(getActivity(),"获取列表异常",Toast.LENGTH_SHORT).show();
                break;
            case 20:
                //更新新添加歌单的id,并且写入到本地
                SongListBean listBean1 = (SongListBean)msg.obj;
                List<SongListBean> beans = MyLogin.bean.getSongList();
                for(int i=0;i<beans.size();i++){
                    if(listBean1.getListName().equals(beans.get(i).getListName())){
                        //找到这个新添加的歌单
                        beans.get(i).setListId(listBean1.getListId());
                        SharedPreferences.Editor editor=getActivity().getSharedPreferences("user_data",Context.MODE_PRIVATE).edit();
                        String beanJson = gson.toJson(beans);
                        editor.putString("song_bean_list",beanJson);
                        editor.apply();
                        View myView = LayoutInflater.from(getActivity()).inflate(R.layout.song_list,null);
                        TextView tv=(TextView)myView.findViewById(R.id.song_list_name);
                        tv.setText(listBean1.getListName());
                        TextView tv1=(TextView)myView.findViewById(R.id.song_list_count);
                        tv1.setText(listBean1.getListCount()+"首");
                        layout1.addView(myView);
                        Log.i("更新歌单id","成功");
                        break;
                    }
                }
                break;
            case 222:
                Toast.makeText(getActivity(),"歌单删除成功",Toast.LENGTH_SHORT).show();
                break;
            case 223:
                Toast.makeText(getActivity(),"服务器异常",Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
}
