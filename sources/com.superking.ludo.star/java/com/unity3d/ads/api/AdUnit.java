package com.unity3d.ads.api;

import android.content.Intent;
import com.unity3d.ads.adunit.AdUnitActivity;
import com.unity3d.ads.adunit.AdUnitError;
import com.unity3d.ads.adunit.AdUnitSoftwareActivity;
import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.misc.Utilities;
import com.unity3d.ads.properties.ClientProperties;
import com.unity3d.ads.webview.bridge.WebViewCallback;
import com.unity3d.ads.webview.bridge.WebViewExposed;
import java.util.ArrayList;
import java.util.Arrays;
import org.json.JSONArray;
import org.json.JSONException;

public class AdUnit {
    private static AdUnitActivity _adUnitActivity;
    private static int _currentActivityId = -1;

    private AdUnit() {
    }

    public static void setAdUnitActivity(AdUnitActivity activity) {
        _adUnitActivity = activity;
    }

    public static AdUnitActivity getAdUnitActivity() {
        return _adUnitActivity;
    }

    public static int getCurrentAdUnitActivityId() {
        return _currentActivityId;
    }

    public static void setCurrentAdUnitActivityId(int activityId) {
        _currentActivityId = activityId;
    }

    @WebViewExposed
    public static void open(Integer activityId, JSONArray views, Integer orientation, WebViewCallback callback) {
        open(activityId, views, orientation, null, callback);
    }

    @WebViewExposed
    public static void open(Integer activityId, JSONArray views, Integer orientation, JSONArray keyevents, WebViewCallback callback) {
        open(activityId, views, orientation, keyevents, Integer.valueOf(0), Boolean.valueOf(true), callback);
    }

    @WebViewExposed
    public static void open(Integer activityId, JSONArray views, Integer orientation, JSONArray keyevents, Integer systemUiVisibility, Boolean hardwareAcceleration, WebViewCallback callback) {
        Intent intent;
        if (hardwareAcceleration.booleanValue()) {
            DeviceLog.debug("Unity Ads opening new hardware accelerated ad unit activity");
            intent = new Intent(ClientProperties.getActivity(), AdUnitActivity.class);
        } else {
            DeviceLog.debug("Unity Ads opening new ad unit activity, hardware acceleration disabled");
            intent = new Intent(ClientProperties.getActivity(), AdUnitSoftwareActivity.class);
        }
        intent.addFlags(268500992);
        if (activityId != null) {
            try {
                intent.putExtra(AdUnitActivity.EXTRA_ACTIVITY_ID, activityId.intValue());
                setCurrentAdUnitActivityId(activityId.intValue());
                try {
                    intent.putExtra(AdUnitActivity.EXTRA_VIEWS, getViewList(views));
                    if (keyevents != null) {
                        try {
                            intent.putExtra(AdUnitActivity.EXTRA_KEY_EVENT_LIST, getKeyEventList(keyevents));
                        } catch (Exception e) {
                            DeviceLog.exception("Error parsing views from viewList", e);
                            callback.error(AdUnitError.CORRUPTED_KEYEVENTLIST, keyevents, e.getMessage());
                            return;
                        }
                    }
                    intent.putExtra(AdUnitActivity.EXTRA_SYSTEM_UI_VISIBILITY, systemUiVisibility);
                    intent.putExtra(AdUnitActivity.EXTRA_ORIENTATION, orientation);
                    ClientProperties.getActivity().startActivity(intent);
                    DeviceLog.debug("Opened AdUnitActivity with: " + views.toString());
                    callback.invoke(new Object[0]);
                    return;
                } catch (Exception e2) {
                    DeviceLog.exception("Error parsing views from viewList", e2);
                    callback.error(AdUnitError.CORRUPTED_VIEWLIST, views, e2.getMessage());
                    return;
                }
            } catch (Exception e22) {
                DeviceLog.exception("Could not set activityId for intent", e22);
                callback.error(AdUnitError.ACTIVITY_ID, Integer.valueOf(activityId.intValue()), e22.getMessage());
                return;
            }
        }
        DeviceLog.error("Activity ID is NULL");
        callback.error(AdUnitError.ACTIVITY_ID, "Activity ID NULL");
    }

    @WebViewExposed
    public static void close(WebViewCallback callback) {
        if (getAdUnitActivity() != null) {
            getAdUnitActivity().finish();
            callback.invoke(new Object[0]);
            return;
        }
        callback.error(AdUnitError.ACTIVITY_NULL, new Object[0]);
    }

