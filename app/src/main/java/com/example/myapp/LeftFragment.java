package com.example.myapp;


import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.myapp.self.MyLogin;


/**
 * A simple {@link Fragment} subclass.
 */
public class LeftFragment extends Fragment{


    private Fragment localFragment,nearFragment,downloadFragment,loveFragment;
    private LinearLayout local,near,download,love;
    private SQLiteDatabase db;

    public LeftFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_left, container, false);
        local=(LinearLayout) view.findViewById(R.id.localmusicbtn);
        near = (LinearLayout) view.findViewById(R.id.nearplay);
        download = (LinearLayout) view.findViewById(R.id.downloadlist);
        love = (LinearLayout) view.findViewById(R.id.lovelist);
        TextView localtv=(TextView)view.findViewById(R.id.localmusictv);
        TextView neartv = (TextView)view.findViewById(R.id.nearlisttv);
        TextView downloadtv = (TextView)view.findViewById(R.id.downloadlisttv);
        TextView loveltv = (TextView)view.findViewById(R.id.lovelisttv);
        localtv.setText(localtv.getText()+"("+getCount("local_music_list")+")");
        neartv.setText(neartv.getText()+"("+getCount("near_music_list")+")");
        downloadtv.setText(downloadtv.getText()+"("+getCount("download_music_list")+")");
        loveltv.setText(loveltv.getText()+"("+getCount("love_music_list")+")");
        LinearLayout userLayout=(LinearLayout)view.findViewById(R.id.music_user);
        userLayout.setOnClickListener(listener);

//        MyLogin.getMyLogin().setLogin(true);

        local.setOnClickListener(listener);
        near.setOnClickListener(listener);
        download.setOnClickListener(listener);
        love.setOnClickListener(listener);

        return view;
    }

    View.OnClickListener listener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FragmentManager manager=getFragmentManager();
            FragmentTransaction transaction=manager.beginTransaction();
            switch (v.getId()){
                case R.id.localmusicbtn:
                    if(localFragment == null){
                        localFragment=new LocalMusicListFragment();
                    }
                    transaction.replace(R.id.other_frag,localFragment);
                    transaction.commit();
                    break;
                case R.id.nearplay:
                    if(nearFragment == null){
                        nearFragment=new NearPlayListFragment();
                    }
                    transaction.replace(R.id.other_frag,nearFragment);
                    transaction.commit();
                    break;
                case R.id.downloadlist:
                    if(downloadFragment==null)
                        downloadFragment = new DownloadMusicFragment();
                    transaction.replace(R.id.other_frag,downloadFragment);
                    transaction.commit();
                    break;
                case R.id.lovelist:
                    if(loveFragment==null)
                        loveFragment = new LoveMusicFragment();
                    transaction.replace(R.id.other_frag,loveFragment);
                    transaction.commit();
                    break;
                case R.id.music_user:
                    boolean logined = MyLogin.getMyLogin().isLogin();
                    if(!logined){
                        Intent intent=new Intent(getActivity(),Login_in.class);
                        startActivity(intent);
                    }
                    break;
                default:
                    break;
            }
        }
    };
    public SQLiteDatabase getSQLiteDB(){
        return getActivity().openOrCreateDatabase("mydb.db", Context.MODE_PRIVATE,null);
    }

    public int getCount(String tableName){
        db=getSQLiteDB();
        Cursor cursor=db.query(tableName,new String[]{"id"},null,null,null,null,null);
        return cursor.getCount();

    }


}
