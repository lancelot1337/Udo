package org.cocos2dx.lib;

import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.SparseArray;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import java.lang.ref.WeakReference;
import org.cocos2dx.lib.Cocos2dxVideoView.OnVideoEventListener;

public class Cocos2dxVideoHelper {
    static final int KeyEventBack = 1000;
    private static final int VideoTaskCreate = 0;
    private static final int VideoTaskFullScreen = 12;
    private static final int VideoTaskKeepRatio = 11;
    private static final int VideoTaskPause = 5;
    private static final int VideoTaskRemove = 1;
    private static final int VideoTaskRestart = 10;
    private static final int VideoTaskResume = 6;
    private static final int VideoTaskSeek = 8;
    private static final int VideoTaskSetRect = 3;
    private static final int VideoTaskSetSource = 2;
    private static final int VideoTaskSetVisible = 9;
    private static final int VideoTaskStart = 4;
    private static final int VideoTaskStop = 7;
    static VideoHandler mVideoHandler = null;
    private static int videoTag = VideoTaskCreate;
    private Cocos2dxActivity mActivity = null;
    private FrameLayout mLayout = null;
    private SparseArray<Cocos2dxVideoView> sVideoViews = null;
    OnVideoEventListener videoEventListener = new OnVideoEventListener() {
        public void onVideoEvent(int tag, int event) {
            Cocos2dxVideoHelper.this.mActivity.runOnGLThread(new VideoEventRunnable(tag, event));
        }
    };

    private class VideoEventRunnable implements Runnable {
        private int mVideoEvent;
        private int mVideoTag;

        public VideoEventRunnable(int tag, int event) {
            this.mVideoTag = tag;
            this.mVideoEvent = event;
        }

        public void run() {
            Cocos2dxVideoHelper.nativeExecuteVideoCallback(this.mVideoTag, this.mVideoEvent);
        }
    }

    static class VideoHandler extends Handler {
        WeakReference<Cocos2dxVideoHelper> mReference;

        VideoHandler(Cocos2dxVideoHelper helper) {
            this.mReference = new WeakReference(helper);
        }

        public void handleMessage(Message msg) {
            Rect rect;
            Cocos2dxVideoHelper helper;
            switch (msg.what) {
                case Cocos2dxVideoHelper.VideoTaskCreate /*0*/:
                    ((Cocos2dxVideoHelper) this.mReference.get())._createVideoView(msg.arg1);
                    break;
                case Cocos2dxVideoHelper.VideoTaskRemove /*1*/:
                    ((Cocos2dxVideoHelper) this.mReference.get())._removeVideoView(msg.arg1);
                    break;
                case Cocos2dxVideoHelper.VideoTaskSetSource /*2*/:
                    ((Cocos2dxVideoHelper) this.mReference.get())._setVideoURL(msg.arg1, msg.arg2, (String) msg.obj);
                    break;
                case Cocos2dxVideoHelper.VideoTaskSetRect /*3*/:
                    rect = msg.obj;
                    ((Cocos2dxVideoHelper) this.mReference.get())._setVideoRect(msg.arg1, rect.left, rect.top, rect.right, rect.bottom);
                    break;
                case Cocos2dxVideoHelper.VideoTaskStart /*4*/:
                    ((Cocos2dxVideoHelper) this.mReference.get())._startVideo(msg.arg1);
                    break;
                case Cocos2dxVideoHelper.VideoTaskPause /*5*/:
                    ((Cocos2dxVideoHelper) this.mReference.get())._pauseVideo(msg.arg1);
                    break;
                case Cocos2dxVideoHelper.VideoTaskResume /*6*/:
                    ((Cocos2dxVideoHelper) this.mReference.get())._resumeVideo(msg.arg1);
                    break;
                case Cocos2dxVideoHelper.VideoTaskStop /*7*/:
                    ((Cocos2dxVideoHelper) this.mReference.get())._stopVideo(msg.arg1);
                    break;
                case Cocos2dxVideoHelper.VideoTaskSeek /*8*/:
                    ((Cocos2dxVideoHelper) this.mReference.get())._seekVideoTo(msg.arg1, msg.arg2);
                    break;
                case Cocos2dxVideoHelper.VideoTaskSetVisible /*9*/:
                    helper = (Cocos2dxVideoHelper) this.mReference.get();
                    if (msg.arg2 != Cocos2dxVideoHelper.VideoTaskRemove) {
                        helper._setVideoVisible(msg.arg1, false);
                        break;
                    } else {
                        helper._setVideoVisible(msg.arg1, true);
                        break;
                    }
                case Cocos2dxVideoHelper.VideoTaskRestart /*10*/:
                    ((Cocos2dxVideoHelper) this.mReference.get())._restartVideo(msg.arg1);
                    break;
                case Cocos2dxVideoHelper.VideoTaskKeepRatio /*11*/:
                    helper = (Cocos2dxVideoHelper) this.mReference.get();
                    if (msg.arg2 != Cocos2dxVideoHelper.VideoTaskRemove) {
                        helper._setVideoKeepRatio(msg.arg1, false);
                        break;
                    } else {
                        helper._setVideoKeepRatio(msg.arg1, true);
                        break;
                    }
                case Cocos2dxVideoHelper.VideoTaskFullScreen /*12*/:
                    helper = (Cocos2dxVideoHelper) this.mReference.get();
                    rect = (Rect) msg.obj;
                    if (msg.arg2 != Cocos2dxVideoHelper.VideoTaskRemove) {
                        helper._setFullScreenEnabled(msg.arg1, false, rect.right, rect.bottom);
                        break;
                    } else {
                        helper._setFullScreenEnabled(msg.arg1, true, rect.right, rect.bottom);
                        break;
                    }
                case Cocos2dxVideoHelper.KeyEventBack /*1000*/:
                    ((Cocos2dxVideoHelper) this.mReference.get()).onBackKeyEvent();
                    break;
            }
            super.handleMessage(msg);
        }
    }

