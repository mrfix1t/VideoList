package com.mrfixit.videolist.app;

public class DownloadTask {
    private int position;
    private String url;

    public DownloadTask(String url, int position) {
        this.position = position;
        this.url = url;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
