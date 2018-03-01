package com.ironsource.sdk.agent;

import android.content.Context;
import android.text.TextUtils;
import com.ironsource.environment.ApplicationContext;
import com.ironsource.sdk.SSAAdvertiserTest;
import com.ironsource.sdk.data.SSAObj;
import com.ironsource.sdk.precache.DownloadManager;
import com.ironsource.sdk.utils.Constants.RequestParameters;
import com.ironsource.sdk.utils.IronSourceSharedPrefHelper;
import com.ironsource.sdk.utils.Logger;
import com.ironsource.sdk.utils.SDKUtils;
import cz.msebera.android.httpclient.HttpStatus;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.URL;
import org.cocos2dx.lib.BuildConfig;

public class IronSourceAdsAdvertiserAgent implements SSAAdvertiserTest {
    private static final String BUNDLE_ID = "bundleId";
    private static final String DEVICE_IDS = "deviceIds";
    private static final String DOMAIN = "/campaigns/onLoad?";
    private static String PACKAGE_NAME = null;
    private static String SERVICE_HOST_NAME = "www.supersonicads.com";
    private static int SERVICE_PORT = 443;
    private static String SERVICE_PROTOCOL = "https";
    private static final String SIGNATURE = "signature";
    private static final String TAG = "IronSourceAdsAdvertiserAgent";
    private static String TIME_API = "https://www.supersonicads.com/timestamp.php";
    public static IronSourceAdsAdvertiserAgent sInstance;

    private class Result {
        private int mResponseCode;
        private String mResponseString;

        public Result(int responseCode, String responseString) {
            setResponseCode(responseCode);
            setResponseString(responseString);
        }

        public int getResponseCode() {
            return this.mResponseCode;
        }

        public void setResponseCode(int responseCode) {
            this.mResponseCode = responseCode;
        }

        public String getResponseString() {
            return this.mResponseString;
        }

        public void setResponseString(String responseString) {
            this.mResponseString = responseString;
        }
    }

    public static final class SuperSonicAdsAdvertiserException extends RuntimeException {
        private static final long serialVersionUID = 8169178234844720921L;

        public SuperSonicAdsAdvertiserException(Throwable t) {
            super(t);
        }
    }

    private IronSourceAdsAdvertiserAgent() {
    }

    public static synchronized IronSourceAdsAdvertiserAgent getInstance() {
        IronSourceAdsAdvertiserAgent ironSourceAdsAdvertiserAgent;
        synchronized (IronSourceAdsAdvertiserAgent.class) {
            Logger.i(TAG, "getInstance()");
            if (sInstance == null) {
                sInstance = new IronSourceAdsAdvertiserAgent();
            }
            ironSourceAdsAdvertiserAgent = sInstance;
        }
        return ironSourceAdsAdvertiserAgent;
    }

