package com.example.myapp;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class LoveMusicFragment extends BaseFragment {


    private static final long serialVersionUID = 605951315102449512L;

    public LoveMusicFragment() {
        // Required empty public constructor
    }

    @Override
    public BaseFragment getFragment() {
        return this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        LinearLayout body = (LinearLayout)myview.findViewById(R.id.body);
        View.inflate(getActivity(),R.layout.fragment_love_music,body);
        listView = (ListView)myview.findViewById(R.id.love_list_view);
        musicList = new ArrayList<>();
        myview.setId(R.id.lovefragment);
        //从数据库读出歌曲信息
        musicList.clear();
        musicList = findAll(love_stable);
        musicList = reSort(musicList);  //从数据库中取出来倒着放，后加入的在最上面
        adapter=new MyAdapter(getActivity(),musicList,LoveMusicFragment.this);
        listView.setAdapter(adapter);
        ((TextView)(this.myview.findViewById(R.id.list_title))).setText("我喜欢("+musicList.size()+")");
        return this.myview;
    }

}
