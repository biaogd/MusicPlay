package com.example.myapp;


import android.app.FragmentTransaction;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

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
public class RightFragment extends Fragment {

    private Gson gson=new Gson();
    private List<NetMusicBean> list;
    private Handler handler;
    private OkHttpClient client;
    private View view;
    private Fragment fragment;
    private TextView errTV;
    private ProgressBar loading;
    private TextView []popularTV;
    private TextView []newTV;
    private int loadInt;
    private LinearLayout popularLayout;
    private  LinearLayout newLayout;

    private Fragment mainFrag;
    public RightFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =inflater.inflate(R.layout.fragment_right, container, false);
        handler = new MyHandler();
        list = new ArrayList<>();
//        Bundle bundle=getArguments();
//        mainFrag = (MainFragment)bundle.getSerializable("fragment");
//        mList=new ArrayList<>();
        client=new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).build();
        popularLayout = (LinearLayout)view.findViewById(R.id.popular_song_layout);
        newLayout = (LinearLayout)view.findViewById(R.id.new_song_layout);
        popularLayout.setOnClickListener(listener);
        newLayout.setOnClickListener(listener);
        popularLayout.setVisibility(View.GONE);
        newLayout.setVisibility(View.GONE);
        popularTV=new TextView[]{(TextView)view.findViewById(R.id.popular_song1),
                (TextView)view.findViewById(R.id.popular_song2),
                (TextView)view.findViewById(R.id.popular_song3),
                (TextView)view.findViewById(R.id.popular_song4),
                (TextView)view.findViewById(R.id.popular_song5)

        };
        newTV=new TextView[]{(TextView)view.findViewById(R.id.new_song1),
                (TextView)view.findViewById(R.id.new_song2),
                (TextView)view.findViewById(R.id.new_song3),
                (TextView)view.findViewById(R.id.new_song4),
                (TextView)view.findViewById(R.id.new_song5)

        };
        errTV = (TextView)view.findViewById(R.id.err_tv);
        loading = (ProgressBar)view.findViewById(R.id.loading);
        loading.setVisibility(View.VISIBLE);
        loadInt = 0;
        if(checkNet(getActivity())){
            getRankMusicList("popular");
            getRankMusicList("new");
        }else {
            errTV.setVisibility(View.VISIBLE);
            loading.setVisibility(View.GONE);
            popularLayout.setVisibility(View.GONE);
            newLayout.setVisibility(View.GONE);
        }
        return view;
    }

    View.OnClickListener listener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.popular_song_layout:
                    FragmentTransaction transaction=getFragmentManager().beginTransaction();
                    if(fragment==null){
                        fragment=RankFragment.newInstance("popular");
                    }
                    transaction.replace(R.id.other_frag,fragment);
                    transaction.commit();
                    break;
                case R.id.new_song_layout:
                    FragmentTransaction transaction1=getFragmentManager().beginTransaction();
                    if(fragment==null){
                        fragment=RankFragment.newInstance("new");
                    }
                    transaction1.replace(R.id.other_frag,fragment);
                    transaction1.commit();
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
                            if (!mulist.equals("null")) {
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
                if(word.equals("popular")){
                    message.arg1=0;
                }else {
                    message.arg1=1;
                }
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
                loadInt++;
                list = (ArrayList<NetMusicBean>)msg.obj;
                int arg = msg.arg1;
                if(arg==0){
                    if(!list.isEmpty()) {
                        for (int i = 0; i < 5; i++) {
                            popularTV[i].setText((i + 1) + "." + list.get(i).getSongAuthor() + " - " + list.get(i).getSongName());
                        }
                    }
                }else {
                    if(!list.isEmpty()) {
                        for (int i = 0; i < 5; i++) {
                            newTV[i].setText((i + 1) + "." + list.get(i).getSongAuthor() + " - " + list.get(i).getSongName());
                        }
                    }
                }
                if(loadInt==2){
                    popularLayout.setVisibility(View.VISIBLE);
                    newLayout.setVisibility(View.VISIBLE);
                    loading.setVisibility(View.GONE);
                    errTV.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
//        if(checkNet(getActivity())){
//            searchMusic("");
//        }else {
//            Toast.makeText(getActivity(),"网络无法连接，稍后重试",Toast.LENGTH_LONG).show();
//            Log.i("网络","无法连接");
//        }
    }
}
