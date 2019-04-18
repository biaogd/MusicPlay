package com.example.myapp;


import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapp.self.AllScannerSongs;
import com.example.myapp.self.Music;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class LocalMusicListFragment extends BaseFragment {
    private static final long serialVersionUID = 8450019617744366672L;
    private List<Music> mylist ;
    private ImageButton options;
    private ProgressBar loading;
    private TextView textView;
    private Handler handler;
    public LocalMusicListFragment() {
    }
    @Override
    public BaseFragment getFragment() {
        return this;
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        LinearLayout body = (LinearLayout)myview.findViewById(R.id.body);
        View.inflate(getActivity(),R.layout.fragment_local_music_list,body);
        handler = new MyHandler();
        myview.setId(R.id.localfragment);
        listView = (ListView)myview.findViewById(R.id.local_list_view);
        myview.setId(R.id.localfragment);
        musicList = new ArrayList<>();
        options = (ImageButton)myview.findViewById(R.id.options);
        options.setOnClickListener(listener);
        //从数据库读出歌曲信息
        musicList.clear();
        musicList = findAll(local_stable);
        adapter=new MyAdapter(getActivity(),musicList,LocalMusicListFragment.this);
        listView.setAdapter(adapter);
        ((TextView)(this.myview.findViewById(R.id.list_title))).setText("本地列表("+musicList.size()+")");
        Button downloadingBtn = (Button)myview.findViewById(R.id.downloading_btn);
        downloadingBtn.setVisibility(View.GONE);
        loading=(ProgressBar)myview.findViewById(R.id.loading);
        textView=(TextView)myview.findViewById(R.id.about_scanner);
        return this.myview;
    }

    View.OnClickListener listener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showPopupMenu();
        }
    };
    public void showPopupMenu(){
        PopupMenu menu=new PopupMenu(getActivity(),options);
        menu.getMenuInflater().inflate(R.menu.local_music_menu,menu.getMenu());
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.scanner_local_music:
                        AlertDialog.Builder alert=new AlertDialog.Builder(getActivity());
                        alert.setTitle("加载歌曲").setMessage("是否扫描内部存储加载更多歌曲?").setNegativeButton("否",null).
                        setPositiveButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            scannerMusic();
                            loading.setVisibility(View.VISIBLE);
                            textView.setVisibility(View.VISIBLE);
                            listView.setVisibility(View.GONE);
                        }
                        });
                        alert.create();
                        alert.show();
                }
                return false;
            }
        });
        menu.show();
    }
    public void scannerMusic(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                mylist = null;
//                            mylist=AllScannerSongs.getAllMusicFromSdcard(getActivity());
                mylist=AllScannerSongs.getMusicFromSdcard(getActivity());
                SQLiteDatabase db= getActivity().openOrCreateDatabase("mydb.db", Context.MODE_PRIVATE,null);
                db.delete(local_stable,null,null);
                for (Music m:mylist){
                    insertMusic(m,local_stable);
                    Log.i("歌曲",m.getSongName()+":插入数据库成功");
                }
                handler.sendEmptyMessage(100);
            }
        }).start();
    }

    public class MyHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what==100){
                musicList.clear();
                musicList = findAll(local_stable);
                MyAdapter adapter=new MyAdapter(getActivity(),musicList,LocalMusicListFragment.this);
                listView.setAdapter(adapter);
                ((TextView)(myview.findViewById(R.id.list_title))).setText("本地列表("+musicList.size()+")");
                loading.setVisibility(View.GONE);
                textView.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
            }
        }
    }
    @Override
    public void onActivityCreated( Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


}
