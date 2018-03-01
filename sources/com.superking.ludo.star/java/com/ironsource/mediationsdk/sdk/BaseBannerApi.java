package com.ironsource.mediationsdk.sdk;

import android.app.Activity;
import com.ironsource.mediationsdk.EBannerSize;
import com.ironsource.mediationsdk.IronSourceBannerLayout;

public interface BaseBannerApi extends BaseApi {
    IronSourceBannerLayout createBanner(Activity activity, EBannerSize eBannerSize);

    void destroyBanner(IronSourceBannerLayout ironSourceBannerLayout);

    void loadBanner(IronSourceBannerLayout ironSourceBannerLayout);

    void loadBanner(IronSourceBannerLayout ironSourceBannerLayout, String str);
}
