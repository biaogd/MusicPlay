package com.example.myapp;


import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.myapp.database.MyDao;

import java.util.ArrayList;


/**
 */
public class DownloadMusicFragment extends BaseFragment {


    private static final long serialVersionUID = -7567294083898084613L;

    private Button downloadBtn;
    private Fragment fragment;
    private MyDao myDao;
    public DownloadMusicFragment() {
        // Required empty public constructor
    }

    @Override
    public BaseFragment getFragment() {
        return this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        super.onCreateView(inflater,container,savedInstanceState);
        LinearLayout body = (LinearLayout)myview.findViewById(R.id.body);
        View.inflate(getActivity(),R.layout.fragment_download_music,body);
        listView = (ListView)myview.findViewById(R.id.download_list_view);
        myview.setId(R.id.downloadfragment);
        musicList = new ArrayList<>();
        myDao=new MyDao(getActivity());
        myDao.initConnect();
        //从数据库读出歌曲信息
        musicList.clear();
        musicList = myDao.findAll(download_stable);
        adapter=new MyAdapter(getActivity(),musicList,DownloadMusicFragment.this);
        listView.setAdapter(adapter);
        ((TextView)(this.myview.findViewById(R.id.list_title))).setText("下载管理("+musicList.size()+")");
        downloadBtn = (Button)myview.findViewById(R.id.downloading_btn);
        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager=getFragmentManager();
                FragmentTransaction transaction=manager.beginTransaction();
                if(fragment==null){
                    fragment =new DownloadFragment();
                }
                transaction.replace(R.id.other_frag,fragment);
                transaction.commit();
            }
        });
        return this.myview;
    }

    @Override
    public void onResume() {
        super.onResume();
        musicList.clear();
        musicList = myDao.findAll(download_stable);
        adapter=new MyAdapter(getActivity(),musicList,DownloadMusicFragment.this);
        listView.setAdapter(adapter);
        ((TextView)(this.myview.findViewById(R.id.list_title))).setText("下载管理("+musicList.size()+")");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(myDao.isConnection()){
            myDao.closeConnect();
        }
    }
}
