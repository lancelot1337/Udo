package org.cocos2dx.lib;

import com.loopj.android.http.AsyncHttpClient;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.client.cache.HeaderConstants;
import cz.msebera.android.httpclient.message.BasicHeader;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import javax.net.ssl.SSLException;

public class Cocos2dxDownloader {
    private int _countOfMaxProcessingTasks;
    private AsyncHttpClient _httpClient = new AsyncHttpClient();
    private int _id;
    private int _runningTaskCount = 0;
    private HashMap _taskMap = new HashMap();
    private Queue<Runnable> _taskQueue = new LinkedList();
    private String _tempFileNameSufix;

    native void nativeOnFinish(int i, int i2, int i3, String str, byte[] bArr);

    native void nativeOnProgress(int i, int i2, long j, long j2, long j3);

    void onProgress(int id, long downloadBytes, long downloadNow, long downloadTotal) {
        DownloadTask task = (DownloadTask) this._taskMap.get(Integer.valueOf(id));
        if (task != null) {
            task.bytesReceived = downloadBytes;
            task.totalBytesReceived = downloadNow;
            task.totalBytesExpected = downloadTotal;
        }
        final int i = id;
        final long j = downloadBytes;
        final long j2 = downloadNow;
        final long j3 = downloadTotal;
        Cocos2dxHelper.runOnGLThread(new Runnable() {
            public void run() {
                Cocos2dxDownloader.this.nativeOnProgress(Cocos2dxDownloader.this._id, i, j, j2, j3);
            }
        });
    }

    public void onStart(int id) {
        DownloadTask task = (DownloadTask) this._taskMap.get(Integer.valueOf(id));
        if (task != null) {
            task.resetStatus();
        }
    }

    public void onFinish(int id, int errCode, String errStr, byte[] data) {
        if (((DownloadTask) this._taskMap.get(Integer.valueOf(id))) != null) {
            this._taskMap.remove(Integer.valueOf(id));
            final int i = id;
            final int i2 = errCode;
            final String str = errStr;
            final byte[] bArr = data;
            Cocos2dxHelper.runOnGLThread(new Runnable() {
                public void run() {
                    Cocos2dxDownloader.this.nativeOnFinish(Cocos2dxDownloader.this._id, i, i2, str, bArr);
                }
            });
        }
    }

    public static Cocos2dxDownloader createDownloader(int id, int timeoutInSeconds, String tempFileNameSufix, int countOfMaxProcessingTasks) {
        Cocos2dxDownloader downloader = new Cocos2dxDownloader();
        downloader._id = id;
        downloader._httpClient.setEnableRedirects(true);
        if (timeoutInSeconds > 0) {
            downloader._httpClient.setTimeout(timeoutInSeconds * GameControllerDelegate.THUMBSTICK_LEFT_X);
        }
        AsyncHttpClient asyncHttpClient = downloader._httpClient;
        AsyncHttpClient.allowRetryExceptionClass(SSLException.class);
        downloader._tempFileNameSufix = tempFileNameSufix;
        downloader._countOfMaxProcessingTasks = countOfMaxProcessingTasks;
        return downloader;
    }

    public static void createTask(final Cocos2dxDownloader downloader, int id_, String url_, String path_) {
        final int id = id_;
        final String url = url_;
        final String path = path_;
        downloader.enqueueTask(new Runnable() {
            public void run() {
                DownloadTask task = new DownloadTask();
                if (path.length() == 0) {
                    task.handler = new DataTaskHandler(downloader, id);
                    task.handle = downloader._httpClient.get(Cocos2dxHelper.getActivity(), url, task.handler);
                }
                if (path.length() != 0) {
                    File tempFile = new File(path + downloader._tempFileNameSufix);
                    if (!tempFile.isDirectory()) {
                        File parent = tempFile.getParentFile();
                        if (parent.isDirectory() || parent.mkdirs()) {
                            File finalFile = new File(path);
                            if (!tempFile.isDirectory()) {
                                task.handler = new FileTaskHandler(downloader, id, tempFile, finalFile);
                                Header[] headers = null;
                                long fileLen = tempFile.length();
                                if (fileLen > 0) {
                                    List<Header> list = new ArrayList();
                                    list.add(new BasicHeader(HeaderConstants.RANGE, "bytes=" + fileLen + "-"));
                                    headers = (Header[]) list.toArray(new Header[list.size()]);
                                }
                                task.handle = downloader._httpClient.get(Cocos2dxHelper.getActivity(), url, headers, null, task.handler);
                            }
                        }
                    }
                }
                if (task.handle == null) {
                    final String errStr = "Can't create DownloadTask for " + url;
                    Cocos2dxHelper.runOnGLThread(new Runnable() {
                        public void run() {
                            downloader.nativeOnFinish(downloader._id, id, 0, errStr, null);
                        }
                    });
                    return;
                }
                downloader._taskMap.put(Integer.valueOf(id), task);
            }
        });
    }

    public static void cancelAllRequests(final Cocos2dxDownloader downloader) {
        Cocos2dxHelper.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                for (Entry entry : downloader._taskMap.entrySet()) {
                    DownloadTask task = (DownloadTask) entry.getValue();
                    if (task.handle != null) {
                        task.handle.cancel(true);
                    }
                }
            }
        });
    }

    public void enqueueTask(Runnable taskRunnable) {
        synchronized (this._taskQueue) {
            if (this._runningTaskCount < this._countOfMaxProcessingTasks) {
                Cocos2dxHelper.getActivity().runOnUiThread(taskRunnable);
                this._runningTaskCount++;
            } else {
                this._taskQueue.add(taskRunnable);
            }
        }
    }

    public void runNextTaskIfExists() {
        synchronized (this._taskQueue) {
            Runnable taskRunnable = (Runnable) this._taskQueue.poll();
            if (taskRunnable != null) {
                Cocos2dxHelper.getActivity().runOnUiThread(taskRunnable);
            } else {
                this._runningTaskCount--;
            }
        }
    }
}
