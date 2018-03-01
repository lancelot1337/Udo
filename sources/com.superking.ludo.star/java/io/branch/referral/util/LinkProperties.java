package io.branch.referral.util;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import io.branch.referral.Branch;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import org.cocos2dx.lib.BuildConfig;
import org.json.JSONArray;
import org.json.JSONObject;

public class LinkProperties implements Parcelable {
    public static final Creator CREATOR = new Creator() {
        public LinkProperties createFromParcel(Parcel in) {
            return new LinkProperties(in);
        }

        public LinkProperties[] newArray(int size) {
            return new LinkProperties[size];
        }
    };
    private String alias_;
    private String campaign_;
    private String channel_;
    private final HashMap<String, String> controlParams_;
    private String feature_;
    private int matchDuration_;
    private String stage_;
    private final ArrayList<String> tags_;

    public LinkProperties() {
        this.tags_ = new ArrayList();
        this.feature_ = "Share";
        this.controlParams_ = new HashMap();
        this.alias_ = BuildConfig.FLAVOR;
        this.stage_ = BuildConfig.FLAVOR;
        this.matchDuration_ = 0;
        this.channel_ = BuildConfig.FLAVOR;
        this.campaign_ = BuildConfig.FLAVOR;
    }

    public LinkProperties setAlias(String alias) {
        this.alias_ = alias;
        return this;
    }

    public LinkProperties addTag(String tag) {
        this.tags_.add(tag);
        return this;
    }

    public LinkProperties addControlParameter(String key, String value) {
        this.controlParams_.put(key, value);
        return this;
    }

    public LinkProperties setFeature(String feature) {
        this.feature_ = feature;
        return this;
    }

    public LinkProperties setDuration(int duration) {
        this.matchDuration_ = duration;
        return this;
    }

    public LinkProperties setStage(String stage) {
        this.stage_ = stage;
        return this;
    }

    public LinkProperties setChannel(String channel) {
        this.channel_ = channel;
        return this;
    }

    public LinkProperties setCampaign(String campaign) {
        this.campaign_ = campaign;
        return this;
    }

    public ArrayList<String> getTags() {
        return this.tags_;
    }

    public HashMap<String, String> getControlParams() {
        return this.controlParams_;
    }

    public int getMatchDuration() {
        return this.matchDuration_;
    }

    public String getAlias() {
        return this.alias_;
    }

    public String getFeature() {
        return this.feature_;
    }

    public String getStage() {
        return this.stage_;
    }

    public String getChannel() {
        return this.channel_;
    }

    public String getCampaign() {
        return this.campaign_;
    }

    public int describeContents() {
        return 0;
    }

    public static LinkProperties getReferredLinkProperties() {
        Branch branchInstance = Branch.getInstance();
        if (branchInstance == null || branchInstance.getLatestReferringParams() == null) {
            return null;
        }
        JSONObject latestParam = branchInstance.getLatestReferringParams();
        try {
            if (!latestParam.has("+clicked_branch_link") || !latestParam.getBoolean("+clicked_branch_link")) {
                return null;
            }
            LinkProperties linkProperties = new LinkProperties();
            try {
                if (latestParam.has("~channel")) {
                    linkProperties.setChannel(latestParam.getString("~channel"));
                }
                if (latestParam.has("~feature")) {
                    linkProperties.setFeature(latestParam.getString("~feature"));
                }
                if (latestParam.has("~stage")) {
                    linkProperties.setStage(latestParam.getString("~stage"));
                }
                if (latestParam.has("~campaign")) {
                    linkProperties.setCampaign(latestParam.getString("~campaign"));
                }
                if (latestParam.has("~duration")) {
                    linkProperties.setDuration(latestParam.getInt("~duration"));
                }
                if (latestParam.has("$match_duration")) {
                    linkProperties.setDuration(latestParam.getInt("$match_duration"));
                }
                if (latestParam.has("~tags")) {
                    JSONArray tagsArray = latestParam.getJSONArray("~tags");
                    for (int i = 0; i < tagsArray.length(); i++) {
                        linkProperties.addTag(tagsArray.getString(i));
                    }
                }
                Iterator<String> keys = latestParam.keys();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    if (key.startsWith("$")) {
                        linkProperties.addControlParameter(key, latestParam.getString(key));
                    }
                }
                return linkProperties;
            } catch (Exception e) {
                return linkProperties;
            }
        } catch (Exception e2) {
            return null;
        }
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.feature_);
        dest.writeString(this.alias_);
        dest.writeString(this.stage_);
        dest.writeString(this.channel_);
        dest.writeString(this.campaign_);
        dest.writeInt(this.matchDuration_);
        dest.writeSerializable(this.tags_);
        dest.writeInt(this.controlParams_.size());
        for (Entry<String, String> entry : this.controlParams_.entrySet()) {
            dest.writeString((String) entry.getKey());
            dest.writeString((String) entry.getValue());
        }
    }

    private LinkProperties(Parcel in) {
        this();
        this.feature_ = in.readString();
        this.alias_ = in.readString();
        this.stage_ = in.readString();
        this.channel_ = in.readString();
        this.campaign_ = in.readString();
        this.matchDuration_ = in.readInt();
        this.tags_.addAll((ArrayList) in.readSerializable());
        int controlPramSize = in.readInt();
        for (int i = 0; i < controlPramSize; i++) {
            this.controlParams_.put(in.readString(), in.readString());
        }
    }
}
