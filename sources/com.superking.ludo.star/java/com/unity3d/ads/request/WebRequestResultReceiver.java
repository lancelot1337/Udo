package com.unity3d.ads.request;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import com.facebook.internal.NativeProtocol;
import com.ironsource.mediationsdk.utils.ServerResponseWrapper;
import com.ironsource.sdk.utils.Constants.ParametersKeys;
import com.unity3d.ads.log.DeviceLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebRequestResultReceiver extends ResultReceiver {
    public static final int RESULT_FAILED = 2;
    public static final int RESULT_SUCCESS = 1;
    private IWebRequestListener _listener;

    public WebRequestResultReceiver(Handler handler, IWebRequestListener listener) {
        super(handler);
        this._listener = listener;
    }

    protected void onReceiveResult(int resultCode, Bundle resultData) {
        DeviceLog.entered();
        if (this._listener != null) {
            switch (resultCode) {
                case RESULT_SUCCESS /*1*/:
                    String url = resultData.getString(ParametersKeys.URL);
                    resultData.remove(ParametersKeys.URL);
                    String response = resultData.getString(ServerResponseWrapper.RESPONSE_FIELD);
                    resultData.remove(ServerResponseWrapper.RESPONSE_FIELD);
                    int responseCode = resultData.getInt("responseCode");
                    resultData.remove("responseCode");
                    this._listener.onComplete(url, response, responseCode, getResponseHeaders(resultData));
                    break;
                case RESULT_FAILED /*2*/:
                    this._listener.onFailed(resultData.getString(ParametersKeys.URL), resultData.getString(NativeProtocol.BRIDGE_ARG_ERROR_BUNDLE));
                    break;
                default:
                    DeviceLog.error("Unhandled resultCode: " + resultCode);
                    this._listener.onFailed(resultData.getString(ParametersKeys.URL), "Invalid resultCode=" + resultCode);
                    break;
            }
        }
        super.onReceiveResult(resultCode, resultData);
    }

    private Map<String, List<String>> getResponseHeaders(Bundle resultData) {
        Map<String, List<String>> responseHeaders = null;
        if (resultData.size() > 0) {
            responseHeaders = new HashMap();
            for (String k : resultData.keySet()) {
                String[] tmpAr = resultData.getStringArray(k);
                if (tmpAr != null) {
                    responseHeaders.put(k, new ArrayList(Arrays.asList(tmpAr)));
                }
            }
        }
        return responseHeaders;
    }
}
