package org.cocos2dx.lib;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.net.Uri;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout.LayoutParams;
import android.widget.MediaController.MediaPlayerControl;
import cz.msebera.android.httpclient.HttpStatus;
import java.io.IOException;
import java.util.Map;

public class Cocos2dxVideoView extends SurfaceView implements MediaPlayerControl {
    private static final String AssetResourceRoot = "assets/";
    private static final int EVENT_COMPLETED = 3;
    private static final int EVENT_PAUSED = 1;
    private static final int EVENT_PLAYING = 0;
    private static final int EVENT_STOPPED = 2;
    private static final int STATE_ERROR = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PAUSED = 4;
    private static final int STATE_PLAYBACK_COMPLETED = 5;
    private static final int STATE_PLAYING = 3;
    private static final int STATE_PREPARED = 2;
    private static final int STATE_PREPARING = 1;
    private String TAG = "Cocos2dxVideoView";
    private OnBufferingUpdateListener mBufferingUpdateListener = new OnBufferingUpdateListener() {
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            Cocos2dxVideoView.this.mCurrentBufferPercentage = percent;
        }
    };
    protected Cocos2dxActivity mCocos2dxActivity = null;
    private OnCompletionListener mCompletionListener = new OnCompletionListener() {
        public void onCompletion(MediaPlayer mp) {
            Cocos2dxVideoView.this.mCurrentState = Cocos2dxVideoView.STATE_PLAYBACK_COMPLETED;
            Cocos2dxVideoView.this.mTargetState = Cocos2dxVideoView.STATE_PLAYBACK_COMPLETED;
            Cocos2dxVideoView.this.release(true);
            if (Cocos2dxVideoView.this.mOnVideoEventListener != null) {
                Cocos2dxVideoView.this.mOnVideoEventListener.onVideoEvent(Cocos2dxVideoView.this.mViewTag, Cocos2dxVideoView.STATE_PLAYING);
            }
        }
    };
    private int mCurrentBufferPercentage;
    private int mCurrentState = STATE_IDLE;
    private int mDuration;
    private OnErrorListener mErrorListener = new OnErrorListener() {
        public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
            Log.d(Cocos2dxVideoView.this.TAG, "Error: " + framework_err + "," + impl_err);
            Cocos2dxVideoView.this.mCurrentState = Cocos2dxVideoView.STATE_ERROR;
            Cocos2dxVideoView.this.mTargetState = Cocos2dxVideoView.STATE_ERROR;
            if ((Cocos2dxVideoView.this.mOnErrorListener == null || !Cocos2dxVideoView.this.mOnErrorListener.onError(Cocos2dxVideoView.this.mMediaPlayer, framework_err, impl_err)) && Cocos2dxVideoView.this.getWindowToken() != null) {
                int messageId;
                Resources r = Cocos2dxVideoView.this.mCocos2dxActivity.getResources();
                if (framework_err == HttpStatus.SC_OK) {
                    messageId = r.getIdentifier("VideoView_error_text_invalid_progressive_playback", "string", "android");
                } else {
                    messageId = r.getIdentifier("VideoView_error_text_unknown", "string", "android");
                }
                new Builder(Cocos2dxVideoView.this.mCocos2dxActivity).setTitle(r.getString(r.getIdentifier("VideoView_error_title", "string", "android"))).setMessage(messageId).setPositiveButton(r.getString(r.getIdentifier("VideoView_error_button", "string", "android")), new OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (Cocos2dxVideoView.this.mOnVideoEventListener != null) {
                            Cocos2dxVideoView.this.mOnVideoEventListener.onVideoEvent(Cocos2dxVideoView.this.mViewTag, Cocos2dxVideoView.STATE_PLAYING);
                        }
                    }
                }).setCancelable(false).show();
            }
            return true;
        }
    };
    protected boolean mFullScreenEnabled = false;
    protected int mFullScreenHeight = STATE_IDLE;
    protected int mFullScreenWidth = STATE_IDLE;
    private boolean mIsAssetRouse = false;
    private boolean mKeepRatio = false;
    private MediaPlayer mMediaPlayer = null;
    private boolean mNeedResume = false;
    private OnErrorListener mOnErrorListener;
    private OnPreparedListener mOnPreparedListener;
    private OnVideoEventListener mOnVideoEventListener;
    OnPreparedListener mPreparedListener = new OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
            Cocos2dxVideoView.this.mCurrentState = Cocos2dxVideoView.STATE_PREPARED;
            if (Cocos2dxVideoView.this.mOnPreparedListener != null) {
                Cocos2dxVideoView.this.mOnPreparedListener.onPrepared(Cocos2dxVideoView.this.mMediaPlayer);
            }
            Cocos2dxVideoView.this.mVideoWidth = mp.getVideoWidth();
            Cocos2dxVideoView.this.mVideoHeight = mp.getVideoHeight();
            int seekToPosition = Cocos2dxVideoView.this.mSeekWhenPrepared;
            if (seekToPosition != 0) {
                Cocos2dxVideoView.this.seekTo(seekToPosition);
            }
            if (!(Cocos2dxVideoView.this.mVideoWidth == 0 || Cocos2dxVideoView.this.mVideoHeight == 0)) {
                Cocos2dxVideoView.this.fixSize();
            }
            if (Cocos2dxVideoView.this.mTargetState == Cocos2dxVideoView.STATE_PLAYING) {
                Cocos2dxVideoView.this.start();
            }
        }
    };
    Callback mSHCallback = new Callback() {
        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            boolean isValidState;
            if (Cocos2dxVideoView.this.mTargetState == Cocos2dxVideoView.STATE_PLAYING) {
                isValidState = true;
            } else {
                isValidState = false;
            }
            boolean hasValidSize;
            if (Cocos2dxVideoView.this.mVideoWidth == w && Cocos2dxVideoView.this.mVideoHeight == h) {
                hasValidSize = true;
            } else {
                hasValidSize = false;
            }
            if (Cocos2dxVideoView.this.mMediaPlayer != null && isValidState && hasValidSize) {
                if (Cocos2dxVideoView.this.mSeekWhenPrepared != 0) {
                    Cocos2dxVideoView.this.seekTo(Cocos2dxVideoView.this.mSeekWhenPrepared);
                }
                Cocos2dxVideoView.this.start();
            }
        }

        public void surfaceCreated(SurfaceHolder holder) {
            Cocos2dxVideoView.this.mSurfaceHolder = holder;
            Cocos2dxVideoView.this.openVideo();
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            Cocos2dxVideoView.this.mSurfaceHolder = null;
            Cocos2dxVideoView.this.release(true);
        }
    };
    private int mSeekWhenPrepared;
    protected OnVideoSizeChangedListener mSizeChangedListener = new OnVideoSizeChangedListener() {
        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
            Cocos2dxVideoView.this.mVideoWidth = mp.getVideoWidth();
            Cocos2dxVideoView.this.mVideoHeight = mp.getVideoHeight();
            if (Cocos2dxVideoView.this.mVideoWidth != 0 && Cocos2dxVideoView.this.mVideoHeight != 0) {
                Cocos2dxVideoView.this.getHolder().setFixedSize(Cocos2dxVideoView.this.mVideoWidth, Cocos2dxVideoView.this.mVideoHeight);
            }
        }
    };
    private SurfaceHolder mSurfaceHolder = null;
    private int mTargetState = STATE_IDLE;
    private String mVideoFilePath = null;
    private int mVideoHeight = STATE_IDLE;
    private Uri mVideoUri;
    private int mVideoWidth = STATE_IDLE;
    protected int mViewHeight = STATE_IDLE;
    protected int mViewLeft = STATE_IDLE;
    private int mViewTag = STATE_IDLE;
    protected int mViewTop = STATE_IDLE;
    protected int mViewWidth = STATE_IDLE;
    protected int mVisibleHeight = STATE_IDLE;
    protected int mVisibleLeft = STATE_IDLE;
    protected int mVisibleTop = STATE_IDLE;
    protected int mVisibleWidth = STATE_IDLE;

    public interface OnVideoEventListener {
        void onVideoEvent(int i, int i2);
    }

    public Cocos2dxVideoView(Cocos2dxActivity activity, int tag) {
        super(activity);
        this.mViewTag = tag;
        this.mCocos2dxActivity = activity;
        initVideoView();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.mVideoWidth == 0 || this.mVideoHeight == 0) {
            setMeasuredDimension(this.mViewWidth, this.mViewHeight);
            Log.i(this.TAG, BuildConfig.FLAVOR + this.mViewWidth + ":" + this.mViewHeight);
            return;
        }
        setMeasuredDimension(this.mVisibleWidth, this.mVisibleHeight);
        Log.i(this.TAG, BuildConfig.FLAVOR + this.mVisibleWidth + ":" + this.mVisibleHeight);
    }

    public void setVideoRect(int left, int top, int maxWidth, int maxHeight) {
        this.mViewLeft = left;
        this.mViewTop = top;
        this.mViewWidth = maxWidth;
        this.mViewHeight = maxHeight;
        fixSize(this.mViewLeft, this.mViewTop, this.mViewWidth, this.mViewHeight);
    }

    public void setFullScreenEnabled(boolean enabled, int width, int height) {
        if (this.mFullScreenEnabled != enabled) {
            this.mFullScreenEnabled = enabled;
            if (!(width == 0 || height == 0)) {
                this.mFullScreenWidth = width;
                this.mFullScreenHeight = height;
            }
            fixSize();
        }
    }

    public int resolveAdjustedSize(int desiredSize, int measureSpec) {
        int result = desiredSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case Integer.MIN_VALUE:
                return Math.min(desiredSize, specSize);
            case STATE_IDLE /*0*/:
                return desiredSize;
            case 1073741824:
                return specSize;
            default:
                return result;
        }
    }

    public void setVisibility(int visibility) {
        if (visibility == STATE_PAUSED) {
            this.mNeedResume = isPlaying();
            if (this.mNeedResume) {
                this.mSeekWhenPrepared = getCurrentPosition();
            }
        } else if (this.mNeedResume) {
            start();
            this.mNeedResume = false;
        }
        super.setVisibility(visibility);
    }

    private void initVideoView() {
        this.mVideoWidth = STATE_IDLE;
        this.mVideoHeight = STATE_IDLE;
        getHolder().addCallback(this.mSHCallback);
        getHolder().setType(STATE_PLAYING);
        setFocusable(true);
        setFocusableInTouchMode(true);
        this.mCurrentState = STATE_IDLE;
        this.mTargetState = STATE_IDLE;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if ((event.getAction() & 255) == STATE_PREPARING) {
            if (isPlaying()) {
                pause();
            } else if (this.mCurrentState == STATE_PAUSED) {
                resume();
            }
        }
        return true;
    }

    public void setVideoFileName(String path) {
        if (path.startsWith(AssetResourceRoot)) {
            path = path.substring(AssetResourceRoot.length());
        }
        if (path.startsWith("/")) {
            this.mIsAssetRouse = false;
            setVideoURI(Uri.parse(path), null);
            return;
        }
        this.mVideoFilePath = path;
        this.mIsAssetRouse = true;
        setVideoURI(Uri.parse(path), null);
    }

    public void setVideoURL(String url) {
        this.mIsAssetRouse = false;
        setVideoURI(Uri.parse(url), null);
    }

    private void setVideoURI(Uri uri, Map<String, String> map) {
        this.mVideoUri = uri;
        this.mSeekWhenPrepared = STATE_IDLE;
        this.mVideoWidth = STATE_IDLE;
        this.mVideoHeight = STATE_IDLE;
        openVideo();
        requestLayout();
        invalidate();
    }

    public void stopPlayback() {
        if (this.mMediaPlayer != null) {
            this.mMediaPlayer.stop();
            this.mMediaPlayer.release();
            this.mMediaPlayer = null;
            this.mCurrentState = STATE_IDLE;
            this.mTargetState = STATE_IDLE;
        }
    }

    private void openVideo() {
        if (this.mSurfaceHolder != null) {
            if (this.mIsAssetRouse) {
                if (this.mVideoFilePath == null) {
                    return;
                }
            } else if (this.mVideoUri == null) {
                return;
            }
            Intent i = new Intent("com.android.music.musicservicecommand");
            i.putExtra("command", "pause");
            this.mCocos2dxActivity.sendBroadcast(i);
            release(false);
            try {
                this.mMediaPlayer = new MediaPlayer();
                this.mMediaPlayer.setOnPreparedListener(this.mPreparedListener);
                this.mMediaPlayer.setOnVideoSizeChangedListener(this.mSizeChangedListener);
                this.mMediaPlayer.setOnCompletionListener(this.mCompletionListener);
                this.mMediaPlayer.setOnErrorListener(this.mErrorListener);
                this.mMediaPlayer.setOnBufferingUpdateListener(this.mBufferingUpdateListener);
                this.mMediaPlayer.setDisplay(this.mSurfaceHolder);
                this.mMediaPlayer.setAudioStreamType(STATE_PLAYING);
                this.mMediaPlayer.setScreenOnWhilePlaying(true);
                this.mDuration = STATE_ERROR;
                this.mCurrentBufferPercentage = STATE_IDLE;
                if (this.mIsAssetRouse) {
                    AssetFileDescriptor afd = this.mCocos2dxActivity.getAssets().openFd(this.mVideoFilePath);
                    this.mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                } else {
                    this.mMediaPlayer.setDataSource(this.mCocos2dxActivity, this.mVideoUri);
                }
                this.mMediaPlayer.prepareAsync();
                this.mCurrentState = STATE_PREPARING;
            } catch (IOException ex) {
                Log.w(this.TAG, "Unable to open content: " + this.mVideoUri, ex);
                this.mCurrentState = STATE_ERROR;
                this.mTargetState = STATE_ERROR;
                this.mErrorListener.onError(this.mMediaPlayer, STATE_PREPARING, STATE_IDLE);
            } catch (IllegalArgumentException ex2) {
                Log.w(this.TAG, "Unable to open content: " + this.mVideoUri, ex2);
                this.mCurrentState = STATE_ERROR;
                this.mTargetState = STATE_ERROR;
                this.mErrorListener.onError(this.mMediaPlayer, STATE_PREPARING, STATE_IDLE);
            }
        }
    }

    public void setKeepRatio(boolean enabled) {
        this.mKeepRatio = enabled;
        fixSize();
    }

    public void fixSize() {
        if (this.mFullScreenEnabled) {
            fixSize(STATE_IDLE, STATE_IDLE, this.mFullScreenWidth, this.mFullScreenHeight);
        } else {
            fixSize(this.mViewLeft, this.mViewTop, this.mViewWidth, this.mViewHeight);
        }
    }

    public void fixSize(int left, int top, int width, int height) {
        if (this.mVideoWidth == 0 || this.mVideoHeight == 0) {
            this.mVisibleLeft = left;
            this.mVisibleTop = top;
            this.mVisibleWidth = width;
            this.mVisibleHeight = height;
        } else if (width == 0 || height == 0) {
            this.mVisibleLeft = left;
            this.mVisibleTop = top;
            this.mVisibleWidth = this.mVideoWidth;
            this.mVisibleHeight = this.mVideoHeight;
        } else if (this.mKeepRatio) {
            if (this.mVideoWidth * height > this.mVideoHeight * width) {
                this.mVisibleWidth = width;
                this.mVisibleHeight = (this.mVideoHeight * width) / this.mVideoWidth;
            } else if (this.mVideoWidth * height < this.mVideoHeight * width) {
                this.mVisibleWidth = (this.mVideoWidth * height) / this.mVideoHeight;
                this.mVisibleHeight = height;
            }
            this.mVisibleLeft = ((width - this.mVisibleWidth) / STATE_PREPARED) + left;
            this.mVisibleTop = ((height - this.mVisibleHeight) / STATE_PREPARED) + top;
        } else {
            this.mVisibleLeft = left;
            this.mVisibleTop = top;
            this.mVisibleWidth = width;
            this.mVisibleHeight = height;
        }
        getHolder().setFixedSize(this.mVisibleWidth, this.mVisibleHeight);
        LayoutParams lParams = new LayoutParams(-2, -2);
        lParams.leftMargin = this.mVisibleLeft;
        lParams.topMargin = this.mVisibleTop;
        lParams.gravity = 51;
        setLayoutParams(lParams);
    }

    public void setOnPreparedListener(OnPreparedListener l) {
        this.mOnPreparedListener = l;
    }

    public void setOnCompletionListener(OnVideoEventListener l) {
        this.mOnVideoEventListener = l;
    }

    public void setOnErrorListener(OnErrorListener l) {
        this.mOnErrorListener = l;
    }

    private void release(boolean cleartargetstate) {
        if (this.mMediaPlayer != null) {
            this.mMediaPlayer.reset();
            this.mMediaPlayer.release();
            this.mMediaPlayer = null;
            this.mCurrentState = STATE_IDLE;
            if (cleartargetstate) {
                this.mTargetState = STATE_IDLE;
            }
        }
    }

    public void start() {
        if (isInPlaybackState()) {
            this.mMediaPlayer.start();
            this.mCurrentState = STATE_PLAYING;
            if (this.mOnVideoEventListener != null) {
                this.mOnVideoEventListener.onVideoEvent(this.mViewTag, STATE_IDLE);
            }
        }
        this.mTargetState = STATE_PLAYING;
    }

    public void pause() {
        if (isInPlaybackState() && this.mMediaPlayer.isPlaying()) {
            this.mMediaPlayer.pause();
            this.mCurrentState = STATE_PAUSED;
            if (this.mOnVideoEventListener != null) {
                this.mOnVideoEventListener.onVideoEvent(this.mViewTag, STATE_PREPARING);
            }
        }
        this.mTargetState = STATE_PAUSED;
    }

    public void stop() {
        if (isInPlaybackState() && this.mMediaPlayer.isPlaying()) {
            stopPlayback();
            if (this.mOnVideoEventListener != null) {
                this.mOnVideoEventListener.onVideoEvent(this.mViewTag, STATE_PREPARED);
            }
        }
    }

    public void suspend() {
        release(false);
    }

    public void resume() {
        if (isInPlaybackState() && this.mCurrentState == STATE_PAUSED) {
            this.mMediaPlayer.start();
            this.mCurrentState = STATE_PLAYING;
            if (this.mOnVideoEventListener != null) {
                this.mOnVideoEventListener.onVideoEvent(this.mViewTag, STATE_IDLE);
            }
        }
    }

    public void restart() {
        if (isInPlaybackState()) {
            this.mMediaPlayer.seekTo(STATE_IDLE);
            this.mMediaPlayer.start();
            this.mCurrentState = STATE_PLAYING;
            this.mTargetState = STATE_PLAYING;
        }
    }

    public int getDuration() {
        if (!isInPlaybackState()) {
            this.mDuration = STATE_ERROR;
            return this.mDuration;
        } else if (this.mDuration > 0) {
            return this.mDuration;
        } else {
            this.mDuration = this.mMediaPlayer.getDuration();
            return this.mDuration;
        }
    }

    public int getCurrentPosition() {
        if (isInPlaybackState()) {
            return this.mMediaPlayer.getCurrentPosition();
        }
        return STATE_IDLE;
    }

    public void seekTo(int msec) {
        if (isInPlaybackState()) {
            this.mMediaPlayer.seekTo(msec);
            this.mSeekWhenPrepared = STATE_IDLE;
            return;
        }
        this.mSeekWhenPrepared = msec;
    }

    public boolean isPlaying() {
        return isInPlaybackState() && this.mMediaPlayer.isPlaying();
    }

    public int getBufferPercentage() {
        if (this.mMediaPlayer != null) {
            return this.mCurrentBufferPercentage;
        }
        return STATE_IDLE;
    }

    public boolean isInPlaybackState() {
        return (this.mMediaPlayer == null || this.mCurrentState == STATE_ERROR || this.mCurrentState == 0 || this.mCurrentState == STATE_PREPARING) ? false : true;
    }

    public boolean canPause() {
        return true;
    }

    public boolean canSeekBackward() {
        return true;
    }

    public boolean canSeekForward() {
        return true;
    }

    public int getAudioSessionId() {
        return this.mMediaPlayer.getAudioSessionId();
    }
}
