package com.ironsource.mediationsdk.events;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import com.ironsource.eventsmodule.DataBaseEventsStorage;
import com.ironsource.eventsmodule.EventData;
import com.ironsource.eventsmodule.EventsSender;
import com.ironsource.eventsmodule.IEventsManager;
import com.ironsource.eventsmodule.IEventsSenderResultListener;
import com.ironsource.mediationsdk.sdk.GeneralProperties;
import com.ironsource.mediationsdk.utils.IronSourceUtils;
import com.ironsource.sdk.precache.DownloadManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import org.cocos2dx.lib.BuildConfig;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class BaseEventsManager implements IEventsManager {
    public static final String KEY_SESSION_DEPTH = "sessionDepth";
    final String DATABASE_NAME = "supersonic_sdk.db";
    final int DATABASE_VERSION = 5;
    final int DEFAULT_BACKUP_THRESHOLD = 1;
    final int DEFAULT_MAX_EVENTS_PER_BATCH = DownloadManager.OPERATION_TIMEOUT;
    final int DEFAULT_MAX_NUMBER_OF_EVENTS = 100;
    final String KEY_PLACEMENT = "placement";
    final String KEY_PROVIDER = "provider";
    protected int mAdUnitType;
    protected int mBackupThreshold = 1;
    protected String mCurrentPlacement;
    protected DataBaseEventsStorage mDbStorage;
    private EventThread mEventThread;
    protected String mEventType;
    protected AbstractEventsFormatter mFormatter;
    protected String mFormatterType;
    protected boolean mHadTopPriorityEvent = false;
    protected boolean mHasServerResponse;
    protected boolean mIsEventsEnabled = true;
    protected ArrayList<EventData> mLocalEvents;
    protected int mMaxEventsPerBatch = DownloadManager.OPERATION_TIMEOUT;
    protected int mMaxNumberOfEvents = 100;
    protected int[] mOptOutEvents;
    protected int mTotalEvents;

    private class EventThread extends HandlerThread {
        private Handler mHandler;

        public EventThread(String name) {
            super(name);
        }

        public void postTask(Runnable task) {
            this.mHandler.post(task);
        }

        public void prepareHandler() {
            this.mHandler = new Handler(getLooper());
        }
    }

    protected abstract String getCurrentPlacement(int i);

    protected abstract int getSessionDepth(EventData eventData);

    protected abstract boolean increaseSessionDepthIfNeeded(EventData eventData);

    protected abstract boolean isTopPriorityEvent(EventData eventData);

    protected abstract void setCurrentPlacement(EventData eventData);

    protected abstract boolean shouldExtractCurrentPlacement(EventData eventData);

    protected abstract boolean shouldIncludeCurrentPlacement(EventData eventData);

    protected void initState() {
        this.mLocalEvents = new ArrayList();
        this.mTotalEvents = 0;
        this.mCurrentPlacement = BuildConfig.FLAVOR;
        this.mFormatter = EventsFormatterFactory.getFormatter(this.mFormatterType, this.mAdUnitType);
        this.mEventThread = new EventThread(this.mEventType + "EventThread");
        this.mEventThread.start();
        this.mEventThread.prepareHandler();
    }

    public synchronized void start(Context context) {
        this.mFormatterType = IronSourceUtils.getDefaultEventsFormatterType(context, this.mEventType, this.mFormatterType);
        verifyCurrentFormatter(this.mFormatterType);
        this.mFormatter.setEventsServerUrl(IronSourceUtils.getDefaultEventsURL(context, this.mEventType, null));
        this.mDbStorage = DataBaseEventsStorage.getInstance(context, "supersonic_sdk.db", 5);
        backupEventsToDb();
        this.mOptOutEvents = IronSourceUtils.getDefaultOptOutEvents(context, this.mEventType);
    }

    public synchronized void log(final EventData event) {
        this.mEventThread.postTask(new Runnable() {
            public void run() {
                if (event != null && BaseEventsManager.this.mIsEventsEnabled) {
                    if (BaseEventsManager.this.shouldEventBeLogged(event)) {
                        int sessionDepth = BaseEventsManager.this.getSessionDepth(event);
                        if (BaseEventsManager.this.increaseSessionDepthIfNeeded(event)) {
                            sessionDepth = BaseEventsManager.this.getSessionDepth(event);
                        }
                        event.addToAdditionalData(BaseEventsManager.KEY_SESSION_DEPTH, Integer.valueOf(sessionDepth));
                        if (BaseEventsManager.this.shouldExtractCurrentPlacement(event)) {
                            BaseEventsManager.this.setCurrentPlacement(event);
                        } else if (!TextUtils.isEmpty(BaseEventsManager.this.getCurrentPlacement(event.getEventId())) && BaseEventsManager.this.shouldIncludeCurrentPlacement(event)) {
                            event.addToAdditionalData("placement", BaseEventsManager.this.getCurrentPlacement(event.getEventId()));
                        }
                        BaseEventsManager.this.mLocalEvents.add(event);
                        BaseEventsManager baseEventsManager = BaseEventsManager.this;
                        baseEventsManager.mTotalEvents++;
                    }
                    boolean isTopPriority = BaseEventsManager.this.isTopPriorityEvent(event);
                    if (!BaseEventsManager.this.mHadTopPriorityEvent && isTopPriority) {
                        BaseEventsManager.this.mHadTopPriorityEvent = true;
                    }
                    if (BaseEventsManager.this.mDbStorage == null) {
                        return;
                    }
                    if (BaseEventsManager.this.shouldSendEvents()) {
                        BaseEventsManager.this.sendEvents();
                    } else if (BaseEventsManager.this.shouldBackupEventsToDb(BaseEventsManager.this.mLocalEvents) || isTopPriority) {
                        BaseEventsManager.this.backupEventsToDb();
                    }
                }
            }
        });
    }

    private void sendEvents() {
        this.mHadTopPriorityEvent = false;
        ArrayList<EventData> combinedEventList = initCombinedEventList(this.mLocalEvents, this.mDbStorage.loadEvents(this.mEventType), this.mMaxEventsPerBatch);
        this.mLocalEvents.clear();
        this.mDbStorage.clearEvents(this.mEventType);
        this.mTotalEvents = 0;
        if (combinedEventList.size() > 0) {
            String dataToSend = this.mFormatter.format(combinedEventList, GeneralProperties.getProperties().toJSON());
            new EventsSender(new IEventsSenderResultListener() {
                public synchronized void onEventsSenderResult(final ArrayList<EventData> extraData, final boolean success) {
                    BaseEventsManager.this.mEventThread.postTask(new Runnable() {
                        public void run() {
                            if (success) {
                                ArrayList<EventData> events = BaseEventsManager.this.mDbStorage.loadEvents(BaseEventsManager.this.mEventType);
                                BaseEventsManager.this.mTotalEvents = events.size() + BaseEventsManager.this.mLocalEvents.size();
                            } else if (extraData != null) {
                                BaseEventsManager.this.mDbStorage.saveEvents(extraData, BaseEventsManager.this.mEventType);
                                ArrayList<EventData> storedEvents = BaseEventsManager.this.mDbStorage.loadEvents(BaseEventsManager.this.mEventType);
                                BaseEventsManager.this.mTotalEvents = storedEvents.size() + BaseEventsManager.this.mLocalEvents.size();
                            }
                        }
                    });
                }
            }).execute(new Object[]{dataToSend, this.mFormatter.getEventsServerUrl(), combinedEventList});
        }
    }

    protected ArrayList<EventData> initCombinedEventList(ArrayList<EventData> localEvents, ArrayList<EventData> storedEvents, int maxSize) {
        ArrayList<EventData> allEvents = new ArrayList();
        allEvents.addAll(localEvents);
        allEvents.addAll(storedEvents);
        Collections.sort(allEvents, new Comparator<EventData>() {
            public int compare(EventData event1, EventData event2) {
                if (event1.getTimeStamp() >= event2.getTimeStamp()) {
                    return 1;
                }
                return -1;
            }
        });
        if (allEvents.size() <= maxSize) {
            return new ArrayList(allEvents);
        }
        ArrayList<EventData> result = new ArrayList(allEvents.subList(0, maxSize));
        this.mDbStorage.saveEvents(allEvents.subList(maxSize, allEvents.size()), this.mEventType);
        return result;
    }

    protected void verifyCurrentFormatter(String formatterType) {
        if (this.mFormatter == null || !this.mFormatter.getFormatterType().equals(formatterType)) {
            this.mFormatter = EventsFormatterFactory.getFormatter(formatterType, this.mAdUnitType);
        }
    }

    public void setBackupThreshold(int backupThreshold) {
        if (backupThreshold > 0) {
            this.mBackupThreshold = backupThreshold;
        }
    }

    public void setMaxNumberOfEvents(int maxNumberOfEvents) {
        if (maxNumberOfEvents > 0) {
            this.mMaxNumberOfEvents = maxNumberOfEvents;
        }
    }

    public void setMaxEventsPerBatch(int maxEventsPerBatch) {
        if (maxEventsPerBatch > 0) {
            this.mMaxEventsPerBatch = maxEventsPerBatch;
        }
    }

    public void setOptOutEvents(int[] optOutEvents, Context context) {
        this.mOptOutEvents = optOutEvents;
        IronSourceUtils.saveDefaultOptOutEvents(context, this.mEventType, optOutEvents);
    }

    public void setEventsUrl(String eventsUrl, Context context) {
        if (!TextUtils.isEmpty(eventsUrl)) {
            if (this.mFormatter != null) {
                this.mFormatter.setEventsServerUrl(eventsUrl);
            }
            IronSourceUtils.saveDefaultEventsURL(context, this.mEventType, eventsUrl);
        }
    }

    public void setFormatterType(String formatterType, Context context) {
        if (!TextUtils.isEmpty(formatterType)) {
            this.mFormatterType = formatterType;
            IronSourceUtils.saveDefaultEventsFormatterType(context, this.mEventType, formatterType);
            verifyCurrentFormatter(formatterType);
        }
    }

    public void setIsEventsEnabled(boolean isEnabled) {
        this.mIsEventsEnabled = isEnabled;
    }

    protected void backupEventsToDb() {
        this.mDbStorage.saveEvents(this.mLocalEvents, this.mEventType);
        this.mLocalEvents.clear();
    }

    protected boolean shouldSendEvents() {
        return (this.mTotalEvents >= this.mMaxNumberOfEvents || this.mHadTopPriorityEvent) && this.mHasServerResponse;
    }

    protected boolean shouldBackupEventsToDb(ArrayList<EventData> events) {
        if (events != null) {
            return events.size() >= this.mBackupThreshold;
        } else {
            return false;
        }
    }

    protected boolean shouldEventBeLogged(EventData event) {
        if (event == null || this.mOptOutEvents == null || this.mOptOutEvents.length <= 0) {
            return true;
        }
        int eventId = event.getEventId();
        for (int i : this.mOptOutEvents) {
            if (eventId == i) {
                return false;
            }
        }
        return true;
    }

    public void setHasServerResponse(boolean hasResponse) {
        this.mHasServerResponse = hasResponse;
    }

    protected String getProviderNameForEvent(EventData event) {
        try {
            return new JSONObject(event.getAdditionalData()).optString("provider", BuildConfig.FLAVOR);
        } catch (JSONException e) {
            return BuildConfig.FLAVOR;
        }
    }

    public void triggerEventsSend() {
        sendEvents();
    }
}
