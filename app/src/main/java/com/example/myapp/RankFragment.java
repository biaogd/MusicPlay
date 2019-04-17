package com.example.myapp;


import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapp.database.MyDao;
import com.example.myapp.self.Music;
import com.example.myapp.self.NetMusicBean;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class RankFragment extends Fragment {
    private Fragment fragment;
    private ListView listView;
    private List<Music> musicList;
    private List<NetMusicBean> list;
    private MyDao myDao;
    private String which;
    private Handler handler;
    private BaseAdapter adapter;
    private Gson gson;
    private ProgressBar loading;
    private TextView errTV;
    public RankFragment() {
        // Required empty public constructor
    }
    //使用getArguments()和setArguments()在fragment之间传递信息
    public static Fragment newInstance(String which){
        RankFragment rankFragment=new RankFragment();
        Bundle bundle=new Bundle();
        bundle.putString("which",which);
        rankFragment.setArguments(bundle);
        return rankFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_rank, container, false);
        myDao = new MyDao(getActivity());
        handler=new MyHandler();
        musicList = new ArrayList<>();
        gson=new Gson();
        //这个fragment获取焦点
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(event.getAction() == KeyEvent.ACTION_DOWN){
                    if (keyCode == KeyEvent.KEYCODE_BACK){
                        FragmentTransaction transaction=getFragmentManager().beginTransaction();
                        if(fragment == null){
                            fragment =MainFragment.newInstance("right");
                        }
                        transaction.replace(R.id.other_frag,fragment);
                        transaction.commit();
                        return true;
                    }
                }
                return false;
            }
        });
        ImageButton backBtn = (ImageButton)view.findViewById(R.id.back_search_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction=getFragmentManager().beginTransaction();
                if(fragment == null){
                    fragment =MainFragment.newInstance("right");
                }
                transaction.replace(R.id.other_frag,fragment);
                transaction.commit();
            }
        });
        Bundle bundle=getArguments();
        this.which = bundle.getString("which");
        TextView rankTV = (TextView)view.findViewById(R.id.rank_name_tv);
        if(which.equals("popular")){
            rankTV.setText("热歌榜");
        }else {
            rankTV.setText("新歌榜");
        }
        listView=(ListView)view.findViewById(R.id.rank_list_view);
        listView.setVisibility(View.GONE);
        loading=(ProgressBar)view.findViewById(R.id.loading);
        loading.setVisibility(View.VISIBLE);
        errTV=(TextView)view.findViewById(R.id.err_tv);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Music music=musicList.get(position);
                Intent intent = new Intent();
                intent.setAction("play_net_music");
                Bundle bundle=new Bundle();
                bundle.putInt("pos",position);
                bundle.putSerializable("musicList",(ArrayList<Music>)musicList);
                intent.putExtra("music_data",bundle);
                getActivity().sendBroadcast(intent);
            }
        });
        return view;
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
    public void getRankMusicList(final String word){
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client=new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(20,TimeUnit.SECONDS)
                        .build();
                List<NetMusicBean> netMusicBeans=new ArrayList<>();
                if(word!=null) {
                    Request req = new Request.Builder().url("http://www.mybiao.top:8000/song/" + word).build();
                    try {
                        Response res= client.newCall(req).execute();
                        if(res.isSuccessful()) {
                            String mulist = res.body().string();
                            if (mulist != null) {
                                JsonParser jsonParser = new JsonParser();
                                JsonArray jsonElements = jsonParser.parse(mulist).getAsJsonArray();
                                for (JsonElement element : jsonElements) {
                                    NetMusicBean bean = gson.fromJson(element, NetMusicBean.class);
                                    netMusicBeans.add(bean);
                                }
                            }
                            res.close();
                        }

                    } catch (Exception e) {
                        handler.sendEmptyMessage(404);
                    }
                }
                Message message=new Message();
                message.what = 100;
                message.obj=netMusicBeans;
                handler.sendMessage(message);
            }
        }).start();
    }
        public class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what==100){
                list = (ArrayList<NetMusicBean>)msg.obj;
                List<Music> musicLists = myDao.findAll("love_music_list");
                for (NetMusicBean bean:list){
                    Music mu=new Music(bean.getSongName(),bean.getSongAuthor(),bean.getAllTime(),"http://www.mybiao.top:8000/song?id="+bean.getId(),bean.getSongSize());
                    for(Music m:musicLists){
                        if(m.getPath().equals(mu.getPath())){
                            mu.setLove(1);
                        }
                    }
                    mu.setFlag(1);
                    musicList.add(mu);
                }
                Log.i("mList的size",""+musicList.size());
                adapter=new MyNetAdapter(getActivity(),musicList);
                listView.setAdapter(adapter);
                listView.setVisibility(View.VISIBLE);
                loading.setVisibility(View.GONE);
            }
            if(msg.what==404){
                loading.setVisibility(View.GONE);
                errTV.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(checkNet(getActivity())){
            if(which.equals("popular")) {
                getRankMusicList("popular");
            }else {
                getRankMusicList("new");
            }
        }else {
            loading.setVisibility(View.GONE);
            errTV.setVisibility(View.VISIBLE);
        }
    }
}
