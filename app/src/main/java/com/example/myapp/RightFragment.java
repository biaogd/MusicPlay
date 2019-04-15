package com.example.myapp;


import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.myapp.database.MyDao;
import com.example.myapp.self.Music;
import com.example.myapp.self.NetMusicBean;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class RightFragment extends Fragment {

    private ListView listView;
    private Gson gson=new Gson();
    private List<Music> mList;
    private List<NetMusicBean> list;
    private BaseAdapter adapter;
    private Handler handler;
    public RightFragment() {
        // Required empty public constructor
    }
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =inflater.inflate(R.layout.fragment_right, container, false);
        listView = (ListView)view.findViewById(R.id.net_music_list);
        handler = new MyHandler();
        list = new ArrayList<>();
        mList=new ArrayList<>();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Music music=mList.get(position);
                Intent intent = new Intent();
                intent.setAction("play_net_music");
                Bundle bundle=new Bundle();
                bundle.putInt("pos",position);
                bundle.putSerializable("musicList",(ArrayList<Music>)mList);
                intent.putExtra("music_data",bundle);
                getActivity().sendBroadcast(intent);
                Log.i("广播发送","已发送");
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
    public void searchMusic(final String word){
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client=new OkHttpClient();
                List<NetMusicBean> netMusicBeans=new ArrayList<>();
                if(word!=null) {
                    Request req = new Request.Builder().url("http://www.mybiao.top:8000/search?word=" + word).build();
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
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Message message=new Message();
                message.what = 100;
                message.obj=netMusicBeans;
                handler.sendMessage(message);
            }
        }).start();
    }
    public class MyHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what==100){
                list = (ArrayList<NetMusicBean>)msg.obj;
                List<Music> musicLists = new MyDao(getActivity()).findAll("love_music_list");
                for (NetMusicBean bean:list){
                    Music mu=new Music(bean.getSongName(),bean.getSongAuthor(),bean.getAllTime(),"http://www.mybiao.top:8000/song?id="+bean.getId(),bean.getSongSize());
                    for(Music m:musicLists){
                        if(m.getPath().equals(mu.getPath())){
                            mu.setLove(1);
                        }
                    }
                    mu.setFlag(1);
                    mList.add(mu);
                }
                Log.i("mList的size",""+mList.size());
                adapter=new MyNetAdapter(getActivity(),mList);
                listView.setAdapter(adapter);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(checkNet(getActivity())){
            searchMusic("");
        }else {
            Toast.makeText(getActivity(),"网络无法连接，稍后重试",Toast.LENGTH_LONG).show();
            Log.i("网络","无法连接");
        }
    }
}