    public static native void nativeExecuteVideoCallback(int i, int i2);

    Cocos2dxVideoHelper(Cocos2dxActivity activity, FrameLayout layout) {
        this.mActivity = activity;
        this.mLayout = layout;
        mVideoHandler = new VideoHandler(this);
        this.sVideoViews = new SparseArray();
    }

    public static int createVideoWidget() {
        Message msg = new Message();
        msg.what = VideoTaskCreate;
        msg.arg1 = videoTag;
        mVideoHandler.sendMessage(msg);
        int i = videoTag;
        videoTag = i + VideoTaskRemove;
        return i;
    }

    private void _createVideoView(int index) {
        Cocos2dxVideoView videoView = new Cocos2dxVideoView(this.mActivity, index);
        this.sVideoViews.put(index, videoView);
        this.mLayout.addView(videoView, new LayoutParams(-2, -2));
        videoView.setZOrderOnTop(true);
        videoView.setOnCompletionListener(this.videoEventListener);
    }

    public static void removeVideoWidget(int index) {
        Message msg = new Message();
        msg.what = VideoTaskRemove;
        msg.arg1 = index;
        mVideoHandler.sendMessage(msg);
    }

    private void _removeVideoView(int index) {
        Cocos2dxVideoView view = (Cocos2dxVideoView) this.sVideoViews.get(index);
        if (view != null) {
            view.stopPlayback();
            this.sVideoViews.remove(index);
            this.mLayout.removeView(view);
        }
    }

    public static void setVideoUrl(int index, int videoSource, String videoUrl) {
        Message msg = new Message();
        msg.what = VideoTaskSetSource;
        msg.arg1 = index;
        msg.arg2 = videoSource;
        msg.obj = videoUrl;
        mVideoHandler.sendMessage(msg);
    }

    private void _setVideoURL(int index, int videoSource, String videoUrl) {
        Cocos2dxVideoView videoView = (Cocos2dxVideoView) this.sVideoViews.get(index);
        if (videoView != null) {
            switch (videoSource) {
                case VideoTaskCreate /*0*/:
                    videoView.setVideoFileName(videoUrl);
                    return;
                case VideoTaskRemove /*1*/:
                    videoView.setVideoURL(videoUrl);
                    return;
                default:
                    return;
            }
        }
    }

    public static void setVideoRect(int index, int left, int top, int maxWidth, int maxHeight) {
        Message msg = new Message();
        msg.what = VideoTaskSetRect;
        msg.arg1 = index;
        msg.obj = new Rect(left, top, maxWidth, maxHeight);
        mVideoHandler.sendMessage(msg);
    }

    private void _setVideoRect(int index, int left, int top, int maxWidth, int maxHeight) {
        Cocos2dxVideoView videoView = (Cocos2dxVideoView) this.sVideoViews.get(index);
        if (videoView != null) {
            videoView.setVideoRect(left, top, maxWidth, maxHeight);
        }
    }

    public static void setFullScreenEnabled(int index, boolean enabled, int width, int height) {
        Message msg = new Message();
        msg.what = VideoTaskFullScreen;
        msg.arg1 = index;
        if (enabled) {
            msg.arg2 = VideoTaskRemove;
        } else {
            msg.arg2 = VideoTaskCreate;
        }
        msg.obj = new Rect(VideoTaskCreate, VideoTaskCreate, width, height);
        mVideoHandler.sendMessage(msg);
    }

