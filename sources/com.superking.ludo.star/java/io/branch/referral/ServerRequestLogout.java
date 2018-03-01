package io.branch.referral;

import android.content.Context;
import android.util.Log;
import io.branch.referral.Branch.LogoutStatusListener;
import io.branch.referral.Defines.Jsonkey;
import io.branch.referral.Defines.RequestPath;
import org.json.JSONException;
import org.json.JSONObject;

class ServerRequestLogout extends ServerRequest {
    private LogoutStatusListener callback_;

    public void onRequestSucceeded(io.branch.referral.ServerResponse r7, io.branch.referral.Branch r8) {
        /* JADX: method processing error */
/*
Error: java.util.NoSuchElementException
	at java.util.HashMap$HashIterator.nextEntry(HashMap.java:789)
	at java.util.HashMap$KeyIterator.next(HashMap.java:814)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.applyRemove(BlockFinallyExtract.java:535)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.extractFinally(BlockFinallyExtract.java:175)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.processExceptionHandler(BlockFinallyExtract.java:79)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:51)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:286)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:173)
*/
        /*
        r6 = this;
        r5 = 0;
        r4 = 1;
        r1 = r6.prefHelper_;	 Catch:{ JSONException -> 0x005f, all -> 0x006d }
        r2 = r7.getObject();	 Catch:{ JSONException -> 0x005f, all -> 0x006d }
        r3 = io.branch.referral.Defines.Jsonkey.SessionID;	 Catch:{ JSONException -> 0x005f, all -> 0x006d }
        r3 = r3.getKey();	 Catch:{ JSONException -> 0x005f, all -> 0x006d }
        r2 = r2.getString(r3);	 Catch:{ JSONException -> 0x005f, all -> 0x006d }
        r1.setSessionID(r2);	 Catch:{ JSONException -> 0x005f, all -> 0x006d }
        r1 = r6.prefHelper_;	 Catch:{ JSONException -> 0x005f, all -> 0x006d }
        r2 = r7.getObject();	 Catch:{ JSONException -> 0x005f, all -> 0x006d }
        r3 = io.branch.referral.Defines.Jsonkey.IdentityID;	 Catch:{ JSONException -> 0x005f, all -> 0x006d }
        r3 = r3.getKey();	 Catch:{ JSONException -> 0x005f, all -> 0x006d }
        r2 = r2.getString(r3);	 Catch:{ JSONException -> 0x005f, all -> 0x006d }
        r1.setIdentityID(r2);	 Catch:{ JSONException -> 0x005f, all -> 0x006d }
        r1 = r6.prefHelper_;	 Catch:{ JSONException -> 0x005f, all -> 0x006d }
        r2 = r7.getObject();	 Catch:{ JSONException -> 0x005f, all -> 0x006d }
        r3 = io.branch.referral.Defines.Jsonkey.Link;	 Catch:{ JSONException -> 0x005f, all -> 0x006d }
        r3 = r3.getKey();	 Catch:{ JSONException -> 0x005f, all -> 0x006d }
        r2 = r2.getString(r3);	 Catch:{ JSONException -> 0x005f, all -> 0x006d }
        r1.setUserURL(r2);	 Catch:{ JSONException -> 0x005f, all -> 0x006d }
        r1 = r6.prefHelper_;	 Catch:{ JSONException -> 0x005f, all -> 0x006d }
        r2 = "bnc_no_value";	 Catch:{ JSONException -> 0x005f, all -> 0x006d }
        r1.setInstallParams(r2);	 Catch:{ JSONException -> 0x005f, all -> 0x006d }
        r1 = r6.prefHelper_;	 Catch:{ JSONException -> 0x005f, all -> 0x006d }
        r2 = "bnc_no_value";	 Catch:{ JSONException -> 0x005f, all -> 0x006d }
        r1.setSessionParams(r2);	 Catch:{ JSONException -> 0x005f, all -> 0x006d }
        r1 = r6.prefHelper_;	 Catch:{ JSONException -> 0x005f, all -> 0x006d }
        r2 = "bnc_no_value";	 Catch:{ JSONException -> 0x005f, all -> 0x006d }
        r1.setIdentity(r2);	 Catch:{ JSONException -> 0x005f, all -> 0x006d }
        r1 = r6.prefHelper_;	 Catch:{ JSONException -> 0x005f, all -> 0x006d }
        r1.clearUserValues();	 Catch:{ JSONException -> 0x005f, all -> 0x006d }
        r1 = r6.callback_;
        if (r1 == 0) goto L_0x005e;
    L_0x0059:
        r1 = r6.callback_;
        r1.onLogoutFinished(r4, r5);
    L_0x005e:
        return;
    L_0x005f:
        r0 = move-exception;
        r0.printStackTrace();	 Catch:{ JSONException -> 0x005f, all -> 0x006d }
        r1 = r6.callback_;
        if (r1 == 0) goto L_0x005e;
    L_0x0067:
        r1 = r6.callback_;
        r1.onLogoutFinished(r4, r5);
        goto L_0x005e;
    L_0x006d:
        r1 = move-exception;
        r2 = r6.callback_;
        if (r2 == 0) goto L_0x0077;
    L_0x0072:
        r2 = r6.callback_;
        r2.onLogoutFinished(r4, r5);
    L_0x0077:
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: io.branch.referral.ServerRequestLogout.onRequestSucceeded(io.branch.referral.ServerResponse, io.branch.referral.Branch):void");
    }

    public ServerRequestLogout(Context context, LogoutStatusListener callback) {
        super(context, RequestPath.Logout.getPath());
        this.callback_ = callback;
        JSONObject post = new JSONObject();
        try {
            post.put(Jsonkey.IdentityID.getKey(), this.prefHelper_.getIdentityID());
            post.put(Jsonkey.DeviceFingerprintID.getKey(), this.prefHelper_.getDeviceFingerPrintID());
            post.put(Jsonkey.SessionID.getKey(), this.prefHelper_.getSessionID());
            if (!this.prefHelper_.getLinkClickID().equals(SystemObserver.BLANK)) {
                post.put(Jsonkey.LinkClickID.getKey(), this.prefHelper_.getLinkClickID());
            }
            setPost(post);
        } catch (JSONException ex) {
            ex.printStackTrace();
            this.constructError_ = true;
        }
    }

    public ServerRequestLogout(String requestPath, JSONObject post, Context context) {
        super(requestPath, post, context);
    }

    public void handleFailure(int statusCode, String causeMsg) {
        if (this.callback_ != null) {
            this.callback_.onLogoutFinished(false, new BranchError("Logout error. " + causeMsg, statusCode));
        }
    }

    public boolean handleErrors(Context context) {
        if (super.doesAppHasInternetPermission(context)) {
            return false;
        }
        Log.i("BranchSDK", "Trouble executing your request. Please add 'android.permission.INTERNET' in your applications manifest file");
        if (this.callback_ != null) {
            this.callback_.onLogoutFinished(false, new BranchError("Logout failed", BranchError.ERR_NO_INTERNET_PERMISSION));
        }
        return true;
    }

    public boolean isGetRequest() {
        return false;
    }

    public void clearCallbacks() {
        this.callback_ = null;
    }
}
