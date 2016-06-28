package com.mrfixit.videolist;

import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Video>> {
    private RecyclerView videoRecyclerView;
    private VideoAdapter adapter;
    private List<Video> videoList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        videoRecyclerView = (RecyclerView) findViewById(R.id.video_recycler_view);
        initRecyclerView();
        getLoaderManager().initLoader(0, null, this);
    }


    private void initRecyclerView() {
        videoRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        adapter = new VideoAdapter(videoList);
        videoRecyclerView.setAdapter(adapter);
    }

    @Override
    public Loader<List<Video>> onCreateLoader(int i, Bundle bundle) {
        return new VideoListLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<List<Video>> loader, List<Video> videos) {
        if (!videos.isEmpty()) {
            videoList = videos;
            adapter.updateVideos(videoList);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Video>> loader) {
        videoList = new ArrayList<>();
        adapter.updateVideos(videoList);
    }
}