    private void _setFullScreenEnabled(int index, boolean enabled, int width, int height) {
        Cocos2dxVideoView videoView = (Cocos2dxVideoView) this.sVideoViews.get(index);
        if (videoView != null) {
            videoView.setFullScreenEnabled(enabled, width, height);
        }
    }

    private void onBackKeyEvent() {
        int viewCount = this.sVideoViews.size();
        for (int i = VideoTaskCreate; i < viewCount; i += VideoTaskRemove) {
            int key = this.sVideoViews.keyAt(i);
            Cocos2dxVideoView videoView = (Cocos2dxVideoView) this.sVideoViews.get(key);
            if (videoView != null) {
                videoView.setFullScreenEnabled(false, VideoTaskCreate, VideoTaskCreate);
                this.mActivity.runOnGLThread(new VideoEventRunnable(key, KeyEventBack));
            }
        }
    }

    public static void startVideo(int index) {
        Message msg = new Message();
        msg.what = VideoTaskStart;
        msg.arg1 = index;
        mVideoHandler.sendMessage(msg);
    }

    private void _startVideo(int index) {
        Cocos2dxVideoView videoView = (Cocos2dxVideoView) this.sVideoViews.get(index);
        if (videoView != null) {
            videoView.start();
        }
    }

    public static void pauseVideo(int index) {
        Message msg = new Message();
        msg.what = VideoTaskPause;
        msg.arg1 = index;
        mVideoHandler.sendMessage(msg);
    }

    private void _pauseVideo(int index) {
        Cocos2dxVideoView videoView = (Cocos2dxVideoView) this.sVideoViews.get(index);
        if (videoView != null) {
            videoView.pause();
        }
    }

    public static void resumeVideo(int index) {
        Message msg = new Message();
        msg.what = VideoTaskResume;
        msg.arg1 = index;
        mVideoHandler.sendMessage(msg);
    }

    private void _resumeVideo(int index) {
        Cocos2dxVideoView videoView = (Cocos2dxVideoView) this.sVideoViews.get(index);
        if (videoView != null) {
            videoView.resume();
        }
    }

    public static void stopVideo(int index) {
        Message msg = new Message();
        msg.what = VideoTaskStop;
        msg.arg1 = index;
        mVideoHandler.sendMessage(msg);
    }

    private void _stopVideo(int index) {
        Cocos2dxVideoView videoView = (Cocos2dxVideoView) this.sVideoViews.get(index);
        if (videoView != null) {
            videoView.stop();
        }
    }

    public static void restartVideo(int index) {
        Message msg = new Message();
        msg.what = VideoTaskRestart;
        msg.arg1 = index;
        mVideoHandler.sendMessage(msg);
    }

    private void _restartVideo(int index) {
        Cocos2dxVideoView videoView = (Cocos2dxVideoView) this.sVideoViews.get(index);
        if (videoView != null) {
            videoView.restart();
        }
    }

    public static void seekVideoTo(int index, int msec) {
        Message msg = new Message();
        msg.what = VideoTaskSeek;
        msg.arg1 = index;
        msg.arg2 = msec;
        mVideoHandler.sendMessage(msg);
    }

    private void _seekVideoTo(int index, int msec) {
        Cocos2dxVideoView videoView = (Cocos2dxVideoView) this.sVideoViews.get(index);
        if (videoView != null) {
            videoView.seekTo(msec);
        }
    }

    public static void setVideoVisible(int index, boolean visible) {
        Message msg = new Message();
        msg.what = VideoTaskSetVisible;
        msg.arg1 = index;
        if (visible) {
            msg.arg2 = VideoTaskRemove;
        } else {
            msg.arg2 = VideoTaskCreate;
        }
        mVideoHandler.sendMessage(msg);
    }

    private void _setVideoVisible(int index, boolean visible) {
        Cocos2dxVideoView videoView = (Cocos2dxVideoView) this.sVideoViews.get(index);
        if (videoView == null) {
            return;
        }
        if (visible) {
            videoView.fixSize();
            videoView.setVisibility(VideoTaskCreate);
            return;
        }
        videoView.setVisibility(VideoTaskStart);
    }

    public static void setVideoKeepRatioEnabled(int index, boolean enable) {
        Message msg = new Message();
        msg.what = VideoTaskKeepRatio;
        msg.arg1 = index;
        if (enable) {
            msg.arg2 = VideoTaskRemove;
        } else {
            msg.arg2 = VideoTaskCreate;
        }
        mVideoHandler.sendMessage(msg);
    }

    private void _setVideoKeepRatio(int index, boolean enable) {
        Cocos2dxVideoView videoView = (Cocos2dxVideoView) this.sVideoViews.get(index);
        if (videoView != null) {
            videoView.setKeepRatio(enable);
        }
    }
}