    public void reportAppStarted(final Context context) {
        if (!IronSourceSharedPrefHelper.getSupersonicPrefHelper(context).getReportAppStarted()) {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        if (IronSourceAdsAdvertiserAgent.this.performRequest(new URL(IronSourceAdsAdvertiserAgent.SERVICE_PROTOCOL, IronSourceAdsAdvertiserAgent.SERVICE_HOST_NAME, IronSourceAdsAdvertiserAgent.SERVICE_PORT, IronSourceAdsAdvertiserAgent.DOMAIN + IronSourceAdsAdvertiserAgent.this.getRequestParameters(context)), context).getResponseCode() == HttpStatus.SC_OK) {
                            IronSourceSharedPrefHelper.getSupersonicPrefHelper(context).setReportAppStarted(true);
                        }
                    } catch (MalformedURLException e) {
                    }
                }
            }).start();
        }
    }

    public void setDomain(String protocol, String host, int port) {
        SERVICE_PROTOCOL = protocol;
        SERVICE_HOST_NAME = host;
        SERVICE_PORT = port;
    }

    public void setTimeAPI(String url) {
        TIME_API = url;
    }

    public void setPackageName(String packageName) {
        PACKAGE_NAME = packageName;
    }

    public void clearReportApp(Context context) {
        IronSourceSharedPrefHelper.getSupersonicPrefHelper(context).setReportAppStarted(false);
    }

    private String getRequestParameters(Context context) {
        String pckName;
        StringBuilder parameters = new StringBuilder();
        if (TextUtils.isEmpty(PACKAGE_NAME)) {
            pckName = ApplicationContext.getPackageName(context);
        } else {
            pckName = PACKAGE_NAME;
        }
        if (!TextUtils.isEmpty(pckName)) {
            parameters.append(RequestParameters.AMPERSAND).append(BUNDLE_ID).append(RequestParameters.EQUAL).append(SDKUtils.encodeString(pckName));
        }
        SDKUtils.loadGoogleAdvertiserInfo(context);
        String advertiserId = SDKUtils.getAdvertiserId();
        boolean isLAT = SDKUtils.isLimitAdTrackingEnabled();
        if (TextUtils.isEmpty(advertiserId)) {
            advertiserId = BuildConfig.FLAVOR;
        } else {
            parameters.append(RequestParameters.AMPERSAND).append(DEVICE_IDS).append(SDKUtils.encodeString(RequestParameters.LEFT_BRACKETS)).append(SDKUtils.encodeString(RequestParameters.AID)).append(SDKUtils.encodeString(RequestParameters.RIGHT_BRACKETS)).append(RequestParameters.EQUAL).append(SDKUtils.encodeString(advertiserId));
            parameters.append(RequestParameters.AMPERSAND).append(SDKUtils.encodeString(RequestParameters.isLAT)).append(RequestParameters.EQUAL).append(SDKUtils.encodeString(Boolean.toString(isLAT)));
        }
        StringBuilder signature = new StringBuilder();
        signature.append(pckName);
        signature.append(advertiserId);
        signature.append(getUTCTimeStamp(context));
        parameters.append(RequestParameters.AMPERSAND).append(SIGNATURE).append(RequestParameters.EQUAL).append(SDKUtils.getMD5(signature.toString()));
        return parameters.toString();
    }

    public Result performRequest(URL url, Context context) {
        Throwable th;
        Result requestResult = new Result();
        HttpURLConnection connection = null;
        int responseCode = 0;
        InputStream is = null;
        StringBuilder builder = null;
        try {
            url.toURI();
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(HttpGet.METHOD_NAME);
            connection.setConnectTimeout(DownloadManager.OPERATION_TIMEOUT);
            connection.setReadTimeout(DownloadManager.OPERATION_TIMEOUT);
            connection.connect();
            responseCode = connection.getResponseCode();
            is = connection.getInputStream();
            byte[] buffer = new byte[102400];
            StringBuilder builder2 = new StringBuilder();
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                while (true) {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    builder2.append(line + "\n");
                }
                if (0 == 0) {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                        }
                    }
                    if (responseCode != HttpStatus.SC_OK) {
                        Logger.i(TAG, " RESPONSE CODE: " + responseCode + " URL: " + url);
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                    requestResult.setResponseCode(responseCode);
                    if (builder2 != null) {
                        requestResult.setResponseString("empty");
                        builder = builder2;
                    } else {
                        requestResult.setResponseString(builder2.toString());
                        builder = builder2;
                    }
                    return requestResult;
                }
                if (is != null) {
                    is.close();
                }
                if (responseCode != HttpStatus.SC_OK) {
                    Logger.i(TAG, " RESPONSE CODE: " + responseCode + " URL: " + url);
                }
                if (connection != null) {
                    connection.disconnect();
                }
                requestResult.setResponseCode(responseCode);
                if (builder2 != null) {
                    requestResult.setResponseString(builder2.toString());
                    builder = builder2;
                } else {
                    requestResult.setResponseString("empty");
                    builder = builder2;
                }
                return requestResult;
            } catch (MalformedURLException e2) {
                builder = builder2;
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e3) {
                    }
                }
                if (responseCode != HttpStatus.SC_OK) {
                    Logger.i(TAG, " RESPONSE CODE: " + responseCode + " URL: " + url);
                }
                if (connection != null) {
                    connection.disconnect();
                }
                requestResult.setResponseCode(responseCode);
                if (builder != null) {
                    requestResult.setResponseString("empty");
                } else {
                    requestResult.setResponseString(builder.toString());
                }
                return requestResult;
            } catch (URISyntaxException e4) {
                builder = builder2;
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e5) {
                    }
                }
                if (responseCode != HttpStatus.SC_OK) {
                    Logger.i(TAG, " RESPONSE CODE: " + responseCode + " URL: " + url);
                }
                if (connection != null) {
                    connection.disconnect();
                }
                requestResult.setResponseCode(responseCode);
                if (builder != null) {
                    requestResult.setResponseString("empty");
                } else {
                    requestResult.setResponseString(builder.toString());
                }
                return requestResult;
            } catch (SocketTimeoutException e6) {
                builder = builder2;
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e7) {
                    }
                }
                if (responseCode != HttpStatus.SC_OK) {
                    Logger.i(TAG, " RESPONSE CODE: " + responseCode + " URL: " + url);
                }
                if (connection != null) {
                    connection.disconnect();
                }
                requestResult.setResponseCode(responseCode);
                if (builder != null) {
                    requestResult.setResponseString("empty");
                } else {
                    requestResult.setResponseString(builder.toString());
                }
                return requestResult;
            } catch (FileNotFoundException e8) {
                builder = builder2;
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e9) {
                    }
                }
                if (responseCode != HttpStatus.SC_OK) {
                    Logger.i(TAG, " RESPONSE CODE: " + responseCode + " URL: " + url);
                }
                if (connection != null) {
                    connection.disconnect();
                }
                requestResult.setResponseCode(responseCode);
                if (builder != null) {
                    requestResult.setResponseString("empty");
                } else {
                    requestResult.setResponseString(builder.toString());
                }
                return requestResult;
            } catch (IOException e10) {
                builder = builder2;
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e11) {
                    }
                }
                if (responseCode != HttpStatus.SC_OK) {
                    Logger.i(TAG, " RESPONSE CODE: " + responseCode + " URL: " + url);
                }
                if (connection != null) {
                    connection.disconnect();
                }
                requestResult.setResponseCode(responseCode);
                if (builder != null) {
                    requestResult.setResponseString("empty");
                } else {
                    requestResult.setResponseString(builder.toString());
                }
                return requestResult;
            } catch (Throwable th2) {
                th = th2;
                builder = builder2;
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e12) {
                    }
                }
                if (responseCode != HttpStatus.SC_OK) {
                    Logger.i(TAG, " RESPONSE CODE: " + responseCode + " URL: " + url);
                }
                if (connection != null) {
                    connection.disconnect();
                }
                requestResult.setResponseCode(responseCode);
                if (builder != null) {
                    requestResult.setResponseString("empty");
                } else {
                    requestResult.setResponseString(builder.toString());
                }
                throw th;
            }
        } catch (MalformedURLException e13) {
            if (is != null) {
                is.close();
            }
            if (responseCode != HttpStatus.SC_OK) {
                Logger.i(TAG, " RESPONSE CODE: " + responseCode + " URL: " + url);
            }
            if (connection != null) {
                connection.disconnect();
            }
            requestResult.setResponseCode(responseCode);
            if (builder != null) {
                requestResult.setResponseString(builder.toString());
            } else {
                requestResult.setResponseString("empty");
            }
            return requestResult;
        } catch (URISyntaxException e14) {
            if (is != null) {
                is.close();
            }
            if (responseCode != HttpStatus.SC_OK) {
                Logger.i(TAG, " RESPONSE CODE: " + responseCode + " URL: " + url);
            }
            if (connection != null) {
                connection.disconnect();
            }
            requestResult.setResponseCode(responseCode);
            if (builder != null) {
                requestResult.setResponseString(builder.toString());
            } else {
                requestResult.setResponseString("empty");
            }
            return requestResult;
        } catch (SocketTimeoutException e15) {
            if (is != null) {
                is.close();
            }
            if (responseCode != HttpStatus.SC_OK) {
                Logger.i(TAG, " RESPONSE CODE: " + responseCode + " URL: " + url);
            }
            if (connection != null) {
                connection.disconnect();
            }
            requestResult.setResponseCode(responseCode);
            if (builder != null) {
                requestResult.setResponseString(builder.toString());
            } else {
                requestResult.setResponseString("empty");
            }
            return requestResult;
        } catch (FileNotFoundException e16) {
            if (is != null) {
                is.close();
            }
            if (responseCode != HttpStatus.SC_OK) {
                Logger.i(TAG, " RESPONSE CODE: " + responseCode + " URL: " + url);
            }
            if (connection != null) {
                connection.disconnect();
            }
            requestResult.setResponseCode(responseCode);
            if (builder != null) {
                requestResult.setResponseString(builder.toString());
            } else {
                requestResult.setResponseString("empty");
            }
            return requestResult;
        } catch (IOException e17) {
            if (is != null) {
                is.close();
            }
            if (responseCode != HttpStatus.SC_OK) {
                Logger.i(TAG, " RESPONSE CODE: " + responseCode + " URL: " + url);
            }
            if (connection != null) {
                connection.disconnect();
            }
            requestResult.setResponseCode(responseCode);
            if (builder != null) {
                requestResult.setResponseString(builder.toString());
            } else {
                requestResult.setResponseString("empty");
            }
            return requestResult;
        } catch (Throwable th3) {
            th = th3;
            if (is != null) {
                is.close();
            }
            if (responseCode != HttpStatus.SC_OK) {
                Logger.i(TAG, " RESPONSE CODE: " + responseCode + " URL: " + url);
            }
            if (connection != null) {
                connection.disconnect();
            }
            requestResult.setResponseCode(responseCode);
            if (builder != null) {
                requestResult.setResponseString(builder.toString());
            } else {
                requestResult.setResponseString("empty");
            }
            throw th;
        }
    }

    private int getUTCTimeStamp(Context context) {
        try {
            Result result = performRequest(new URL(TIME_API), context);
            if (result.getResponseCode() == HttpStatus.SC_OK) {
                SSAObj ssaObj = new SSAObj(result.getResponseString());
                if (ssaObj.containsKey(EventEntry.COLUMN_NAME_TIMESTAMP)) {
                    int time = Integer.parseInt(ssaObj.getString(EventEntry.COLUMN_NAME_TIMESTAMP));
                    return time - (time % 60);
                }
            }
        } catch (MalformedURLException e) {
        }
        return 0;
    }
}
