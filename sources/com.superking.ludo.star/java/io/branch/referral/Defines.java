package io.branch.referral;

import com.facebook.internal.NativeProtocol;
import com.facebook.share.internal.ShareConstants;
import com.google.firebase.analytics.FirebaseAnalytics.Param;
import com.ironsource.environment.ConnectivityService;
import com.ironsource.sdk.utils.Constants.ParametersKeys;
import com.ironsource.sdk.utils.Constants.RequestParameters;
import cz.msebera.android.httpclient.cookie.ClientCookie;
import cz.msebera.android.httpclient.protocol.HTTP;
import io.branch.indexing.ContentDiscoveryManifest;
import org.cocos2dx.lib.BuildConfig;

public class Defines {

    public enum Jsonkey {
        IdentityID("identity_id"),
        Identity(HTTP.IDENTITY_CODING),
        DeviceFingerprintID("device_fingerprint_id"),
        SessionID("session_id"),
        LinkClickID("link_click_id"),
        GoogleSearchInstallReferrer("google_search_install_referrer"),
        FaceBookAppLinkChecked("facebook_app_link_checked"),
        BranchLinkUsed("branch_used"),
        ReferringBranchIdentity("referring_branch_identity"),
        BranchIdentity("branch_identity"),
        BranchKey(RemoteInterface.BRANCH_KEY),
        Bucket("bucket"),
        DefaultBucket(Branch.REFERRAL_BUCKET_DEFAULT),
        Amount("amount"),
        CalculationType("calculation_type"),
        Location(Param.LOCATION),
        Type(EventEntry.COLUMN_NAME_TYPE),
        CreationSource("creation_source"),
        Prefix("prefix"),
        Expiration("expiration"),
        Event("event"),
        Metadata("metadata"),
        CommerceData("commerce_data"),
        ReferralCode(Branch.REFERRAL_CODE),
        Total(ParametersKeys.TOTAL),
        Unique("unique"),
        Length("length"),
        Direction("direction"),
        BeginAfterID("begin_after_id"),
        Link(ShareConstants.WEB_DIALOG_PARAM_LINK),
        ReferringData("referring_data"),
        ReferringLink("referring_link"),
        Data(EventEntry.COLUMN_NAME_DATA),
        OS("os"),
        HardwareID("hardware_id"),
        HardwareIDType("hardware_id_type"),
        HardwareIDTypeVendor("vendor_id"),
        HardwareIDTypeRandom("random"),
        IsHardwareIDReal("is_hardware_id_real"),
        AppVersion("app_version"),
        OSVersion("os_version"),
        Country("country"),
        Language("language"),
        IsReferrable("is_referrable"),
        Update("update"),
        URIScheme("uri_scheme"),
        AppIdentifier("app_identifier"),
        LinkIdentifier("link_identifier"),
        GoogleAdvertisingID("google_advertising_id"),
        LATVal("lat_val"),
        Debug(RequestParameters.DEBUG),
        Brand("brand"),
        Model("model"),
        ScreenDpi("screen_dpi"),
        ScreenHeight("screen_height"),
        ScreenWidth("screen_width"),
        WiFi(ConnectivityService.NETWORK_TYPE_WIFI),
        LocalIP("local_ip"),
        Clicked_Branch_Link("+clicked_branch_link"),
        IsFirstSession("+is_first_session"),
        AndroidDeepLinkPath("$android_deeplink_path"),
        DeepLinkPath(Branch.DEEPLINK_PATH),
        AndroidAppLinkURL("android_app_link_url"),
        AndroidPushNotificationKey("branch"),
        AndroidPushIdentifier("push_identifier"),
        ForceNewBranchSession("branch_force_new_session"),
        CanonicalIdentifier("$canonical_identifier"),
        ContentTitle(Branch.OG_TITLE),
        ContentDesc(Branch.OG_DESC),
        ContentImgUrl(Branch.OG_IMAGE_URL),
        CanonicalUrl("$canonical_url"),
        ContentType("$content_type"),
        PublicallyIndexable("$publicly_indexable"),
        ContentKeyWords("$keywords"),
        ContentExpiryTime("$exp_date"),
        Params(NativeProtocol.WEB_DIALOG_PARAMS),
        SharedLink("$shared_link"),
        ShareError("$share_error"),
        External_Intent_URI("external_intent_uri"),
        External_Intent_Extra("external_intent_extra"),
        Last_Round_Trip_Time("lrtt"),
        Branch_Round_Trip_Time("brtt"),
        Branch_Instrumentation("instrumentation"),
        Queue_Wait_Time("qwt"),
        BranchViewData("branch_view_data"),
        BranchViewID(ShareConstants.WEB_DIALOG_PARAM_ID),
        BranchViewAction(ParametersKeys.ACTION),
        BranchViewNumOfUse("number_of_use"),
        BranchViewUrl(ParametersKeys.URL),
        BranchViewHtml("html"),
        Path(ClientCookie.PATH_ATTR),
        ViewList("view_list"),
        ContentActionView(ParametersKeys.VIEW),
        ContentPath("content_path"),
        ContentNavPath("content_nav_path"),
        ReferralLink("referral_link"),
        ContentData("content_data"),
        ContentEvents(EventEntry.TABLE_NAME),
        ContentAnalyticsMode("content_analytics_mode"),
        ContentDiscovery(ContentDiscoveryManifest.CONTENT_DISCOVER_KEY);
        
        private String key;

        private Jsonkey(String key) {
            this.key = BuildConfig.FLAVOR;
            this.key = key;
        }

        public String getKey() {
            return this.key;
        }

        public String toString() {
            return this.key;
        }
    }

    public enum LinkParam {
        Tags("tags"),
        Alias("alias"),
        Type(EventEntry.COLUMN_NAME_TYPE),
        Duration("duration"),
        Channel("channel"),
        Feature("feature"),
        Stage(ParametersKeys.STAGE),
        Campaign(Param.CAMPAIGN),
        Data(EventEntry.COLUMN_NAME_DATA),
        URL(ParametersKeys.URL);
        
        private String key;

        private LinkParam(String key) {
            this.key = BuildConfig.FLAVOR;
            this.key = key;
        }

        public String getKey() {
            return this.key;
        }

        public String toString() {
            return this.key;
        }
    }

    public enum RequestPath {
        RedeemRewards("v1/redeem"),
        GetURL("v1/url"),
        RegisterInstall("v1/install"),
        RegisterClose("v1/close"),
        RegisterOpen("v1/open"),
        RegisterView("v1/register-view"),
        Referrals("v1/referrals/"),
        SendAPPList("v1/applist"),
        GetCredits("v1/credits/"),
        GetCreditHistory("v1/credithistory"),
        CompletedAction("v1/event"),
        IdentifyUser("v1/profile"),
        Logout("v1/logout"),
        GetReferralCode("v1/referralcode"),
        ValidateReferralCode("v1/referralcode/"),
        ApplyReferralCode("v1/applycode/"),
        DebugConnect("v1/debug/connect"),
        ContentEvent("v1/content-events");
        
        private String key;

        private RequestPath(String key) {
            this.key = BuildConfig.FLAVOR;
            this.key = key;
        }

        public String getPath() {
            return this.key;
        }

        public String toString() {
            return this.key;
        }
    }
}
