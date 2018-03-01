package io.branch.referral;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import io.branch.referral.Branch.BranchReferralInitListener;
import io.branch.referral.Defines.RequestPath;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class ServerRequestQueue {
    private static final int MAX_ITEMS = 25;
    private static final String PREF_KEY = "BNCServerRequestQueue";
    private static ServerRequestQueue SharedInstance;
    private Editor editor = this.sharedPref.edit();
    private final List<ServerRequest> queue;
    private SharedPreferences sharedPref;

    public static ServerRequestQueue getInstance(Context c) {
        if (SharedInstance == null) {
            synchronized (ServerRequestQueue.class) {
                if (SharedInstance == null) {
                    SharedInstance = new ServerRequestQueue(c);
                }
            }
        }
        return SharedInstance;
    }

    @SuppressLint({"CommitPrefEdits"})
    private ServerRequestQueue(Context c) {
        this.sharedPref = c.getSharedPreferences("BNC_Server_Request_Queue", 0);
        this.queue = retrieve(c);
    }

    private void persist() {
        new Thread(new Runnable() {
            public void run() {
                synchronized (ServerRequestQueue.this.queue) {
                    JSONArray jsonArr = new JSONArray();
                    for (ServerRequest aQueue : ServerRequestQueue.this.queue) {
                        JSONObject json = aQueue.toJSON();
                        if (json != null) {
                            jsonArr.put(json);
                        }
                    }
                    try {
                        ServerRequestQueue.this.editor.putString(ServerRequestQueue.PREF_KEY, jsonArr.toString()).commit();
                        if (!true) {
                            try {
                                ServerRequestQueue.this.editor.putString(ServerRequestQueue.PREF_KEY, jsonArr.toString()).commit();
                            } catch (ConcurrentModificationException e) {
                            }
                        }
                    } catch (ConcurrentModificationException ex) {
                        PrefHelper.Debug("Persisting Queue: ", "Failed to persit queue " + ex.getMessage());
                        if (null == null) {
                            try {
                                ServerRequestQueue.this.editor.putString(ServerRequestQueue.PREF_KEY, jsonArr.toString()).commit();
                            } catch (ConcurrentModificationException e2) {
                            }
                        }
                    } catch (Throwable th) {
                        if (null == null) {
                            try {
                                ServerRequestQueue.this.editor.putString(ServerRequestQueue.PREF_KEY, jsonArr.toString()).commit();
                            } catch (ConcurrentModificationException e3) {
                            }
                        }
                    }
                }
                return;
            }
        }).start();
    }

    private List<ServerRequest> retrieve(Context context) {
        List<ServerRequest> result = Collections.synchronizedList(new LinkedList());
        String jsonStr = this.sharedPref.getString(PREF_KEY, null);
        if (jsonStr != null) {
            try {
                JSONArray jsonArr = new JSONArray(jsonStr);
                for (int i = 0; i < Math.min(jsonArr.length(), MAX_ITEMS); i++) {
                    ServerRequest req = ServerRequest.fromJSON(jsonArr.getJSONObject(i), context);
                    if (!(req == null || (req instanceof ServerRequestRegisterClose) || (req instanceof ServerRequestLogout))) {
                        result.add(req);
                    }
                }
            } catch (JSONException e) {
            }
        }
        return result;
    }

    public int getSize() {
        return this.queue.size();
    }

    public void enqueue(ServerRequest request) {
        if (request != null) {
            this.queue.add(request);
            if (getSize() >= MAX_ITEMS) {
                this.queue.remove(1);
            }
            persist();
        }
    }

    public ServerRequest dequeue() {
        ServerRequest req = null;
        try {
            req = (ServerRequest) this.queue.remove(0);
            persist();
            return req;
        } catch (IndexOutOfBoundsException e) {
            return req;
        } catch (NoSuchElementException e2) {
            return req;
        }
    }

    public ServerRequest peek() {
        ServerRequest req = null;
        try {
            return (ServerRequest) this.queue.get(0);
        } catch (IndexOutOfBoundsException e) {
            return req;
        } catch (NoSuchElementException e2) {
            return req;
        }
    }

    public ServerRequest peekAt(int index) {
        ServerRequest req = null;
        try {
            return (ServerRequest) this.queue.get(index);
        } catch (IndexOutOfBoundsException e) {
            return req;
        } catch (NoSuchElementException e2) {
            return req;
        }
    }

    public void insert(ServerRequest request, int index) {
        try {
            if (this.queue.size() < index) {
                index = this.queue.size();
            }
            this.queue.add(index, request);
            persist();
        } catch (IndexOutOfBoundsException e) {
        }
    }

    public ServerRequest removeAt(int index) {
        ServerRequest req = null;
        try {
            req = (ServerRequest) this.queue.remove(index);
            persist();
            return req;
        } catch (IndexOutOfBoundsException e) {
            return req;
        }
    }

    public boolean remove(ServerRequest request) {
        boolean isRemoved = false;
        try {
            isRemoved = this.queue.remove(request);
            persist();
            return isRemoved;
        } catch (UnsupportedOperationException e) {
            return isRemoved;
        }
    }

    public void clear() {
        try {
            this.queue.clear();
            persist();
        } catch (UnsupportedOperationException e) {
        }
    }

    public boolean containsClose() {
        synchronized (this.queue) {
            for (ServerRequest req : this.queue) {
                if (req != null && req.getRequestPath().equals(RequestPath.RegisterClose.getPath())) {
                    return true;
                }
            }
            return false;
        }
    }

    public boolean containsInstallOrOpen() {
        synchronized (this.queue) {
            for (ServerRequest req : this.queue) {
                if (req != null && ((req instanceof ServerRequestRegisterInstall) || (req instanceof ServerRequestRegisterOpen))) {
                    return true;
                }
            }
            return false;
        }
    }

    public void moveInstallOrOpenToFront(ServerRequest request, int networkCount, BranchReferralInitListener callback) {
        synchronized (this.queue) {
            Iterator<ServerRequest> iter = this.queue.iterator();
            while (iter.hasNext()) {
                ServerRequest req = (ServerRequest) iter.next();
                if (req != null && ((req instanceof ServerRequestRegisterInstall) || (req instanceof ServerRequestRegisterOpen))) {
                    iter.remove();
                    break;
                }
            }
        }
        if (networkCount == 0) {
            insert(request, 0);
        } else {
            insert(request, 1);
        }
    }

    public void setInstallOrOpenCallback(BranchReferralInitListener callback) {
        synchronized (this.queue) {
            for (ServerRequest req : this.queue) {
                if (req != null) {
                    if (req instanceof ServerRequestRegisterInstall) {
                        ((ServerRequestRegisterInstall) req).setInitFinishedCallback(callback);
                    } else if (req instanceof ServerRequestRegisterOpen) {
                        ((ServerRequestRegisterOpen) req).setInitFinishedCallback(callback);
                    }
                }
            }
        }
    }

    public void unlockProcessWait(PROCESS_WAIT_LOCK lock) {
        synchronized (this.queue) {
            for (ServerRequest req : this.queue) {
                if (req != null) {
                    req.removeProcessWaitLock(lock);
                }
            }
        }
    }

    public void setStrongMatchWaitLock() {
        synchronized (this.queue) {
            for (ServerRequest req : this.queue) {
                if (req != null && (req instanceof ServerRequestInitSession)) {
                    req.addProcessWaitLock(PROCESS_WAIT_LOCK.STRONG_MATCH_PENDING_WAIT_LOCK);
                }
            }
        }
    }
}
