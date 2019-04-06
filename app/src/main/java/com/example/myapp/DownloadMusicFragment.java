package com.example.myapp;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class DownloadMusicFragment extends BaseFragment {


    private static final long serialVersionUID = -7567294083898084613L;

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
        //从数据库读出歌曲信息
        musicList.clear();
        musicList = findAll(download_stable);
        adapter=new MyAdapter(getActivity(),musicList,DownloadMusicFragment.this);
        listView.setAdapter(adapter);
        ((TextView)(this.myview.findViewById(R.id.list_title))).setText("下载管理("+musicList.size()+")");
        return this.myview;
    }

}
