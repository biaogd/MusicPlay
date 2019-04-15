package com.example.myapp;


import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

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
public class SearchFragment extends Fragment {

    private Fragment fragment;
    private SearchView searchView;
    private Handler handler;
    private Gson gson;
    private List<Music> musicList;
    private ListView listView;
    private BaseAdapter adapter;
    private LinearLayout place;
    private TextView textView;
    public SearchFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_search, container, false);
        //这个fragment获取焦点
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(event.getAction() == KeyEvent.ACTION_DOWN){
                    if (keyCode == KeyEvent.KEYCODE_BACK){
                        FragmentManager manager=getFragmentManager();
                        FragmentTransaction transaction=manager.beginTransaction();
                        if(fragment == null){
                            fragment =new MainFragment();
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
                FragmentManager manager=getFragmentManager();
                FragmentTransaction transaction=manager.beginTransaction();
                if(fragment == null){
                    fragment =new MainFragment();
                }
                transaction.replace(R.id.other_frag,fragment);
                transaction.commit();
            }
        });
        searchView = (SearchView)view.findViewById(R.id.search_music_view);
       searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
           @Override
           public boolean onQueryTextSubmit(String query) {
               searchMusic(query);
               return false;
           }

           @Override
           public boolean onQueryTextChange(String newText) {
               if(newText.trim().length()==0){
                   musicList.clear();
                   adapter.notifyDataSetChanged();
               }
               return false;
           }
       });
        listView = (ListView)view.findViewById(R.id.search_list_view);
        place = (LinearLayout)view.findViewById(R.id.main_place);
        textView = new TextView(getActivity());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,15);
        textView.setText("未找到");
        LinearLayout.LayoutParams layoutParams= new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        textView.setLayoutParams(layoutParams);

        handler = new MyHandler();
        gson=new Gson();
        musicList=new ArrayList<>();
        adapter=new MyNetAdapter(getActivity(),musicList);
        listView.setAdapter(adapter);
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
                Log.i("广播发送","已发送");
            }
        });
        return view;
    }
    public void searchMusic(final String word){
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client=new OkHttpClient();
                List<NetMusicBean> netMusicBeans=new ArrayList<>();
                if(word!=null) {
                    Request req = new Request.Builder().url("http://www.mybiao.top:8000/searchBy?word=" + word).build();
                    try {
                        Response res= client.newCall(req).execute();
                        if(res.isSuccessful()) {
                            String mulist = res.body().string();
                            if (!mulist.equals("null")) {
                                Log.i("muList",mulist);
                                JsonParser jsonParser = new JsonParser();
                                JsonArray jsonElements = jsonParser.parse(mulist).getAsJsonArray();
                                for (JsonElement element : jsonElements) {
                                    NetMusicBean bean = gson.fromJson(element, NetMusicBean.class);
                                    Log.i("搜索到",bean.toString());
                                    netMusicBeans.add(bean);
                                }
                            }
                        }

                    } catch (IOException e) {
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
            if(msg.what == 100){
//                place.removeAllViews();
//                place.addView(listView);
                musicList.clear();
                List<NetMusicBean> list=(ArrayList<NetMusicBean>)msg.obj;
                List<Music> musicLists = new MyDao(getActivity()).findAll("love_music_list");
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
                if(musicList.size()==0){
                    place.removeAllViews();
                    place.addView(textView);
                }else{
                    place.removeAllViews();
                    place.addView(listView);
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }
}
