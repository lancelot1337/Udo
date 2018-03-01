package io.branch.referral;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

class DeferredAppLinkDataHandler {
    private static final String NATIVE_URL_KEY = "com.facebook.platform.APPLINK_NATIVE_URL";

    public interface AppLinkFetchEvents {
        void onAppLinkFetchFinished(String str);
    }

    DeferredAppLinkDataHandler() {
    }

    public static Boolean fetchDeferredAppLinkData(Context context, final AppLinkFetchEvents callback) {
        boolean isRequestSucceeded = true;
        try {
            Class.forName("com.facebook.FacebookSdk").getMethod("sdkInitialize", new Class[]{Context.class}).invoke(null, new Object[]{context});
            final Class<?> AppLinkDataClass = Class.forName("com.facebook.applinks.AppLinkData");
            Method fetchDeferredAppLinkDataMethod = AppLinkDataClass.getMethod("fetchDeferredAppLinkData", new Class[]{Context.class, String.class, Class.forName("com.facebook.applinks.AppLinkData$CompletionHandler")});
            InvocationHandler ALDataCompletionHandler = new InvocationHandler() {
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    if (method.getName().equals("onDeferredAppLinkDataFetched") && args[0] != null) {
                        String appLinkUrl = null;
                        Object appLinkDataClass = AppLinkDataClass.cast(args[0]);
                        Bundle appLinkDataBundle = (Bundle) Bundle.class.cast(AppLinkDataClass.getMethod("getArgumentBundle", new Class[0]).invoke(appLinkDataClass, new Object[0]));
                        if (appLinkDataBundle != null) {
                            appLinkUrl = appLinkDataBundle.getString(DeferredAppLinkDataHandler.NATIVE_URL_KEY);
                        }
                        if (callback != null) {
                            callback.onAppLinkFetchFinished(appLinkUrl);
                        }
                    } else if (callback != null) {
                        callback.onAppLinkFetchFinished(null);
                    }
                    return null;
                }
            };
            Object completionListenerInterface = Proxy.newProxyInstance(AppLinkDataCompletionHandlerClass.getClassLoader(), new Class[]{AppLinkDataCompletionHandlerClass}, ALDataCompletionHandler);
            if (TextUtils.isEmpty(context.getString(context.getResources().getIdentifier("facebook_app_id", "string", context.getPackageName())))) {
                isRequestSucceeded = false;
            } else {
                fetchDeferredAppLinkDataMethod.invoke(null, new Object[]{context, context.getString(context.getResources().getIdentifier("facebook_app_id", "string", context.getPackageName())), completionListenerInterface});
            }
        } catch (Throwable th) {
            isRequestSucceeded = false;
        }
        return Boolean.valueOf(isRequestSucceeded);
    }
}
