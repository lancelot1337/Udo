package com.unity3d.ads.connectivity;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build.VERSION;
import android.telephony.TelephonyManager;
import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.properties.ClientProperties;
import com.unity3d.ads.webview.WebViewApp;
import com.unity3d.ads.webview.WebViewEventCategory;
import io.branch.referral.R;
import java.util.HashSet;
import java.util.Iterator;
import org.cocos2dx.lib.Cocos2dxEditBox;
import org.cocos2dx.lib.Cocos2dxHandler;

public class ConnectivityMonitor {
    private static int _connected = -1;
    private static HashSet<IConnectivityListener> _listeners = null;
    private static boolean _listening = false;
    private static int _networkType = -1;
    private static boolean _webappMonitoring = false;
    private static boolean _wifi = false;

    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$unity3d$ads$connectivity$ConnectivityEvent = new int[ConnectivityEvent.values().length];

        static {
            try {
                $SwitchMap$com$unity3d$ads$connectivity$ConnectivityEvent[ConnectivityEvent.CONNECTED.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$unity3d$ads$connectivity$ConnectivityEvent[ConnectivityEvent.DISCONNECTED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$unity3d$ads$connectivity$ConnectivityEvent[ConnectivityEvent.NETWORK_CHANGE.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    public static void setConnectionMonitoring(boolean monitoring) {
        _webappMonitoring = monitoring;
        updateListeningStatus();
    }

    public static void addListener(IConnectivityListener listener) {
        if (_listeners == null) {
            _listeners = new HashSet();
        }
        _listeners.add(listener);
        updateListeningStatus();
    }

    public static void removeListener(IConnectivityListener listener) {
        if (_listeners != null) {
            _listeners.remove(listener);
            updateListeningStatus();
        }
    }

    public static void stopAll() {
        _listeners = null;
        _webappMonitoring = false;
        updateListeningStatus();
    }

    private static void updateListeningStatus() {
        if (_webappMonitoring || !(_listeners == null || _listeners.isEmpty())) {
            startListening();
        } else {
            stopListening();
        }
    }

    private static void startListening() {
        if (!_listening) {
            _listening = true;
            initConnectionStatus();
            if (VERSION.SDK_INT < 21) {
                ConnectivityChangeReceiver.register();
            } else {
                ConnectivityNetworkCallback.register();
            }
        }
    }

    private static void stopListening() {
        if (_listening) {
            _listening = false;
            if (VERSION.SDK_INT < 21) {
                ConnectivityChangeReceiver.unregister();
            } else {
                ConnectivityNetworkCallback.unregister();
            }
        }
    }

    private static void initConnectionStatus() {
        boolean z = true;
        ConnectivityManager cm = (ConnectivityManager) ClientProperties.getApplicationContext().getSystemService("connectivity");
        if (cm != null) {
            NetworkInfo ni = cm.getActiveNetworkInfo();
            if (ni == null || !ni.isConnected()) {
                _connected = 0;
                return;
            }
            _connected = 1;
            if (ni.getType() != 1) {
                z = false;
            }
            _wifi = z;
            if (!_wifi) {
                _networkType = ((TelephonyManager) ClientProperties.getApplicationContext().getSystemService("phone")).getNetworkType();
            }
        }
    }

    public static void connected() {
        if (_connected != 1) {
            DeviceLog.debug("Unity Ads connectivity change: connected");
            initConnectionStatus();
            if (_listeners != null) {
                Iterator it = _listeners.iterator();
                while (it.hasNext()) {
                    ((IConnectivityListener) it.next()).onConnected();
                }
            }
            sendToWebview(ConnectivityEvent.CONNECTED, _wifi, _networkType);
        }
    }

    public static void disconnected() {
        if (_connected != 0) {
            _connected = 0;
            DeviceLog.debug("Unity Ads connectivity change: disconnected");
            if (_listeners != null) {
                Iterator it = _listeners.iterator();
                while (it.hasNext()) {
                    ((IConnectivityListener) it.next()).onDisconnected();
                }
            }
            sendToWebview(ConnectivityEvent.DISCONNECTED, false, 0);
        }
    }

    public static void connectionStatusChanged() {
        boolean wifiStatus = true;
        if (_connected == 1) {
            NetworkInfo ni = ((ConnectivityManager) ClientProperties.getApplicationContext().getSystemService("connectivity")).getActiveNetworkInfo();
            if (ni != null && ni.isConnected()) {
                if (ni.getType() != 1) {
                    wifiStatus = false;
                }
                int mobileNetworkType = ((TelephonyManager) ClientProperties.getApplicationContext().getSystemService("phone")).getNetworkType();
                if (wifiStatus != _wifi || (mobileNetworkType != _networkType && !_wifi)) {
                    _wifi = wifiStatus;
                    _networkType = mobileNetworkType;
                    DeviceLog.debug("Unity Ads connectivity change: network change");
                    sendToWebview(ConnectivityEvent.NETWORK_CHANGE, wifiStatus, mobileNetworkType);
                }
            }
        }
    }

    private static void sendToWebview(ConnectivityEvent eventType, boolean wifi, int networkType) {
        if (_webappMonitoring) {
            WebViewApp webViewApp = WebViewApp.getCurrentApp();
            if (webViewApp != null && webViewApp.isWebAppLoaded()) {
                switch (AnonymousClass1.$SwitchMap$com$unity3d$ads$connectivity$ConnectivityEvent[eventType.ordinal()]) {
                    case Cocos2dxHandler.HANDLER_SHOW_DIALOG /*1*/:
                        if (wifi) {
                            webViewApp.sendEvent(WebViewEventCategory.CONNECTIVITY, ConnectivityEvent.CONNECTED, Boolean.valueOf(wifi), Integer.valueOf(0));
                            return;
                        }
                        webViewApp.sendEvent(WebViewEventCategory.CONNECTIVITY, ConnectivityEvent.CONNECTED, Boolean.valueOf(wifi), Integer.valueOf(networkType));
                        return;
                    case R.styleable.View_paddingStart /*2*/:
                        webViewApp.sendEvent(WebViewEventCategory.CONNECTIVITY, ConnectivityEvent.DISCONNECTED, new Object[0]);
                        return;
                    case Cocos2dxEditBox.kEndActionReturn /*3*/:
                        if (wifi) {
                            webViewApp.sendEvent(WebViewEventCategory.CONNECTIVITY, ConnectivityEvent.NETWORK_CHANGE, Boolean.valueOf(wifi), Integer.valueOf(0));
                            return;
                        }
                        webViewApp.sendEvent(WebViewEventCategory.CONNECTIVITY, ConnectivityEvent.NETWORK_CHANGE, Boolean.valueOf(wifi), Integer.valueOf(networkType));
                        return;
                    default:
                        return;
                }
            }
        }
    }
}
