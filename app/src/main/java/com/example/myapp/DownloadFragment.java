package com.example.myapp;


import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

import com.example.myapp.self.DownloadBean;
import com.example.myapp.self.Music;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class DownloadFragment extends Fragment {
    private Fragment fragment;
    private ListView listView;
    private List<DownloadBean> dList;
    private BroadcastReceiver broadcast;
    private BaseAdapter adapter;
    public DownloadFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myview=inflater.inflate(R.layout.fragment_download, container, false);
        //下面三行代码是使myview获取焦点，以响应各种事件
        myview.setFocusable(true);
        myview.setFocusableInTouchMode(true);
        myview.requestFocus();
        Log.i("焦点","得到了焦点");
        myview.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Log.i("onkey","方法执行了");
                if(event.getAction() == KeyEvent.ACTION_DOWN){
                    if (keyCode == KeyEvent.KEYCODE_BACK){
                        Log.i("返回键","点击了返回键");
                        FragmentManager manager=getFragmentManager();
                        FragmentTransaction transaction=manager.beginTransaction();
                        if(fragment == null){
                            fragment =new DownloadMusicFragment();
                        }
                        transaction.replace(R.id.other_frag,fragment);
                        transaction.commit();
                        return true;
                    }
                }
                return false;
            }
        });
        ImageButton backBtns=(ImageButton)myview.findViewById(R.id.backbtns);
        backBtns.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager=getFragmentManager();
                FragmentTransaction transaction=manager.beginTransaction();
                if(fragment == null){
                    fragment =new DownloadMusicFragment();
                }
                transaction.replace(R.id.other_frag,fragment);
                transaction.commit();
            }
        });
        dList=new ArrayList<>();
        broadcast=new DownloadBroadcast();
        IntentFilter filter=new IntentFilter();
        filter.addAction("update_dw_progress");
        filter.addAction("return_download_list");
        getActivity().registerReceiver(broadcast,filter);
        listView=(ListView)myview.findViewById(R.id.dwing_music_list);
        adapter=new DownloadAdapter(getActivity(),dList);
        listView.setAdapter(adapter);
        Intent intent=new Intent("get_download_list");
        getActivity().sendBroadcast(intent);
        return myview;
    }
    public class DownloadBroadcast extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("update_dw_progress")){
                int progress = intent.getIntExtra("progress",0);
                Music m=(Music)intent.getSerializableExtra("music");
                int i=0;
                int size = dList.size();
                for (i=0;i<size;i++){
                    if(dList.get(i).getMusic().getPath().equals(m.getPath())){
                        dList.get(i).setProgress(progress);
                        if(progress==100){
                            dList.remove(i);
                        }
                        break;
                    }
                }
//                if(i>=size){
//                    DownloadBean bean=new DownloadBean(m,0);
//                    dList.add(bean);
//                }
                adapter.notifyDataSetChanged();
            }
            if(intent.getAction().equals("return_download_list")){
                List<Music> musicList=(ArrayList)intent.getSerializableExtra("list");
                for (Music m:musicList){
                    DownloadBean bean=new DownloadBean(m,0);
                    dList.add(bean);
                }
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(broadcast);

    }
}
