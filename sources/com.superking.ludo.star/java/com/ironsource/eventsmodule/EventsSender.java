package com.ironsource.eventsmodule;

import android.os.AsyncTask;
import cz.msebera.android.httpclient.HttpStatus;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.protocol.HTTP;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class EventsSender extends AsyncTask<Object, Void, Boolean> {
    private final String APPLICATION_JSON = "application/json";
    private final String CONTENT_TYPE_FIELD = HTTP.CONTENT_TYPE;
    private final String SERVER_REQUEST_ENCODING = HTTP.UTF_8;
    private final String SERVER_REQUEST_METHOD = HttpPost.METHOD_NAME;
    private final int SERVER_REQUEST_TIMEOUT = 15000;
    private ArrayList extraData;
    private IEventsSenderResultListener mResultListener;

    public EventsSender(IEventsSenderResultListener resultListener) {
        this.mResultListener = resultListener;
    }

    protected Boolean doInBackground(Object... objects) {
        try {
            boolean z;
            URL requestURL = new URL((String) objects[1]);
            this.extraData = (ArrayList) objects[2];
            HttpURLConnection conn = (HttpURLConnection) requestURL.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod(HttpPost.METHOD_NAME);
            conn.setRequestProperty(HTTP.CONTENT_TYPE, "application/json");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, HTTP.UTF_8));
            writer.write((String) objects[0]);
            writer.flush();
            writer.close();
            os.close();
            int responseCode = conn.getResponseCode();
            conn.disconnect();
            if (responseCode == HttpStatus.SC_OK) {
                z = true;
            } else {
                z = false;
            }
            return Boolean.valueOf(z);
        } catch (Exception e) {
            return Boolean.valueOf(false);
        }
    }

    protected void onPostExecute(Boolean success) {
        if (this.mResultListener != null) {
            this.mResultListener.onEventsSenderResult(this.extraData, success.booleanValue());
        }
    }
}
