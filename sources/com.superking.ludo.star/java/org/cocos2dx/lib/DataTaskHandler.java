package org.cocos2dx.lib;

import android.util.Log;
import com.loopj.android.http.BinaryHttpResponseHandler;
import cz.msebera.android.httpclient.Header;

/* compiled from: Cocos2dxDownloader */
class DataTaskHandler extends BinaryHttpResponseHandler {
    private Cocos2dxDownloader _downloader;
    int _id;
    private long _lastBytesWritten = 0;

    void LogD(String msg) {
        Log.d("Cocos2dxDownloader", msg);
    }

    public DataTaskHandler(Cocos2dxDownloader downloader, int id) {
        super(new String[]{".*"});
        this._downloader = downloader;
        this._id = id;
    }

    public void onProgress(long bytesWritten, long totalSize) {
        this._downloader.onProgress(this._id, bytesWritten - this._lastBytesWritten, bytesWritten, totalSize);
        this._lastBytesWritten = bytesWritten;
    }

    public void onStart() {
        this._downloader.onStart(this._id);
    }

    public void onFailure(int i, Header[] headers, byte[] errorResponse, Throwable throwable) {
        LogD("onFailure(i:" + i + " headers:" + headers + " throwable:" + throwable);
        String errStr = BuildConfig.FLAVOR;
        if (throwable != null) {
            errStr = throwable.toString();
        }
        this._downloader.onFinish(this._id, i, errStr, null);
    }

    public void onSuccess(int i, Header[] headers, byte[] binaryData) {
        LogD("onSuccess(i:" + i + " headers:" + headers);
        this._downloader.onFinish(this._id, 0, null, binaryData);
    }
}
