package com.ironsource.mediationsdk.logger;

import com.facebook.share.internal.ShareConstants;
import com.google.firebase.analytics.FirebaseAnalytics.Param;
import com.ironsource.mediationsdk.logger.IronSourceLogger.IronSourceTag;
import org.json.JSONException;
import org.json.JSONObject;

class ServerLogEntry {
    private int mLogLevel;
    private String mMessage;
    private IronSourceTag mTag;
    private String mTimetamp;

    public ServerLogEntry(IronSourceTag tag, String timestamp, String message, int level) {
        this.mTag = tag;
        this.mTimetamp = timestamp;
        this.mMessage = message;
        this.mLogLevel = level;
    }

    public JSONObject toJSON() {
        JSONObject result = new JSONObject();
        try {
            result.put(EventEntry.COLUMN_NAME_TIMESTAMP, this.mTimetamp);
            result.put("tag", this.mTag);
            result.put(Param.LEVEL, this.mLogLevel);
            result.put(ShareConstants.WEB_DIALOG_PARAM_MESSAGE, this.mMessage);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public int getLogLevel() {
        return this.mLogLevel;
    }
}
