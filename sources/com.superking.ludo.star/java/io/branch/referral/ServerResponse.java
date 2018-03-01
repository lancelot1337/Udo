package io.branch.referral;

import com.facebook.internal.NativeProtocol;
import com.facebook.share.internal.ShareConstants;
import org.cocos2dx.lib.BuildConfig;
import org.json.JSONArray;
import org.json.JSONObject;

public class ServerResponse {
    private Object post_;
    private int statusCode_;
    private String tag_;

    public ServerResponse(String tag, int statusCode) {
        this.tag_ = tag;
        this.statusCode_ = statusCode;
    }

    public String getTag() {
        return this.tag_;
    }

    public int getStatusCode() {
        return this.statusCode_;
    }

    public void setPost(Object post) {
        this.post_ = post;
    }

    public JSONObject getObject() {
        if (this.post_ instanceof JSONObject) {
            return (JSONObject) this.post_;
        }
        return new JSONObject();
    }

    public JSONArray getArray() {
        if (this.post_ instanceof JSONArray) {
            return (JSONArray) this.post_;
        }
        return null;
    }

    public String getFailReason() {
        String causeMsg = BuildConfig.FLAVOR;
        try {
            JSONObject postObj = getObject();
            if (postObj != null && postObj.has(NativeProtocol.BRIDGE_ARG_ERROR_BUNDLE) && postObj.getJSONObject(NativeProtocol.BRIDGE_ARG_ERROR_BUNDLE).has(ShareConstants.WEB_DIALOG_PARAM_MESSAGE)) {
                causeMsg = postObj.getJSONObject(NativeProtocol.BRIDGE_ARG_ERROR_BUNDLE).getString(ShareConstants.WEB_DIALOG_PARAM_MESSAGE);
                if (causeMsg != null && causeMsg.trim().length() > 0) {
                    causeMsg = causeMsg + ".";
                }
            }
        } catch (Exception e) {
        }
        return causeMsg;
    }
}
