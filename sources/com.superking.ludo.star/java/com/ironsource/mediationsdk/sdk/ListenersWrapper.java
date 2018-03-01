package com.ironsource.mediationsdk.sdk;

import android.os.Handler;
import android.os.Looper;
import com.facebook.internal.ServerProtocol;
import com.ironsource.eventsmodule.EventData;
import com.ironsource.mediationsdk.events.InterstitialEventsManager;
import com.ironsource.mediationsdk.events.RewardedVideoEventsManager;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.logger.IronSourceLogger.IronSourceTag;
import com.ironsource.mediationsdk.logger.IronSourceLoggerManager;
import com.ironsource.mediationsdk.model.Placement;
import com.ironsource.mediationsdk.utils.IronSourceConstants;
import com.ironsource.mediationsdk.utils.IronSourceUtils;
import com.ironsource.sdk.utils.Constants.ParametersKeys;
import cz.msebera.android.httpclient.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

public class ListenersWrapper implements InternalOfferwallListener, InterstitialListener, RewardedInterstitialListener, RewardedVideoListener {
    private CallbackHandlerThread mCallbackHandlerThread = new CallbackHandlerThread();
    private InterstitialListener mInterstitialListener;
    private OfferwallListener mOfferwallListener;
    private RewardedInterstitialListener mRewardedInterstitialListener;
    private RewardedVideoListener mRewardedVideoListener;

    private class CallbackHandlerThread extends Thread {
        private Handler mCallbackHandler;

        private CallbackHandlerThread() {
        }

        public void run() {
            Looper.prepare();
            this.mCallbackHandler = new Handler();
            Looper.loop();
        }

        public Handler getCallbackHandler() {
            return this.mCallbackHandler;
        }
    }

    public ListenersWrapper() {
        this.mCallbackHandlerThread.start();
    }

    private boolean canSendCallback(Object productListener) {
        return (productListener == null || this.mCallbackHandlerThread == null) ? false : true;
    }

    private void sendCallback(Runnable callbackRunnable) {
        if (this.mCallbackHandlerThread != null) {
            Handler callbackHandler = this.mCallbackHandlerThread.getCallbackHandler();
            if (callbackHandler != null) {
                callbackHandler.post(callbackRunnable);
            }
        }
    }

    public void setRewardedVideoListener(RewardedVideoListener rewardedVideoListener) {
        this.mRewardedVideoListener = rewardedVideoListener;
    }

    public void setInterstitialListener(InterstitialListener interstitialListener) {
        this.mInterstitialListener = interstitialListener;
    }

    public void setOfferwallListener(OfferwallListener offerwallListener) {
        this.mOfferwallListener = offerwallListener;
    }

    public void setRewardedInterstitialListener(RewardedInterstitialListener rewardedInterstitialListener) {
        this.mRewardedInterstitialListener = rewardedInterstitialListener;
    }

    public void onRewardedVideoAdOpened() {
        IronSourceLoggerManager.getLogger().log(IronSourceTag.CALLBACK, "onRewardedVideoAdOpened()", 1);
        if (canSendCallback(this.mRewardedVideoListener)) {
            sendCallback(new Runnable() {
                public void run() {
                    ListenersWrapper.this.mRewardedVideoListener.onRewardedVideoAdOpened();
                }
            });
        }
    }

    public void onRewardedVideoAdClosed() {
        IronSourceLoggerManager.getLogger().log(IronSourceTag.CALLBACK, "onRewardedVideoAdClosed()", 1);
        if (canSendCallback(this.mRewardedVideoListener)) {
            sendCallback(new Runnable() {
                public void run() {
                    ListenersWrapper.this.mRewardedVideoListener.onRewardedVideoAdClosed();
                }
            });
        }
    }

