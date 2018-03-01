package com.bugsnag.android;

import com.facebook.internal.NativeProtocol;
import com.facebook.share.internal.ShareConstants;
import com.ironsource.sdk.utils.Constants;

public enum BreadcrumbType {
    ERROR(NativeProtocol.BRIDGE_ARG_ERROR_BUNDLE),
    LOG("log"),
    MANUAL("manual"),
    NAVIGATION("navigation"),
    PROCESS("process"),
    REQUEST(ShareConstants.WEB_DIALOG_RESULT_PARAM_REQUEST_ID),
    STATE(Constants.RESTORED_STATE),
    USER("user");
    
    private final String type;

    private BreadcrumbType(String type) {
        this.type = type;
    }

    public String toString() {
        return this.type;
    }
}
