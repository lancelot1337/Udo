package com.bugsnag.android.ndk;

import com.bugsnag.android.NotifyType;
import io.branch.referral.R;
import java.util.Observable;
import java.util.Observer;
import org.cocos2dx.lib.Cocos2dxEditBox;
import org.cocos2dx.lib.Cocos2dxHandler;

public class BugsnagObserver implements Observer {
    private boolean ndkInitialized = false;

    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$bugsnag$android$NotifyType = new int[NotifyType.values().length];

        static {
            try {
                $SwitchMap$com$bugsnag$android$NotifyType[NotifyType.USER.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$bugsnag$android$NotifyType[NotifyType.APP.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$bugsnag$android$NotifyType[NotifyType.DEVICE.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$bugsnag$android$NotifyType[NotifyType.CONTEXT.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$bugsnag$android$NotifyType[NotifyType.RELEASE_STAGES.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$bugsnag$android$NotifyType[NotifyType.FILTERS.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$bugsnag$android$NotifyType[NotifyType.BREADCRUMB.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$bugsnag$android$NotifyType[NotifyType.META.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$bugsnag$android$NotifyType[NotifyType.ALL.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
        }
    }

    public static native void populateAppDetails();

    public static native void populateBreadcumbDetails();

    public static native void populateContextDetails();

    public static native void populateDeviceDetails();

    public static native void populateErrorDetails();

    public static native void populateFilterDetails();

    public static native void populateMetaDataDetails();

    public static native void populateReleaseStagesDetails();

    public static native void populateUserDetails();

    public static native void setupBugsnag();

    static {
        System.loadLibrary("bugsnag-ndk");
    }

    public void update(Observable o, Object arg) {
        if (!this.ndkInitialized) {
            setupBugsnag();
            this.ndkInitialized = true;
        } else if (arg instanceof Integer) {
            switch (AnonymousClass1.$SwitchMap$com$bugsnag$android$NotifyType[NotifyType.fromInt((Integer) arg).ordinal()]) {
                case Cocos2dxHandler.HANDLER_SHOW_DIALOG /*1*/:
                    populateUserDetails();
                    return;
                case R.styleable.View_paddingStart /*2*/:
                    populateAppDetails();
                    return;
                case Cocos2dxEditBox.kEndActionReturn /*3*/:
                    populateDeviceDetails();
                    return;
                case R.styleable.View_theme /*4*/:
                    populateContextDetails();
                    return;
                case R.styleable.Toolbar_contentInsetStart /*5*/:
                    populateReleaseStagesDetails();
                    return;
                case R.styleable.Toolbar_contentInsetEnd /*6*/:
                    populateFilterDetails();
                    return;
                case R.styleable.Toolbar_contentInsetLeft /*7*/:
                    populateBreadcumbDetails();
                    return;
                case R.styleable.Toolbar_contentInsetRight /*8*/:
                    populateMetaDataDetails();
                    return;
                default:
                    populateErrorDetails();
                    return;
            }
        } else {
            populateErrorDetails();
        }
    }
}