    @WebViewExposed
    public static void setViews(final JSONArray views, WebViewCallback callback) {
        boolean corrupted = false;
        try {
            String[] viewList = getViewList(views);
        } catch (JSONException e) {
            callback.error(AdUnitError.CORRUPTED_VIEWLIST, views);
            corrupted = true;
        }
        if (!corrupted) {
            Utilities.runOnUiThread(new Runnable() {
                public void run() {
                    if (AdUnit.getAdUnitActivity() != null) {
                        try {
                            AdUnit.getAdUnitActivity().setViews(AdUnit.getViewList(views));
                        } catch (Exception e) {
                            DeviceLog.exception("Corrupted viewlist", e);
                        }
                    }
                }
            });
        }
        if (getAdUnitActivity() != null) {
            callback.invoke(views);
            return;
        }
        callback.error(AdUnitError.ACTIVITY_NULL, new Object[0]);
    }

    @WebViewExposed
    public static void getViews(WebViewCallback callback) {
        if (getAdUnitActivity() != null) {
            String[] views = getAdUnitActivity().getViews();
            callback.invoke(new JSONArray(Arrays.asList(views)));
            return;
        }
        callback.error(AdUnitError.ACTIVITY_NULL, new Object[0]);
    }

    @WebViewExposed
    public static void setOrientation(final Integer orientation, WebViewCallback callback) {
        Utilities.runOnUiThread(new Runnable() {
            public void run() {
                if (AdUnit.getAdUnitActivity() != null) {
                    AdUnit.getAdUnitActivity().setOrientation(orientation.intValue());
                }
            }
        });
        if (getAdUnitActivity() != null) {
            callback.invoke(orientation);
            return;
        }
        callback.error(AdUnitError.ACTIVITY_NULL, new Object[0]);
    }

    @WebViewExposed
    public static void getOrientation(WebViewCallback callback) {
        if (getAdUnitActivity() != null) {
            callback.invoke(Integer.valueOf(getAdUnitActivity().getRequestedOrientation()));
            return;
        }
        callback.error(AdUnitError.ACTIVITY_NULL, new Object[0]);
    }

    @WebViewExposed
    public static void setKeepScreenOn(final Boolean screenOn, WebViewCallback callback) {
        Utilities.runOnUiThread(new Runnable() {
            public void run() {
                if (AdUnit.getAdUnitActivity() != null) {
                    AdUnit.getAdUnitActivity().setKeepScreenOn(screenOn.booleanValue());
                }
            }
        });
        if (getAdUnitActivity() != null) {
            callback.invoke(new Object[0]);
        } else {
            callback.error(AdUnitError.ACTIVITY_NULL, new Object[0]);
        }
    }

    @WebViewExposed
    public static void setSystemUiVisibility(final Integer systemUiVisibility, WebViewCallback callback) {
        Utilities.runOnUiThread(new Runnable() {
            public void run() {
                if (AdUnit.getAdUnitActivity() != null) {
                    AdUnit.getAdUnitActivity().setSystemUiVisibility(systemUiVisibility.intValue());
                }
            }
        });
        if (getAdUnitActivity() != null) {
            callback.invoke(systemUiVisibility);
            return;
        }
        callback.error(AdUnitError.ACTIVITY_NULL, new Object[0]);
    }

    @WebViewExposed
    public static void setKeyEventList(JSONArray keyevents, WebViewCallback callback) {
        if (getAdUnitActivity() != null) {
            try {
                getAdUnitActivity().setKeyEventList(getKeyEventList(keyevents));
                callback.invoke(keyevents);
                return;
            } catch (Exception e) {
                DeviceLog.exception("Error parsing views from viewList", e);
                callback.error(AdUnitError.CORRUPTED_KEYEVENTLIST, keyevents, e.getMessage());
                return;
            }
        }
        callback.error(AdUnitError.ACTIVITY_NULL, new Object[0]);
    }

    private static String[] getViewList(JSONArray views) throws JSONException {
        String[] viewList = new String[views.length()];
        for (int viewidx = 0; viewidx < views.length(); viewidx++) {
            viewList[viewidx] = views.getString(viewidx);
        }
        return viewList;
    }

    private static ArrayList<Integer> getKeyEventList(JSONArray keyevents) throws JSONException {
        ArrayList<Integer> keyEvents = new ArrayList();
        for (Integer idx = Integer.valueOf(0); idx.intValue() < keyevents.length(); idx = Integer.valueOf(idx.intValue() + 1)) {
            keyEvents.add(Integer.valueOf(keyevents.getInt(idx.intValue())));
        }
        return keyEvents;
    }
}
