package com.unity3d.ads.cache;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import com.google.firebase.analytics.FirebaseAnalytics.Param;
import com.unity3d.ads.api.Request;
import com.unity3d.ads.device.Device;
import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.request.IWebRequestProgressListener;
import com.unity3d.ads.request.WebRequest;
import com.unity3d.ads.webview.WebViewApp;
import com.unity3d.ads.webview.WebViewEventCategory;
import cz.msebera.android.httpclient.client.cache.HeaderConstants;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cocos2dx.lib.Cocos2dxHandler;

class CacheThreadHandler extends Handler {
    private boolean _active = false;
    private boolean _canceled = false;
    private WebRequest _currentRequest = null;

    CacheThreadHandler() {
    }

    public void handleMessage(Message msg) {
        Bundle data = msg.getData();
        String source = data.getString(Param.SOURCE);
        String target = data.getString("target");
        int connectTimeout = data.getInt("connectTimeout");
        int readTimeout = data.getInt("readTimeout");
        int progressInterval = data.getInt("progressInterval");
        switch (msg.what) {
            case Cocos2dxHandler.HANDLER_SHOW_DIALOG /*1*/:
                downloadFile(source, target, new File(target).length(), connectTimeout, readTimeout, progressInterval);
                return;
            default:
                return;
        }
    }

    public void setCancelStatus(boolean canceled) {
        this._canceled = canceled;
        if (canceled && this._currentRequest != null) {
            this._active = false;
            this._currentRequest.cancel();
        }
    }

    public boolean isActive() {
        return this._active;
    }

