package com.google.firebase.messaging;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.WorkerThread;
import android.util.Log;
import com.facebook.internal.NativeProtocol;
import com.google.firebase.iid.zzb;
import com.google.firebase.iid.zzg;
import com.ironsource.sdk.utils.Constants.ErrorCodes;
import io.branch.referral.R;
import java.util.Iterator;
import org.cocos2dx.lib.Cocos2dxEditBox;
import org.cocos2dx.lib.Cocos2dxHandler;

public class FirebaseMessagingService extends zzb {
    static void zzD(Bundle bundle) {
        Iterator it = bundle.keySet().iterator();
        while (it.hasNext()) {
            String str = (String) it.next();
            if (str != null && str.startsWith("google.c.")) {
                it.remove();
            }
        }
    }

    private void zzI(Intent intent) {
        PendingIntent pendingIntent = (PendingIntent) intent.getParcelableExtra("pending_intent");
        if (pendingIntent != null) {
            try {
                pendingIntent.send();
            } catch (CanceledException e) {
                Log.e("FirebaseMessaging", "Notification pending intent canceled");
            }
        }
        if (zzX(intent.getExtras())) {
            zzd.zzj(this, intent);
        }
    }

    private void zzJ(Intent intent) {
        String stringExtra = intent.getStringExtra("message_type");
        if (stringExtra == null) {
            stringExtra = "gcm";
        }
        Object obj = -1;
        switch (stringExtra.hashCode()) {
            case -2062414158:
                if (stringExtra.equals("deleted_messages")) {
                    obj = 1;
                    break;
                }
                break;
            case 102161:
                if (stringExtra.equals("gcm")) {
                    obj = null;
                    break;
                }
                break;
            case 814694033:
                if (stringExtra.equals("send_error")) {
                    obj = 3;
                    break;
                }
                break;
            case 814800675:
                if (stringExtra.equals("send_event")) {
                    obj = 2;
                    break;
                }
                break;
        }
        switch (obj) {
            case Cocos2dxEditBox.kEndActionUnknown /*0*/:
                if (zzX(intent.getExtras())) {
                    zzd.zzi(this, intent);
                }
                zzl(intent);
                return;
            case Cocos2dxHandler.HANDLER_SHOW_DIALOG /*1*/:
                onDeletedMessages();
                return;
            case R.styleable.View_paddingStart /*2*/:
                onMessageSent(intent.getStringExtra("google.message_id"));
                return;
            case Cocos2dxEditBox.kEndActionReturn /*3*/:
                onSendError(zzm(intent), new SendException(intent.getStringExtra(NativeProtocol.BRIDGE_ARG_ERROR_BUNDLE)));
                return;
            default:
                String str = "FirebaseMessaging";
                String str2 = "Received message with unknown type: ";
                stringExtra = String.valueOf(stringExtra);
                Log.w(str, stringExtra.length() != 0 ? str2.concat(stringExtra) : new String(str2));
                return;
        }
    }

    static boolean zzX(Bundle bundle) {
        return bundle == null ? false : ErrorCodes.FOLDER_NOT_EXIST_CODE.equals(bundle.getString("google.c.a.e"));
    }

    private void zzl(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras == null) {
            extras = new Bundle();
        }
        extras.remove("android.support.content.wakelockid");
        if (zza.zzE(extras)) {
            if (!zza.zzcx(this).zzG(extras)) {
                if (zzX(extras)) {
                    zzd.zzl(this, intent);
                }
            } else {
                return;
            }
        }
        onMessageReceived(new RemoteMessage(extras));
    }

    private String zzm(Intent intent) {
        String stringExtra = intent.getStringExtra("google.message_id");
        return stringExtra == null ? intent.getStringExtra("message_id") : stringExtra;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleIntent(android.content.Intent r5) {
        /*
        r4 = this;
        r0 = r5.getAction();
        if (r0 != 0) goto L_0x0008;
    L_0x0006:
        r0 = "";
    L_0x0008:
        r1 = -1;
        r2 = r0.hashCode();
        switch(r2) {
            case 75300319: goto L_0x0038;
            case 366519424: goto L_0x002e;
            default: goto L_0x0010;
        };
    L_0x0010:
        r0 = r1;
    L_0x0011:
        switch(r0) {
            case 0: goto L_0x0042;
            case 1: goto L_0x0046;
            default: goto L_0x0014;
        };
    L_0x0014:
        r1 = "FirebaseMessaging";
        r2 = "Unknown intent action: ";
        r0 = r5.getAction();
        r0 = java.lang.String.valueOf(r0);
        r3 = r0.length();
        if (r3 == 0) goto L_0x0054;
    L_0x0026:
        r0 = r2.concat(r0);
    L_0x002a:
        android.util.Log.d(r1, r0);
    L_0x002d:
        return;
    L_0x002e:
        r2 = "com.google.android.c2dm.intent.RECEIVE";
        r0 = r0.equals(r2);
        if (r0 == 0) goto L_0x0010;
    L_0x0036:
        r0 = 0;
        goto L_0x0011;
    L_0x0038:
        r2 = "com.google.firebase.messaging.NOTIFICATION_DISMISS";
        r0 = r0.equals(r2);
        if (r0 == 0) goto L_0x0010;
    L_0x0040:
        r0 = 1;
        goto L_0x0011;
    L_0x0042:
        r4.zzJ(r5);
        goto L_0x002d;
    L_0x0046:
        r0 = r5.getExtras();
        r0 = zzX(r0);
        if (r0 == 0) goto L_0x002d;
    L_0x0050:
        com.google.firebase.messaging.zzd.zzk(r4, r5);
        goto L_0x002d;
    L_0x0054:
        r0 = new java.lang.String;
        r0.<init>(r2);
        goto L_0x002a;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.firebase.messaging.FirebaseMessagingService.handleIntent(android.content.Intent):void");
    }

    @WorkerThread
    public void onDeletedMessages() {
    }

    @WorkerThread
    public void onMessageReceived(RemoteMessage remoteMessage) {
    }

    @WorkerThread
    public void onMessageSent(String str) {
    }

    @WorkerThread
    public void onSendError(String str, Exception exception) {
    }

    protected Intent zzD(Intent intent) {
        return zzg.zzabU().zzabW();
    }

    public boolean zzE(Intent intent) {
        if (!"com.google.firebase.messaging.NOTIFICATION_OPEN".equals(intent.getAction())) {
            return false;
        }
        zzI(intent);
        return true;
    }
}
