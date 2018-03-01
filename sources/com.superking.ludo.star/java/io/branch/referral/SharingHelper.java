package io.branch.referral;

import com.facebook.messenger.MessengerUtils;
import org.cocos2dx.lib.BuildConfig;

public class SharingHelper {

    public enum SHARE_WITH {
        FACEBOOK("com.facebook.katana"),
        FACEBOOK_MESSENGER(MessengerUtils.PACKAGE_NAME),
        TWITTER("com.twitter.android"),
        MESSAGE(".mms"),
        EMAIL("com.google.android.email"),
        FLICKR("com.yahoo.mobile.client.android.flickr"),
        GOOGLE_DOC("com.google.android.apps.docs"),
        WHATS_APP("com.whatsapp"),
        PINTEREST("com.pinterest"),
        HANGOUT("com.google.android.talk"),
        INSTAGRAM("com.instagram.android"),
        WECHAT("jom.tencent.mm"),
        GMAIL("com.google.android.gm");
        
        private String name;

        private SHARE_WITH(String key) {
            this.name = BuildConfig.FLAVOR;
            this.name = key;
        }

        public String getAppName() {
            return this.name;
        }

        public String toString() {
            return this.name;
        }
    }
}
