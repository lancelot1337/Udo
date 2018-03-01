package com.unity3d.ads.request;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.facebook.internal.NativeProtocol;
import com.ironsource.mediationsdk.utils.ServerResponseWrapper;
import com.ironsource.sdk.utils.Constants.ParametersKeys;
import com.unity3d.ads.log.DeviceLog;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class WebRequestHandler extends Handler {
    public void handleMessage(Message msg) {
        Bundle data = msg.getData();
        String url = data.getString(ParametersKeys.URL);
        data.remove(ParametersKeys.URL);
        String type = data.getString(EventEntry.COLUMN_NAME_TYPE);
        data.remove(EventEntry.COLUMN_NAME_TYPE);
        String body = data.getString("body");
        data.remove("body");
        WebRequestResultReceiver receiver = (WebRequestResultReceiver) data.getParcelable("receiver");
        data.remove("receiver");
        int connectTimeout = data.getInt("connectTimeout");
        data.remove("connectTimeout");
        int readTimeout = data.getInt("readTimeout");
        data.remove("readTimeout");
        HashMap<String, List<String>> headers = null;
        if (data.size() > 0) {
            DeviceLog.debug("There are headers left in data, reading them");
            headers = new HashMap();
            for (String k : data.keySet()) {
                headers.put(k, Arrays.asList(data.getStringArray(k)));
            }
        }
        if (msg.what == 1) {
            DeviceLog.debug("Handling request message: " + url + " type=" + type);
            try {
                makeRequest(url, type, headers, body, connectTimeout, readTimeout, receiver);
                return;
            } catch (MalformedURLException e) {
                DeviceLog.exception("Malformed URL", e);
                if (receiver != null) {
                    receiver.send(2, getBundleForFailResult(url, "Malformed URL", type, body));
                    return;
                }
                return;
            }
        }
        DeviceLog.error("No implementation for message: " + msg.what);
        if (receiver != null) {
            receiver.send(2, getBundleForFailResult(url, "Invalid Thread Message", type, body));
        }
    }

    private void makeRequest(String url, String type, HashMap<String, List<String>> headers, String body, int connectTimeout, int readTimeout, WebRequestResultReceiver receiver) throws MalformedURLException {
        WebRequest request = new WebRequest(url, type, headers, connectTimeout, readTimeout);
        if (body != null) {
            request.setBody(body);
        }
        try {
            String response = request.makeRequest();
            Bundle data = new Bundle();
            data.putString(ServerResponseWrapper.RESPONSE_FIELD, response);
            data.putString(ParametersKeys.URL, url);
            data.putInt("responseCode", request.getResponseCode());
            for (String key : request.getResponseHeaders().keySet()) {
                if (!(key == null || key.contentEquals("null"))) {
                    String[] values = new String[((List) request.getResponseHeaders().get(key)).size()];
                    for (int valueidx = 0; valueidx < ((List) request.getResponseHeaders().get(key)).size(); valueidx++) {
                        values[valueidx] = (String) ((List) request.getResponseHeaders().get(key)).get(valueidx);
                    }
                    data.putStringArray(key, values);
                }
            }
            receiver.send(1, data);
        } catch (IOException e) {
            DeviceLog.exception("Error completing request", e);
            receiver.send(2, getBundleForFailResult(url, e.getMessage(), type, body));
        }
    }

    private Bundle getBundleForFailResult(String url, String error, String type, String body) {
        Bundle data = new Bundle();
        data.putString(ParametersKeys.URL, url);
        data.putString(NativeProtocol.BRIDGE_ARG_ERROR_BUNDLE, error);
        data.putString(EventEntry.COLUMN_NAME_TYPE, type);
        data.putString("body", body);
        return data;
    }
}
