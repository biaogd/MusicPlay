package com.example.myapp;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */

public class NearPlayListFragment extends BaseFragment{
    private static final long serialVersionUID = -3002111685436173611L;
    private ImageButton options;
    public NearPlayListFragment(){

    }

    @Override
    public BaseFragment getFragment() {
        return NearPlayListFragment.this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        LinearLayout body = (LinearLayout)myview.findViewById(R.id.body);
        View.inflate(getActivity(),R.layout.fragment_near_play_list,body);
        listView = (ListView)myview.findViewById(R.id.near_list_view);
        myview.setId(R.id.nearfragment);
        musicList = new ArrayList<>();
        musicList.clear();
        musicList = findAll(near_stable);
        musicList=reSort(musicList);
        adapter = new MyAdapter(getActivity(),musicList,NearPlayListFragment.this);
        listView.setAdapter(adapter);
        ((TextView)(this.myview.findViewById(R.id.list_title))).setText("最近播放("+musicList.size()+")");
        options = (ImageButton)myview.findViewById(R.id.options);
        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu();
            }
        });
        return this.myview;
    }

    public void showPopupMenu(){
        PopupMenu menu=new PopupMenu(getActivity(),options);
        menu.getMenuInflater().inflate(R.menu.near_music_menu,menu.getMenu());
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.clear_near_music:
                        deleteAll(near_stable);
                        musicList.clear();
                        adapter.notifyDataSetChanged();
                        Toast.makeText(getActivity(),"清空成功",Toast.LENGTH_LONG).show();
                }
                return false;
            }
        });
        menu.show();
    }
}