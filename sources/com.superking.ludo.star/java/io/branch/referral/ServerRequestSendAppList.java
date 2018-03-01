package io.branch.referral;

import android.content.Context;
import android.util.Log;
import io.branch.referral.Defines.Jsonkey;
import io.branch.referral.Defines.RequestPath;
import org.json.JSONException;
import org.json.JSONObject;

class ServerRequestSendAppList extends ServerRequest {
    public ServerRequestSendAppList(Context context) {
        super(context, RequestPath.SendAPPList.getPath());
        SystemObserver sysObserver = new SystemObserver(context);
        JSONObject post = new JSONObject();
        try {
            post.put(Jsonkey.DeviceFingerprintID.getKey(), this.prefHelper_.getDeviceFingerPrintID());
            post.put("apps_data", sysObserver.getListOfApps());
            setPost(post);
        } catch (JSONException ex) {
            ex.printStackTrace();
            this.constructError_ = true;
        }
    }

    public ServerRequestSendAppList(String requestPath, JSONObject post, Context context) {
        super(requestPath, post, context);
    }

    public void onRequestSucceeded(ServerResponse resp, Branch branch) {
        this.prefHelper_.clearSystemReadStatus();
    }

    public void handleFailure(int statusCode, String causeMsg) {
    }

    public boolean handleErrors(Context context) {
        if (super.doesAppHasInternetPermission(context)) {
            return false;
        }
        Log.i("BranchSDK", "Trouble executing your request. Please add 'android.permission.INTERNET' in your applications manifest file");
        return true;
    }

    public boolean isGetRequest() {
        return false;
    }

    public void clearCallbacks() {
    }
}
