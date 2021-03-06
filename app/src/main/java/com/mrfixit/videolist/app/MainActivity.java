package com.mrfixit.videolist.app;

import android.app.LoaderManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.mrfixit.videolist.R;
import com.mrfixit.videolist.video.Video;
import com.mrfixit.videolist.video.VideoAdapter;
import com.mrfixit.videolist.video.VideoListLoader;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Video>>,
        VideoAdapter.OnAdapterCallbacks, DownloadService.OnDownloadListener {
    private RecyclerView videoRecyclerView;
    private ProgressBar progressBar;
    private VideoAdapter adapter;
    private LinkedList<DownloadTask> pendingTasks = new LinkedList<>();

    private static final String TAG = MainActivity.class.getSimpleName();

    private boolean bound = false;
    private DownloadService downloadService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        videoRecyclerView = (RecyclerView) findViewById(R.id.activity_main_video_recycler_view);
        progressBar = (ProgressBar) findViewById(R.id.activity_main_progress_bar);
        initRecyclerView();
        getLoaderManager().initLoader(0, null, this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, DownloadService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bound) {
            unbindService(mConnection);
            bound = false;
        }
    }


    private void initRecyclerView() {
        videoRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        adapter = new VideoAdapter(this, this);
        videoRecyclerView.setHasFixedSize(true);
        videoRecyclerView.setAdapter(adapter);
        videoRecyclerView.setItemAnimator(null);
        videoRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING ||
                        newState == RecyclerView.SCROLL_STATE_SETTLING ||
                        newState == RecyclerView.SCROLL_STATE_IDLE) {
                    adapter.onScrolled(recyclerView);
                }
            }
        });
    }

    @Override
    public Loader<List<Video>> onCreateLoader(int i, Bundle bundle) {
        progressBar.setVisibility(View.VISIBLE);
        return new VideoListLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<List<Video>> loader, List<Video> videos) {
        progressBar.setVisibility(View.GONE);
        if (!videos.isEmpty()) {
            adapter.updateVideos(videos);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Video>> loader) {
        progressBar.setVisibility(View.GONE);
        adapter.updateVideos(new ArrayList<Video>());
    }


    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            DownloadService.LocalBinder binder = (DownloadService.LocalBinder) service;
            downloadService = binder.getService();
            bound = true;

            if (!pendingTasks.isEmpty()) {
                while (!pendingTasks.isEmpty()) {
                    downloadService.downloadVideo(pendingTasks.poll(), MainActivity.this);
                    Log.i(TAG, "send pending download task");
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bound = false;
            pendingTasks.clear();
        }
    };

    @Override
    public void onDownloadTask(DownloadTask downloadTask) {
        if (downloadService == null) {
            pendingTasks.add(downloadTask);
            Log.i(TAG, "add pending task");
            return;
        }
        downloadService.downloadVideo(downloadTask, this);
    }

    @Override
    public void onFinished(final int position) {
        Log.i(TAG, "download finished, pos = " + position);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyItemChanged(position);
            }
        });
    }
}
