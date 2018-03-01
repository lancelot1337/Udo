package com.unity3d.ads.request;

import android.os.Bundle;
import android.os.ConditionVariable;
import android.os.Looper;
import android.os.Message;
import com.ironsource.sdk.utils.Constants.ParametersKeys;
import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.request.WebRequest.RequestType;
import cz.msebera.android.httpclient.HttpHeaders;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

public class WebRequestThread extends Thread {
    protected static final int MSG_REQUEST = 1;
    private static WebRequestHandler _handler;
    private static boolean _ready = false;
    private static final Object _readyLock = new Object();

    private static void init() {
        WebRequestThread thread = new WebRequestThread();
        thread.setName("UnityAdsWebRequestThread");
        thread.start();
        while (!_ready) {
            try {
                synchronized (_readyLock) {
                    _readyLock.wait();
                }
            } catch (InterruptedException e) {
                DeviceLog.debug("Couldn't synchronize thread");
            }
        }
    }

    public void run() {
        Looper.prepare();
        if (_handler == null) {
            _handler = new WebRequestHandler();
        }
        _ready = true;
        synchronized (_readyLock) {
            _readyLock.notify();
        }
        Looper.loop();
    }

    public static synchronized void request(String url, RequestType requestType, Map<String, List<String>> headers, Integer connectTimeout, Integer readTimeout, IWebRequestListener listener) {
        synchronized (WebRequestThread.class) {
            request(url, requestType, headers, null, connectTimeout, readTimeout, listener);
        }
    }

    public static synchronized void request(String url, RequestType requestType, Map<String, List<String>> headers, String requestBody, Integer connectTimeout, Integer readTimeout, IWebRequestListener listener) {
        synchronized (WebRequestThread.class) {
            request(MSG_REQUEST, url, requestType, headers, requestBody, connectTimeout, readTimeout, listener, new WebRequestResultReceiver(_handler, listener));
        }
    }

    public static synchronized void request(int msgWhat, String url, RequestType requestType, Map<String, List<String>> headers, String requestBody, Integer connectTimeout, Integer readTimeout, IWebRequestListener listener, WebRequestResultReceiver receiver) {
        synchronized (WebRequestThread.class) {
            if (!_ready) {
                init();
            }
            if (url == null || url.length() < 3) {
                listener.onFailed(url, "Request is NULL or too short");
            } else {
                Bundle params = new Bundle();
                params.putString(ParametersKeys.URL, url);
                params.putString(EventEntry.COLUMN_NAME_TYPE, requestType.name());
                params.putString("body", requestBody);
                params.putParcelable("receiver", receiver);
                params.putInt("connectTimeout", connectTimeout.intValue());
                params.putInt("readTimeout", readTimeout.intValue());
                if (headers != null) {
                    for (String s : headers.keySet()) {
                        params.putStringArray(s, (String[]) ((List) headers.get(s)).toArray(new String[((List) headers.get(s)).size()]));
                    }
                }
                Message msg = new Message();
                msg.what = msgWhat;
                msg.setData(params);
                _handler.sendMessage(msg);
            }
        }
    }

    public static synchronized boolean resolve(final String host, final IResolveHostListener listener) {
        boolean z;
        synchronized (WebRequestThread.class) {
            if (host != null) {
                if (host.length() >= 3) {
                    new Thread(new Runnable() {
                        public void run() {
                            Exception e;
                            final ConditionVariable cv = new ConditionVariable();
                            Thread t = null;
                            try {
                                Thread t2 = new Thread(new Runnable() {
                                    public void run() {
                                        try {
                                            listener.onResolve(host, InetAddress.getByName(host).getHostAddress());
                                        } catch (UnknownHostException e) {
                                            DeviceLog.exception("Unknown host", e);
                                            listener.onFailed(host, ResolveHostError.UNKNOWN_HOST, e.getMessage());
                                        }
                                        cv.open();
                                    }
                                });
                                try {
                                    t2.start();
                                    t = t2;
                                } catch (Exception e2) {
                                    e = e2;
                                    t = t2;
                                    DeviceLog.exception("Exception while resolving host", e);
                                    listener.onFailed(host, ResolveHostError.UNEXPECTED_EXCEPTION, e.getMessage());
                                    if (!cv.block(20000)) {
                                    }
                                    return;
                                }
                            } catch (Exception e3) {
                                e = e3;
                                DeviceLog.exception("Exception while resolving host", e);
                                listener.onFailed(host, ResolveHostError.UNEXPECTED_EXCEPTION, e.getMessage());
                                if (!cv.block(20000)) {
                                    return;
                                }
                            }
                            if (!cv.block(20000) && t != null) {
                                t.interrupt();
                                listener.onFailed(host, ResolveHostError.TIMEOUT, HttpHeaders.TIMEOUT);
                            }
                        }
                    }).start();
                    z = true;
                }
            }
            listener.onFailed(host, ResolveHostError.INVALID_HOST, "Host is NULL");
            z = false;
        }
        return z;
    }
}
