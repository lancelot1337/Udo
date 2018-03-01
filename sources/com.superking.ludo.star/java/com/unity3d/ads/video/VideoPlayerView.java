package com.unity3d.ads.video;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Build.VERSION;
import android.widget.VideoView;
import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.webview.WebViewApp;
import com.unity3d.ads.webview.WebViewEventCategory;
import cz.msebera.android.httpclient.HttpStatus;
import java.util.Timer;
import java.util.TimerTask;

public class VideoPlayerView extends VideoView {
    private boolean _infoListenerEnabled = true;
    private MediaPlayer _mediaPlayer = null;
    private int _progressEventInterval = HttpStatus.SC_INTERNAL_SERVER_ERROR;
    private Timer _videoTimer;
    private String _videoUrl;
    private Float _volume = null;

    public VideoPlayerView(Context context) {
        super(context);
    }

    private void startVideoProgressTimer() {
        this._videoTimer = new Timer();
        this._videoTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                boolean isPlaying = false;
                try {
                    isPlaying = VideoPlayerView.this.isPlaying();
                    WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.VIDEOPLAYER, VideoPlayerEvent.PROGRESS, Integer.valueOf(VideoPlayerView.this.getCurrentPosition()));
                } catch (IllegalStateException e) {
                    DeviceLog.exception("Exception while sending current position to webapp", e);
                    WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.VIDEOPLAYER, VideoPlayerEvent.ILLEGAL_STATE, VideoPlayerEvent.PROGRESS, Boolean.valueOf(isPlaying));
                }
            }
        }, (long) this._progressEventInterval, (long) this._progressEventInterval);
    }

    public void stopVideoProgressTimer() {
        if (this._videoTimer != null) {
            this._videoTimer.cancel();
            this._videoTimer.purge();
            this._videoTimer = null;
        }
    }

    public boolean prepare(String url, final Float initialVolume) {
        DeviceLog.entered();
        this._videoUrl = url;
        setOnPreparedListener(new OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                if (mp != null) {
                    VideoPlayerView.this._mediaPlayer = mp;
                }
                VideoPlayerView.this.setVolume(initialVolume);
                WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.VIDEOPLAYER, VideoPlayerEvent.PREPARED, Integer.valueOf(mp.getDuration()), Integer.valueOf(mp.getVideoWidth()), Integer.valueOf(mp.getVideoHeight()), VideoPlayerView.this._videoUrl);
            }
        });
        setOnErrorListener(new OnErrorListener() {
            public boolean onError(MediaPlayer mp, int what, int extra) {
                if (mp != null) {
                    VideoPlayerView.this._mediaPlayer = mp;
                }
                WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.VIDEOPLAYER, VideoPlayerEvent.GENERIC_ERROR, Integer.valueOf(what), Integer.valueOf(extra), VideoPlayerView.this._videoUrl);
                VideoPlayerView.this.stopVideoProgressTimer();
                return true;
            }
        });
        setInfoListenerEnabled(this._infoListenerEnabled);
        try {
            setVideoPath(this._videoUrl);
            return true;
        } catch (Exception e) {
            WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.VIDEOPLAYER, VideoPlayerEvent.PREPARE_ERROR, this._videoUrl);
            DeviceLog.exception("Error preparing video: " + this._videoUrl, e);
            return false;
        }
    }

    public void play() {
        DeviceLog.entered();
        setOnCompletionListener(new OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                if (mp != null) {
                    VideoPlayerView.this._mediaPlayer = mp;
                }
                WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.VIDEOPLAYER, VideoPlayerEvent.COMPLETED, VideoPlayerView.this._videoUrl);
                VideoPlayerView.this.stopVideoProgressTimer();
            }
        });
        start();
        stopVideoProgressTimer();
        startVideoProgressTimer();
        WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.VIDEOPLAYER, VideoPlayerEvent.PLAY, this._videoUrl);
    }

    public void setInfoListenerEnabled(boolean enabled) {
        this._infoListenerEnabled = enabled;
        if (VERSION.SDK_INT <= 16) {
            return;
        }
        if (this._infoListenerEnabled) {
            setOnInfoListener(new OnInfoListener() {
                public boolean onInfo(MediaPlayer mp, int what, int extra) {
                    WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.VIDEOPLAYER, VideoPlayerEvent.INFO, Integer.valueOf(what), Integer.valueOf(extra), VideoPlayerView.this._videoUrl);
                    return true;
                }
            });
        } else {
            setOnInfoListener(null);
        }
    }

    public void pause() {
        try {
            super.pause();
            stopVideoProgressTimer();
            WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.VIDEOPLAYER, VideoPlayerEvent.PAUSE, this._videoUrl);
        } catch (Exception e) {
            WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.VIDEOPLAYER, VideoPlayerEvent.PAUSE_ERROR, this._videoUrl);
            DeviceLog.exception("Error pausing video", e);
        }
    }

    public void seekTo(int msec) {
        try {
            super.seekTo(msec);
            WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.VIDEOPLAYER, VideoPlayerEvent.SEEKTO, this._videoUrl);
        } catch (Exception e) {
            WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.VIDEOPLAYER, VideoPlayerEvent.SEEKTO_ERROR, this._videoUrl);
            DeviceLog.exception("Error seeking video", e);
        }
    }

    public void stop() {
        stopPlayback();
        stopVideoProgressTimer();
        WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.VIDEOPLAYER, VideoPlayerEvent.STOP, this._videoUrl);
    }

    public float getVolume() {
        return this._volume.floatValue();
    }

    public void setVolume(Float volume) {
        try {
            this._mediaPlayer.setVolume(volume.floatValue(), volume.floatValue());
            this._volume = volume;
        } catch (Exception e) {
            DeviceLog.exception("MediaPlayer generic error", e);
        }
    }

    public void setProgressEventInterval(int ms) {
        this._progressEventInterval = ms;
        if (this._videoTimer != null) {
            stopVideoProgressTimer();
            startVideoProgressTimer();
        }
    }

    public int getProgressEventInterval() {
        return this._progressEventInterval;
    }
}
