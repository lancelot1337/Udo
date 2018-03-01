package com.unity3d.ads.api;

import com.unity3d.ads.UnityAds;
import com.unity3d.ads.UnityAds.FinishState;
import com.unity3d.ads.UnityAds.UnityAdsError;
import com.unity3d.ads.misc.Utilities;
import com.unity3d.ads.webview.bridge.WebViewCallback;
import com.unity3d.ads.webview.bridge.WebViewExposed;

public class Listener {
    @WebViewExposed
    public static void sendReadyEvent(final String placementId, WebViewCallback callback) {
        if (UnityAds.getListener() != null) {
            Utilities.runOnUiThread(new Runnable() {
                public void run() {
                    UnityAds.getListener().onUnityAdsReady(placementId);
                }
            });
        }
        callback.invoke(new Object[0]);
    }

    @WebViewExposed
    public static void sendStartEvent(final String placementId, WebViewCallback callback) {
        if (UnityAds.getListener() != null) {
            Utilities.runOnUiThread(new Runnable() {
                public void run() {
                    UnityAds.getListener().onUnityAdsStart(placementId);
                }
            });
        }
        callback.invoke(new Object[0]);
    }

    @WebViewExposed
    public static void sendFinishEvent(final String placementId, final String result, WebViewCallback callback) {
        if (UnityAds.getListener() != null) {
            Utilities.runOnUiThread(new Runnable() {
                public void run() {
                    UnityAds.getListener().onUnityAdsFinish(placementId, FinishState.valueOf(result));
                }
            });
        }
        callback.invoke(new Object[0]);
    }

    @WebViewExposed
    public static void sendErrorEvent(final String error, final String message, WebViewCallback callback) {
        if (UnityAds.getListener() != null) {
            Utilities.runOnUiThread(new Runnable() {
                public void run() {
                    UnityAds.getListener().onUnityAdsError(UnityAdsError.valueOf(error), message);
                }
            });
        }
        callback.invoke(new Object[0]);
    }
}
