package io.branch.referral;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import io.branch.referral.Defines.LinkParam;
import java.util.Collection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class BranchLinkData extends JSONObject {
    private String alias;
    private String campaign;
    private String channel;
    private int duration;
    private String feature;
    private String params;
    private String stage;
    private Collection<String> tags;
    private int type;

    public void putTags(Collection<String> tags) throws JSONException {
        if (tags != null) {
            this.tags = tags;
            JSONArray tagArray = new JSONArray();
            for (String tag : tags) {
                tagArray.put(tag);
            }
            put(LinkParam.Tags.getKey(), tagArray);
        }
    }

    public Collection<String> getTags() {
        return this.tags;
    }

    public void putAlias(String alias) throws JSONException {
        if (alias != null) {
            this.alias = alias;
            put(LinkParam.Alias.getKey(), alias);
        }
    }

    public String getAlias() {
        return this.alias;
    }

    public void putType(int type) throws JSONException {
        if (type != 0) {
            this.type = type;
            put(LinkParam.Type.getKey(), type);
        }
    }

    public int getType() {
        return this.type;
    }

    public void putDuration(int duration) throws JSONException {
        if (duration > 0) {
            this.duration = duration;
            put(LinkParam.Duration.getKey(), duration);
        }
    }

    public int getDuration() {
        return this.duration;
    }

    public void putChannel(String channel) throws JSONException {
        if (channel != null) {
            this.channel = channel;
            put(LinkParam.Channel.getKey(), channel);
        }
    }

    public String getChannel() {
        return this.channel;
    }

    public void putFeature(String feature) throws JSONException {
        if (feature != null) {
            this.feature = feature;
            put(LinkParam.Feature.getKey(), feature);
        }
    }

    public String getFeature() {
        return this.feature;
    }

    public void putStage(String stage) throws JSONException {
        if (stage != null) {
            this.stage = stage;
            put(LinkParam.Stage.getKey(), stage);
        }
    }

    public String getStage() {
        return this.stage;
    }

    public void putCampaign(String campaign) throws JSONException {
        if (campaign != null) {
            this.campaign = campaign;
            put(LinkParam.Campaign.getKey(), campaign);
        }
    }

    public String getCampaign() {
        return this.campaign;
    }

    public void putParams(String params) throws JSONException {
        this.params = params;
        put(LinkParam.Data.getKey(), params);
    }

    public String getParams() {
        return this.params;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BranchLinkData other = (BranchLinkData) obj;
        if (this.alias == null) {
            if (other.alias != null) {
                return false;
            }
        } else if (!this.alias.equals(other.alias)) {
            return false;
        }
        if (this.channel == null) {
            if (other.channel != null) {
                return false;
            }
        } else if (!this.channel.equals(other.channel)) {
            return false;
        }
        if (this.feature == null) {
            if (other.feature != null) {
                return false;
            }
        } else if (!this.feature.equals(other.feature)) {
            return false;
        }
        if (this.params == null) {
            if (other.params != null) {
                return false;
            }
        } else if (!this.params.equals(other.params)) {
            return false;
        }
        if (this.stage == null) {
            if (other.stage != null) {
                return false;
            }
        } else if (!this.stage.equals(other.stage)) {
            return false;
        }
        if (this.campaign == null) {
            if (other.campaign != null) {
                return false;
            }
        } else if (!this.campaign.equals(other.campaign)) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        if (this.duration != other.duration) {
            return false;
        }
        if (this.tags == null) {
            if (other.tags != null) {
                return false;
            }
            return true;
        } else if (this.tags.toString().equals(other.tags.toString())) {
            return true;
        } else {
            return false;
        }
    }

    @SuppressLint({"DefaultLocale"})
    public int hashCode() {
        int i = 0;
        int hashCode = 19 * ((19 * ((19 * ((19 * ((19 * ((19 * (this.type + 19)) + (this.alias == null ? 0 : this.alias.toLowerCase().hashCode()))) + (this.channel == null ? 0 : this.channel.toLowerCase().hashCode()))) + (this.feature == null ? 0 : this.feature.toLowerCase().hashCode()))) + (this.stage == null ? 0 : this.stage.toLowerCase().hashCode()))) + (this.campaign == null ? 0 : this.campaign.toLowerCase().hashCode()));
        if (this.params != null) {
            i = this.params.toLowerCase().hashCode();
        }
        int result = (19 * (hashCode + i)) + this.duration;
        if (this.tags != null) {
            for (String tag : this.tags) {
                result = (19 * result) + tag.toLowerCase().hashCode();
            }
        }
        return result;
    }

    public JSONObject getLinkDataJsonObject() {
        JSONObject linkDataJson = new JSONObject();
        try {
            if (!TextUtils.isEmpty(this.channel)) {
                linkDataJson.put("~" + LinkParam.Channel.getKey(), this.channel);
            }
            if (!TextUtils.isEmpty(this.alias)) {
                linkDataJson.put("~" + LinkParam.Alias.getKey(), this.alias);
            }
            if (!TextUtils.isEmpty(this.feature)) {
                linkDataJson.put("~" + LinkParam.Feature.getKey(), this.feature);
            }
            if (!TextUtils.isEmpty(this.stage)) {
                linkDataJson.put("~" + LinkParam.Stage.getKey(), this.stage);
            }
            if (!TextUtils.isEmpty(this.campaign)) {
                linkDataJson.put("~" + LinkParam.Campaign.getKey(), this.campaign);
            }
            if (has(LinkParam.Tags.getKey())) {
                linkDataJson.put(LinkParam.Tags.getKey(), getJSONArray(LinkParam.Tags.getKey()));
            }
            linkDataJson.put("~" + LinkParam.Type.getKey(), this.type);
            linkDataJson.put("~" + LinkParam.Duration.getKey(), this.duration);
        } catch (JSONException e) {
        }
        return linkDataJson;
    }
}