    public void onRewardedVideoAvailabilityChanged(final boolean available) {
        IronSourceLoggerManager.getLogger().log(IronSourceTag.CALLBACK, "onRewardedVideoAvailabilityChanged(available:" + available + ")", 1);
        JSONObject data = IronSourceUtils.getMediationAdditionalData();
        try {
            data.put(ParametersKeys.VIDEO_STATUS, String.valueOf(available));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RewardedVideoEventsManager.getInstance().log(new EventData(7, data));
        if (canSendCallback(this.mRewardedVideoListener)) {
            sendCallback(new Runnable() {
                public void run() {
                    ListenersWrapper.this.mRewardedVideoListener.onRewardedVideoAvailabilityChanged(available);
                }
            });
        }
    }

    public void onRewardedVideoAdStarted() {
        IronSourceLoggerManager.getLogger().log(IronSourceTag.CALLBACK, "onRewardedVideoAdStarted()", 1);
        if (canSendCallback(this.mRewardedVideoListener)) {
            sendCallback(new Runnable() {
                public void run() {
                    ListenersWrapper.this.mRewardedVideoListener.onRewardedVideoAdStarted();
                }
            });
        }
    }

    public void onRewardedVideoAdEnded() {
        IronSourceLoggerManager.getLogger().log(IronSourceTag.CALLBACK, "onRewardedVideoAdEnded()", 1);
        if (canSendCallback(this.mRewardedVideoListener)) {
            sendCallback(new Runnable() {
                public void run() {
                    ListenersWrapper.this.mRewardedVideoListener.onRewardedVideoAdEnded();
                }
            });
        }
    }

    public void onRewardedVideoAdRewarded(final Placement placement) {
        IronSourceLoggerManager.getLogger().log(IronSourceTag.CALLBACK, "onRewardedVideoAdRewarded(" + placement.toString() + ")", 1);
        if (canSendCallback(this.mRewardedVideoListener)) {
            sendCallback(new Runnable() {
                public void run() {
                    ListenersWrapper.this.mRewardedVideoListener.onRewardedVideoAdRewarded(placement);
                }
            });
        }
    }

    public void onRewardedVideoAdShowFailed(final IronSourceError error) {
        IronSourceLoggerManager.getLogger().log(IronSourceTag.CALLBACK, "onRewardedVideoAdShowFailed(" + error.toString() + ")", 1);
        JSONObject data = IronSourceUtils.getMediationAdditionalData();
        try {
            data.put(ParametersKeys.VIDEO_STATUS, "false");
            if (error.getErrorCode() == IronSourceError.ERROR_REACHED_CAP_LIMIT) {
                data.put("reason", 1);
            }
            data.put(IronSourceConstants.ERROR_CODE_KEY, error.getErrorCode());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RewardedVideoEventsManager.getInstance().log(new EventData(17, data));
        if (canSendCallback(this.mRewardedVideoListener)) {
            sendCallback(new Runnable() {
                public void run() {
                    ListenersWrapper.this.mRewardedVideoListener.onRewardedVideoAdShowFailed(error);
                }
            });
        }
    }

    public void onInterstitialAdReady() {
        IronSourceLoggerManager.getLogger().log(IronSourceTag.CALLBACK, "onInterstitialAdReady()", 1);
        JSONObject data = IronSourceUtils.getMediationAdditionalData();
        try {
            data.put(ParametersKeys.VIDEO_STATUS, ServerProtocol.DIALOG_RETURN_SCOPES_TRUE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        InterstitialEventsManager.getInstance().log(new EventData(27, data));
        if (canSendCallback(this.mInterstitialListener)) {
            sendCallback(new Runnable() {
                public void run() {
                    ListenersWrapper.this.mInterstitialListener.onInterstitialAdReady();
                }
            });
        }
    }

    public void onInterstitialAdLoadFailed(final IronSourceError error) {
        IronSourceLoggerManager.getLogger().log(IronSourceTag.CALLBACK, "onInterstitialAdLoadFailed(" + error + ")", 1);
        if (!(error == null || IronSourceError.ERROR_NO_INTERNET_CONNECTION == error.getErrorCode())) {
            JSONObject data = IronSourceUtils.getMediationAdditionalData();
            try {
                data.put(ParametersKeys.VIDEO_STATUS, "false");
                data.put(IronSourceConstants.ERROR_CODE_KEY, error.getErrorCode());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            InterstitialEventsManager.getInstance().log(new EventData(27, data));
        }
        if (canSendCallback(this.mInterstitialListener)) {
            sendCallback(new Runnable() {
                public void run() {
                    ListenersWrapper.this.mInterstitialListener.onInterstitialAdLoadFailed(error);
                }
            });
        }
    }

    public void onInterstitialAdOpened() {
        IronSourceLoggerManager.getLogger().log(IronSourceTag.CALLBACK, "onInterstitialAdOpened()", 1);
        if (canSendCallback(this.mInterstitialListener)) {
            sendCallback(new Runnable() {
                public void run() {
                    ListenersWrapper.this.mInterstitialListener.onInterstitialAdOpened();
                }
            });
        }
    }

    public void onInterstitialAdShowSucceeded() {
        IronSourceLoggerManager.getLogger().log(IronSourceTag.CALLBACK, "onInterstitialAdShowSucceeded()", 1);
        if (canSendCallback(this.mInterstitialListener)) {
            sendCallback(new Runnable() {
                public void run() {
                    ListenersWrapper.this.mInterstitialListener.onInterstitialAdShowSucceeded();
                }
            });
        }
    }

    public void onInterstitialAdShowFailed(final IronSourceError error) {
        IronSourceLoggerManager.getLogger().log(IronSourceTag.CALLBACK, "onInterstitialAdShowFailed(" + error + ")", 1);
        JSONObject data = IronSourceUtils.getMediationAdditionalData();
        try {
            if (error.getErrorCode() == IronSourceError.ERROR_REACHED_CAP_LIMIT) {
                data.put("reason", 1);
            }
            data.put(IronSourceConstants.ERROR_CODE_KEY, error.getErrorCode());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        InterstitialEventsManager.getInstance().log(new EventData(29, data));
        if (canSendCallback(this.mInterstitialListener)) {
            sendCallback(new Runnable() {
                public void run() {
                    ListenersWrapper.this.mInterstitialListener.onInterstitialAdShowFailed(error);
                }
            });
        }
    }

    public void onInterstitialAdClicked() {
        IronSourceLoggerManager.getLogger().log(IronSourceTag.CALLBACK, "onInterstitialAdClicked()", 1);
        if (canSendCallback(this.mInterstitialListener)) {
            sendCallback(new Runnable() {
                public void run() {
                    ListenersWrapper.this.mInterstitialListener.onInterstitialAdClicked();
                }
            });
        }
    }

    public void onInterstitialAdClosed() {
        IronSourceLoggerManager.getLogger().log(IronSourceTag.CALLBACK, "onInterstitialAdClosed()", 1);
        if (canSendCallback(this.mInterstitialListener)) {
            sendCallback(new Runnable() {
                public void run() {
                    ListenersWrapper.this.mInterstitialListener.onInterstitialAdClosed();
                }
            });
        }
    }

    public void onOfferwallOpened() {
        IronSourceLoggerManager.getLogger().log(IronSourceTag.CALLBACK, "onOfferwallOpened()", 1);
        if (canSendCallback(this.mOfferwallListener)) {
            sendCallback(new Runnable() {
                public void run() {
                    ListenersWrapper.this.mOfferwallListener.onOfferwallOpened();
                }
            });
        }
    }

    public void onOfferwallShowFailed(final IronSourceError error) {
        IronSourceLoggerManager.getLogger().log(IronSourceTag.CALLBACK, "onOfferwallShowFailed(" + error + ")", 1);
        if (canSendCallback(this.mOfferwallListener)) {
            sendCallback(new Runnable() {
                public void run() {
                    ListenersWrapper.this.mOfferwallListener.onOfferwallShowFailed(error);
                }
            });
        }
    }

    public boolean onOfferwallAdCredited(int credits, int totalCredits, boolean totalCreditsFlag) {
        boolean result = false;
        if (this.mOfferwallListener != null) {
            result = this.mOfferwallListener.onOfferwallAdCredited(credits, totalCredits, totalCreditsFlag);
        }
        IronSourceLoggerManager.getLogger().log(IronSourceTag.CALLBACK, "onOfferwallAdCredited(credits:" + credits + ", " + "totalCredits:" + totalCredits + ", " + "totalCreditsFlag:" + totalCreditsFlag + "):" + result, 1);
        return result;
    }

    public void onGetOfferwallCreditsFailed(final IronSourceError error) {
        IronSourceLoggerManager.getLogger().log(IronSourceTag.CALLBACK, "onGetOfferwallCreditsFailed(" + error + ")", 1);
        if (canSendCallback(this.mOfferwallListener)) {
            sendCallback(new Runnable() {
                public void run() {
                    ListenersWrapper.this.mOfferwallListener.onGetOfferwallCreditsFailed(error);
                }
            });
        }
    }

    public void onOfferwallClosed() {
        IronSourceLoggerManager.getLogger().log(IronSourceTag.CALLBACK, "onOfferwallClosed()", 1);
        if (canSendCallback(this.mOfferwallListener)) {
            sendCallback(new Runnable() {
                public void run() {
                    ListenersWrapper.this.mOfferwallListener.onOfferwallClosed();
                }
            });
        }
    }

    public void onOfferwallAvailable(boolean isAvailable) {
        onOfferwallAvailable(isAvailable, null);
    }

    public void onOfferwallAvailable(final boolean isAvailable, IronSourceError error) {
        String logString = "onOfferwallAvailable(isAvailable: " + isAvailable + ")";
        if (error != null) {
            logString = logString + ", error: " + error.getErrorMessage();
        }
        IronSourceLoggerManager.getLogger().log(IronSourceTag.CALLBACK, logString, 1);
        JSONObject data = IronSourceUtils.getMediationAdditionalData();
        try {
            data.put(ParametersKeys.VIDEO_STATUS, String.valueOf(isAvailable));
            if (error != null) {
                data.put(IronSourceConstants.ERROR_CODE_KEY, error.getErrorCode());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RewardedVideoEventsManager.getInstance().log(new EventData(HttpStatus.SC_MOVED_TEMPORARILY, data));
        if (canSendCallback(this.mOfferwallListener)) {
            sendCallback(new Runnable() {
                public void run() {
                    ListenersWrapper.this.mOfferwallListener.onOfferwallAvailable(isAvailable);
                }
            });
        }
    }

    public void onInterstitialAdRewarded() {
        IronSourceLoggerManager.getLogger().log(IronSourceTag.CALLBACK, "onInterstitialAdRewarded()", 1);
        if (canSendCallback(this.mRewardedInterstitialListener)) {
            sendCallback(new Runnable() {
                public void run() {
                    ListenersWrapper.this.mRewardedInterstitialListener.onInterstitialAdRewarded();
                }
            });
        }
    }
}
