package io.branch.indexing;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import io.branch.referral.PrefHelper;
import io.branch.referral.util.LinkProperties;
import java.lang.reflect.Method;

class AppIndexingHelper {
    AppIndexingHelper() {
    }

    public static void addToAppIndex(final Context context, final BranchUniversalObject buo) {
        new Thread(new Runnable() {
            public void run() {
                String urlForAppIndexing = buo.getShortUrl(context, new LinkProperties().setChannel("google_search"));
                if (!TextUtils.isEmpty(urlForAppIndexing)) {
                    try {
                        AppIndexingHelper.listOnGoogleSearch(urlForAppIndexing, context, buo);
                    } catch (Throwable th) {
                        PrefHelper.Debug("BranchSDK", "Branch Warning: Unable to list your content in google search. Please make sure you have the latest google play libs added to your project");
                    }
                }
            }
        }).run();
    }

    private static void listOnGoogleSearch(String shortLink, Context context, BranchUniversalObject branchUniversalObject) throws Exception {
        Class<?> ThingClass = Class.forName("com.google.android.gms.appindexing.Thing");
        Class<?> ThingBuilderClass = Class.forName("com.google.android.gms.appindexing.Thing$Builder");
        Object thingBuilder = ThingBuilderClass.getConstructor(new Class[0]).newInstance(new Object[0]);
        Method setNameMethod = ThingBuilderClass.getMethod("setName", new Class[]{String.class});
        Method setDescMethod = ThingBuilderClass.getMethod("setDescription", new Class[]{String.class});
        Method setUrlMethod = ThingBuilderClass.getMethod("setUrl", new Class[]{Uri.class});
        Method thingBuildMethod = ThingBuilderClass.getMethod("build", new Class[0]);
        setNameMethod.invoke(thingBuilder, new Object[]{branchUniversalObject.getTitle()});
        setDescMethod.invoke(thingBuilder, new Object[]{branchUniversalObject.getDescription()});
        setUrlMethod.invoke(thingBuilder, new Object[]{Uri.parse(shortLink)});
        Object thingObj = thingBuildMethod.invoke(thingBuilder, new Object[0]);
        Class<?> ThingActionClass = Class.forName("com.google.android.gms.appindexing.Action");
        Class<?> ThingActionBuilderClass = Class.forName("com.google.android.gms.appindexing.Action$Builder");
        Object actionBuilder = ThingActionBuilderClass.getConstructor(new Class[]{String.class}).newInstance(new Object[]{(String) ThingActionClass.getDeclaredField("TYPE_VIEW").get(null)});
        Method setObjectMethod = ThingActionBuilderClass.getMethod("setObject", new Class[]{ThingClass});
        Method setActionStatusMethod = ThingActionBuilderClass.getMethod("setActionStatus", new Class[]{String.class});
        Method actionBuildMethod = ThingActionBuilderClass.getMethod("build", new Class[0]);
        setObjectMethod.invoke(actionBuilder, new Object[]{thingObj});
        setActionStatusMethod.invoke(actionBuilder, new Object[]{(String) ThingActionClass.getDeclaredField("STATUS_TYPE_COMPLETED").get(null)});
        Object actionObj = actionBuildMethod.invoke(actionBuilder, new Object[0]);
        Class<?> AppIndexClass = Class.forName("com.google.android.gms.appindexing.AppIndex");
        Class<?> ApiClass = Class.forName("com.google.android.gms.common.api.Api");
        Class<?> GoogleApiClientClass = Class.forName("com.google.android.gms.common.api.GoogleApiClient");
        Class<?> GoogleApiClientBuilderClass = Class.forName("com.google.android.gms.common.api.GoogleApiClient$Builder");
        Object apiClientBuilder = GoogleApiClientBuilderClass.getConstructor(new Class[]{Context.class}).newInstance(new Object[]{context});
        Method addApiMethod = GoogleApiClientBuilderClass.getMethod("addApi", new Class[]{ApiClass});
        Method apiClientBuildMethod = GoogleApiClientBuilderClass.getMethod("build", new Class[0]);
        Method apiClientConnectMethod = GoogleApiClientClass.getMethod("connect", new Class[0]);
        Method apiClientDisConnectMethod = GoogleApiClientClass.getMethod("disconnect", new Class[0]);
        addApiMethod.invoke(apiClientBuilder, new Object[]{ApiClass.cast(AppIndexClass.getDeclaredField("API").get(null))});
        Object googleApiClientApiClientObj = apiClientBuildMethod.invoke(apiClientBuilder, new Object[0]);
        apiClientConnectMethod.invoke(googleApiClientApiClientObj, new Object[0]);
        Class<?> AppIndexApiClass = Class.forName("com.google.android.gms.appindexing.AppIndexApi");
        Object appIndexApiObj = AppIndexClass.getDeclaredField("AppIndexApi").get(null);
        AppIndexApiClass.getMethod("start", new Class[]{GoogleApiClientClass, ThingActionClass}).invoke(appIndexApiObj, new Object[]{googleApiClientApiClientObj, actionObj});
        apiClientDisConnectMethod.invoke(googleApiClientApiClientObj, new Object[0]);
    }
}
