package org.cocos2dx.cpp;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.integration.IntegrationHelper;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.model.Placement;
import com.ironsource.mediationsdk.sdk.RewardedVideoListener;
import com.ironsource.sdk.SSAFactory;
import org.cocos2dx.lib.BuildConfig;
import org.cocos2dx.lib.Cocos2dxHelper;

public class AdsInterface {
    private static String APP_KEY = null;
    private static final String FALLBACK_USER_ID = "userId";
    private static final String LOCAL_STORAGE_KEY = "sk::adwatch::";
    private static String currParam;
    private static boolean mPendingVideoRequest = false;

    private static class SKRewardedVideoListener implements RewardedVideoListener {
        private boolean watchSuccess;

        private SKRewardedVideoListener() {
            this.watchSuccess = false;
        }

        public void onRewardedVideoAdOpened() {
            Log.d("super1", "onRewardedVideoAdOpened");
        }

        public void onRewardedVideoAdClosed() {
            Log.d("super1", "onRewardedVideoAdClosed");
            if (this.watchSuccess) {
                Cocos2dxHelper.setIntegerForKey(AdsInterface.LOCAL_STORAGE_KEY + AdsInterface.currParam, 1);
                return;
            }
            AppActivity app = AppActivity.getInstance();
            if (app != null) {
                app.runOnGLThread(new Runnable() {
                    public void run() {
                        AdsInterface.nativeOnWatchResult("incomplete");
                    }
                });
            }
        }

        public void onRewardedVideoAvailabilityChanged(boolean b) {
            Log.d("super1", "onRewardedVideoAvailabilityChanged value: " + b);
            if (b && AdsInterface.mPendingVideoRequest) {
                AdsInterface.mPendingVideoRequest = false;
                AdsInterface.playAd(BuildConfig.FLAVOR);
            }
        }

        public void onRewardedVideoAdStarted() {
            Log.d("super1", "onRewardedVideoAdStarted");
            this.watchSuccess = false;
        }

        public void onRewardedVideoAdEnded() {
            Log.d("super1", "onRewardedVideoAdEnded");
        }

        public void onRewardedVideoAdRewarded(Placement placement) {
            Log.d("super1", "onRewardedVideoAdRewarded placement: " + placement);
            this.watchSuccess = true;
        }

        public void onRewardedVideoAdShowFailed(IronSourceError ironSourceError) {
            Log.d("super1", "onRewardedVideoAdShowFailed Error: " + ironSourceError);
            AppActivity app = AppActivity.getInstance();
            if (app != null) {
                app.runOnGLThread(new Runnable() {
                    public void run() {
                        AdsInterface.nativeOnWatchResult("fail");
                    }
                });
            }
        }
    }

    private static native void nativeOnWatchResult(String str);

    public static void init(AppActivity activity) {
        APP_KEY = EMHelpers.getIronSrcAppId();
        IntegrationHelper.validateIntegration(activity);
        startIronSourceInitTask();
    }

    private static void startIronSourceInitTask() {
        new AsyncTask<Void, Void, String>() {
            protected String doInBackground(Void... params) {
                return IronSource.getAdvertiserId(AppActivity.getInstance());
            }

            protected void onPostExecute(String advertisingId) {
                if (TextUtils.isEmpty(advertisingId)) {
                    advertisingId = AdsInterface.FALLBACK_USER_ID;
                }
                AdsInterface.initIronSource(AdsInterface.APP_KEY, advertisingId);
            }
        }.execute(new Void[0]);
    }

    private static void initIronSource(String appKey, String userId) {
        AppActivity app = AppActivity.getInstance();
        if (app != null) {
            SSAFactory.getAdvertiserInstance().reportAppStarted(app);
            IronSource.setRewardedVideoListener(new SKRewardedVideoListener());
            IronSource.setUserId(userId);
            IronSource.init(app, appKey);
        }
    }

    public static void onResume() {
        IronSource.onResume(AppActivity.getInstance());
    }

    public static void onPause() {
        IronSource.onPause(AppActivity.getInstance());
    }

    public static void playAd(String param) {
        currParam = param;
        AppActivity app = AppActivity.getInstance();
        if (app != null) {
            app.runOnUiThread(new Runnable() {
                public void run() {
                    if (IronSource.isRewardedVideoAvailable()) {
                        Log.d("super1", "IronSource Rewarded Video is Available");
                        IronSource.showRewardedVideo();
                        return;
                    }
                    AdsInterface.mPendingVideoRequest = true;
                    Log.d("super1", "IronSource Rewarded Video not Available");
                }
            });
        }
    }

    public static void clearPendingRequest() {
        mPendingVideoRequest = false;
    }

    public static boolean isAdAvailable() {
        return IronSource.isRewardedVideoAvailable();
    }

    public static void onDestroy() {
    }
}
