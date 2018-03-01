package com.ironsource.mediationsdk;

import android.app.Activity;
import android.os.Build.VERSION;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;
import com.facebook.internal.ServerProtocol;
import com.ironsource.eventsmodule.EventData;
import com.ironsource.mediationsdk.events.InterstitialEventsManager;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.logger.IronSourceLogger.IronSourceTag;
import com.ironsource.mediationsdk.logger.IronSourceLoggerManager;
import com.ironsource.mediationsdk.sdk.BannerListener;
import com.ironsource.mediationsdk.sdk.BannerManagerListener;
import com.ironsource.mediationsdk.utils.CappingManager;
import com.ironsource.mediationsdk.utils.IronSourceConstants;
import com.ironsource.mediationsdk.utils.IronSourceUtils;
import com.ironsource.sdk.utils.Constants.ParametersKeys;
import cz.msebera.android.httpclient.HttpStatus;
import org.json.JSONObject;

public class IronSourceBannerLayout extends FrameLayout implements BannerAdaptersListener {
    private boolean isAdLoaded = false;
    private boolean isDestoyed = false;
    private boolean isImpressionReported = false;
    private Activity mActivity;
    private AbstractAdapter mAdapter;
    private BannerListener mBannerListener;
    private BannerManagerListener mBannerManager;
    private View mBannerView;
    private String mPlacementName;
    private EBannerSize mSize;

    public IronSourceBannerLayout(Activity activity, EBannerSize size, BannerManagerListener bannerManager) {
        super(activity);
        this.mBannerManager = bannerManager;
        this.mActivity = activity;
        if (size == null) {
            size = EBannerSize.BANNER;
        }
        this.mSize = size;
    }

