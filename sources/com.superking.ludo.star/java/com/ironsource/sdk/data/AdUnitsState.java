package com.ironsource.sdk.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.cocos2dx.lib.BuildConfig;
import org.json.JSONException;
import org.json.JSONObject;

public class AdUnitsState implements Parcelable {
    public static final Creator<AdUnitsState> CREATOR = new Creator<AdUnitsState>() {
        public AdUnitsState createFromParcel(Parcel source) {
            return new AdUnitsState(source);
        }

        public AdUnitsState[] newArray(int size) {
            return new AdUnitsState[size];
        }
    };
    private String mDisplayedDemandSourceName;
    private int mDisplayedProduct;
    private String mInterstitialAppKey;
    private Map<String, String> mInterstitialExtraParams;
    private boolean mInterstitialInitSuccess;
    private boolean mInterstitialLoadSuccess;
    private boolean mInterstitialReportInit;
    private boolean mInterstitialReportLoad;
    private String mInterstitialUserId;
    private Map<String, String> mOfferWallExtraParams;
    private boolean mOfferwallInitSuccess;
    private boolean mOfferwallReportInit;
    private String mRVAppKey;
    private String mRVUserId;
    private boolean mShouldRestore;

    public AdUnitsState() {
        initialize();
    }

    private AdUnitsState(Parcel source) {
        boolean z = true;
        initialize();
        try {
            boolean z2;
            this.mShouldRestore = source.readByte() != (byte) 0;
            this.mDisplayedProduct = source.readInt();
            this.mRVAppKey = source.readString();
            this.mRVUserId = source.readString();
            this.mDisplayedDemandSourceName = source.readString();
            if (source.readByte() != (byte) 0) {
                z2 = true;
            } else {
                z2 = false;
            }
            this.mInterstitialReportInit = z2;
            if (source.readByte() != (byte) 0) {
                z2 = true;
            } else {
                z2 = false;
            }
            this.mInterstitialInitSuccess = z2;
            this.mInterstitialAppKey = source.readString();
            this.mInterstitialUserId = source.readString();
            this.mInterstitialExtraParams = getMapFromJsonString(source.readString());
            if (source.readByte() != (byte) 0) {
                z2 = true;
            } else {
                z2 = false;
            }
            this.mOfferwallInitSuccess = z2;
            if (source.readByte() == (byte) 0) {
                z = false;
            }
            this.mOfferwallReportInit = z;
            this.mOfferWallExtraParams = getMapFromJsonString(source.readString());
        } catch (Throwable th) {
            initialize();
        }
    }

    private void initialize() {
        this.mShouldRestore = false;
        this.mDisplayedProduct = -1;
        this.mInterstitialReportInit = true;
        this.mOfferwallReportInit = true;
        this.mOfferwallInitSuccess = false;
        this.mInterstitialInitSuccess = false;
        String str = BuildConfig.FLAVOR;
        this.mInterstitialUserId = str;
        this.mInterstitialAppKey = str;
        this.mInterstitialExtraParams = new HashMap();
        this.mOfferWallExtraParams = new HashMap();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i = 1;
        try {
            int i2;
            dest.writeByte((byte) (this.mShouldRestore ? 1 : 0));
            dest.writeInt(this.mDisplayedProduct);
            dest.writeString(this.mRVAppKey);
            dest.writeString(this.mRVUserId);
            dest.writeString(this.mDisplayedDemandSourceName);
            if (this.mInterstitialReportInit) {
                i2 = 1;
            } else {
                i2 = 0;
            }
            dest.writeByte((byte) i2);
            if (this.mInterstitialInitSuccess) {
                i2 = 1;
            } else {
                i2 = 0;
            }
            dest.writeByte((byte) i2);
            dest.writeString(this.mInterstitialAppKey);
            dest.writeString(this.mInterstitialUserId);
            dest.writeString(new JSONObject(this.mInterstitialExtraParams).toString());
            if (this.mOfferwallInitSuccess) {
                i2 = 1;
            } else {
                i2 = 0;
            }
            dest.writeByte((byte) i2);
            if (!this.mOfferwallReportInit) {
                i = 0;
            }
            dest.writeByte((byte) i);
            dest.writeString(new JSONObject(this.mOfferWallExtraParams).toString());
        } catch (Throwable th) {
        }
    }

    public boolean isInterstitialInitSuccess() {
        return this.mInterstitialInitSuccess;
    }

    public boolean isInterstitialLoadSuccess() {
        return this.mInterstitialLoadSuccess;
    }

    public String getInterstitialAppKey() {
        return this.mInterstitialAppKey;
    }