    private void downloadFile(String source, String target, long position, int connectTimeout, int readTimeout, int progressInterval) {
        Exception e;
        OutputStream outputStream;
        Throwable th;
        if (!this._canceled && source != null && target != null) {
            if (position > 0) {
                DeviceLog.debug("Unity Ads cache: resuming download " + source + " to " + target + " at " + position + " bytes");
            } else {
                DeviceLog.debug("Unity Ads cache: start downloading " + source + " to " + target);
            }
            if (Device.isActiveNetworkConnected()) {
                this._active = true;
                long startTime = SystemClock.elapsedRealtime();
                File file = new File(target);
                FileOutputStream fileOutput = null;
                try {
                    OutputStream fileOutputStream = new FileOutputStream(file, position > 0);
                    try {
                        this._currentRequest = getWebRequest(source, position, connectTimeout, readTimeout);
                        final long j = position;
                        final int i = progressInterval;
                        this._currentRequest.setProgressListener(new IWebRequestProgressListener() {
                            private long lastProgressEventTime = System.currentTimeMillis();

                            public void onRequestStart(String url, long total, int responseCode, Map<String, List<String>> headers) {
                                WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.CACHE, CacheEvent.DOWNLOAD_STARTED, url, Long.valueOf(j), Long.valueOf(total), Integer.valueOf(responseCode), Request.getResponseHeadersMap(headers));
                            }

                            public void onRequestProgress(String url, long bytes, long total) {
                                if (i > 0 && System.currentTimeMillis() - this.lastProgressEventTime > ((long) i)) {
                                    this.lastProgressEventTime = System.currentTimeMillis();
                                    WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.CACHE, CacheEvent.DOWNLOAD_PROGRESS, url, Long.valueOf(bytes), Long.valueOf(total));
                                }
                            }
                        });
                        long total = this._currentRequest.makeStreamRequest(fileOutputStream);
                        this._active = false;
                        postProcessDownload(startTime, source, file, total, this._currentRequest.getContentLength(), this._currentRequest.isCanceled(), this._currentRequest.getResponseCode(), this._currentRequest.getResponseHeaders());
                        this._currentRequest = null;
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (Exception e2) {
                                DeviceLog.exception("Error closing stream", e2);
                                WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.CACHE, CacheEvent.DOWNLOAD_ERROR, CacheError.FILE_IO_ERROR, source, e2.getMessage());
                                outputStream = fileOutputStream;
                                return;
                            }
                        }
                        outputStream = fileOutputStream;
                        return;
                    } catch (FileNotFoundException e3) {
                        e2 = e3;
                        outputStream = fileOutputStream;
                        try {
                            DeviceLog.exception("Couldn't create target file", e2);
                            this._active = false;
                            WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.CACHE, CacheEvent.DOWNLOAD_ERROR, CacheError.FILE_IO_ERROR, source, e2.getMessage());
                            this._currentRequest = null;
                            if (fileOutput != null) {
                                try {
                                    fileOutput.close();
                                    return;
                                } catch (Exception e22) {
                                    DeviceLog.exception("Error closing stream", e22);
                                    WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.CACHE, CacheEvent.DOWNLOAD_ERROR, CacheError.FILE_IO_ERROR, source, e22.getMessage());
                                    return;
                                }
                            }
                            return;
                        } catch (Throwable th2) {
                            th = th2;
                            this._currentRequest = null;
                            if (fileOutput != null) {
                                try {
                                    fileOutput.close();
                                } catch (Exception e222) {
                                    DeviceLog.exception("Error closing stream", e222);
                                    WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.CACHE, CacheEvent.DOWNLOAD_ERROR, CacheError.FILE_IO_ERROR, source, e222.getMessage());
                                }
                            }
                            throw th;
                        }
                    } catch (MalformedURLException e4) {
                        e222 = e4;
                        outputStream = fileOutputStream;
                        DeviceLog.exception("Malformed URL", e222);
                        this._active = false;
                        WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.CACHE, CacheEvent.DOWNLOAD_ERROR, CacheError.MALFORMED_URL, source, e222.getMessage());
                        this._currentRequest = null;
                        if (fileOutput != null) {
                            try {
                                fileOutput.close();
                                return;
                            } catch (Exception e2222) {
                                DeviceLog.exception("Error closing stream", e2222);
                                WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.CACHE, CacheEvent.DOWNLOAD_ERROR, CacheError.FILE_IO_ERROR, source, e2222.getMessage());
                                return;
                            }
                        }
                        return;
                    } catch (IOException e5) {
                        e2222 = e5;
                        outputStream = fileOutputStream;
                        DeviceLog.exception("Couldn't request stream", e2222);
                        this._active = false;
                        WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.CACHE, CacheEvent.DOWNLOAD_ERROR, CacheError.FILE_IO_ERROR, source, e2222.getMessage());
                        this._currentRequest = null;
                        if (fileOutput != null) {
                            try {
                                fileOutput.close();
                                return;
                            } catch (Exception e22222) {
                                DeviceLog.exception("Error closing stream", e22222);
                                WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.CACHE, CacheEvent.DOWNLOAD_ERROR, CacheError.FILE_IO_ERROR, source, e22222.getMessage());
                                return;
                            }
                        }
                        return;
                    } catch (Throwable th3) {
                        th = th3;
                        outputStream = fileOutputStream;
                        this._currentRequest = null;
                        if (fileOutput != null) {
                            fileOutput.close();
                        }
                        throw th;
                    }
                } catch (FileNotFoundException e6) {
                    e22222 = e6;
                    DeviceLog.exception("Couldn't create target file", e22222);
                    this._active = false;
                    WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.CACHE, CacheEvent.DOWNLOAD_ERROR, CacheError.FILE_IO_ERROR, source, e22222.getMessage());
                    this._currentRequest = null;
                    if (fileOutput != null) {
                        fileOutput.close();
                        return;
                    }
                    return;
                } catch (MalformedURLException e7) {
                    e22222 = e7;
                    DeviceLog.exception("Malformed URL", e22222);
                    this._active = false;
                    WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.CACHE, CacheEvent.DOWNLOAD_ERROR, CacheError.MALFORMED_URL, source, e22222.getMessage());
                    this._currentRequest = null;
                    if (fileOutput != null) {
                        fileOutput.close();
                        return;
                    }
                    return;
                } catch (IOException e8) {
                    e22222 = e8;
                    DeviceLog.exception("Couldn't request stream", e22222);
                    this._active = false;
                    WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.CACHE, CacheEvent.DOWNLOAD_ERROR, CacheError.FILE_IO_ERROR, source, e22222.getMessage());
                    this._currentRequest = null;
                    if (fileOutput != null) {
                        fileOutput.close();
                        return;
                    }
                    return;
                }
            }
            DeviceLog.debug("Unity Ads cache: download cancelled, no internet connection available");
            WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.CACHE, CacheEvent.DOWNLOAD_ERROR, CacheError.NO_INTERNET, source);
        }
    }

    private void postProcessDownload(long startTime, String source, File targetFile, long byteCount, long totalBytes, boolean canceled, int responseCode, Map<String, List<String>> responseHeaders) {
        long duration = SystemClock.elapsedRealtime() - startTime;
        if (!targetFile.setReadable(true, false)) {
            DeviceLog.debug("Unity Ads cache: could not set file readable!");
        }
        if (canceled) {
            DeviceLog.debug("Unity Ads cache: downloading of " + source + " stopped");
            WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.CACHE, CacheEvent.DOWNLOAD_STOPPED, source, Long.valueOf(byteCount), Long.valueOf(totalBytes), Long.valueOf(duration), Integer.valueOf(responseCode), Request.getResponseHeadersMap(responseHeaders));
            return;
        }
        DeviceLog.debug("Unity Ads cache: File " + targetFile.getName() + " of " + byteCount + " bytes downloaded in " + duration + "ms");
        WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.CACHE, CacheEvent.DOWNLOAD_END, source, Long.valueOf(byteCount), Long.valueOf(totalBytes), Long.valueOf(duration), Integer.valueOf(responseCode), Request.getResponseHeadersMap(responseHeaders));
    }

    private WebRequest getWebRequest(String source, long position, int connectTimeout, int readTimeout) throws MalformedURLException {
        HashMap<String, List<String>> headers = new HashMap();
        if (position > 0) {
            headers.put(HeaderConstants.RANGE, new ArrayList(Arrays.asList(new String[]{"bytes=" + position + "-"})));
        }
        return new WebRequest(source, HttpGet.METHOD_NAME, headers, connectTimeout, readTimeout);
    }
}