    public void attachAdapterToBanner(AbstractAdapter adapter, View bannerView) {
        this.mAdapter = adapter;
        this.mBannerView = bannerView;
        resetBannerImpression();
        getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                if (IronSourceBannerLayout.this.isShown()) {
                    if (VERSION.SDK_INT < 16) {
                        IronSourceBannerLayout.this.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        IronSourceBannerLayout.this.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                    if (IronSourceBannerLayout.this.isAdLoaded) {
                        IronSourceBannerLayout.this.reportBannerImpression();
                    }
                }
            }
        });
    }

    public void destroyBanner() {
        this.isDestoyed = true;
        if (this.mAdapter != null) {
            this.mAdapter.destroyBanner(this);
        }
        resetBannerImpression();
        this.mBannerManager = null;
        this.mBannerListener = null;
        this.mActivity = null;
        this.mSize = null;
        this.mPlacementName = null;
        this.mBannerView = null;
    }

    public boolean isDestoyed() {
        return this.isDestoyed;
    }

    public View getBannerView() {
        return this.mBannerView;
    }

    public Activity getActivity() {
        return this.mActivity;
    }

    public EBannerSize getSize() {
        return this.mSize;
    }

    public String getPlacementName() {
        return this.mPlacementName;
    }

    public void setPlacementName(String placementName) {
        this.mPlacementName = placementName;
    }

    public void setBannerListener(BannerListener listener) {
        IronSourceLoggerManager.getLogger().log(IronSourceTag.API, "setBannerListener()", 1);
        this.mBannerListener = listener;
    }

    public void removeBannerListener() {
        IronSourceLoggerManager.getLogger().log(IronSourceTag.API, "removeBannerListener()", 1);
        this.mBannerListener = null;
    }

    public BannerListener getBannerListener() {
        return this.mBannerListener;
    }

    public void onBannerAdLoaded(AbstractAdapter adapter) {
        if (!shoudIgnoreThisCallback(adapter) && !this.isAdLoaded) {
            IronSourceLoggerManager.getLogger().log(IronSourceTag.INTERNAL, "onBannerAdLoaded() | internal | adapter: " + adapter.getProviderName(), 0);
            JSONObject providerData = IronSourceUtils.getProviderAdditionalData(adapter);
            JSONObject mediationData = IronSourceUtils.getMediationAdditionalData();
            try {
                int bannerSizeData = getSize().getValue();
                providerData.put(ParametersKeys.VIDEO_STATUS, ServerProtocol.DIALOG_RETURN_SCOPES_TRUE);
                mediationData.put(ParametersKeys.VIDEO_STATUS, ServerProtocol.DIALOG_RETURN_SCOPES_TRUE);
                providerData.put("bannerAdSize", bannerSizeData);
                mediationData.put("bannerAdSize", bannerSizeData);
            } catch (Exception e) {
                e.printStackTrace();
            }
            EventData providerEvent = new EventData(HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED, providerData);
            EventData mediationEvent = new EventData(HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED, mediationData);
            InterstitialEventsManager.getInstance().log(providerEvent);
            InterstitialEventsManager.getInstance().log(mediationEvent);
            this.isAdLoaded = true;
            if (isShown()) {
                reportBannerImpression();
            }
            if (this.mBannerManager != null) {
                this.mBannerManager.onBannerAdLoaded(adapter);
            }
            if (this.mBannerListener != null) {
                IronSourceLoggerManager.getLogger().log(IronSourceTag.CALLBACK, "onBannerAdLoaded()", 1);
                this.mBannerListener.onBannerAdLoaded();
            }
        }
    }

    public void onBannerAdLoadFailed(IronSourceError error, AbstractAdapter adapter) {
        if (!shoudIgnoreThisCallback(adapter)) {
            IronSourceLoggerManager.getLogger().log(IronSourceTag.INTERNAL, "onBannerAdLoadFailed() | internal | adapter: " + adapter.getProviderName(), 0);
            this.mAdapter = null;
            try {
                if (this.mBannerView != null) {
                    removeView(this.mBannerView);
                    this.mBannerView = null;
                }
            } catch (Exception e) {
            }
            JSONObject data = IronSourceUtils.getProviderAdditionalData(adapter);
            try {
                int bannerSizeData = getSize().getValue();
                data.put(ParametersKeys.VIDEO_STATUS, "false");
                data.put(IronSourceConstants.ERROR_CODE_KEY, error.getErrorCode());
                data.put("bannerAdSize", bannerSizeData);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            InterstitialEventsManager.getInstance().log(new EventData(HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED, data));
            if (this.mBannerManager != null) {
                this.mBannerManager.onBannerAdLoadFailed(error, adapter);
            }
        }
    }

    public void onBannerAdClicked(AbstractAdapter adapter) {
        if (!shoudIgnoreThisCallback(adapter)) {
            IronSourceLoggerManager.getLogger().log(IronSourceTag.INTERNAL, "onBannerAdClicked() | internal | adapter: " + adapter.getProviderName(), 0);
            JSONObject data = IronSourceUtils.getProviderAdditionalData(adapter);
            try {
                data.put("bannerAdSize", getSize().getValue());
            } catch (Exception e) {
                e.printStackTrace();
            }
            InterstitialEventsManager.getInstance().log(new EventData(HttpStatus.SC_REQUEST_TIMEOUT, data));
            if (this.mBannerManager != null) {
                this.mBannerManager.onBannerAdClicked(adapter);
            }
            if (this.mBannerListener != null) {
                IronSourceLoggerManager.getLogger().log(IronSourceTag.CALLBACK, "onBannerAdClicked()", 1);
                this.mBannerListener.onBannerAdClicked();
            }
        }
    }

    public void onBannerAdScreenPresented(AbstractAdapter adapter) {
        if (!shoudIgnoreThisCallback(adapter)) {
            IronSourceLoggerManager.getLogger().log(IronSourceTag.INTERNAL, "onBannerAdScreenPresented() | internal | adapter: " + adapter.getProviderName(), 0);
            if (this.mBannerManager != null) {
                this.mBannerManager.onBannerAdScreenPresented(adapter);
            }
            if (this.mBannerListener != null) {
                IronSourceLoggerManager.getLogger().log(IronSourceTag.CALLBACK, "onBannerAdScreenPresented()", 1);
                this.mBannerListener.onBannerAdScreenPresented();
            }
        }
    }

    public void onBannerAdScreenDismissed(AbstractAdapter adapter) {
        if (!shoudIgnoreThisCallback(adapter)) {
            IronSourceLoggerManager.getLogger().log(IronSourceTag.INTERNAL, "onBannerAdScreenDismissed() | internal | adapter: " + adapter.getProviderName(), 0);
            if (this.mBannerManager != null) {
                this.mBannerManager.onBannerAdScreenDismissed(adapter);
            }
            if (this.mBannerListener != null) {
                IronSourceLoggerManager.getLogger().log(IronSourceTag.CALLBACK, "onBannerAdScreenDismissed()", 1);
                this.mBannerListener.onBannerAdScreenDismissed();
            }
        }
    }

    public void onBannerAdLeftApplication(AbstractAdapter adapter) {
        if (!shoudIgnoreThisCallback(adapter)) {
            IronSourceLoggerManager.getLogger().log(IronSourceTag.INTERNAL, "onBannerAdLeftApplication() | internal | adapter: " + adapter.getProviderName(), 0);
            if (this.mBannerManager != null) {
                this.mBannerManager.onBannerAdLeftApplication(adapter);
            }
            if (this.mBannerListener != null) {
                IronSourceLoggerManager.getLogger().log(IronSourceTag.CALLBACK, "onBannerAdLeftApplication()", 1);
                this.mBannerListener.onBannerAdLeftApplication();
            }
        }
    }

    private boolean shoudIgnoreThisCallback(AbstractAdapter adapter) {
        return this.mAdapter == null || adapter == null || !this.mAdapter.getProviderName().equals(adapter.getProviderName());
    }

    private synchronized void resetBannerImpression() {
        this.isImpressionReported = false;
        this.isAdLoaded = false;
    }

    private synchronized void reportBannerImpression() {
        if (!this.isImpressionReported) {
            this.isImpressionReported = true;
            CappingManager.incrementShowCounter(this.mActivity, this.mPlacementName);
            if (this.mBannerManager != null) {
                this.mBannerManager.onBannerImpression(this.mAdapter, this);
            }
        }
    }
}