    public String getInterstitialUserId() {
        return this.mInterstitialUserId;
    }

    public Map<String, String> getInterstitialExtraParams() {
        return this.mInterstitialExtraParams;
    }

    public boolean reportInitInterstitial() {
        return this.mInterstitialReportInit;
    }

    public boolean reportLoadInterstitial() {
        return this.mInterstitialReportLoad;
    }

    public boolean shouldRestore() {
        return this.mShouldRestore;
    }

    public int getDisplayedProduct() {
        return this.mDisplayedProduct;
    }

    public String getDisplayedDemandSourceName() {
        return this.mDisplayedDemandSourceName;
    }

    public boolean getOfferwallInitSuccess() {
        return this.mOfferwallInitSuccess;
    }

    public Map<String, String> getOfferWallExtraParams() {
        return this.mOfferWallExtraParams;
    }

    public boolean reportInitOfferwall() {
        return this.mOfferwallReportInit;
    }

    public void setOfferWallExtraParams(Map<String, String> offerWallExtraParams) {
        this.mOfferWallExtraParams = offerWallExtraParams;
    }

    public void setInterstitialInitSuccess(boolean mInterstitialInitSuccess) {
        this.mInterstitialInitSuccess = mInterstitialInitSuccess;
    }

    public void setInterstitialLoadSuccess(boolean mInterstitialLoadSuccess) {
        this.mInterstitialLoadSuccess = mInterstitialLoadSuccess;
    }

    public void setInterstitialAppKey(String mInterstitialAppKey) {
        this.mInterstitialAppKey = mInterstitialAppKey;
    }

    public void setInterstitialUserId(String mInterstitialUserId) {
        this.mInterstitialUserId = mInterstitialUserId;
    }

    public void setInterstitialExtraParams(Map<String, String> mInterstitialExtraParams) {
        this.mInterstitialExtraParams = mInterstitialExtraParams;
    }

    public void setReportInitInterstitial(boolean shouldReport) {
        this.mInterstitialReportInit = shouldReport;
    }

    public void setReportLoadInterstitial(boolean shouldReport) {
        this.mInterstitialReportLoad = shouldReport;
    }

    public void setShouldRestore(boolean mShouldRestore) {
        this.mShouldRestore = mShouldRestore;
    }

    public void adOpened(int product) {
        this.mDisplayedProduct = product;
    }

    public void adClosed() {
        this.mDisplayedProduct = -1;
    }

    public void setOfferwallInitSuccess(boolean offerwallInitSuccess) {
        this.mOfferwallInitSuccess = offerwallInitSuccess;
    }

    public void setOfferwallReportInit(boolean offerwallReportInit) {
        this.mOfferwallReportInit = offerwallReportInit;
    }

    public String getRVAppKey() {
        return this.mRVAppKey;
    }

    public void setRVAppKey(String mRVAppKey) {
        this.mRVAppKey = mRVAppKey;
    }

    public String getRVUserId() {
        return this.mRVUserId;
    }

    public void setDisplayedDemandSourceName(String displayedDemandSourceName) {
        this.mDisplayedDemandSourceName = displayedDemandSourceName;
    }

    public void setRVUserId(String mRVUserId) {
        this.mRVUserId = mRVUserId;
    }

    private Map<String, String> getMapFromJsonString(String jsonString) {
        Map<String, String> result = new HashMap();
        try {
            JSONObject json = new JSONObject(jsonString);
            Iterator<String> keys = json.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                result.put(key, json.getString(key));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Throwable e2) {
            e2.printStackTrace();
        }
        return result;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        try {
            builder.append("shouldRestore:").append(this.mShouldRestore).append(", ");
            builder.append("displayedProduct:").append(this.mDisplayedProduct).append(", ");
            builder.append("ISReportInit:").append(this.mInterstitialReportInit).append(", ");
            builder.append("ISInitSuccess:").append(this.mInterstitialInitSuccess).append(", ");
            builder.append("ISAppKey").append(this.mInterstitialAppKey).append(", ");
            builder.append("ISUserId").append(this.mInterstitialUserId).append(", ");
            builder.append("ISExtraParams").append(this.mInterstitialExtraParams).append(", ");
            builder.append("OWReportInit").append(this.mOfferwallReportInit).append(", ");
            builder.append("OWInitSuccess").append(this.mOfferwallInitSuccess).append(", ");
            builder.append("OWExtraParams").append(this.mOfferWallExtraParams).append(", ");
        } catch (Throwable th) {
        }
        return builder.toString();
    }
}
