package com.mrfixit.videolist;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class VideoLoadService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
