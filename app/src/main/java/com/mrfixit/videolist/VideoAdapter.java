package com.mrfixit.videolist;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoHolder> {
    private List<Video> videoList;
    private OnAdapterCallbacks onAdapterCallbacks;
    private WeakReference<Activity> activityWeakReference;

    private VideoRequestHandler videoRequestHandler;
    private Picasso picassoInstance;

    private VideoHolder currentHolder;

    public interface OnAdapterCallbacks {
        void needDownload(DownloadTask task);
    }

    public VideoAdapter(List<Video> videoList, OnAdapterCallbacks callbacks, Activity activity) {
        this.videoList = videoList;
        onAdapterCallbacks = callbacks;
        activityWeakReference = new WeakReference<>(activity);
        videoRequestHandler = new VideoRequestHandler();
        picassoInstance = new Picasso.Builder(activityWeakReference.get().getApplicationContext())
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
    public void onBindViewHolder(final VideoHolder holder, int position) {
        Video video = videoList.get(position);
        holder.titleTextView.setText(video.getTitle());

        File file = new File(activityWeakReference.get().getFilesDir(), Util.getVideoFileName(video.getUrl()));
        Resources resources = activityWeakReference.get().getResources();
        int width = (int) resources.getDimension(R.dimen.item_video_width);
        int height = (int) resources.getDimension(R.dimen.item_video_min_height);
        Util.resizeView(holder.videoView, width, height);
        if (file.exists()) {
            holder.videoUri = file.getPath();
            picassoInstance.load(videoRequestHandler.SCHEME_VIDEO + ":" + file.getPath()).into(holder.thumbnailImageView);
            holder.videoPlayButton.setVisibility(View.VISIBLE);
            holder.videoProgressBar.setVisibility(View.INVISIBLE);
        } else {
            onAdapterCallbacks.needDownload(new DownloadTask(video.getUrl(), position));
            holder.videoProgressBar.setVisibility(View.VISIBLE);
            holder.videoView.setVisibility(View.INVISIBLE);
            holder.videoPlayButton.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    public int getItemCount() {
        return videoList.size();
    }


    @Override
    public void onViewRecycled(VideoHolder holder) {
        if (holder == currentHolder) {
            currentHolder = null;
            holder.stopVideo();
        }
        holder.videoView.stopPlayback();
        super.onViewRecycled(holder);
    }


    public void onScrolled(RecyclerView recyclerView) {
        if (currentHolder != null) {
            currentHolder.onScrolled(recyclerView);
        }
    }


    public class VideoHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        ImageView thumbnailImageView;
        ProgressBar videoProgressBar;
        CustomTextureVideoView videoView;
        ImageView videoPlayButton;

        String videoUri;

        public VideoHolder(View itemView) {
            super(itemView);
            titleTextView = (TextView) itemView.findViewById(R.id.item_video_title);
            thumbnailImageView = (ImageView) itemView.findViewById(R.id.item_video_thumbnail);
            videoProgressBar = (ProgressBar) itemView.findViewById(R.id.item_video_progress_bar);
            videoView = (CustomTextureVideoView) itemView.findViewById(R.id.item_video_view);
            videoPlayButton = (ImageView) itemView.findViewById(R.id.item_video_play_btn);

            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    int width = mp.getVideoWidth();
                    int height = mp.getVideoHeight();
                    videoView.setIsPrepared(true);
                    Activity activity = activityWeakReference.get();
                    if (activity == null) {
                        return;
                    }
                    int widthDim = (int) activity.getResources().getDimension(R.dimen.item_video_width);
                    Util.resizeView(videoView, widthDim,
                            widthDim * height / width);
                    if (currentHolder == VideoHolder.this) {
                        thumbnailImageView.setVisibility(View.GONE);
                        videoProgressBar.setVisibility(View.INVISIBLE);
                        videoView.setVisibility(View.VISIBLE);
                        videoView.seekTo(0);
                        videoView.start();
                    }
                }
            });

            videoView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus && currentHolder == VideoHolder.this) {
                        stopVideo();
                    }
                }
            });
            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    thumbnailImageView.setVisibility(View.VISIBLE);
                    videoPlayButton.setVisibility(View.VISIBLE);
                    if (videoView.getVisibility() == View.VISIBLE) {
                        videoView.setVisibility(View.INVISIBLE);
                    }
                    videoProgressBar.setVisibility(View.INVISIBLE);
                    currentHolder = null;
                }
            });
            videoPlayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentHolder != null && currentHolder != VideoHolder.this) {
                        currentHolder.videoView.pause();
                        currentHolder.thumbnailImageView.setVisibility(View.INVISIBLE);
                        currentHolder.videoPlayButton.setVisibility(View.VISIBLE);
                        currentHolder.videoProgressBar.setVisibility(View.INVISIBLE);
                        if (currentHolder.videoView.getVisibility() == View.VISIBLE) {
                            currentHolder.videoView.setVisibility(View.INVISIBLE);
                        }
                        currentHolder = null;
                    }

                    currentHolder = VideoHolder.this;

                    videoPlayButton.setVisibility(View.INVISIBLE);
                    videoProgressBar.setVisibility(View.VISIBLE);
                    videoView.setVisibility(View.VISIBLE);
                    thumbnailImageView.setVisibility(View.INVISIBLE);
                    if (!videoUri.equals(videoView.getVideoPath())) {
                        videoView.setIsPrepared(false);
                        videoView.setVideoPath(videoUri);
                        videoView.requestFocus();
                    } else {
                        if (videoView.isPrepared()) {
                            videoProgressBar.setVisibility(View.INVISIBLE);
                        } else {
                            videoProgressBar.setVisibility(View.VISIBLE);
                        }
                        videoView.requestFocus();
                        videoView.seekTo(0);
                        videoView.start();
                    }
                }
            });

        }

        public void stopVideo() {
            videoView.pause();
            if (videoView.getVisibility() == View.VISIBLE) {
                videoView.setVisibility(View.INVISIBLE);
            }
            thumbnailImageView.setVisibility(View.VISIBLE);
            videoPlayButton.setVisibility(View.VISIBLE);
            videoProgressBar.setVisibility(View.INVISIBLE);
            currentHolder = null;
        }

        public void onScrolled(RecyclerView recyclerView) {
            if (isViewNotVisible(videoPlayButton, recyclerView) || isViewNotVisible(videoProgressBar, recyclerView)) {
                stopVideo();
            }
        }

        public boolean isViewNotVisible(View view, RecyclerView recyclerView) {
            Rect scrollBounds = new Rect();
            recyclerView.getHitRect(scrollBounds);
            return view.getVisibility() == View.VISIBLE && !view.getLocalVisibleRect(scrollBounds);
        }
    }
}
