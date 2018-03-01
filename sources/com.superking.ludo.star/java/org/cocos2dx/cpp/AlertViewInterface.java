package org.cocos2dx.cpp;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnShowListener;
import android.graphics.Typeface;
import android.os.Vibrator;
import android.util.Log;
import com.superking.ludo.star.R;

public class AlertViewInterface {
    private static final String TAG = "AlertViewInterface";
    private static AlertDialog mDialog;

    private static native void nativeOnNegativeButtonPressed(String str);

    private static native void nativeOnNeutralButtonPressed(String str);

    private static native void nativeOnPositiveButtonPressed(String str);

    private static native void stateReload();

    public static void showDialog(String type, String title, String message, String positive, String negative) {
        final AppActivity _staticInstance = AppActivity.getInstance();
        Log.d("super1", "Reached this place. AVI");
        if (_staticInstance != null && _staticInstance.getApplicationContext() != null) {
            final String str = title;
            final String str2 = message;
            final String str3 = positive;
            final String str4 = type;
            final String str5 = negative;
            _staticInstance.runOnUiThread(new Runnable() {
                public void run() {
                    Builder builder;
                    Log.d(AlertViewInterface.TAG, "Inside run");
                    try {
                        builder = new Builder(_staticInstance, 5);
                    } catch (NoSuchMethodError e) {
                        builder = new Builder(_staticInstance);
                    }
                    builder.setTitle(str);
                    builder.setIcon(R.mipmap.ic_launcher);
                    builder.setMessage(str2);
                    builder.setPositiveButton(str3, new OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            _staticInstance.runOnGLThread(new Runnable() {
                                public void run() {
                                    Log.d("super1", "AlertViewInterface Positive Clicked");
                                    AlertViewInterface.nativeOnPositiveButtonPressed(str4);
                                }
                            });
                        }
                    });
                    builder.setNegativeButton(str5, new OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            _staticInstance.runOnGLThread(new Runnable() {
                                public void run() {
                                    AlertViewInterface.nativeOnNegativeButtonPressed(str4);
                                }
                            });
                        }
                    });
                    AlertDialog dd = builder.create();
                    if (dd != null) {
                        dd.setCanceledOnTouchOutside(false);
                        dd.setCancelable(false);
                        dd.show();
                        dd.getButton(-2).setTextColor(-65536);
                        dd.getButton(-1).setTypeface(Typeface.DEFAULT_BOLD);
                        dd.setOnShowListener(new OnShowListener() {
                            public void onShow(DialogInterface arg0) {
                            }
                        });
                        return;
                    }
                    _staticInstance.runOnGLThread(new Runnable() {
                        public void run() {
                            AlertViewInterface.nativeOnNegativeButtonPressed(str4);
                        }
                    });
                }
            });
        }
    }

    public static void showNetworkError() {
        final AppActivity _staticInstance = AppActivity.getInstance();
        if (_staticInstance != null && _staticInstance.getApplicationContext() != null) {
            _staticInstance.runOnUiThread(new Runnable() {
                public void run() {
                    if (AlertViewInterface.mDialog != null) {
                        AlertViewInterface.mDialog.dismiss();
                    }
                    Builder builder = new Builder(_staticInstance);
                    builder.setTitle("No Internet Connection");
                    builder.setIcon(R.mipmap.ic_launcher);
                    builder.setMessage("No Internet Connection. Make sure Wi-Fi or cellular data is turned on, then try again.");
                    builder.setCancelable(false);
                    builder.setPositiveButton("Retry", new OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            _staticInstance.runOnGLThread(new Runnable() {
                                public void run() {
                                    AlertViewInterface.stateReload();
                                }
                            });
                        }
                    });
                    AlertViewInterface.mDialog = builder.create();
                    AlertViewInterface.mDialog.show();
                }
            });
        }
    }

    public static void hideNetworkError() {
        AppActivity _staticInstance = AppActivity.getInstance();
        if (_staticInstance != null && _staticInstance.getApplicationContext() != null) {
            _staticInstance.runOnUiThread(new Runnable() {
                public void run() {
                    if (AlertViewInterface.mDialog != null) {
                        AlertViewInterface.mDialog.dismiss();
                    }
                }
            });
        }
    }

    public static void vibrate(int millisecs) {
        ((Vibrator) AppActivity.getInstance().getApplicationContext().getSystemService("vibrator")).vibrate(400);
    }
}
