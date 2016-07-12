package com.mrfixit.videolist.video;

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

import com.mrfixit.videolist.util.CustomTextureVideoView;
import com.mrfixit.videolist.app.DownloadTask;
import com.mrfixit.videolist.R;
import com.mrfixit.videolist.util.Util;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoHolder> {
    private List<Video> videoList = new ArrayList<>();
    private OnAdapterCallbacks onAdapterCallbacks;
    private WeakReference<Activity> activityWeakReference;

    private VideoRequestHandler videoRequestHandler;
    private Picasso picassoInstance;

    private VideoHolder currentHolder;

    public interface OnAdapterCallbacks {
        void onDownloadTask(DownloadTask task);
    }

    public VideoAdapter(OnAdapterCallbacks callbacks, Activity activity) {
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
        //resize videoView to default size
        Resources resources = activityWeakReference.get().getResources();
        int width = (int) resources.getDimension(R.dimen.item_video_width);
        int height = (int) resources.getDimension(R.dimen.item_video_min_height);
        Util.resizeView(holder.videoView, width, height);

        File videoFile = new File(activityWeakReference.get().getFilesDir(), Util.getVideoFileName(video.getUrl()));

        if (videoFile.exists()) {
            holder.videoUri = videoFile.getPath();
            picassoInstance.load(videoRequestHandler.SCHEME_VIDEO + ":" + videoFile.getPath()).into(holder.thumbnailImageView);
            holder.videoPlayButton.setVisibility(View.VISIBLE);
            holder.videoProgressBar.setVisibility(View.INVISIBLE);
        } else {
            onAdapterCallbacks.onDownloadTask(new DownloadTask(video.getUrl(), position));
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
            holder.stopVideo(currentHolder);
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
                    videoView.setIsPrepared(true);
                    resizeVideoView(mp);
                    if (currentHolder == VideoHolder.this) {
                        thumbnailImageView.setVisibility(View.INVISIBLE);
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
                        stopVideo(currentHolder);
                    }
                }
            });

            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    setDefaultState(VideoHolder.this);
                }
            });

            videoPlayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentHolder != null && currentHolder != VideoHolder.this) {
                        stopVideo(currentHolder);
                    }

                    currentHolder = VideoHolder.this;

                    thumbnailImageView.setVisibility(View.INVISIBLE);
                    videoPlayButton.setVisibility(View.INVISIBLE);
                    videoView.setVisibility(View.VISIBLE);
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

        private void stopVideo(VideoHolder videoHolder) {
            videoHolder.videoView.pause();
            setDefaultState(videoHolder);
        }

        private void setDefaultState(VideoHolder videoHolder) {
            videoHolder.videoView.setVisibility(View.INVISIBLE);
            videoHolder.thumbnailImageView.setVisibility(View.VISIBLE);
            videoHolder.videoPlayButton.setVisibility(View.VISIBLE);
            videoHolder.videoProgressBar.setVisibility(View.INVISIBLE);
            currentHolder = null;
        }

        private void onScrolled(RecyclerView recyclerView) {
            if (isViewNotVisible(videoPlayButton, recyclerView) || isViewNotVisible(videoProgressBar, recyclerView)) {
                stopVideo(this);
            }
        }

        private boolean isViewNotVisible(View view, RecyclerView recyclerView) {
            Rect scrollBounds = new Rect();
            recyclerView.getHitRect(scrollBounds);
            return view.getVisibility() == View.VISIBLE && !view.getLocalVisibleRect(scrollBounds);
        }

        private void resizeVideoView(MediaPlayer mp) {
            int width = mp.getVideoWidth();
            int height = mp.getVideoHeight();
            Activity activity = activityWeakReference.get();
            if (activity == null) {
                return;
            }
            int widthDim = (int) activity.getResources().getDimension(R.dimen.item_video_width);
            Util.resizeView(videoView, widthDim, widthDim * height / width);
        }
    }
}
