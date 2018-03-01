package io.branch.referral;

import android.app.Application;

public class BranchApp extends Application {
    public void onCreate() {
        super.onCreate();
        if (BranchUtil.isTestModeEnabled(this)) {
            Branch.getTestInstance(this);
        } else {
            Branch.getInstance(this);
        }
    }
}
