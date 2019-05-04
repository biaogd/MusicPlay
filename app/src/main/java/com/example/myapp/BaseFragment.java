package com.example.myapp;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapp.database.MyDao;
import com.example.myapp.self.Music;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BaseFragment extends Fragment implements Serializable {

    private static final long serialVersionUID = 8278847306510730368L;
    protected View myview;
    private ImageButton backButton;
    private Fragment fragment;
    private SQLiteDatabase db;

    protected Service myService;
    protected List<Music> musicList;
    protected ListView listView;
    protected BaseAdapter adapter;

    protected String songName;
    protected String songAuthor;
    protected static final String local_stable = "local_music_list";
    protected static final String near_stable = "near_music_list";
    protected static final String download_stable = "download_music_list";
    protected static final String love_stable = "love_music_list";
    protected MyBroadcastReceiver broadcast;
    private MyDao myDao;
    public BaseFragment(){
    }

    public BaseFragment getFragment(){
        return BaseFragment.this;
    }

    public SQLiteDatabase getSQLiteDB(){
        return getActivity().openOrCreateDatabase("mydb.db", Context.MODE_PRIVATE,null);
    }

    /**
     * 初始化Fragment
     */
    public void initFragment(){

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        this.myview=inflater.inflate(R.layout.basetab,container,false);
        backButton=(ImageButton)myview.findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager=getFragmentManager();
                FragmentTransaction transaction=manager.beginTransaction();
                if(fragment == null){
                    fragment =new MainFragment();
                    transaction.add(R.id.other_frag,fragment);
                }
                transaction.hide(getFragment());
                transaction.show(fragment);
                transaction.commit();
            }
        });
        //下面三行代码是使myview获取焦点，以响应各种事件
        myview.setFocusable(true);
        myview.setFocusableInTouchMode(true);
        myview.requestFocus();
        myview.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(event.getAction() == KeyEvent.ACTION_DOWN){
                    if (keyCode == KeyEvent.KEYCODE_BACK){
                        FragmentManager manager=getFragmentManager();
                        FragmentTransaction transaction=manager.beginTransaction();
                        if(fragment == null){
                            fragment =new MainFragment();
                            transaction.add(R.id.other_frag,fragment);
                        }
                        transaction.hide(getFragment());
                        transaction.show(fragment);
                        transaction.commit();
                        return true;
                    }
                }
                return false;
            }
        });
        broadcast=new MyBroadcastReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction("updateMusic");
        getActivity().registerReceiver(broadcast,filter);
//        在这个fragment创建的时候发送广播，获取现在正在播放的音乐，用于标识正在播放的音乐的颜色不同
        Intent intent1=new Intent();
        intent1.setAction("getnowplaymusic");
        getActivity().sendBroadcast(intent1);
        return null;
    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        myDao=new MyDao(getActivity());
        myDao.initConnect();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Music music=musicList.get(position);
                File file = new File(music.getPath());
                String tname="";
                if(music.getFlag()==0) {
                    if (file.exists()) {
                        Intent intent = new Intent();
                        intent.setAction("startMusic");
                        Bundle bundle = new Bundle();
                        bundle.putInt("position", position);
                        String whichFragment;
                        if (getFragment() instanceof LocalMusicListFragment) {
                            whichFragment = "localFragment";
                        } else if (getFragment() instanceof NearPlayListFragment) {
                            whichFragment = "nearFragment";
                        } else if (getFragment() instanceof DownloadMusicFragment) {
                            whichFragment = "downloadFragment";
                        } else {
                            whichFragment = "loveFragment";
                        }
                        bundle.putString("whichFragment", whichFragment);
                        bundle.putSerializable("musicList", (ArrayList<Music>) musicList);
                        intent.putExtra("mList", bundle);
                        getActivity().sendBroadcast(intent);
                        if (getFragment() instanceof NearPlayListFragment) {
                            myDao.deleteMusic(music, near_stable);
                            musicList.remove(position);
                            musicList.add(0, music);
                            adapter.notifyDataSetChanged();
                            myDao.insertMusic(music, near_stable);
                        }
                    } else {
                        //这个本地音乐文件不存在时
                        Toast.makeText(getActivity(), "音乐文件不存在，无法播放", Toast.LENGTH_SHORT).show();
//                        String paths = musicList.get(position).getPath();
//                        getSQLiteDB().delete(local_stable, "path=?", new String[]{paths});
                        if(getFragment() instanceof LocalMusicListFragment){
                            tname = local_stable;
                        }else if(getFragment() instanceof DownloadMusicFragment){
                            tname = download_stable;
                        }else if(getFragment() instanceof NearPlayListFragment){
                            tname = near_stable;
                        }else {
                            tname = love_stable;
                        }
                        myDao.deleteMusic(music,tname);
                        musicList.remove(position);
                        adapter.notifyDataSetChanged();
                    }
                }else {
                    //播放的是网络歌曲
                    Intent intent = new Intent();
                    intent.setAction("startMusic");
                    Bundle bundle = new Bundle();
                    bundle.putInt("position", position);
                    String whichFragment;
                    if (getFragment() instanceof LocalMusicListFragment) {
                        whichFragment = "localFragment";
                    } else if (getFragment() instanceof NearPlayListFragment) {
                        whichFragment = "nearFragment";
                    } else if (getFragment() instanceof DownloadMusicFragment) {
                        whichFragment = "downloadFragment";
                    } else {
                        whichFragment = "loveFragment";
                    }
                    bundle.putString("whichFragment", whichFragment);
                    bundle.putSerializable("musicList", (ArrayList<Music>) musicList);
                    intent.putExtra("mList", bundle);
                    getActivity().sendBroadcast(intent);
                    if (getFragment() instanceof NearPlayListFragment) {
                        myDao.deleteMusic(music, near_stable);
                        musicList.remove(position);
                        musicList.add(0, music);
                        adapter.notifyDataSetChanged();
                        myDao.insertMusic(music, near_stable);
                    }
                }
            }
        });

    }

    private class MyBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("updateMusic")){
                Log.i("获取到广播","在BaseFragment中");
                Bundle bundle=intent.getBundleExtra("nowplay");
                Music music = (Music) bundle.getSerializable("nowplaymusic");
                int j=0;
                for(j =0;j<musicList.size();j++){
                    if(musicList.get(j).getId()==-1000){
                        Log.i("找到了","等于-1000的歌曲idd");
                        musicList.get(j).setId(-1);
                    }
                }
                int i=0;
                for(i=0;i<musicList.size();i++){
                    if(musicList.get(i).getPath().equals(music.getPath())){
                        Log.i("找到了相同的歌曲","正在播放的歌曲和列表中的相同");
                        musicList.get(i).setId(-1000);
                    }
                }
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(broadcast);
        if(myDao.isConnection()){
            myDao.closeConnect();
        }
    }



    protected List<Music> reSort(List<Music> list){
        List<Music> myList =new ArrayList<>();
        for(int i=list.size()-1;i>=0;i--){
            myList.add(list.get(i));
        }
        return myList;
    }

}


