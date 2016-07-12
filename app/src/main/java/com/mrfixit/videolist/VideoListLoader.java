package com.mrfixit.videolist;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class VideoListLoader extends AsyncTaskLoader<List<Video>> {
    private Context context;

    private List<Video> data;

    private static final String TAG = VideoListLoader.class.getSimpleName();

    public VideoListLoader(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public List<Video> loadInBackground() {
        List<Video> videos = new ArrayList<>();
        String json = Util.loadAssetTextAsString(context, "list.json");
        if (!TextUtils.isEmpty(json)) {
            try {
                JSONArray jsonArray = new JSONArray(json);
                for (int i = 0, n = jsonArray.length(); i < n; i++) {
                    Video video = new Video();
                    JSONObject videoObject = jsonArray.getJSONObject(i);
                    video.setUrl(videoObject.getString("url"));
                    video.setTitle(videoObject.getString("title"));
                    videos.add(video);
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error parse json");
            }
        }
        return videos;
    }

    @Override
    public void deliverResult(List<Video> data) {
        if (isReset()) {
            releaseResources(data);
            return;
        }

        List<Video> oldData = this.data;
        this.data = data;

        if (isStarted()) {
            super.deliverResult(data);
        }

        if (oldData != null && oldData != data) {
            releaseResources(oldData);
        }
    }

    @Override
    protected void onStartLoading() {
        if (data != null) {
            deliverResult(data);
        }
        if (takeContentChanged() || data == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        onStopLoading();
        if (data != null) {
            releaseResources(data);
            data = null;
        }
    }

    @Override
    public void onCanceled(List<Video> data) {
        super.onCanceled(data);
        releaseResources(data);
    }

    private void releaseResources(List<Video> data) {
    }

}
