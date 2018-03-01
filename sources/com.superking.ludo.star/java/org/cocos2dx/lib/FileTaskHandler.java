package org.cocos2dx.lib;

import android.util.Log;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import cz.msebera.android.httpclient.Header;
import java.io.File;

/* compiled from: Cocos2dxDownloader */
class FileTaskHandler extends FileAsyncHttpResponseHandler {
    private Cocos2dxDownloader _downloader;
    File _finalFile;
    int _id;
    private long _initFileLen = getTargetFile().length();
    private long _lastBytesWritten = 0;

    void LogD(String msg) {
        Log.d("Cocos2dxDownloader", msg);
    }

    public FileTaskHandler(Cocos2dxDownloader downloader, int id, File temp, File finalFile) {
        super(temp, true);
        this._finalFile = finalFile;
        this._downloader = downloader;
        this._id = id;
    }

    public void onProgress(long bytesWritten, long totalSize) {
        this._downloader.onProgress(this._id, bytesWritten - this._lastBytesWritten, bytesWritten + this._initFileLen, totalSize + this._initFileLen);
        this._lastBytesWritten = bytesWritten;
    }

    public void onStart() {
        this._downloader.onStart(this._id);
    }

    public void onFinish() {
        this._downloader.runNextTaskIfExists();
    }

    public void onFailure(int i, Header[] headers, Throwable throwable, File file) {
        LogD("onFailure(i:" + i + " headers:" + headers + " throwable:" + throwable + " file:" + file);
        String errStr = BuildConfig.FLAVOR;
        if (throwable != null) {
            errStr = throwable.toString();
        }
        this._downloader.onFinish(this._id, i, errStr, null);
    }

    public void onSuccess(int i, Header[] headers, File file) {
        LogD("onSuccess(i:" + i + " headers:" + headers + " file:" + file);
        String errStr = null;
        if (this._finalFile.exists()) {
            if (this._finalFile.isDirectory()) {
                errStr = "Dest file is directory:" + this._finalFile.getAbsolutePath();
            } else if (!this._finalFile.delete()) {
                errStr = "Can't remove old file:" + this._finalFile.getAbsolutePath();
            }
            this._downloader.onFinish(this._id, 0, errStr, null);
        }
        getTargetFile().renameTo(this._finalFile);
        this._downloader.onFinish(this._id, 0, errStr, null);
    }
}
