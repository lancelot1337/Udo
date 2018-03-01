package com.unity3d.ads.adunit;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.ironsource.sdk.utils.Constants.ParametersKeys;
import com.unity3d.ads.api.AdUnit;
import com.unity3d.ads.api.VideoPlayer;
import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.misc.ViewUtilities;
import com.unity3d.ads.video.VideoPlayerView;
import com.unity3d.ads.webview.WebViewApp;
import com.unity3d.ads.webview.WebViewEventCategory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import org.cocos2dx.lib.Cocos2dxEditBox;
import org.cocos2dx.lib.Cocos2dxHandler;

public class AdUnitActivity extends Activity {
    public static final String EXTRA_ACTIVITY_ID = "activityId";
    public static final String EXTRA_KEEP_SCREEN_ON = "keepScreenOn";
    public static final String EXTRA_KEY_EVENT_LIST = "keyEvents";
    public static final String EXTRA_ORIENTATION = "orientation";
    public static final String EXTRA_SYSTEM_UI_VISIBILITY = "systemUiVisibility";
    public static final String EXTRA_VIEWS = "views";
    private int _activityId;
    boolean _keepScreenOn;
    private ArrayList<Integer> _keyEventList;
    private RelativeLayout _layout;
    private int _orientation = -1;
    private int _systemUiVisibility;
    private String[] _views;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (WebViewApp.getCurrentApp() == null) {
            DeviceLog.error("Unity Ads web app is null, closing Unity Ads activity from onCreate");
            finish();
            return;
        }
        AdUnitEvent event;
        AdUnit.setAdUnitActivity(this);
        createLayout();
        ViewUtilities.removeViewFromParent(this._layout);
        addContentView(this._layout, this._layout.getLayoutParams());
        if (savedInstanceState == null) {
            this._views = getIntent().getStringArrayExtra(EXTRA_VIEWS);
            this._keyEventList = getIntent().getIntegerArrayListExtra(EXTRA_KEY_EVENT_LIST);
            if (getIntent().hasExtra(EXTRA_ORIENTATION)) {
                this._orientation = getIntent().getIntExtra(EXTRA_ORIENTATION, -1);
            }
            if (getIntent().hasExtra(EXTRA_SYSTEM_UI_VISIBILITY)) {
                this._systemUiVisibility = getIntent().getIntExtra(EXTRA_SYSTEM_UI_VISIBILITY, 0);
            }
            if (getIntent().hasExtra(EXTRA_ACTIVITY_ID)) {
                this._activityId = getIntent().getIntExtra(EXTRA_ACTIVITY_ID, -1);
            }
            event = AdUnitEvent.ON_CREATE;
        } else {
            this._views = savedInstanceState.getStringArray(EXTRA_VIEWS);
            this._orientation = savedInstanceState.getInt(EXTRA_ORIENTATION, -1);
            this._systemUiVisibility = savedInstanceState.getInt(EXTRA_SYSTEM_UI_VISIBILITY, 0);
            this._keyEventList = savedInstanceState.getIntegerArrayList(EXTRA_KEY_EVENT_LIST);
            this._keepScreenOn = savedInstanceState.getBoolean(EXTRA_KEEP_SCREEN_ON);
            this._activityId = savedInstanceState.getInt(EXTRA_ACTIVITY_ID, -1);
            setKeepScreenOn(this._keepScreenOn);
            event = AdUnitEvent.ON_RESTORE;
        }
        setOrientation(this._orientation);
        setSystemUiVisibility(this._systemUiVisibility);
        if (this._views != null && Arrays.asList(this._views).contains("videoplayer")) {
            createVideoPlayer();
        }
        WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.ADUNIT, event, Integer.valueOf(this._activityId));
    }

    protected void onStart() {
        super.onStart();
        if (WebViewApp.getCurrentApp() != null) {
            WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.ADUNIT, AdUnitEvent.ON_START, Integer.valueOf(this._activityId));
        } else if (!isFinishing()) {
            DeviceLog.error("Unity Ads web app is null, closing Unity Ads activity from onStart");
            finish();
        }
    }

    protected void onStop() {
        super.onStop();
        if (WebViewApp.getCurrentApp() != null) {
            WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.ADUNIT, AdUnitEvent.ON_STOP, Integer.valueOf(this._activityId));
        } else if (!isFinishing()) {
            DeviceLog.error("Unity Ads web app is null, closing Unity Ads activity from onStop");
            finish();
        }
    }

    protected void onResume() {
        super.onResume();
        if (WebViewApp.getCurrentApp() != null) {
            setViews(this._views);
            WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.ADUNIT, AdUnitEvent.ON_RESUME, Integer.valueOf(this._activityId));
        } else if (!isFinishing()) {
            DeviceLog.error("Unity Ads web app is null, closing Unity Ads activity from onResume");
            finish();
        }
    }

    protected void onPause() {
        super.onPause();
        if (WebViewApp.getCurrentApp() != null) {
            if (isFinishing()) {
                ViewUtilities.removeViewFromParent(WebViewApp.getCurrentApp().getWebView());
            }
            destroyVideoPlayer();
            WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.ADUNIT, AdUnitEvent.ON_PAUSE, Boolean.valueOf(isFinishing()), Integer.valueOf(this._activityId));
        } else if (!isFinishing()) {
            DeviceLog.error("Unity Ads web app is null, closing Unity Ads activity from onPause");
            finish();
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_ORIENTATION, this._orientation);
        outState.putInt(EXTRA_SYSTEM_UI_VISIBILITY, this._systemUiVisibility);
        outState.putIntegerArrayList(EXTRA_KEY_EVENT_LIST, this._keyEventList);
        outState.putBoolean(EXTRA_KEEP_SCREEN_ON, this._keepScreenOn);
        outState.putStringArray(EXTRA_VIEWS, this._views);
        outState.putInt(EXTRA_ACTIVITY_ID, this._activityId);
    }

    protected void onDestroy() {
        super.onDestroy();
        if (WebViewApp.getCurrentApp() != null) {
            AdUnit.setAdUnitActivity(null);
            WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.ADUNIT, AdUnitEvent.ON_DESTROY, Boolean.valueOf(isFinishing()), Integer.valueOf(this._activityId));
            if (AdUnit.getCurrentAdUnitActivityId() == this._activityId) {
                AdUnit.setAdUnitActivity(null);
            }
        } else if (!isFinishing()) {
            DeviceLog.error("Unity Ads web app is null, closing Unity Ads activity from onDestroy");
            finish();
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (this._keyEventList == null || !this._keyEventList.contains(Integer.valueOf(keyCode))) {
            return false;
        }
        WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.ADUNIT, AdUnitEvent.KEY_DOWN, Integer.valueOf(keyCode), Long.valueOf(event.getEventTime()), Long.valueOf(event.getDownTime()), Integer.valueOf(event.getRepeatCount()), Integer.valueOf(this._activityId));
        return true;
    }

    public void setViews(String[] views) {
        String[] actualViews;
        int i;
        int i2 = 0;
        if (views == null) {
            actualViews = new String[0];
        } else {
            actualViews = views;
        }
        ArrayList<String> newViews = new ArrayList(Arrays.asList(actualViews));
        if (this._views == null) {
            this._views = new String[0];
        }
        ArrayList<String> removedViews = new ArrayList(Arrays.asList(this._views));
        removedViews.removeAll(newViews);
        Iterator it = removedViews.iterator();
        while (it.hasNext()) {
            String view = (String) it.next();
            i = -1;
            switch (view.hashCode()) {
                case 1224424441:
                    if (view.equals(ParametersKeys.WEB_VIEW)) {
                        i = 1;
                        break;
                    }
                    break;
                case 1865295644:
                    if (view.equals("videoplayer")) {
                        i = 0;
                        break;
                    }
                    break;
            }
            switch (i) {
                case Cocos2dxEditBox.kEndActionUnknown /*0*/:
                    destroyVideoPlayer();
                    break;
                case Cocos2dxHandler.HANDLER_SHOW_DIALOG /*1*/:
                    ViewUtilities.removeViewFromParent(WebViewApp.getCurrentApp().getWebView());
                    break;
                default:
                    break;
            }
        }
        this._views = actualViews;
        i = actualViews.length;
        while (i2 < i) {
            view = actualViews[i2];
            if (view != null) {
                if (view.equals("videoplayer")) {
                    createVideoPlayer();
                    handleViewPlacement(VideoPlayer.getVideoPlayerView());
                } else if (!view.equals(ParametersKeys.WEB_VIEW)) {
                    continue;
                } else if (WebViewApp.getCurrentApp() != null) {
                    handleViewPlacement(WebViewApp.getCurrentApp().getWebView());
                } else {
                    DeviceLog.error("WebApp IS NULL!");
                    throw new NullPointerException();
                }
            }
            i2++;
        }
    }

    private void handleViewPlacement(View view) {
        if (view.getParent() == null || !view.getParent().equals(this._layout)) {
            ViewUtilities.removeViewFromParent(view);
            LayoutParams params = new LayoutParams(-1, -1);
            params.addRule(13);
            params.setMargins(0, 0, 0, 0);
            view.setPadding(0, 0, 0, 0);
            this._layout.addView(view, params);
            return;
        }
        this._layout.bringChildToFront(view);
    }

    public String[] getViews() {
        return this._views;
    }

    public void setOrientation(int orientation) {
        this._orientation = orientation;
        setRequestedOrientation(orientation);
    }

    public boolean setKeepScreenOn(boolean keepScreenOn) {
        this._keepScreenOn = keepScreenOn;
        if (getWindow() == null) {
            return false;
        }
        if (keepScreenOn) {
            getWindow().addFlags(128);
        } else {
            getWindow().clearFlags(128);
        }
        return true;
    }

    public boolean setSystemUiVisibility(int flags) {
        this._systemUiVisibility = flags;
        if (VERSION.SDK_INT < 11) {
            return false;
        }
        try {
            getWindow().getDecorView().setSystemUiVisibility(flags);
            return true;
        } catch (Exception e) {
            DeviceLog.exception("Error while setting SystemUIVisibility", e);
            return false;
        }
    }

    public void setKeyEventList(ArrayList<Integer> keyevents) {
        this._keyEventList = keyevents;
    }

    private void createLayout() {
        if (this._layout == null) {
            this._layout = new RelativeLayout(this);
            this._layout.setLayoutParams(new LayoutParams(-1, -1));
            ViewUtilities.setBackground(this._layout, new ColorDrawable(-16777216));
        }
    }

    private void createVideoPlayer() {
        if (VideoPlayer.getVideoPlayerView() == null) {
            VideoPlayer.setVideoPlayerView(new VideoPlayerView(this));
        }
    }

    private void destroyVideoPlayer() {
        if (VideoPlayer.getVideoPlayerView() != null) {
            VideoPlayer.getVideoPlayerView().stopVideoProgressTimer();
            VideoPlayer.getVideoPlayerView().stopPlayback();
            ViewUtilities.removeViewFromParent(VideoPlayer.getVideoPlayerView());
        }
        VideoPlayer.setVideoPlayerView(null);
    }
}
