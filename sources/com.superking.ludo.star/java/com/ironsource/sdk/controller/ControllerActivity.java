package com.ironsource.sdk.controller;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.ironsource.environment.DeviceStatus;
import com.ironsource.sdk.agent.IronSourceAdsPublisherAgent;
import com.ironsource.sdk.controller.IronSourceWebView.State;
import com.ironsource.sdk.data.AdUnitsState;
import com.ironsource.sdk.data.SSAEnums.ProductType;
import com.ironsource.sdk.handlers.BackButtonHandler;
import com.ironsource.sdk.listeners.OnWebViewChangeListener;
import com.ironsource.sdk.utils.Constants;
import com.ironsource.sdk.utils.Constants.ParametersKeys;
import com.ironsource.sdk.utils.Logger;
import com.ironsource.sdk.utils.SDKUtils;

public class ControllerActivity extends Activity implements VideoEventsListener, OnWebViewChangeListener {
    private static final String TAG = ControllerActivity.class.getSimpleName();
    private static final int WEB_VIEW_VIEW_ID = 1;
    final LayoutParams MATCH_PARENT_LAYOUT_PARAMS = new LayoutParams(-1, -1);
    private boolean calledFromOnCreate = false;
    public int currentRequestedRotation = -1;
    private final Runnable decorViewSettings = new Runnable() {
        public void run() {
            ControllerActivity.this.getWindow().getDecorView().setSystemUiVisibility(SDKUtils.getActivityUIFlags(ControllerActivity.this.mIsImmersive));
        }
    };
    private RelativeLayout mContainer;
    private boolean mIsImmersive = false;
    private String mProductType;
    private AdUnitsState mState;
    private Handler mUiThreadHandler = new Handler();
    private IronSourceWebView mWebViewController;
    private FrameLayout mWebViewFrameContainer;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.i(TAG, "onCreate");
        hideActivityTitle();
        hideActivtiyStatusBar();
        this.mWebViewController = IronSourceAdsPublisherAgent.getInstance(this).getWebViewController();
        this.mWebViewController.setId(WEB_VIEW_VIEW_ID);
        this.mWebViewController.setOnWebViewControllerChangeListener(this);
        this.mWebViewController.setVideoEventsListener(this);
        Intent intent = getIntent();
        this.mProductType = intent.getStringExtra(ParametersKeys.PRODUCT_TYPE);
        this.mIsImmersive = intent.getBooleanExtra(ParametersKeys.IMMERSIVE, false);
        if (this.mIsImmersive) {
            getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new OnSystemUiVisibilityChangeListener() {
                public void onSystemUiVisibilityChange(int visibility) {
                    if ((visibility & 4098) == 0) {
                        ControllerActivity.this.mUiThreadHandler.removeCallbacks(ControllerActivity.this.decorViewSettings);
                        ControllerActivity.this.mUiThreadHandler.postDelayed(ControllerActivity.this.decorViewSettings, 500);
                    }
                }
            });
            runOnUiThread(this.decorViewSettings);
        }
        if (!TextUtils.isEmpty(this.mProductType) && ProductType.OfferWall.toString().equalsIgnoreCase(this.mProductType)) {
            if (savedInstanceState != null) {
                AdUnitsState state = (AdUnitsState) savedInstanceState.getParcelable(Constants.RESTORED_STATE);
                if (state != null) {
                    this.mState = state;
                    this.mWebViewController.restoreState(state);
                }
                finish();
            } else {
                this.mState = this.mWebViewController.getSavedState();
            }
        }
        this.mContainer = new RelativeLayout(this);
        setContentView(this.mContainer, this.MATCH_PARENT_LAYOUT_PARAMS);
        this.mWebViewFrameContainer = this.mWebViewController.getLayout();
        if (this.mContainer.findViewById(WEB_VIEW_VIEW_ID) == null && this.mWebViewFrameContainer.getParent() != null) {
            this.calledFromOnCreate = true;
            finish();
        }
        initOrientationState();
    }

    private void initOrientationState() {
        Intent intent = getIntent();
        handleOrientationState(intent.getStringExtra(ParametersKeys.ORIENTATION_SET_FLAG), intent.getIntExtra(ParametersKeys.ROTATION_SET_FLAG, 0));
    }

    private void handleOrientationState(String orientation, int rotation) {
        if (orientation == null) {
            return;
        }
        if (ParametersKeys.ORIENTATION_LANDSCAPE.equalsIgnoreCase(orientation)) {
            setInitiateLandscapeOrientation();
        } else if (ParametersKeys.ORIENTATION_PORTRAIT.equalsIgnoreCase(orientation)) {
            setInitiatePortraitOrientation();
        } else if (ParametersKeys.ORIENTATION_DEVICE.equalsIgnoreCase(orientation)) {
            if (DeviceStatus.isDeviceOrientationLocked(this)) {
                setRequestedOrientation(WEB_VIEW_VIEW_ID);
            }
        } else if (getRequestedOrientation() == -1) {
            setRequestedOrientation(4);
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (!TextUtils.isEmpty(this.mProductType) && ProductType.OfferWall.toString().equalsIgnoreCase(this.mProductType)) {
            this.mState.setShouldRestore(true);
            outState.putParcelable(Constants.RESTORED_STATE, this.mState);
        }
    }

    protected void onResume() {
        super.onResume();
        Logger.i(TAG, "onResume");
        this.mContainer.addView(this.mWebViewFrameContainer, this.MATCH_PARENT_LAYOUT_PARAMS);
        if (this.mWebViewController != null) {
            this.mWebViewController.registerConnectionReceiver(this);
            this.mWebViewController.resume();
            this.mWebViewController.viewableChange(true, ParametersKeys.MAIN);
        }
        ((AudioManager) getSystemService("audio")).requestAudioFocus(null, 3, 2);
    }

    protected void onPause() {
        super.onPause();
        Logger.i(TAG, "onPause");
        ((AudioManager) getSystemService("audio")).abandonAudioFocus(null);
        if (this.mWebViewController != null) {
            this.mWebViewController.unregisterConnectionReceiver(this);
            this.mWebViewController.pause();
            this.mWebViewController.viewableChange(false, ParametersKeys.MAIN);
        }
        removeWebViewContainerView();
    }

    protected void onDestroy() {
        super.onDestroy();
        Logger.i(TAG, "onDestroy");
        if (this.calledFromOnCreate) {
            removeWebViewContainerView();
        }
        if (this.mWebViewController != null) {
            this.mWebViewController.setState(State.Gone);
            this.mWebViewController.removeVideoEventsListener();
        }
    }

    private void removeWebViewContainerView() {
        if (this.mContainer != null) {
            ViewGroup parent = (ViewGroup) this.mWebViewFrameContainer.getParent();
            if (parent.findViewById(WEB_VIEW_VIEW_ID) != null) {
                parent.removeView(this.mWebViewFrameContainer);
            }
        }
    }

    public void onCloseRequested() {
        finish();
    }

    public void onOrientationChanged(String orientation, int rotation) {
        handleOrientationState(orientation, rotation);
    }

    public boolean onBackButtonPressed() {
        onBackPressed();
        return true;
    }

    public void onBackPressed() {
        Logger.i(TAG, "onBackPressed");
        if (!BackButtonHandler.getInstance().handleBackButton(this)) {
            super.onBackPressed();
        }
    }

    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        Logger.i(TAG, "onUserLeaveHint");
    }

    private void hideActivityTitle() {
        requestWindowFeature(WEB_VIEW_VIEW_ID);
    }

    private void hideActivtiyStatusBar() {
        getWindow().setFlags(1024, 1024);
    }

    private void keepScreenOn() {
        runOnUiThread(new Runnable() {
            public void run() {
                ControllerActivity.this.getWindow().addFlags(128);
            }
        });
    }

    private void cancelScreenOn() {
        runOnUiThread(new Runnable() {
            public void run() {
                ControllerActivity.this.getWindow().clearFlags(128);
            }
        });
    }

    private void setInitiateLandscapeOrientation() {
        int rotation = DeviceStatus.getApplicationRotation(this);
        Logger.i(TAG, "setInitiateLandscapeOrientation");
        if (rotation == 0) {
            Logger.i(TAG, "ROTATION_0");
            setRequestedOrientation(0);
        } else if (rotation == 2) {
            Logger.i(TAG, "ROTATION_180");
            setRequestedOrientation(8);
        } else if (rotation == 3) {
            Logger.i(TAG, "ROTATION_270 Right Landscape");
            setRequestedOrientation(8);
        } else if (rotation == WEB_VIEW_VIEW_ID) {
            Logger.i(TAG, "ROTATION_90 Left Landscape");
            setRequestedOrientation(0);
        } else {
            Logger.i(TAG, "No Rotation");
        }
    }

    private void setInitiatePortraitOrientation() {
        int rotation = DeviceStatus.getApplicationRotation(this);
        Logger.i(TAG, "setInitiatePortraitOrientation");
        if (rotation == 0) {
            Logger.i(TAG, "ROTATION_0");
            setRequestedOrientation(WEB_VIEW_VIEW_ID);
        } else if (rotation == 2) {
            Logger.i(TAG, "ROTATION_180");
            setRequestedOrientation(9);
        } else if (rotation == WEB_VIEW_VIEW_ID) {
            Logger.i(TAG, "ROTATION_270 Right Landscape");
            setRequestedOrientation(WEB_VIEW_VIEW_ID);
        } else if (rotation == 3) {
            Logger.i(TAG, "ROTATION_90 Left Landscape");
            setRequestedOrientation(WEB_VIEW_VIEW_ID);
        } else {
            Logger.i(TAG, "No Rotation");
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 4 && this.mWebViewController.inCustomView()) {
            this.mWebViewController.hideCustomView();
            return true;
        }
        if (this.mIsImmersive && (keyCode == 25 || keyCode == 24)) {
            this.mUiThreadHandler.removeCallbacks(this.decorViewSettings);
            this.mUiThreadHandler.postDelayed(this.decorViewSettings, 500);
        }
        return super.onKeyDown(keyCode, event);
    }

    public void setRequestedOrientation(int requestedOrientation) {
        if (this.currentRequestedRotation != requestedOrientation) {
            Logger.i(TAG, "Rotation: Req = " + requestedOrientation + " Curr = " + this.currentRequestedRotation);
            this.currentRequestedRotation = requestedOrientation;
            super.setRequestedOrientation(requestedOrientation);
        }
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (this.mIsImmersive && hasFocus) {
            runOnUiThread(this.decorViewSettings);
        }
    }

    public void onVideoStarted() {
        toggleKeepScreen(true);
    }

    public void onVideoPaused() {
        toggleKeepScreen(false);
    }

    public void onVideoResumed() {
        toggleKeepScreen(true);
    }

    public void onVideoEnded() {
        toggleKeepScreen(false);
    }

    public void onVideoStopped() {
        toggleKeepScreen(false);
    }

    public void toggleKeepScreen(boolean screenOn) {
        if (screenOn) {
            keepScreenOn();
        } else {
            cancelScreenOn();
        }
    }
}
