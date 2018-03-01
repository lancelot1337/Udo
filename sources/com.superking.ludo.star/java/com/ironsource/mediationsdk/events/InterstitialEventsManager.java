package com.ironsource.mediationsdk.events;

import com.ironsource.eventsmodule.EventData;
import com.ironsource.mediationsdk.utils.IronSourceConstants;
import com.ironsource.mediationsdk.utils.SessionDepthManager;
import cz.msebera.android.httpclient.HttpStatus;
import org.cocos2dx.lib.BuildConfig;

public class InterstitialEventsManager extends BaseEventsManager {
    private static InterstitialEventsManager sInstance;
    private String mCurrentBNPlacement;
    private String mCurrentISPlacement;

    private InterstitialEventsManager() {
        this.mFormatterType = EventsFormatterFactory.TYPE_IRONBEAST;
        this.mAdUnitType = 2;
        this.mEventType = IronSourceConstants.INTERSTITIAL_EVENT_TYPE;
        this.mCurrentISPlacement = BuildConfig.FLAVOR;
        this.mCurrentBNPlacement = BuildConfig.FLAVOR;
    }

    public static InterstitialEventsManager getInstance() {
        if (sInstance == null) {
            sInstance = new InterstitialEventsManager();
            sInstance.initState();
        }
        return sInstance;
    }

    protected boolean shouldExtractCurrentPlacement(EventData event) {
        return event.getEventId() == 23 || event.getEventId() == HttpStatus.SC_PAYMENT_REQUIRED;
    }

    protected boolean shouldIncludeCurrentPlacement(EventData event) {
        return event.getEventId() == 25 || event.getEventId() == 26 || event.getEventId() == 28 || event.getEventId() == 29 || event.getEventId() == 34 || event.getEventId() == HttpStatus.SC_METHOD_NOT_ALLOWED || event.getEventId() == HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED || event.getEventId() == HttpStatus.SC_REQUEST_TIMEOUT || event.getEventId() == HttpStatus.SC_REQUEST_URI_TOO_LONG;
    }

    protected boolean isTopPriorityEvent(EventData currentEvent) {
        return currentEvent.getEventId() == 26 || currentEvent.getEventId() == HttpStatus.SC_METHOD_NOT_ALLOWED;
    }

    protected int getSessionDepth(EventData event) {
        int sessionDepth = SessionDepthManager.getInstance().getSessionDepth(2);
        if (event.getEventId() < HttpStatus.SC_BAD_REQUEST || event.getEventId() >= HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            return sessionDepth;
        }
        return SessionDepthManager.getInstance().getSessionDepth(3);
    }

    protected boolean increaseSessionDepthIfNeeded(EventData event) {
        if (event.getEventId() == 26) {
            SessionDepthManager.getInstance().increaseSessionDepth(2);
            return false;
        } else if (event.getEventId() != HttpStatus.SC_PAYMENT_REQUIRED || !getProviderNameForEvent(event).equals(IronSourceConstants.MEDIATION_PROVIDER_NAME)) {
            return false;
        } else {
            SessionDepthManager.getInstance().increaseSessionDepth(3);
            return true;
        }
    }

    protected void setCurrentPlacement(EventData event) {
        if (event.getEventId() < HttpStatus.SC_BAD_REQUEST || event.getEventId() >= HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            this.mCurrentISPlacement = event.getAdditionalDataJSON().optString("placement");
        } else {
            this.mCurrentBNPlacement = event.getAdditionalDataJSON().optString("placement");
        }
    }

    protected String getCurrentPlacement(int eventId) {
        if (eventId < HttpStatus.SC_BAD_REQUEST || eventId >= HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            return this.mCurrentISPlacement;
        }
        return this.mCurrentBNPlacement;
    }
}
