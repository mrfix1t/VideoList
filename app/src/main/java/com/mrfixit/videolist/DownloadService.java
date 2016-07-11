package com.mrfixit.videolist;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Михаил on 06.07.2016.
 */
public class DownloadService extends Service {
    private final IBinder binder = new LocalBinder();
    private ExecutorService executorService;
    private static final int THREAD_NUM = 4;

    public interface OnDownloadListener {
        void onFinished(int position);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        executorService = Executors.newFixedThreadPool(THREAD_NUM);
    }

    public class LocalBinder extends Binder {
        public DownloadService getService() {
            return DownloadService.this;
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    public void downloadVideo(final DownloadTask downloadTask, final OnDownloadListener listener) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                File file = new File(getFilesDir(), Util.getVideoFileName(downloadTask.getUrl()));
                Util.saveUrl(file.getAbsolutePath(), downloadTask.getUrl());
                listener.onFinished(downloadTask.getPosition());
            }
        });
    }


}
