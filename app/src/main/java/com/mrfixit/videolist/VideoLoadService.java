package com.mrfixit.videolist;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Михаил on 29.06.2016.
 */
public class VideoLoadService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
