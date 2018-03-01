package io.branch.referral;

import android.os.AsyncTask;
import android.os.Build.VERSION;

public abstract class BranchAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
    public AsyncTask<Params, Progress, Result> executeTask(Params... params) {
        if (VERSION.SDK_INT < 11) {
            return execute(params);
        }
        try {
            return executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
        } catch (Throwable th) {
            return execute(params);
        }
    }
}
