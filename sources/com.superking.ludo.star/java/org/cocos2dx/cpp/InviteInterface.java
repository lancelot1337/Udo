package org.cocos2dx.cpp;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import cz.msebera.android.httpclient.protocol.HTTP;
import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.Branch.BranchLinkCreateListener;
import io.branch.referral.BranchError;
import io.branch.referral.util.LinkProperties;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;

public class InviteInterface {
    private static final String GAME_NAME = "Ludo Star";
    private static final String TAG = "InviteInterface";

    public static void shareWhatsApp(String message, String path) {
        Log.d(TAG, "super1: Share via Whatsapp" + message + " path:" + path);
        Intent sendIntent = new Intent("android.intent.action.SEND");
        sendIntent.putExtra("android.intent.extra.TEXT", message + path);
        sendIntent.setType(HTTP.PLAIN_TEXT_TYPE);
        AppActivity app = AppActivity.getInstance();
        try {
            app.getPackageManager().getPackageInfo("com.whatsapp", 1);
            sendIntent.setPackage("com.whatsapp");
        } catch (NameNotFoundException e) {
            sendIntent.putExtra("android.intent.extra.SUBJECT", GAME_NAME);
            sendIntent.putExtra("android.intent.extra.TEXT", message);
        }
        try {
            app.startActivity(Intent.createChooser(sendIntent, "Share via..."));
        } catch (ActivityNotFoundException e2) {
        }
    }

    public static void shareScreenshot(String path, String message) {
        Log.d(TAG, "super1: shareScreenshot" + path + " path:" + message);
        File file = new File(path);
        if (file.exists()) {
            Bitmap bitIMAGE = BitmapFactory.decodeFile(file.getAbsolutePath());
            Intent share = new Intent("android.intent.action.SEND");
            share.setType("image/jpeg");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitIMAGE.compress(CompressFormat.PNG, 100, bytes);
            String newPath = Environment.getExternalStorageDirectory() + File.separator + "temporary_file.png";
            Log.d(TAG, "super1: shareScreenshot >>>> " + newPath + " path:" + message);
            File f = new File(newPath);
            try {
                f.createNewFile();
                new FileOutputStream(f).write(bytes.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
            share.putExtra("android.intent.extra.STREAM", Uri.parse("file://" + newPath));
            if (AppActivity.getInstance() != null) {
                AppActivity.getInstance().startActivity(Intent.createChooser(share, "Share with friends"));
            }
        }
    }

    public static void shareViaOthers(String message, String subject) {
        Intent sendIntent = new Intent("android.intent.action.SEND");
        sendIntent.setType(HTTP.PLAIN_TEXT_TYPE);
        sendIntent.putExtra("android.intent.extra.TEXT", message);
        sendIntent.putExtra("android.intent.extra.SUBJECT", subject);
        AppActivity app = AppActivity.getInstance();
        if (app != null) {
            try {
                app.startActivity(Intent.createChooser(sendIntent, "Share via..."));
            } catch (ActivityNotFoundException ex) {
                Log.e(TAG, ex.getMessage());
            }
        }
    }

    public static void shareViaBranch(final String title, final String message, String userId, String dataJson) {
        AppActivity app = AppActivity.getInstance();
        if (app != null) {
            try {
                new BranchUniversalObject().setCanonicalIdentifier("user/" + userId).setTitle(title).setContentDescription(message).addContentMetadata("dataJson", new JSONObject(dataJson).toString()).setContentExpiration(new Date(System.currentTimeMillis() + 1800000)).generateShortUrl(app, new LinkProperties().setChannel("Share").setFeature("GameInvite"), new BranchLinkCreateListener() {
                    public void onLinkCreate(String url, BranchError error) {
                        if (error == null && url != null && !url.isEmpty()) {
                            InviteInterface.shareViaOthers(title + "\n" + message + "\n" + url, message);
                        }
                    }
                });
            } catch (JSONException e) {
                Log.d(TAG, e.getMessage());
            }
        }
    }

    public static void initiateEmail(String subject, String message, String emailTo) {
        Intent email = new Intent("android.intent.action.SEND");
        email.putExtra("android.intent.extra.EMAIL", new String[]{emailTo});
        email.putExtra("android.intent.extra.SUBJECT", subject);
        email.putExtra("android.intent.extra.TEXT", message);
        email.setType("message/rfc822");
        try {
            AppActivity app = AppActivity.getInstance();
            if (app != null) {
                app.startActivity(Intent.createChooser(email, "Choose an Email client :"));
            }
        } catch (ActivityNotFoundException e) {
        }
    }
}
