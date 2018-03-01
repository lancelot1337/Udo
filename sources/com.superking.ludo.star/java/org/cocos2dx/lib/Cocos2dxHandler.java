package org.cocos2dx.lib;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;
import android.os.Message;
import java.lang.ref.WeakReference;

public class Cocos2dxHandler extends Handler {
    public static final int HANDLER_SHOW_DIALOG = 1;
    private WeakReference<Cocos2dxActivity> mActivity;

    public static class DialogMessage {
        public String message;
        public String title;

        public DialogMessage(String title, String message) {
            this.title = title;
            this.message = message;
        }
    }

    public Cocos2dxHandler(Cocos2dxActivity activity) {
        this.mActivity = new WeakReference(activity);
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case HANDLER_SHOW_DIALOG /*1*/:
                showDialog(msg);
                return;
            default:
                return;
        }
    }

    private void showDialog(Message msg) {
        DialogMessage dialogMessage = msg.obj;
        new Builder((Cocos2dxActivity) this.mActivity.get()).setTitle(dialogMessage.title).setMessage(dialogMessage.message).setPositiveButton("Ok", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        }).create().show();
    }
}
