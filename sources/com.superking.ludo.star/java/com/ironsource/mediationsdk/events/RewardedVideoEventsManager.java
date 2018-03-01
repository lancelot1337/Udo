package com.ironsource.mediationsdk.events;

import com.ironsource.eventsmodule.EventData;
import com.ironsource.mediationsdk.utils.IronSourceConstants;
import com.ironsource.mediationsdk.utils.SessionDepthManager;
import cz.msebera.android.httpclient.HttpStatus;
import org.cocos2dx.lib.BuildConfig;

public class RewardedVideoEventsManager extends BaseEventsManager {
    private static RewardedVideoEventsManager sInstance;
    private String mCurrentOWPlacment;
    private String mCurrentRVPlacment;

    private RewardedVideoEventsManager() {
        this.mFormatterType = EventsFormatterFactory.TYPE_OUTCOME;
        this.mAdUnitType = 3;
        this.mEventType = IronSourceConstants.REWARDED_VIDEO_EVENT_TYPE;
        this.mCurrentRVPlacment = BuildConfig.FLAVOR;
        this.mCurrentOWPlacment = BuildConfig.FLAVOR;
    }

    public static RewardedVideoEventsManager getInstance() {
        if (sInstance == null) {
            sInstance = new RewardedVideoEventsManager();
            sInstance.initState();
        }
        return sInstance;
    }

    protected boolean shouldExtractCurrentPlacement(EventData event) {
        return event.getEventId() == 2 || event.getEventId() == 10;
    }

    protected boolean shouldIncludeCurrentPlacement(EventData event) {
        return event.getEventId() == 5 || event.getEventId() == 6 || event.getEventId() == 8 || event.getEventId() == 9 || event.getEventId() == 19 || event.getEventId() == 20 || event.getEventId() == HttpStatus.SC_USE_PROXY;
    }

    protected boolean isTopPriorityEvent(EventData currentEvent) {
        return currentEvent.getEventId() == 6 || currentEvent.getEventId() == 10 || currentEvent.getEventId() == 14 || currentEvent.getEventId() == HttpStatus.SC_USE_PROXY;
    }

    protected int getSessionDepth(EventData event) {
        int sessionDepth = SessionDepthManager.getInstance().getSessionDepth(1);
        if (event.getEventId() == 15 || (event.getEventId() >= HttpStatus.SC_MULTIPLE_CHOICES && event.getEventId() < HttpStatus.SC_BAD_REQUEST)) {
            return SessionDepthManager.getInstance().getSessionDepth(0);
        }
        return sessionDepth;
    }

    protected void setCurrentPlacement(EventData event) {
        if (event.getEventId() == 15 || (event.getEventId() >= HttpStatus.SC_MULTIPLE_CHOICES && event.getEventId() < HttpStatus.SC_BAD_REQUEST)) {
            this.mCurrentOWPlacment = event.getAdditionalDataJSON().optString("placement");
        } else {
            this.mCurrentRVPlacment = event.getAdditionalDataJSON().optString("placement");
        }
    }

    protected String getCurrentPlacement(int eventId) {
        if (eventId == 15 || (eventId >= HttpStatus.SC_MULTIPLE_CHOICES && eventId < HttpStatus.SC_BAD_REQUEST)) {
            return this.mCurrentOWPlacment;
        }
        return this.mCurrentRVPlacment;
    }

    protected boolean increaseSessionDepthIfNeeded(EventData event) {
        if (event.getEventId() == 6) {
            SessionDepthManager.getInstance().increaseSessionDepth(1);
        } else if (event.getEventId() == HttpStatus.SC_USE_PROXY) {
            SessionDepthManager.getInstance().increaseSessionDepth(0);
        }
        return false;
    }
}
