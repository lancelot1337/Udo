package com.ironsource.mediationsdk.logger;

import android.util.Log;
import com.ironsource.mediationsdk.logger.IronSourceLogger.IronSourceTag;
import com.ironsource.sdk.utils.Constants.RequestParameters;
import io.branch.referral.R;
import org.cocos2dx.lib.BuildConfig;
import org.cocos2dx.lib.Cocos2dxEditBox;
import org.cocos2dx.lib.Cocos2dxHandler;

public class ConsoleLogger extends IronSourceLogger {
    public static final String NAME = "console";

    private ConsoleLogger() {
        super(NAME);
    }

    public ConsoleLogger(int debugLevel) {
        super(NAME, debugLevel);
    }

    public void log(IronSourceTag tag, String message, int logLevel) {
        switch (logLevel) {
            case Cocos2dxEditBox.kEndActionUnknown /*0*/:
                Log.v(BuildConfig.FLAVOR + tag, message);
                return;
            case Cocos2dxHandler.HANDLER_SHOW_DIALOG /*1*/:
                Log.i(BuildConfig.FLAVOR + tag, message);
                return;
            case R.styleable.View_paddingStart /*2*/:
                Log.w(BuildConfig.FLAVOR + tag, message);
                return;
            case Cocos2dxEditBox.kEndActionReturn /*3*/:
                Log.e(BuildConfig.FLAVOR + tag, message);
                return;
            default:
                return;
        }
    }

    public void logException(IronSourceTag tag, String message, Throwable e) {
        log(tag, message + ":stacktrace[" + Log.getStackTraceString(e) + RequestParameters.RIGHT_BRACKETS, 3);
    }
}
