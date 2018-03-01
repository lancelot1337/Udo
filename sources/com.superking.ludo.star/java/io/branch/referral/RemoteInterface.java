package io.branch.referral;

import android.content.Context;
import android.os.Build.VERSION;
import android.os.NetworkOnMainThreadException;
import android.util.Log;
import com.facebook.internal.ServerProtocol;
import com.ironsource.sdk.utils.Constants.RequestParameters;
import cz.msebera.android.httpclient.HttpHeaders;
import cz.msebera.android.httpclient.HttpStatus;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.protocol.HTTP;
import io.branch.referral.Defines.Jsonkey;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Iterator;
import javax.net.ssl.HttpsURLConnection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class RemoteInterface {
    public static final String BRANCH_KEY = "branch_key";
    private static final int DEFAULT_TIMEOUT = 3000;
    public static final int NO_BRANCH_KEY_STATUS = -1234;
    public static final int NO_CONNECTIVITY_STATUS = -1009;
    static final String SDK_VERSION = "2.5.9";
    protected PrefHelper prefHelper_;

    private io.branch.referral.ServerResponse make_restful_get(java.lang.String r22, org.json.JSONObject r23, java.lang.String r24, int r25, int r26, boolean r27) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0136 in list [B:51:0x01b9]
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:42)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:286)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:173)
*/
        /*
        r21 = this;
        r12 = r22;
        r8 = new org.json.JSONObject;
        r8.<init>();
        r5 = 0;
        if (r25 > 0) goto L_0x000c;
    L_0x000a:
        r25 = 3000; // 0xbb8 float:4.204E-42 double:1.482E-320;
    L_0x000c:
        r0 = r21;
        r1 = r26;
        r17 = r0.addCommonParams(r8, r1);
        if (r17 == 0) goto L_0x0137;
    L_0x0016:
        if (r23 == 0) goto L_0x0036;
    L_0x0018:
        r10 = r23.keys();
    L_0x001c:
        r17 = r10.hasNext();
        if (r17 == 0) goto L_0x0036;
    L_0x0022:
        r9 = r10.next();
        r9 = (java.lang.String) r9;
        r0 = r23;	 Catch:{ JSONException -> 0x0034 }
        r17 = r0.getString(r9);	 Catch:{ JSONException -> 0x0034 }
        r0 = r17;	 Catch:{ JSONException -> 0x0034 }
        r8.put(r9, r0);	 Catch:{ JSONException -> 0x0034 }
        goto L_0x001c;
    L_0x0034:
        r17 = move-exception;
        goto L_0x001c;
    L_0x0036:
        r17 = new java.lang.StringBuilder;
        r17.<init>();
        r0 = r17;
        r17 = r0.append(r12);
        r0 = r21;
        r18 = r0.convertJSONtoString(r8);
        r17 = r17.append(r18);
        r12 = r17.toString();
        r14 = java.lang.System.currentTimeMillis();
        if (r27 == 0) goto L_0x006f;
    L_0x0055:
        r17 = "BranchSDK";	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r18 = new java.lang.StringBuilder;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r18.<init>();	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r19 = "getting ";	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r18 = r18.append(r19);	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r0 = r18;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r18 = r0.append(r12);	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r18 = r18.toString();	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        io.branch.referral.PrefHelper.Debug(r17, r18);	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
    L_0x006f:
        r16 = new java.net.URL;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r0 = r16;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r0.<init>(r12);	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r17 = r16.openConnection();	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r0 = r17;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r0 = (javax.net.ssl.HttpsURLConnection) r0;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r5 = r0;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r0 = r25;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r5.setConnectTimeout(r0);	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r0 = r25;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r5.setReadTimeout(r0);	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r18 = java.lang.System.currentTimeMillis();	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r18 = r18 - r14;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r0 = r18;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r11 = (int) r0;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r17 = io.branch.referral.Branch.getInstance();	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        if (r17 == 0) goto L_0x00c4;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
    L_0x0098:
        r17 = io.branch.referral.Branch.getInstance();	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r18 = new java.lang.StringBuilder;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r18.<init>();	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r0 = r18;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r1 = r24;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r18 = r0.append(r1);	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r19 = "-";	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r18 = r18.append(r19);	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r19 = io.branch.referral.Defines.Jsonkey.Last_Round_Trip_Time;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r19 = r19.getKey();	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r18 = r18.append(r19);	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r18 = r18.toString();	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r19 = java.lang.String.valueOf(r11);	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r17.addExtraInstrumentationData(r18, r19);	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
    L_0x00c4:
        r13 = r5.getResponseCode();	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r17 = 500; // 0x1f4 float:7.0E-43 double:2.47E-321;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r0 = r17;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        if (r13 < r0) goto L_0x01be;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
    L_0x00ce:
        r0 = r21;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r0 = r0.prefHelper_;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r17 = r0;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r17 = r17.getRetryCount();	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r0 = r26;
        r1 = r17;
        if (r0 >= r1) goto L_0x01be;
    L_0x00de:
        r0 = r21;	 Catch:{ InterruptedException -> 0x0145 }
        r0 = r0.prefHelper_;	 Catch:{ InterruptedException -> 0x0145 }
        r17 = r0;	 Catch:{ InterruptedException -> 0x0145 }
        r17 = r17.getRetryInterval();	 Catch:{ InterruptedException -> 0x0145 }
        r0 = r17;	 Catch:{ InterruptedException -> 0x0145 }
        r0 = (long) r0;	 Catch:{ InterruptedException -> 0x0145 }
        r18 = r0;	 Catch:{ InterruptedException -> 0x0145 }
        java.lang.Thread.sleep(r18);	 Catch:{ InterruptedException -> 0x0145 }
    L_0x00f0:
        r26 = r26 + 1;
        r17 = r21.make_restful_get(r22, r23, r24, r25, r26, r27);	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r18 = io.branch.referral.Branch.getInstance();
        if (r18 == 0) goto L_0x0131;
    L_0x00fc:
        r18 = java.lang.System.currentTimeMillis();
        r18 = r18 - r14;
        r0 = r18;
        r4 = (int) r0;
        r18 = io.branch.referral.Branch.getInstance();
        r19 = new java.lang.StringBuilder;
        r19.<init>();
        r0 = r19;
        r1 = r24;
        r19 = r0.append(r1);
        r20 = "-";
        r19 = r19.append(r20);
        r20 = io.branch.referral.Defines.Jsonkey.Branch_Round_Trip_Time;
        r20 = r20.getKey();
        r19 = r19.append(r20);
        r19 = r19.toString();
        r20 = java.lang.String.valueOf(r4);
        r18.addExtraInstrumentationData(r19, r20);
    L_0x0131:
        if (r5 == 0) goto L_0x0136;
    L_0x0133:
        r5.disconnect();
    L_0x0136:
        return r17;
    L_0x0137:
        r17 = new io.branch.referral.ServerResponse;
        r18 = -1234; // 0xfffffffffffffb2e float:NaN double:NaN;
        r0 = r17;
        r1 = r24;
        r2 = r18;
        r0.<init>(r1, r2);
        goto L_0x0136;
    L_0x0145:
        r6 = move-exception;
        r6.printStackTrace();	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        goto L_0x00f0;
    L_0x014a:
        r7 = move-exception;
        if (r27 == 0) goto L_0x016f;
    L_0x014d:
        r17 = r21.getClass();	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r17 = r17.getSimpleName();	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r18 = new java.lang.StringBuilder;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r18.<init>();	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r19 = "Http connect exception: ";	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r18 = r18.append(r19);	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r19 = r7.getMessage();	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r18 = r18.append(r19);	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r18 = r18.toString();	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        io.branch.referral.PrefHelper.Debug(r17, r18);	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
    L_0x016f:
        r17 = new io.branch.referral.ServerResponse;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r18 = -1009; // 0xfffffffffffffc0f float:NaN double:NaN;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r0 = r17;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r1 = r24;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r2 = r18;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r0.<init>(r1, r2);	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r18 = io.branch.referral.Branch.getInstance();
        if (r18 == 0) goto L_0x01b7;
    L_0x0182:
        r18 = java.lang.System.currentTimeMillis();
        r18 = r18 - r14;
        r0 = r18;
        r4 = (int) r0;
        r18 = io.branch.referral.Branch.getInstance();
        r19 = new java.lang.StringBuilder;
        r19.<init>();
        r0 = r19;
        r1 = r24;
        r19 = r0.append(r1);
        r20 = "-";
        r19 = r19.append(r20);
        r20 = io.branch.referral.Defines.Jsonkey.Branch_Round_Trip_Time;
        r20 = r20.getKey();
        r19 = r19.append(r20);
        r19 = r19.toString();
        r20 = java.lang.String.valueOf(r4);
        r18.addExtraInstrumentationData(r19, r20);
    L_0x01b7:
        if (r5 == 0) goto L_0x0136;
    L_0x01b9:
        r5.disconnect();
        goto L_0x0136;
    L_0x01be:
        r17 = 200; // 0xc8 float:2.8E-43 double:9.9E-322;
        r0 = r17;
        if (r13 == r0) goto L_0x021c;
    L_0x01c4:
        r17 = r5.getErrorStream();	 Catch:{ FileNotFoundException -> 0x026e }
        if (r17 == 0) goto L_0x021c;	 Catch:{ FileNotFoundException -> 0x026e }
    L_0x01ca:
        r17 = r5.getErrorStream();	 Catch:{ FileNotFoundException -> 0x026e }
        r0 = r21;	 Catch:{ FileNotFoundException -> 0x026e }
        r1 = r17;	 Catch:{ FileNotFoundException -> 0x026e }
        r2 = r24;	 Catch:{ FileNotFoundException -> 0x026e }
        r3 = r27;	 Catch:{ FileNotFoundException -> 0x026e }
        r17 = r0.processEntityForJSON(r1, r13, r2, r3);	 Catch:{ FileNotFoundException -> 0x026e }
        r18 = io.branch.referral.Branch.getInstance();
        if (r18 == 0) goto L_0x0215;
    L_0x01e0:
        r18 = java.lang.System.currentTimeMillis();
        r18 = r18 - r14;
        r0 = r18;
        r4 = (int) r0;
        r18 = io.branch.referral.Branch.getInstance();
        r19 = new java.lang.StringBuilder;
        r19.<init>();
        r0 = r19;
        r1 = r24;
        r19 = r0.append(r1);
        r20 = "-";
        r19 = r19.append(r20);
        r20 = io.branch.referral.Defines.Jsonkey.Branch_Round_Trip_Time;
        r20 = r20.getKey();
        r19 = r19.append(r20);
        r19 = r19.toString();
        r20 = java.lang.String.valueOf(r4);
        r18.addExtraInstrumentationData(r19, r20);
    L_0x0215:
        if (r5 == 0) goto L_0x0136;
    L_0x0217:
        r5.disconnect();
        goto L_0x0136;
    L_0x021c:
        r17 = r5.getInputStream();	 Catch:{ FileNotFoundException -> 0x026e }
        r0 = r21;	 Catch:{ FileNotFoundException -> 0x026e }
        r1 = r17;	 Catch:{ FileNotFoundException -> 0x026e }
        r2 = r24;	 Catch:{ FileNotFoundException -> 0x026e }
        r3 = r27;	 Catch:{ FileNotFoundException -> 0x026e }
        r17 = r0.processEntityForJSON(r1, r13, r2, r3);	 Catch:{ FileNotFoundException -> 0x026e }
        r18 = io.branch.referral.Branch.getInstance();
        if (r18 == 0) goto L_0x0267;
    L_0x0232:
        r18 = java.lang.System.currentTimeMillis();
        r18 = r18 - r14;
        r0 = r18;
        r4 = (int) r0;
        r18 = io.branch.referral.Branch.getInstance();
        r19 = new java.lang.StringBuilder;
        r19.<init>();
        r0 = r19;
        r1 = r24;
        r19 = r0.append(r1);
        r20 = "-";
        r19 = r19.append(r20);
        r20 = io.branch.referral.Defines.Jsonkey.Branch_Round_Trip_Time;
        r20 = r20.getKey();
        r19 = r19.append(r20);
        r19 = r19.toString();
        r20 = java.lang.String.valueOf(r4);
        r18.addExtraInstrumentationData(r19, r20);
    L_0x0267:
        if (r5 == 0) goto L_0x0136;
    L_0x0269:
        r5.disconnect();
        goto L_0x0136;
    L_0x026e:
        r7 = move-exception;
        if (r27 == 0) goto L_0x028d;
    L_0x0271:
        r17 = "BranchSDK";	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r18 = new java.lang.StringBuilder;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r18.<init>();	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r19 = "A resource conflict occurred with this request ";	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r18 = r18.append(r19);	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r0 = r18;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r1 = r24;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r18 = r0.append(r1);	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r18 = r18.toString();	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        io.branch.referral.PrefHelper.Debug(r17, r18);	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
    L_0x028d:
        r17 = 0;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r0 = r21;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r1 = r17;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r2 = r24;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r3 = r27;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r17 = r0.processEntityForJSON(r1, r13, r2, r3);	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r18 = io.branch.referral.Branch.getInstance();
        if (r18 == 0) goto L_0x02d6;
    L_0x02a1:
        r18 = java.lang.System.currentTimeMillis();
        r18 = r18 - r14;
        r0 = r18;
        r4 = (int) r0;
        r18 = io.branch.referral.Branch.getInstance();
        r19 = new java.lang.StringBuilder;
        r19.<init>();
        r0 = r19;
        r1 = r24;
        r19 = r0.append(r1);
        r20 = "-";
        r19 = r19.append(r20);
        r20 = io.branch.referral.Defines.Jsonkey.Branch_Round_Trip_Time;
        r20 = r20.getKey();
        r19 = r19.append(r20);
        r19 = r19.toString();
        r20 = java.lang.String.valueOf(r4);
        r18.addExtraInstrumentationData(r19, r20);
    L_0x02d6:
        if (r5 == 0) goto L_0x0136;
    L_0x02d8:
        r5.disconnect();
        goto L_0x0136;
    L_0x02dd:
        r7 = move-exception;
        r0 = r21;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r0 = r0.prefHelper_;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r17 = r0;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r17 = r17.getRetryCount();	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r0 = r26;
        r1 = r17;
        if (r0 >= r1) goto L_0x038f;
    L_0x02ee:
        r0 = r21;	 Catch:{ InterruptedException -> 0x0348 }
        r0 = r0.prefHelper_;	 Catch:{ InterruptedException -> 0x0348 }
        r17 = r0;	 Catch:{ InterruptedException -> 0x0348 }
        r17 = r17.getRetryInterval();	 Catch:{ InterruptedException -> 0x0348 }
        r0 = r17;	 Catch:{ InterruptedException -> 0x0348 }
        r0 = (long) r0;	 Catch:{ InterruptedException -> 0x0348 }
        r18 = r0;	 Catch:{ InterruptedException -> 0x0348 }
        java.lang.Thread.sleep(r18);	 Catch:{ InterruptedException -> 0x0348 }
    L_0x0300:
        r26 = r26 + 1;
        r17 = r21.make_restful_get(r22, r23, r24, r25, r26, r27);	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r18 = io.branch.referral.Branch.getInstance();
        if (r18 == 0) goto L_0x0341;
    L_0x030c:
        r18 = java.lang.System.currentTimeMillis();
        r18 = r18 - r14;
        r0 = r18;
        r4 = (int) r0;
        r18 = io.branch.referral.Branch.getInstance();
        r19 = new java.lang.StringBuilder;
        r19.<init>();
        r0 = r19;
        r1 = r24;
        r19 = r0.append(r1);
        r20 = "-";
        r19 = r19.append(r20);
        r20 = io.branch.referral.Defines.Jsonkey.Branch_Round_Trip_Time;
        r20 = r20.getKey();
        r19 = r19.append(r20);
        r19 = r19.toString();
        r20 = java.lang.String.valueOf(r4);
        r18.addExtraInstrumentationData(r19, r20);
    L_0x0341:
        if (r5 == 0) goto L_0x0136;
    L_0x0343:
        r5.disconnect();
        goto L_0x0136;
    L_0x0348:
        r6 = move-exception;
        r6.printStackTrace();	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        goto L_0x0300;
    L_0x034d:
        r17 = move-exception;
        r18 = io.branch.referral.Branch.getInstance();
        if (r18 == 0) goto L_0x0389;
    L_0x0354:
        r18 = java.lang.System.currentTimeMillis();
        r18 = r18 - r14;
        r0 = r18;
        r4 = (int) r0;
        r18 = io.branch.referral.Branch.getInstance();
        r19 = new java.lang.StringBuilder;
        r19.<init>();
        r0 = r19;
        r1 = r24;
        r19 = r0.append(r1);
        r20 = "-";
        r19 = r19.append(r20);
        r20 = io.branch.referral.Defines.Jsonkey.Branch_Round_Trip_Time;
        r20 = r20.getKey();
        r19 = r19.append(r20);
        r19 = r19.toString();
        r20 = java.lang.String.valueOf(r4);
        r18.addExtraInstrumentationData(r19, r20);
    L_0x0389:
        if (r5 == 0) goto L_0x038e;
    L_0x038b:
        r5.disconnect();
    L_0x038e:
        throw r17;
    L_0x038f:
        r17 = new io.branch.referral.ServerResponse;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r18 = -111; // 0xffffffffffffff91 float:NaN double:NaN;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r0 = r17;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r1 = r24;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r2 = r18;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r0.<init>(r1, r2);	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r18 = io.branch.referral.Branch.getInstance();
        if (r18 == 0) goto L_0x03d7;
    L_0x03a2:
        r18 = java.lang.System.currentTimeMillis();
        r18 = r18 - r14;
        r0 = r18;
        r4 = (int) r0;
        r18 = io.branch.referral.Branch.getInstance();
        r19 = new java.lang.StringBuilder;
        r19.<init>();
        r0 = r19;
        r1 = r24;
        r19 = r0.append(r1);
        r20 = "-";
        r19 = r19.append(r20);
        r20 = io.branch.referral.Defines.Jsonkey.Branch_Round_Trip_Time;
        r20 = r20.getKey();
        r19 = r19.append(r20);
        r19 = r19.toString();
        r20 = java.lang.String.valueOf(r4);
        r18.addExtraInstrumentationData(r19, r20);
    L_0x03d7:
        if (r5 == 0) goto L_0x0136;
    L_0x03d9:
        r5.disconnect();
        goto L_0x0136;
    L_0x03de:
        r7 = move-exception;
        if (r27 == 0) goto L_0x0403;
    L_0x03e1:
        r17 = r21.getClass();	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r17 = r17.getSimpleName();	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r18 = new java.lang.StringBuilder;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r18.<init>();	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r19 = "Http connect exception: ";	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r18 = r18.append(r19);	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r19 = r7.getMessage();	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r18 = r18.append(r19);	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r18 = r18.toString();	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        io.branch.referral.PrefHelper.Debug(r17, r18);	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
    L_0x0403:
        r17 = new io.branch.referral.ServerResponse;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r18 = -1009; // 0xfffffffffffffc0f float:NaN double:NaN;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r0 = r17;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r1 = r24;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r2 = r18;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r0.<init>(r1, r2);	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r18 = io.branch.referral.Branch.getInstance();
        if (r18 == 0) goto L_0x044b;
    L_0x0416:
        r18 = java.lang.System.currentTimeMillis();
        r18 = r18 - r14;
        r0 = r18;
        r4 = (int) r0;
        r18 = io.branch.referral.Branch.getInstance();
        r19 = new java.lang.StringBuilder;
        r19.<init>();
        r0 = r19;
        r1 = r24;
        r19 = r0.append(r1);
        r20 = "-";
        r19 = r19.append(r20);
        r20 = io.branch.referral.Defines.Jsonkey.Branch_Round_Trip_Time;
        r20 = r20.getKey();
        r19 = r19.append(r20);
        r19 = r19.toString();
        r20 = java.lang.String.valueOf(r4);
        r18.addExtraInstrumentationData(r19, r20);
    L_0x044b:
        if (r5 == 0) goto L_0x0136;
    L_0x044d:
        r5.disconnect();
        goto L_0x0136;
    L_0x0452:
        r7 = move-exception;
        if (r27 == 0) goto L_0x0477;
    L_0x0455:
        r17 = r21.getClass();	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r17 = r17.getSimpleName();	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r18 = new java.lang.StringBuilder;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r18.<init>();	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r19 = "IO exception: ";	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r18 = r18.append(r19);	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r19 = r7.getMessage();	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r18 = r18.append(r19);	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r18 = r18.toString();	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        io.branch.referral.PrefHelper.Debug(r17, r18);	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
    L_0x0477:
        r17 = new io.branch.referral.ServerResponse;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r18 = 500; // 0x1f4 float:7.0E-43 double:2.47E-321;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r0 = r17;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r1 = r24;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r2 = r18;	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r0.<init>(r1, r2);	 Catch:{ SocketException -> 0x014a, SocketTimeoutException -> 0x02dd, UnknownHostException -> 0x03de, IOException -> 0x0452, all -> 0x034d }
        r18 = io.branch.referral.Branch.getInstance();
        if (r18 == 0) goto L_0x04bf;
    L_0x048a:
        r18 = java.lang.System.currentTimeMillis();
        r18 = r18 - r14;
        r0 = r18;
        r4 = (int) r0;
        r18 = io.branch.referral.Branch.getInstance();
        r19 = new java.lang.StringBuilder;
        r19.<init>();
        r0 = r19;
        r1 = r24;
        r19 = r0.append(r1);
        r20 = "-";
        r19 = r19.append(r20);
        r20 = io.branch.referral.Defines.Jsonkey.Branch_Round_Trip_Time;
        r20 = r20.getKey();
        r19 = r19.append(r20);
        r19 = r19.toString();
        r20 = java.lang.String.valueOf(r4);
        r18.addExtraInstrumentationData(r19, r20);
    L_0x04bf:
        if (r5 == 0) goto L_0x0136;
    L_0x04c1:
        r5.disconnect();
        goto L_0x0136;
        */
        throw new UnsupportedOperationException("Method not decompiled: io.branch.referral.RemoteInterface.make_restful_get(java.lang.String, org.json.JSONObject, java.lang.String, int, int, boolean):io.branch.referral.ServerResponse");
    }

    public RemoteInterface(Context context) {
        this.prefHelper_ = PrefHelper.getInstance(context);
    }

    private ServerResponse processEntityForJSON(InputStream inStream, int statusCode, String tag, boolean log) {
        ServerResponse result = new ServerResponse(tag, statusCode);
        if (inStream != null) {
            try {
                String line = new BufferedReader(new InputStreamReader(inStream)).readLine();
                if (log) {
                    PrefHelper.Debug("BranchSDK", "returned " + line);
                }
                if (line != null) {
                    try {
                        result.setPost(new JSONObject(line));
                    } catch (JSONException e) {
                        try {
                            result.setPost(new JSONArray(line));
                        } catch (JSONException ex2) {
                            if (log) {
                                PrefHelper.Debug(getClass().getSimpleName(), "JSON exception: " + ex2.getMessage());
                            }
                        }
                    }
                }
            } catch (IOException ex) {
                if (log) {
                    PrefHelper.Debug(getClass().getSimpleName(), "IO exception: " + ex.getMessage());
                }
            }
        }
        return result;
    }

    public ServerResponse make_restful_get(String url, JSONObject params, String tag, int timeout) {
        return make_restful_get(url, params, tag, timeout, 0, true);
    }

    private boolean addCommonParams(JSONObject post, int retryNumber) {
        try {
            String branch_key = this.prefHelper_.getBranchKey();
            post.put(ServerProtocol.DIALOG_PARAM_SDK_VERSION, "android2.5.9");
            post.put("retryNumber", retryNumber);
            if (!branch_key.equals(SystemObserver.BLANK)) {
                post.put(BRANCH_KEY, this.prefHelper_.getBranchKey());
                return true;
            }
        } catch (JSONException e) {
        }
        return false;
    }

    public ServerResponse make_restful_post(JSONObject body, String url, String tag, int timeout) {
        return make_restful_post(body, url, tag, timeout, 0, true);
    }

    public ServerResponse make_restful_post(JSONObject body, String url, String tag, int timeout, boolean log) {
        return make_restful_post(body, url, tag, timeout, 0, log);
    }

    private ServerResponse make_restful_post(JSONObject body, String url, String tag, int timeout, int retryNumber, boolean log) {
        ServerResponse processEntityForJSON;
        HttpsURLConnection connection = null;
        if (timeout <= 0) {
            timeout = DEFAULT_TIMEOUT;
        }
        JSONObject bodyCopy = new JSONObject();
        long reqStartTime = System.currentTimeMillis();
        try {
            Iterator<?> keys = body.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                try {
                    bodyCopy.put(key, body.get(key));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (addCommonParams(bodyCopy, retryNumber)) {
                if (log) {
                    PrefHelper.Debug("BranchSDK", "posting to " + url);
                    PrefHelper.Debug("BranchSDK", "Post value = " + bodyCopy.toString(4));
                }
                connection = (HttpsURLConnection) new URL(url).openConnection();
                connection.setConnectTimeout(timeout);
                connection.setReadTimeout(timeout);
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setRequestProperty(HTTP.CONTENT_TYPE, "application/json");
                connection.setRequestProperty(HttpHeaders.ACCEPT, "application/json");
                connection.setRequestMethod(HttpPost.METHOD_NAME);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
                int lrtt = (int) (System.currentTimeMillis() - reqStartTime);
                if (Branch.getInstance() != null) {
                    Branch.getInstance().addExtraInstrumentationData(tag + "-" + Jsonkey.Last_Round_Trip_Time.getKey(), String.valueOf(lrtt));
                }
                outputStreamWriter.write(bodyCopy.toString());
                outputStreamWriter.flush();
                int responseCode = connection.getResponseCode();
                if (responseCode < 500 || retryNumber >= this.prefHelper_.getRetryCount()) {
                    if (responseCode != 200) {
                        try {
                            if (connection.getErrorStream() != null) {
                                processEntityForJSON = processEntityForJSON(connection.getErrorStream(), responseCode, tag, log);
                                if (Branch.getInstance() != null) {
                                    Branch.getInstance().addExtraInstrumentationData(tag + "-" + Jsonkey.Branch_Round_Trip_Time.getKey(), String.valueOf((int) (System.currentTimeMillis() - reqStartTime)));
                                }
                                if (connection != null) {
                                    connection.disconnect();
                                }
                            }
                        } catch (FileNotFoundException e2) {
                            if (log) {
                                PrefHelper.Debug("BranchSDK", "A resource conflict occurred with this request " + tag);
                            }
                            processEntityForJSON = processEntityForJSON(null, responseCode, tag, log);
                            if (Branch.getInstance() != null) {
                                Branch.getInstance().addExtraInstrumentationData(tag + "-" + Jsonkey.Branch_Round_Trip_Time.getKey(), String.valueOf((int) (System.currentTimeMillis() - reqStartTime)));
                            }
                            if (connection != null) {
                                connection.disconnect();
                            }
                        }
                    }
                    processEntityForJSON = processEntityForJSON(connection.getInputStream(), responseCode, tag, log);
                    if (Branch.getInstance() != null) {
                        Branch.getInstance().addExtraInstrumentationData(tag + "-" + Jsonkey.Branch_Round_Trip_Time.getKey(), String.valueOf((int) (System.currentTimeMillis() - reqStartTime)));
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                } else {
                    try {
                        Thread.sleep((long) this.prefHelper_.getRetryInterval());
                    } catch (InterruptedException e3) {
                        e3.printStackTrace();
                    }
                    retryNumber++;
                    processEntityForJSON = make_restful_post(bodyCopy, url, tag, timeout, retryNumber, log);
                    if (Branch.getInstance() != null) {
                        Branch.getInstance().addExtraInstrumentationData(tag + "-" + Jsonkey.Branch_Round_Trip_Time.getKey(), String.valueOf((int) (System.currentTimeMillis() - reqStartTime)));
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            } else {
                processEntityForJSON = new ServerResponse(tag, NO_BRANCH_KEY_STATUS);
                if (Branch.getInstance() != null) {
                    Branch.getInstance().addExtraInstrumentationData(tag + "-" + Jsonkey.Branch_Round_Trip_Time.getKey(), String.valueOf((int) (System.currentTimeMillis() - reqStartTime)));
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }
        } catch (SocketException ex) {
            if (log) {
                PrefHelper.Debug(getClass().getSimpleName(), "Http connect exception: " + ex.getMessage());
            }
            processEntityForJSON = new ServerResponse(tag, NO_CONNECTIVITY_STATUS);
            if (Branch.getInstance() != null) {
                Branch.getInstance().addExtraInstrumentationData(tag + "-" + Jsonkey.Branch_Round_Trip_Time.getKey(), String.valueOf((int) (System.currentTimeMillis() - reqStartTime)));
            }
            if (connection != null) {
                connection.disconnect();
            }
        } catch (UnknownHostException ex2) {
            if (log) {
                PrefHelper.Debug(getClass().getSimpleName(), "Http connect exception: " + ex2.getMessage());
            }
            processEntityForJSON = new ServerResponse(tag, NO_CONNECTIVITY_STATUS);
            if (Branch.getInstance() != null) {
                Branch.getInstance().addExtraInstrumentationData(tag + "-" + Jsonkey.Branch_Round_Trip_Time.getKey(), String.valueOf((int) (System.currentTimeMillis() - reqStartTime)));
            }
            if (connection != null) {
                connection.disconnect();
            }
        } catch (SocketTimeoutException e4) {
            if (retryNumber < this.prefHelper_.getRetryCount()) {
                try {
                    Thread.sleep((long) this.prefHelper_.getRetryInterval());
                } catch (InterruptedException e32) {
                    e32.printStackTrace();
                }
                processEntityForJSON = make_restful_post(bodyCopy, url, tag, timeout, retryNumber + 1, log);
                if (Branch.getInstance() != null) {
                    Branch.getInstance().addExtraInstrumentationData(tag + "-" + Jsonkey.Branch_Round_Trip_Time.getKey(), String.valueOf((int) (System.currentTimeMillis() - reqStartTime)));
                }
                if (connection != null) {
                    connection.disconnect();
                }
            } else {
                processEntityForJSON = new ServerResponse(tag, BranchError.ERR_BRANCH_REQ_TIMED_OUT);
                if (Branch.getInstance() != null) {
                    Branch.getInstance().addExtraInstrumentationData(tag + "-" + Jsonkey.Branch_Round_Trip_Time.getKey(), String.valueOf((int) (System.currentTimeMillis() - reqStartTime)));
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }
        } catch (Exception ex3) {
            if (log) {
                PrefHelper.Debug(getClass().getSimpleName(), "Exception: " + ex3.getMessage());
            }
            if (VERSION.SDK_INT >= 11 && (ex3 instanceof NetworkOnMainThreadException)) {
                Log.i("BranchSDK", "Branch Error: Don't call our synchronous methods on the main thread!!!");
            }
            processEntityForJSON = new ServerResponse(tag, HttpStatus.SC_INTERNAL_SERVER_ERROR);
            if (Branch.getInstance() != null) {
                Branch.getInstance().addExtraInstrumentationData(tag + "-" + Jsonkey.Branch_Round_Trip_Time.getKey(), String.valueOf((int) (System.currentTimeMillis() - reqStartTime)));
            }
            if (connection != null) {
                connection.disconnect();
            }
        } catch (Throwable th) {
            if (Branch.getInstance() != null) {
                Branch.getInstance().addExtraInstrumentationData(tag + "-" + Jsonkey.Branch_Round_Trip_Time.getKey(), String.valueOf((int) (System.currentTimeMillis() - reqStartTime)));
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return processEntityForJSON;
    }

    private String convertJSONtoString(JSONObject json) {
        StringBuilder result = new StringBuilder();
        if (json != null) {
            JSONArray names = json.names();
            if (names != null) {
                boolean first = true;
                int size = names.length();
                int i = 0;
                while (i < size) {
                    try {
                        String key = names.getString(i);
                        if (first) {
                            result.append("?");
                            first = false;
                        } else {
                            result.append(RequestParameters.AMPERSAND);
                        }
                        result.append(key).append(RequestParameters.EQUAL).append(json.getString(key));
                        i++;
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }
        }
        return result.toString();
    }
}
