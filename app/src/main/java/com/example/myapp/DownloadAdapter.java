package com.example.myapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.myapp.self.DownloadBean;
import com.example.myapp.self.Music;

import java.text.DecimalFormat;
import java.util.List;

public class DownloadAdapter extends BaseAdapter {

    private List<DownloadBean> downloadBeanList;
    private Context context;
    private ViewHolder holder;

    public DownloadAdapter(Context context,List<DownloadBean> downloadBeans){
        this.context=context;
        this.downloadBeanList = downloadBeans;
    }
    @Override
    public int getCount() {
        return downloadBeanList.size();
    }

    @Override
    public Object getItem(int position) {
        return downloadBeanList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    public class ViewHolder{
        TextView songTV;
        TextView sizeTV;
        ProgressBar progressBar;
        TextView waitTV;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView==null){
            convertView = LayoutInflater.from(context).inflate(R.layout.download,null);
            holder=new ViewHolder();
            holder.songTV=(TextView)convertView.findViewById(R.id.song_intro);
            holder.sizeTV=(TextView)convertView.findViewById(R.id.song_size_tv);
            holder.progressBar=(ProgressBar)convertView.findViewById(R.id.dw_progress);
            holder.waitTV = (TextView)convertView.findViewById(R.id.download_waiting);
            convertView.setTag(holder);
        }else {
            holder=(ViewHolder)convertView.getTag();
        }
        DownloadBean downloadBean= downloadBeanList.get(position);
        Music music=downloadBean.getMusic();
        int progress = downloadBean.getProgress();
        holder.songTV.setText(music.getSongName()+" - "+music.getSongAuthor());
        double sizes = (double)music.getSongSize()/(1024*1024);
        DecimalFormat format=new DecimalFormat("#.00");
        holder.sizeTV.setText(format.format(sizes)+"M");
        holder.progressBar.setProgress(progress);
        if(progress==0){
            holder.waitTV.setVisibility(View.VISIBLE);
            holder.progressBar.setVisibility(View.GONE);
        }else {
            holder.waitTV.setVisibility(View.GONE);
            holder.progressBar.setVisibility(View.VISIBLE);
        }
        return convertView;
    }
}
