package io.branch.indexing;

import android.app.Activity;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import io.branch.referral.Branch;
import io.branch.referral.Branch.BranchLinkCreateListener;
import io.branch.referral.Branch.BranchLinkShareListener;
import io.branch.referral.Branch.IChannelProperties;
import io.branch.referral.Branch.ShareLinkBuilder;
import io.branch.referral.BranchError;
import io.branch.referral.BranchShortLinkBuilder;
import io.branch.referral.Defines.Jsonkey;
import io.branch.referral.util.BranchEvent;
import io.branch.referral.util.CurrencyType;
import io.branch.referral.util.LinkProperties;
import io.branch.referral.util.ShareSheetStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import org.cocos2dx.lib.BuildConfig;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BranchUniversalObject implements Parcelable {
    public static final Creator CREATOR = new Creator() {
        public BranchUniversalObject createFromParcel(Parcel in) {
            return new BranchUniversalObject(in);
        }

        public BranchUniversalObject[] newArray(int size) {
            return new BranchUniversalObject[size];
        }
    };
    private String canonicalIdentifier_;
    private String canonicalUrl_;
    private CurrencyType currency_;
    private String description_;
    private long expirationInMilliSec_;
    private String imageUrl_;
    private CONTENT_INDEX_MODE indexMode_;
    private final ArrayList<String> keywords_;
    private final HashMap<String, String> metadata_;
    private Double price_;
    private String title_;
    private String type_;

    public enum CONTENT_INDEX_MODE {
        PUBLIC,
        PRIVATE
    }

    private class LinkShareListenerWrapper implements BranchLinkShareListener {
        private final BranchLinkShareListener originalCallback_;

        public LinkShareListenerWrapper(BranchLinkShareListener originalCallback) {
            this.originalCallback_ = originalCallback;
        }

        public void onShareLinkDialogLaunched() {
            BranchUniversalObject.this.userCompletedAction(BranchEvent.SHARE_STARTED);
            if (this.originalCallback_ != null) {
                this.originalCallback_.onShareLinkDialogLaunched();
            }
        }

        public void onShareLinkDialogDismissed() {
            if (this.originalCallback_ != null) {
                this.originalCallback_.onShareLinkDialogDismissed();
            }
        }

        public void onLinkShareResponse(String sharedLink, String sharedChannel, BranchError error) {
            HashMap<String, String> metaData = new HashMap();
            if (error == null) {
                metaData.put(Jsonkey.SharedLink.getKey(), sharedLink);
            } else {
                metaData.put(Jsonkey.ShareError.getKey(), error.getMessage());
            }
            BranchUniversalObject.this.userCompletedAction(BranchEvent.SHARE_COMPLETED, metaData);
            if (this.originalCallback_ != null) {
                this.originalCallback_.onLinkShareResponse(sharedLink, sharedChannel, error);
            }
        }

        public void onChannelSelected(String channelName) {
            if (this.originalCallback_ != null) {
                this.originalCallback_.onChannelSelected(channelName);
            }
        }
    }

    public interface RegisterViewStatusListener {
        void onRegisterViewFinished(boolean z, BranchError branchError);
    }

    public BranchUniversalObject() {
        this.metadata_ = new HashMap();
        this.keywords_ = new ArrayList();
        this.canonicalIdentifier_ = BuildConfig.FLAVOR;
        this.canonicalUrl_ = BuildConfig.FLAVOR;
        this.title_ = BuildConfig.FLAVOR;
        this.description_ = BuildConfig.FLAVOR;
        this.type_ = BuildConfig.FLAVOR;
        this.indexMode_ = CONTENT_INDEX_MODE.PUBLIC;
        this.expirationInMilliSec_ = 0;
        this.currency_ = CurrencyType.USD;
    }

    public BranchUniversalObject setCanonicalIdentifier(@NonNull String canonicalIdentifier) {
        this.canonicalIdentifier_ = canonicalIdentifier;
        return this;
    }

    public BranchUniversalObject setCanonicalUrl(@NonNull String canonicalUrl) {
        this.canonicalUrl_ = canonicalUrl;
        return this;
    }

    public BranchUniversalObject setTitle(@NonNull String title) {
        this.title_ = title;
        return this;
    }

    public BranchUniversalObject setContentDescription(String description) {
        this.description_ = description;
        return this;
    }

    public BranchUniversalObject setContentImageUrl(@NonNull String imageUrl) {
        this.imageUrl_ = imageUrl;
        return this;
    }

    public BranchUniversalObject addContentMetadata(HashMap<String, String> metadata) {
        this.metadata_.putAll(metadata);
        return this;
    }

    public BranchUniversalObject addContentMetadata(String key, String value) {
        this.metadata_.put(key, value);
        return this;
    }

    public BranchUniversalObject setContentType(String type) {
        this.type_ = type;
        return this;
    }

    public BranchUniversalObject setContentIndexingMode(CONTENT_INDEX_MODE indexMode) {
        this.indexMode_ = indexMode;
        return this;
    }

    public BranchUniversalObject addKeyWords(ArrayList<String> keywords) {
        this.keywords_.addAll(keywords);
        return this;
    }

    public BranchUniversalObject addKeyWord(String keyword) {
        this.keywords_.add(keyword);
        return this;
    }

    public BranchUniversalObject setContentExpiration(Date expirationDate) {
        this.expirationInMilliSec_ = expirationDate.getTime();
        return this;
    }

    public BranchUniversalObject setPrice(double price, CurrencyType currency) {
        this.price_ = Double.valueOf(price);
        this.currency_ = currency;
        return this;
    }

    public void listOnGoogleSearch(Context context) {
        AppIndexingHelper.addToAppIndex(context, this);
    }

    public void userCompletedAction(String action) {
        userCompletedAction(action, null);
    }

    public void userCompletedAction(String action, HashMap<String, String> metadata) {
        JSONObject actionCompletedPayload = new JSONObject();
        try {
            JSONArray canonicalIDList = new JSONArray();
            canonicalIDList.put(this.canonicalIdentifier_);
            actionCompletedPayload.put(BranchEvent.CANONICAL_ID_LIST, canonicalIDList);
            actionCompletedPayload.put(this.canonicalIdentifier_, convertToJson());
            if (metadata != null) {
                for (String key : metadata.keySet()) {
                    actionCompletedPayload.put(key, metadata.get(key));
                }
            }
            if (Branch.getInstance() != null) {
                Branch.getInstance().userCompletedAction(action, actionCompletedPayload);
            }
        } catch (JSONException e) {
        }
    }

    public boolean isPublicallyIndexable() {
        return this.indexMode_ == CONTENT_INDEX_MODE.PUBLIC;
    }

    public HashMap<String, String> getMetadata() {
        return this.metadata_;
    }

    public long getExpirationTime() {
        return this.expirationInMilliSec_;
    }

    public String getCanonicalIdentifier() {
        return this.canonicalIdentifier_;
    }

    public String getCanonicalUrl() {
        return this.canonicalUrl_;
    }

    public String getDescription() {
        return this.description_;
    }

    public String getImageUrl() {
        return this.imageUrl_;
    }

    public String getTitle() {
        return this.title_;
    }

    public String getType() {
        return this.type_;
    }

    public double getPrice() {
        return this.price_ != null ? this.price_.doubleValue() : 0.0d;
    }

    public String getCurrencyType() {
        return this.currency_.toString();
    }

    public JSONArray getKeywordsJsonArray() {
        JSONArray keywordArray = new JSONArray();
        Iterator i$ = this.keywords_.iterator();
        while (i$.hasNext()) {
            keywordArray.put((String) i$.next());
        }
        return keywordArray;
    }

    public ArrayList<String> getKeywords() {
        return this.keywords_;
    }

    public void registerView() {
        registerView(null);
    }

    public void registerView(@Nullable RegisterViewStatusListener callback) {
        if (Branch.getInstance() != null) {
            Branch.getInstance().registerView(this, callback);
        } else if (callback != null) {
            callback.onRegisterViewFinished(false, new BranchError("Register view error", BranchError.ERR_BRANCH_NOT_INSTANTIATED));
        }
    }

    public String getShortUrl(@NonNull Context context, @NonNull LinkProperties linkProperties) {
        return getLinkBuilder(context, linkProperties).getShortUrl();
    }

    public String getShortUrl(@NonNull Context context, @NonNull LinkProperties linkProperties, boolean defaultToLongUrl) {
        return ((BranchShortLinkBuilder) getLinkBuilder(context, linkProperties).setDefaultToLongUrl(defaultToLongUrl)).getShortUrl();
    }

    public void generateShortUrl(@NonNull Context context, @NonNull LinkProperties linkProperties, @Nullable BranchLinkCreateListener callback) {
        getLinkBuilder(context, linkProperties).generateShortUrl(callback);
    }

    public void generateShortUrl(@NonNull Context context, @NonNull LinkProperties linkProperties, @Nullable BranchLinkCreateListener callback, boolean defaultToLongUrl) {
        ((BranchShortLinkBuilder) getLinkBuilder(context, linkProperties).setDefaultToLongUrl(defaultToLongUrl)).generateShortUrl(callback);
    }

    public void showShareSheet(@NonNull Activity activity, @NonNull LinkProperties linkProperties, @NonNull ShareSheetStyle style, @Nullable BranchLinkShareListener callback) {
        showShareSheet(activity, linkProperties, style, callback, null);
    }

    public void showShareSheet(@NonNull Activity activity, @NonNull LinkProperties linkProperties, @NonNull ShareSheetStyle style, @Nullable BranchLinkShareListener callback, IChannelProperties channelProperties) {
        if (Branch.getInstance() != null) {
            ShareLinkBuilder shareLinkBuilder = new ShareLinkBuilder(activity, getLinkBuilder(activity, linkProperties)).setCallback(new LinkShareListenerWrapper(callback)).setChannelProperties(channelProperties).setSubject(style.getMessageTitle()).setMessage(style.getMessageBody());
            if (style.getCopyUrlIcon() != null) {
                shareLinkBuilder.setCopyUrlStyle(style.getCopyUrlIcon(), style.getCopyURlText(), style.getUrlCopiedMessage());
            }
            if (style.getMoreOptionIcon() != null) {
                shareLinkBuilder.setMoreOptionStyle(style.getMoreOptionIcon(), style.getMoreOptionText());
            }
            if (style.getDefaultURL() != null) {
                shareLinkBuilder.setDefaultURL(style.getDefaultURL());
            }
            if (style.getPreferredOptions().size() > 0) {
                shareLinkBuilder.addPreferredSharingOptions(style.getPreferredOptions());
            }
            if (style.getStyleResourceID() > 0) {
                shareLinkBuilder.setStyleResourceID(style.getStyleResourceID());
            }
            shareLinkBuilder.setDividerHeight(style.getDividerHeight());
            shareLinkBuilder.setAsFullWidthStyle(style.getIsFullWidthStyle());
            shareLinkBuilder.setSharingTitle(style.getSharingTitle());
            shareLinkBuilder.setSharingTitle(style.getSharingTitleView());
            if (style.getIncludedInShareSheet() != null && style.getIncludedInShareSheet().size() > 0) {
                shareLinkBuilder.includeInShareSheet(style.getIncludedInShareSheet());
            }
            if (style.getExcludedFromShareSheet() != null && style.getExcludedFromShareSheet().size() > 0) {
                shareLinkBuilder.excludeFromShareSheet(style.getExcludedFromShareSheet());
            }
            shareLinkBuilder.shareLink();
        } else if (callback != null) {
            callback.onLinkShareResponse(null, null, new BranchError("Trouble sharing link. ", BranchError.ERR_BRANCH_NOT_INSTANTIATED));
        } else {
            Log.e("BranchSDK", "Sharing error. Branch instance is not created yet. Make sure you have initialised Branch.");
        }
    }

    private BranchShortLinkBuilder getLinkBuilder(@NonNull Context context, @NonNull LinkProperties linkProperties) {
        BranchShortLinkBuilder shortLinkBuilder = new BranchShortLinkBuilder(context);
        if (linkProperties.getTags() != null) {
            shortLinkBuilder.addTags(linkProperties.getTags());
        }
        if (linkProperties.getFeature() != null) {
            shortLinkBuilder.setFeature(linkProperties.getFeature());
        }
        if (linkProperties.getAlias() != null) {
            shortLinkBuilder.setAlias(linkProperties.getAlias());
        }
        if (linkProperties.getChannel() != null) {
            shortLinkBuilder.setChannel(linkProperties.getChannel());
        }
        if (linkProperties.getStage() != null) {
            shortLinkBuilder.setStage(linkProperties.getStage());
        }
        if (linkProperties.getCampaign() != null) {
            shortLinkBuilder.setCampaign(linkProperties.getCampaign());
        }
        if (linkProperties.getMatchDuration() > 0) {
            shortLinkBuilder.setDuration(linkProperties.getMatchDuration());
        }
        if (!TextUtils.isEmpty(this.title_)) {
            shortLinkBuilder.addParameters(Jsonkey.ContentTitle.getKey(), this.title_);
        }
        if (!TextUtils.isEmpty(this.canonicalIdentifier_)) {
            shortLinkBuilder.addParameters(Jsonkey.CanonicalIdentifier.getKey(), this.canonicalIdentifier_);
        }
        if (!TextUtils.isEmpty(this.canonicalUrl_)) {
            shortLinkBuilder.addParameters(Jsonkey.CanonicalUrl.getKey(), this.canonicalUrl_);
        }
        JSONArray keywords = getKeywordsJsonArray();
        if (keywords.length() > 0) {
            shortLinkBuilder.addParameters(Jsonkey.ContentKeyWords.getKey(), keywords);
        }
        if (!TextUtils.isEmpty(this.description_)) {
            shortLinkBuilder.addParameters(Jsonkey.ContentDesc.getKey(), this.description_);
        }
        if (!TextUtils.isEmpty(this.imageUrl_)) {
            shortLinkBuilder.addParameters(Jsonkey.ContentImgUrl.getKey(), this.imageUrl_);
        }
        if (!TextUtils.isEmpty(this.type_)) {
            shortLinkBuilder.addParameters(Jsonkey.ContentType.getKey(), this.type_);
        }
        if (this.expirationInMilliSec_ > 0) {
            shortLinkBuilder.addParameters(Jsonkey.ContentExpiryTime.getKey(), BuildConfig.FLAVOR + this.expirationInMilliSec_);
        }
        shortLinkBuilder.addParameters(Jsonkey.PublicallyIndexable.getKey(), BuildConfig.FLAVOR + isPublicallyIndexable());
        if (this.price_ != null) {
            shortLinkBuilder.addParameters(BranchEvent.PURCHASE_AMOUNT, BuildConfig.FLAVOR + this.price_);
            shortLinkBuilder.addParameters(BranchEvent.PURCHASE_CURRENCY, this.currency_.toString());
        }
        for (String key : this.metadata_.keySet()) {
            shortLinkBuilder.addParameters(key, (String) this.metadata_.get(key));
        }
        HashMap<String, String> controlParam = linkProperties.getControlParams();
        for (String key2 : controlParam.keySet()) {
            shortLinkBuilder.addParameters(key2, (String) controlParam.get(key2));
        }
        return shortLinkBuilder;
    }

    public static BranchUniversalObject getReferredBranchUniversalObject() {
        Branch branchInstance = Branch.getInstance();
        if (branchInstance == null) {
            return null;
        }
        try {
            if (branchInstance.getLatestReferringParams() == null) {
                return null;
            }
            if (branchInstance.getLatestReferringParams().has("+clicked_branch_link") && branchInstance.getLatestReferringParams().getBoolean("+clicked_branch_link")) {
                return createInstance(branchInstance.getLatestReferringParams());
            }
            if (branchInstance.getDeeplinkDebugParams() == null || branchInstance.getDeeplinkDebugParams().length() <= 0) {
                return null;
            }
            return createInstance(branchInstance.getLatestReferringParams());
        } catch (Exception e) {
            return null;
        }
    }

    public static BranchUniversalObject createInstance(JSONObject jsonObject) {
        try {
            BranchUniversalObject branchUniversalObject = new BranchUniversalObject();
            try {
                if (jsonObject.has(Jsonkey.ContentTitle.getKey())) {
                    branchUniversalObject.title_ = jsonObject.getString(Jsonkey.ContentTitle.getKey());
                }
                if (jsonObject.has(Jsonkey.CanonicalIdentifier.getKey())) {
                    branchUniversalObject.canonicalIdentifier_ = jsonObject.getString(Jsonkey.CanonicalIdentifier.getKey());
                }
                if (jsonObject.has(Jsonkey.CanonicalUrl.getKey())) {
                    branchUniversalObject.canonicalUrl_ = jsonObject.getString(Jsonkey.CanonicalUrl.getKey());
                }
                if (jsonObject.has(Jsonkey.ContentDesc.getKey())) {
                    branchUniversalObject.description_ = jsonObject.getString(Jsonkey.ContentDesc.getKey());
                }
                if (jsonObject.has(Jsonkey.ContentImgUrl.getKey())) {
                    branchUniversalObject.imageUrl_ = jsonObject.getString(Jsonkey.ContentImgUrl.getKey());
                }
                if (jsonObject.has(Jsonkey.ContentType.getKey())) {
                    branchUniversalObject.type_ = jsonObject.getString(Jsonkey.ContentType.getKey());
                }
                if (jsonObject.has(Jsonkey.ContentExpiryTime.getKey())) {
                    branchUniversalObject.expirationInMilliSec_ = jsonObject.getLong(Jsonkey.ContentExpiryTime.getKey());
                }
                if (jsonObject.has(BranchEvent.PURCHASE_AMOUNT)) {
                    branchUniversalObject.price_ = Double.valueOf(jsonObject.getDouble(BranchEvent.PURCHASE_AMOUNT));
                }
                if (jsonObject.has(BranchEvent.PURCHASE_CURRENCY)) {
                    branchUniversalObject.currency_ = CurrencyType.valueOf(jsonObject.getString(BranchEvent.PURCHASE_CURRENCY));
                }
                Iterator<String> keys = jsonObject.keys();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    branchUniversalObject.addContentMetadata(key, jsonObject.getString(key));
                }
                if (jsonObject.has(Jsonkey.ContentKeyWords.getKey())) {
                    JSONArray keywordJsonArray = null;
                    Object keyWordArrayObject = jsonObject.get(Jsonkey.ContentKeyWords.getKey());
                    if (keyWordArrayObject instanceof JSONArray) {
                        keywordJsonArray = (JSONArray) keyWordArrayObject;
                    } else if (keyWordArrayObject instanceof String) {
                        keywordJsonArray = new JSONArray((String) keyWordArrayObject);
                    }
                    if (keywordJsonArray != null) {
                        for (int i = 0; i < keywordJsonArray.length(); i++) {
                            branchUniversalObject.keywords_.add((String) keywordJsonArray.get(i));
                        }
                    }
                }
                if (jsonObject.has(Jsonkey.PublicallyIndexable.getKey())) {
                    try {
                        branchUniversalObject.indexMode_ = jsonObject.getBoolean(Jsonkey.PublicallyIndexable.getKey()) ? CONTENT_INDEX_MODE.PUBLIC : CONTENT_INDEX_MODE.PRIVATE;
                    } catch (JSONException e) {
                        branchUniversalObject.indexMode_ = jsonObject.getInt(Jsonkey.PublicallyIndexable.getKey()) == 1 ? CONTENT_INDEX_MODE.PUBLIC : CONTENT_INDEX_MODE.PRIVATE;
                    }
                }
                return branchUniversalObject;
            } catch (Exception e2) {
                return branchUniversalObject;
            }
        } catch (Exception e3) {
            return null;
        }
    }

    public JSONObject convertToJson() {
        JSONObject buoJsonModel = new JSONObject();
        try {
            Iterator i$;
            if (!TextUtils.isEmpty(this.title_)) {
                buoJsonModel.put(Jsonkey.ContentTitle.getKey(), this.title_);
            }
            if (!TextUtils.isEmpty(this.canonicalIdentifier_)) {
                buoJsonModel.put(Jsonkey.CanonicalIdentifier.getKey(), this.canonicalIdentifier_);
            }
            if (!TextUtils.isEmpty(this.canonicalUrl_)) {
                buoJsonModel.put(Jsonkey.CanonicalUrl.getKey(), this.canonicalUrl_);
            }
            if (this.keywords_.size() > 0) {
                JSONArray keyWordJsonArray = new JSONArray();
                i$ = this.keywords_.iterator();
                while (i$.hasNext()) {
                    keyWordJsonArray.put((String) i$.next());
                }
                buoJsonModel.put(Jsonkey.ContentKeyWords.getKey(), keyWordJsonArray);
            }
            if (!TextUtils.isEmpty(this.description_)) {
                buoJsonModel.put(Jsonkey.ContentDesc.getKey(), this.description_);
            }
            if (!TextUtils.isEmpty(this.imageUrl_)) {
                buoJsonModel.put(Jsonkey.ContentImgUrl.getKey(), this.imageUrl_);
            }
            if (!TextUtils.isEmpty(this.type_)) {
                buoJsonModel.put(Jsonkey.ContentType.getKey(), this.type_);
            }
            if (this.expirationInMilliSec_ > 0) {
                buoJsonModel.put(Jsonkey.ContentExpiryTime.getKey(), this.expirationInMilliSec_);
            }
            buoJsonModel.put(Jsonkey.PublicallyIndexable.getKey(), isPublicallyIndexable());
            if (this.price_ != null) {
                buoJsonModel.put(BranchEvent.PURCHASE_AMOUNT, this.price_);
                buoJsonModel.put(BranchEvent.PURCHASE_CURRENCY, this.currency_.toString());
            }
            for (String metadataKey : this.metadata_.keySet()) {
                buoJsonModel.put(metadataKey, this.metadata_.get(metadataKey));
            }
        } catch (JSONException e) {
        }
        return buoJsonModel;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.canonicalIdentifier_);
        dest.writeString(this.canonicalUrl_);
        dest.writeString(this.title_);
        dest.writeString(this.description_);
        dest.writeString(this.imageUrl_);
        dest.writeString(this.type_);
        dest.writeLong(this.expirationInMilliSec_);
        dest.writeInt(this.indexMode_.ordinal());
        dest.writeDouble(this.price_ != null ? this.price_.doubleValue() : -1.0d);
        dest.writeInt(this.currency_.ordinal());
        dest.writeSerializable(this.keywords_);
        dest.writeInt(this.metadata_.size());
        for (Entry<String, String> entry : this.metadata_.entrySet()) {
            dest.writeString((String) entry.getKey());
            dest.writeString((String) entry.getValue());
        }
    }

    private BranchUniversalObject(Parcel in) {
        this();
        this.canonicalIdentifier_ = in.readString();
        this.canonicalUrl_ = in.readString();
        this.title_ = in.readString();
        this.description_ = in.readString();
        this.imageUrl_ = in.readString();
        this.type_ = in.readString();
        this.expirationInMilliSec_ = in.readLong();
        this.indexMode_ = CONTENT_INDEX_MODE.values()[in.readInt()];
        this.price_ = Double.valueOf(in.readDouble());
        if (this.price_.doubleValue() < 0.0d) {
            this.price_ = null;
        }
        this.currency_ = CurrencyType.values()[in.readInt()];
        this.keywords_.addAll((ArrayList) in.readSerializable());
        int metadataSize = in.readInt();
        for (int i = 0; i < metadataSize; i++) {
            this.metadata_.put(in.readString(), in.readString());
        }
    }
}
