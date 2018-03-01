package org.cocos2dx.lib;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;

/* compiled from: Cocos2dxDownloader */
class DownloadTask {
    long bytesReceived;
    byte[] data;
    RequestHandle handle = null;
    AsyncHttpResponseHandler handler = null;
    long totalBytesExpected;
    long totalBytesReceived;

    DownloadTask() {
        resetStatus();
    }

    void resetStatus() {
        this.bytesReceived = 0;
        this.totalBytesReceived = 0;
        this.totalBytesExpected = 0;
        this.data = null;
    }
}
