package com.ironsource.sdk.handlers;

import android.app.Activity;
import com.ironsource.sdk.agent.IronSourceAdsPublisherAgent;
import com.ironsource.sdk.controller.IronSourceWebView;
import com.ironsource.sdk.data.SSAEnums.BackButtonState;
import com.ironsource.sdk.utils.IronSourceSharedPrefHelper;
import org.cocos2dx.lib.Cocos2dxEditBox;

public class BackButtonHandler {
    public static BackButtonHandler mInstance;

    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$ironsource$sdk$data$SSAEnums$BackButtonState = new int[BackButtonState.values().length];

        static {
            try {
                $SwitchMap$com$ironsource$sdk$data$SSAEnums$BackButtonState[BackButtonState.None.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$ironsource$sdk$data$SSAEnums$BackButtonState[BackButtonState.Device.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$ironsource$sdk$data$SSAEnums$BackButtonState[BackButtonState.Controller.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    public static BackButtonHandler getInstance() {
        if (mInstance == null) {
            return new BackButtonHandler();
        }
        return mInstance;
    }

    public boolean handleBackButton(Activity activity) {
        switch (AnonymousClass1.$SwitchMap$com$ironsource$sdk$data$SSAEnums$BackButtonState[IronSourceSharedPrefHelper.getSupersonicPrefHelper().getBackButtonState().ordinal()]) {
            case Cocos2dxEditBox.kEndActionReturn /*3*/:
                IronSourceWebView webViewController = IronSourceAdsPublisherAgent.getInstance(activity).getWebViewController();
                if (webViewController != null) {
                    webViewController.nativeNavigationPressed("back");
                }
                return true;
            default:
                return false;
        }
    }
}
