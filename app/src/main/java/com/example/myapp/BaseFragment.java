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
        Log.i("建立碎片","重新创建一个Fragment");
        this.myview=inflater.inflate(R.layout.basetab,container,false);
        backButton=(ImageButton)myview.findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("back_button","the button press********************");
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
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Music music=musicList.get(position);
                File file = new File(music.getPath());
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
                            deleteMusic(music, near_stable);
                            musicList.remove(position);
                            musicList.add(0, music);
                            adapter.notifyDataSetChanged();
                            insertMusic(music, near_stable);
                        }
                    } else {
                        //这个本地音乐文件不存在时
                        Toast.makeText(getActivity(), "音乐文件不存在，无法播放", Toast.LENGTH_SHORT).show();
                        String paths = musicList.get(position).getPath();
                        getSQLiteDB().delete(local_stable, "path=?", new String[]{paths});
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
                        deleteMusic(music, near_stable);
                        musicList.remove(position);
                        musicList.add(0, music);
                        adapter.notifyDataSetChanged();
                        insertMusic(music, near_stable);
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
    }

    /**
     * 提供一个基础方法，用于查询数据库中所有的信息
     * @param tableName 表的名字
     * @return  一个包含所有Music对象的列表
     */
    protected List<Music> findAll(String tableName){
        List<Music> list=new ArrayList<>();
        db= getSQLiteDB();
        Cursor cursor = db.query(tableName,null,null,null,null,null,null);
        if(cursor.moveToFirst()){
            do {
                Music music=new Music();
                music.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                music.setSongName(cursor.getString(cursor.getColumnIndexOrThrow("song_name")));
                music.setSongAuthor(cursor.getString(cursor.getColumnIndexOrThrow("song_author")));
                music.setAlltime(cursor.getInt(cursor.getColumnIndexOrThrow("all_time")));
                music.setPath(cursor.getString(cursor.getColumnIndexOrThrow("path")));
                music.setSongSize(cursor.getInt(cursor.getColumnIndexOrThrow("song_size")));
                music.setFlag(cursor.getInt(cursor.getColumnIndexOrThrow("flag")));
                music.setLove(cursor.getInt(cursor.getColumnIndexOrThrow("love")));
                list.add(music);
            }while (cursor.moveToNext());
        }
        //关闭数据库连接
        if(!cursor.isClosed()){
            cursor.close();
        }
        if (db.isOpen()){
            db.close();
        }
        return list;
    }

    /**
     * 把歌曲信息插入到数据库当中
     * @param music Music对象，要插入的音乐对象
     * @param tableName 表名，插入到这个数据库当中
     * @return  返回影响的行数，用于判断是否插入成功
     */
    protected long insertMusic(Music music,String tableName){
        db = getSQLiteDB();
        ContentValues values=new ContentValues();
        values.put("song_name",music.getSongName());
        values.put("song_author",music.getSongAuthor());
        values.put("all_time",music.getAlltime());
        values.put("path",music.getPath());
        values.put("song_size",music.getSongSize());
        values.put("flag",music.getFlag());
        values.put("love",music.getLove());
        long i=db.insert(tableName,null,values);
        if(db.isOpen()){
            db.close();
        }
        return i;
    }

    /**
     * 从数据库中删除一行，根据音乐的路径删除，路径是唯一的
     * @param music 要删除的MusicBean
     * @param tableName 表名，从这个数据库删除
     * @return long 返回影响的行数
     */
    protected long deleteMusic(Music music, String tableName){
        db = getSQLiteDB();
        String dpath = music.getPath();
        long row = db.delete(tableName,"path=?",new String[]{dpath});
        if(db.isOpen()){
            db.close();
        }
        return row;
    }

    /**
     * 删除表的全部内容
     * @param tableName
     */
    protected void deleteAll(String tableName){
        db = getSQLiteDB();
        long row = db.delete(tableName,null,null);
        Log.i("删除的个数"," "+row);
        if(db.isOpen()){
            db.close();
        }
    }

    protected void deleteMusicFromListView(int position,BaseAdapter adapter){
        musicList.remove(position);
        adapter.notifyDataSetChanged();
    }

    protected List<Music> reSort(List<Music> list){
        List<Music> myList =new ArrayList<>();
        for(int i=list.size()-1;i>=0;i--){
            myList.add(list.get(i));
        }
        return myList;
    }

    public void updateNearList(Music music) {
        List<Music> list1=findAll(near_stable);
        list1 = reSort(list1);
        int i;
        for(i=0;i<list1.size();i++){
            Music m=list1.get(i);
            if(m.getPath().equals(music.getPath())){      //列表中已经有这个歌曲
                deleteMusic(music,near_stable);     //从数据库中删除
                if(getFragment() instanceof NearPlayListFragment){
                    musicList.remove(i);
                    musicList.add(0,music);
                    adapter.notifyDataSetChanged();
                }
                insertMusic(music,near_stable);

                break;
            }
        }
        if(i>=list1.size()){     //这首歌不再数据库当中，也就是不再最近播放列表中
            insertMusic(music,near_stable);
            if(getFragment() instanceof NearPlayListFragment) {
                musicList.add(0, music);
                adapter.notifyDataSetChanged();
            }
        }
        Log.i("music:", music.toString());
    }
}


