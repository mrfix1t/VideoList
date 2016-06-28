package com.mrfixit.videolist;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Михаил on 29.06.2016.
 */
public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoHolder> {
    private List<Video> videoList;

    public VideoAdapter(List<Video> videoList) {
        this.videoList = videoList;
    }


    public void updateVideos(List<Video> videos) {
        videoList = videos;
        notifyDataSetChanged();
    }


    @Override
    public VideoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_video, parent, false);
        return new VideoHolder(v);
    }

    @Override
    public void onBindViewHolder(VideoHolder holder, int position) {
        Video video = videoList.get(position);
        holder.titleTextView.setText(video.getTitle());
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public static class VideoHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public TextView progressTextView;

        public VideoHolder(View itemView) {
            super(itemView);
            titleTextView = (TextView) itemView.findViewById(R.id.item_video_title);
            progressTextView = (TextView) itemView.findViewById(R.id.item_video_progress);
        }
    }
}
