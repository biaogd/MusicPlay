package com.example.myapp;


import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.example.myapp.database.MyDao;
import com.example.myapp.self.Music;
import com.example.myapp.self.SongListBean;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class SelfFragment extends Fragment {
    private Fragment fragment;
    private ListView listView;
    private List<Music> musicList;
    private ImageButton backBtn;
    private TextView titleTv;
    private BaseAdapter adapter;
    private Gson gson;
    private MyDao myDao;
    private static final String selfTable="self_music_list";

    //使用getArguments()和setArguments()在fragment之间传递信息
    public static Fragment newInstance(SongListBean bean){
        SelfFragment selfFragment=new SelfFragment();
        Bundle bundle=new Bundle();
        bundle.putSerializable("which",bean);
        selfFragment.setArguments(bundle);
        return selfFragment;
    }
    public SelfFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view= inflater.inflate(R.layout.fragment_self, container, false);
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
                            fragment =MainFragment.newInstance("left");
                        }
                        transaction.replace(R.id.other_frag,fragment);
                        transaction.commit();
                        return true;
                    }
                }
                return false;
            }
        });
        gson=new Gson();
        myDao =new MyDao(getActivity());
        //根据传递过来的参数加载不同的音乐列表
        Bundle bundle=getArguments();
        SongListBean bean = (SongListBean) bundle.getSerializable("which");
        if(bean!=null) {
            musicList = myDao.findAll(bean.getListId(), selfTable);
        }
        backBtn = (ImageButton)view.findViewById(R.id.self_back_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction=getFragmentManager().beginTransaction();
                if(fragment == null){
                    fragment =MainFragment.newInstance("left");
                }
                transaction.replace(R.id.other_frag,fragment);
                transaction.commit();
            }
        });
        titleTv=(TextView)view.findViewById(R.id.self_name_tv);
        if(bean!=null) {
            titleTv.setText(bean.getListName());
        }
        listView=(ListView)view.findViewById(R.id.self_list_view);
        adapter=new SelfAdapter(getActivity(),musicList,bean);
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
            }
        });
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(myDao.isConnection()){
            myDao.closeConnect();
        }
    }
}
