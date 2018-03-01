package io.branch.referral;

import android.content.Context;
import io.branch.referral.Defines.RequestPath;
import org.json.JSONObject;

public class BranchRemoteInterface extends RemoteInterface {
    private SystemObserver sysObserver_;

    public /* bridge */ /* synthetic */ ServerResponse make_restful_get(String x0, JSONObject x1, String x2, int x3) {
        return super.make_restful_get(x0, x1, x2, x3);
    }

    public /* bridge */ /* synthetic */ ServerResponse make_restful_post(JSONObject x0, String x1, String x2, int x3) {
        return super.make_restful_post(x0, x1, x2, x3);
    }

    public /* bridge */ /* synthetic */ ServerResponse make_restful_post(JSONObject x0, String x1, String x2, int x3, boolean x4) {
        return super.make_restful_post(x0, x1, x2, x3, x4);
    }

    public BranchRemoteInterface(Context context) {
        super(context);
        this.sysObserver_ = new SystemObserver(context);
    }

    public ServerResponse createCustomUrlSync(JSONObject post) {
        return make_restful_post(post, this.prefHelper_.getAPIBaseUrl() + "v1/url", RequestPath.GetURL.getPath(), this.prefHelper_.getTimeout());
    }

    public SystemObserver getSystemObserver() {
        return this.sysObserver_;
    }
}
