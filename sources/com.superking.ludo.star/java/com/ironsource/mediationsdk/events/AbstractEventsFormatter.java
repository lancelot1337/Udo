package com.ironsource.mediationsdk.events;

import android.text.TextUtils;
import com.ironsource.eventsmodule.EventData;
import com.ironsource.mediationsdk.utils.IronSourceUtils;
import io.branch.referral.R;
import java.util.ArrayList;
import org.cocos2dx.lib.BuildConfig;
import org.cocos2dx.lib.Cocos2dxEditBox;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class AbstractEventsFormatter {
    private final String EVENTS_KEY_DEFAULT = EventEntry.TABLE_NAME;
    private final String EVENTS_KEY_IS = "InterstitialEvents";
    private final String EVENTS_KEY_RV = EventEntry.TABLE_NAME;
    private final String KEY_AD_UNIT = "adUnit";
    private final String KEY_EVENT_ID = "eventId";
    private final String KEY_TIMESTAMP = EventEntry.COLUMN_NAME_TIMESTAMP;
    int mAdUnit;
    JSONObject mGeneralProperties;
    private String mServerUrl;

    public abstract String format(ArrayList<EventData> arrayList, JSONObject jSONObject);

    protected abstract String getDefaultEventsUrl();

    public abstract String getFormatterType();

    private String getEventsKey(int adUnit) {
        switch (adUnit) {
            case R.styleable.View_paddingStart /*2*/:
                return "InterstitialEvents";
            case Cocos2dxEditBox.kEndActionReturn /*3*/:
                return EventEntry.TABLE_NAME;
            default:
                return EventEntry.TABLE_NAME;
        }
    }

    JSONObject createJSONForEvent(EventData event) {
        try {
            JSONObject jsonEvent = new JSONObject(event.getAdditionalData());
            jsonEvent.put("eventId", event.getEventId());
            jsonEvent.put(EventEntry.COLUMN_NAME_TIMESTAMP, event.getTimeStamp());
            return jsonEvent;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    String createDataToSend(JSONArray eventsArray) {
        String result = BuildConfig.FLAVOR;
        try {
            if (this.mGeneralProperties != null) {
                JSONObject data = new JSONObject(this.mGeneralProperties.toString());
                data.put(EventEntry.COLUMN_NAME_TIMESTAMP, IronSourceUtils.getTimeStamp());
                data.put("adUnit", this.mAdUnit);
                data.put(getEventsKey(this.mAdUnit), eventsArray);
                result = data.toString();
            }
        } catch (Exception e) {
        }
        return result;
    }

    public String getEventsServerUrl() {
        return TextUtils.isEmpty(this.mServerUrl) ? getDefaultEventsUrl() : this.mServerUrl;
    }

    public void setEventsServerUrl(String url) {
        this.mServerUrl = url;
    }
}
