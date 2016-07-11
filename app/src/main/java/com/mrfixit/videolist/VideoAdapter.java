package com.mrfixit.videolist;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

/**
 * Created by Михаил on 29.06.2016.
 */
public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoHolder> {
    private List<Video> videoList;
    private OnAdapterCallbacks onAdapterCallbacks;
    private Context context;

    private VideoRequestHandler videoRequestHandler;
    private Picasso picassoInstance;

    public interface OnAdapterCallbacks {
        void needDownload(DownloadTask task);
    }

    public VideoAdapter(List<Video> videoList, OnAdapterCallbacks callbacks, Context context) {
        this.videoList = videoList;
        onAdapterCallbacks = callbacks;
        this.context = context;
        videoRequestHandler = new VideoRequestHandler();
        picassoInstance = new Picasso.Builder(context.getApplicationContext())
                .addRequestHandler(videoRequestHandler)
                .build();
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

        File file = new File(context.getFilesDir(), Util.getVideoFileName(video.getUrl()));
        if (file.exists()) {
            picassoInstance.load(videoRequestHandler.SCHEME_VIDEO + ":" + file.getPath()).into(holder.thumbnailImageView);
            Log.i(VideoAdapter.class.getSimpleName(), "file exists, name = " + Util.getVideoFileName(video.getUrl()));
        } else {
            onAdapterCallbacks.needDownload(new DownloadTask(video.getUrl(), position));
        }
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public static class VideoHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public ImageView thumbnailImageView;

        public VideoHolder(View itemView) {
            super(itemView);
            titleTextView = (TextView) itemView.findViewById(R.id.item_video_title);
            thumbnailImageView = (ImageView) itemView.findViewById(R.id.item_video_thumbnail);
        }
    }
}
