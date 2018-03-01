package org.cocos2dx.lib;

import android.os.Handler;
import android.os.Looper;
import android.util.SparseArray;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class Cocos2dxWebViewHelper {
    private static final String TAG = Cocos2dxWebViewHelper.class.getSimpleName();
    private static Cocos2dxActivity sCocos2dxActivity;
    private static Handler sHandler;
    private static FrameLayout sLayout;
    private static int viewTag = 0;
    private static SparseArray<Cocos2dxWebView> webViews;

    private static native void didFailLoading(int i, String str);

    private static native void didFinishLoading(int i, String str);

    private static native void onJsCallback(int i, String str);

    private static native boolean shouldStartLoading(int i, String str);

    public Cocos2dxWebViewHelper(FrameLayout layout) {
        sLayout = layout;
        sHandler = new Handler(Looper.myLooper());
        sCocos2dxActivity = (Cocos2dxActivity) Cocos2dxActivity.getContext();
        webViews = new SparseArray();
    }

    public static boolean _shouldStartLoading(int index, String message) {
        return !shouldStartLoading(index, message);
    }

    public static void _didFinishLoading(int index, String message) {
        didFinishLoading(index, message);
    }

    public static void _didFailLoading(int index, String message) {
        didFailLoading(index, message);
    }

    public static void _onJsCallback(int index, String message) {
        onJsCallback(index, message);
    }

    public static int createWebView() {
        final int index = viewTag;
        sCocos2dxActivity.runOnUiThread(new Runnable() {
            public void run() {
                Cocos2dxWebView webView = new Cocos2dxWebView(Cocos2dxWebViewHelper.sCocos2dxActivity, index);
                Cocos2dxWebViewHelper.sLayout.addView(webView, new LayoutParams(-2, -2));
                Cocos2dxWebViewHelper.webViews.put(index, webView);
            }
        });
        int i = viewTag;
        viewTag = i + 1;
        return i;
    }

    public static void removeWebView(final int index) {
        sCocos2dxActivity.runOnUiThread(new Runnable() {
            public void run() {
                Cocos2dxWebView webView = (Cocos2dxWebView) Cocos2dxWebViewHelper.webViews.get(index);
                if (webView != null) {
                    Cocos2dxWebViewHelper.webViews.remove(index);
                    Cocos2dxWebViewHelper.sLayout.removeView(webView);
                }
            }
        });
    }

    public static void setVisible(final int index, final boolean visible) {
        sCocos2dxActivity.runOnUiThread(new Runnable() {
            public void run() {
                Cocos2dxWebView webView = (Cocos2dxWebView) Cocos2dxWebViewHelper.webViews.get(index);
                if (webView != null) {
                    webView.setVisibility(visible ? 0 : 8);
                }
            }
        });
    }

    public static void setWebViewRect(int index, int left, int top, int maxWidth, int maxHeight) {
        final int i = index;
        final int i2 = left;
        final int i3 = top;
        final int i4 = maxWidth;
        final int i5 = maxHeight;
        sCocos2dxActivity.runOnUiThread(new Runnable() {
            public void run() {
                Cocos2dxWebView webView = (Cocos2dxWebView) Cocos2dxWebViewHelper.webViews.get(i);
                if (webView != null) {
                    webView.setWebViewRect(i2, i3, i4, i5);
                }
            }
        });
    }

    public static void setJavascriptInterfaceScheme(final int index, final String scheme) {
        sCocos2dxActivity.runOnUiThread(new Runnable() {
            public void run() {
                Cocos2dxWebView webView = (Cocos2dxWebView) Cocos2dxWebViewHelper.webViews.get(index);
                if (webView != null) {
                    webView.setJavascriptInterfaceScheme(scheme);
                }
            }
        });
    }

    public static void loadData(int index, String data, String mimeType, String encoding, String baseURL) {
        final int i = index;
        final String str = baseURL;
        final String str2 = data;
        final String str3 = mimeType;
        final String str4 = encoding;
        sCocos2dxActivity.runOnUiThread(new Runnable() {
            public void run() {
                Cocos2dxWebView webView = (Cocos2dxWebView) Cocos2dxWebViewHelper.webViews.get(i);
                if (webView != null) {
                    webView.loadDataWithBaseURL(str, str2, str3, str4, null);
                }
            }
        });
    }

    public static void loadHTMLString(final int index, final String data, final String baseUrl) {
        sCocos2dxActivity.runOnUiThread(new Runnable() {
            public void run() {
                Cocos2dxWebView webView = (Cocos2dxWebView) Cocos2dxWebViewHelper.webViews.get(index);
                if (webView != null) {
                    webView.loadDataWithBaseURL(baseUrl, data, null, null, null);
                }
            }
        });
    }

    public static void loadUrl(final int index, final String url) {
        sCocos2dxActivity.runOnUiThread(new Runnable() {
            public void run() {
                Cocos2dxWebView webView = (Cocos2dxWebView) Cocos2dxWebViewHelper.webViews.get(index);
                if (webView != null) {
                    webView.loadUrl(url);
                }
            }
        });
    }

    public static void loadFile(final int index, final String filePath) {
        sCocos2dxActivity.runOnUiThread(new Runnable() {
            public void run() {
                Cocos2dxWebView webView = (Cocos2dxWebView) Cocos2dxWebViewHelper.webViews.get(index);
                if (webView != null) {
                    webView.loadUrl(filePath);
                }
            }
        });
    }

    public static void stopLoading(final int index) {
        sCocos2dxActivity.runOnUiThread(new Runnable() {
            public void run() {
                Cocos2dxWebView webView = (Cocos2dxWebView) Cocos2dxWebViewHelper.webViews.get(index);
                if (webView != null) {
                    webView.stopLoading();
                }
            }
        });
    }

    public static void reload(final int index) {
        sCocos2dxActivity.runOnUiThread(new Runnable() {
            public void run() {
                Cocos2dxWebView webView = (Cocos2dxWebView) Cocos2dxWebViewHelper.webViews.get(index);
                if (webView != null) {
                    webView.reload();
                }
            }
        });
    }

    public static <T> T callInMainThread(Callable<T> call) throws ExecutionException, InterruptedException {
        FutureTask<T> task = new FutureTask(call);
        sHandler.post(task);
        return task.get();
    }

    public static boolean canGoBack(final int index) {
        try {
            return ((Boolean) callInMainThread(new Callable<Boolean>() {
                public Boolean call() throws Exception {
                    Cocos2dxWebView webView = (Cocos2dxWebView) Cocos2dxWebViewHelper.webViews.get(index);
                    boolean z = webView != null && webView.canGoBack();
                    return Boolean.valueOf(z);
                }
            })).booleanValue();
        } catch (ExecutionException e) {
            return false;
        } catch (InterruptedException e2) {
            return false;
        }
    }

    public static boolean canGoForward(final int index) {
        try {
            return ((Boolean) callInMainThread(new Callable<Boolean>() {
                public Boolean call() throws Exception {
                    Cocos2dxWebView webView = (Cocos2dxWebView) Cocos2dxWebViewHelper.webViews.get(index);
                    boolean z = webView != null && webView.canGoForward();
                    return Boolean.valueOf(z);
                }
            })).booleanValue();
        } catch (ExecutionException e) {
            return false;
        } catch (InterruptedException e2) {
            return false;
        }
    }

    public static void goBack(final int index) {
        sCocos2dxActivity.runOnUiThread(new Runnable() {
            public void run() {
                Cocos2dxWebView webView = (Cocos2dxWebView) Cocos2dxWebViewHelper.webViews.get(index);
                if (webView != null) {
                    webView.goBack();
                }
            }
        });
    }

    public static void goForward(final int index) {
        sCocos2dxActivity.runOnUiThread(new Runnable() {
            public void run() {
                Cocos2dxWebView webView = (Cocos2dxWebView) Cocos2dxWebViewHelper.webViews.get(index);
                if (webView != null) {
                    webView.goForward();
                }
            }
        });
    }

    public static void evaluateJS(final int index, final String js) {
        sCocos2dxActivity.runOnUiThread(new Runnable() {
            public void run() {
                Cocos2dxWebView webView = (Cocos2dxWebView) Cocos2dxWebViewHelper.webViews.get(index);
                if (webView != null) {
                    webView.loadUrl("javascript:" + js);
                }
            }
        });
    }

    public static void setScalesPageToFit(final int index, final boolean scalesPageToFit) {
        sCocos2dxActivity.runOnUiThread(new Runnable() {
            public void run() {
                Cocos2dxWebView webView = (Cocos2dxWebView) Cocos2dxWebViewHelper.webViews.get(index);
                if (webView != null) {
                    webView.setScalesPageToFit(scalesPageToFit);
                }
            }
        });
    }
}
