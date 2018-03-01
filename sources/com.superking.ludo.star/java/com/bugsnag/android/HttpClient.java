package com.bugsnag.android;

import com.bugsnag.android.JsonStream.Streamable;
import cz.msebera.android.httpclient.protocol.HTTP;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

class HttpClient {

    static class BadResponseException extends Exception {
        public BadResponseException(String url, int responseCode) {
            super(String.format(Locale.US, "Got non-200 response code (%d) from %s", new Object[]{Integer.valueOf(responseCode), url}));
        }
    }

    static class NetworkException extends IOException {
        public NetworkException(String url, Exception ex) {
            super(String.format("Network error when posting to %s", new Object[]{url}));
            initCause(ex);
        }
    }

    HttpClient() {
    }

    static void post(String urlString, Streamable payload) throws NetworkException, BadResponseException {
        OutputStream out;
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(urlString).openConnection();
            conn.setDoOutput(true);
            conn.setChunkedStreamingMode(0);
            conn.addRequestProperty(HTTP.CONTENT_TYPE, "application/json");
            out = null;
            out = conn.getOutputStream();
            JsonStream stream = new JsonStream(new OutputStreamWriter(out));
            payload.toStream(stream);
            stream.close();
            IOUtils.closeQuietly(out);
            int status = conn.getResponseCode();
            if (status / 100 != 2) {
                throw new BadResponseException(urlString, status);
            }
            IOUtils.close(conn);
        } catch (IOException e) {
            throw new NetworkException(urlString, e);
        } catch (Throwable th) {
            IOUtils.close(conn);
        }
    }
}
