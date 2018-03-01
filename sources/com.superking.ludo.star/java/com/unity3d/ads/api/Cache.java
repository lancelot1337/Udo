package com.unity3d.ads.api;

import com.facebook.share.internal.ShareConstants;
import com.unity3d.ads.cache.CacheError;
import com.unity3d.ads.cache.CacheThread;
import com.unity3d.ads.device.Device;
import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.misc.Utilities;
import com.unity3d.ads.properties.SdkProperties;
import com.unity3d.ads.webview.bridge.WebViewCallback;
import com.unity3d.ads.webview.bridge.WebViewExposed;
import java.io.File;
import java.io.FilenameFilter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Cache {
    @WebViewExposed
    public static void download(String url, String fileId, WebViewCallback callback) {
        if (CacheThread.isActive()) {
            callback.error(CacheError.FILE_ALREADY_CACHING, new Object[0]);
        } else if (Device.isActiveNetworkConnected()) {
            CacheThread.download(url, fileIdToFilename(fileId));
            callback.invoke(new Object[0]);
        } else {
            callback.error(CacheError.NO_INTERNET, new Object[0]);
        }
    }

    @WebViewExposed
    public static void stop(WebViewCallback callback) {
        if (CacheThread.isActive()) {
            CacheThread.cancel();
            callback.invoke(new Object[0]);
            return;
        }
        callback.error(CacheError.NOT_CACHING, new Object[0]);
    }

    @WebViewExposed
    public static void isCaching(WebViewCallback callback) {
        callback.invoke(Boolean.valueOf(CacheThread.isActive()));
    }

    @WebViewExposed
    public static void getFiles(WebViewCallback callback) {
        File cacheDirectory = SdkProperties.getCacheDirectory();
        if (cacheDirectory != null) {
            DeviceLog.debug("Unity Ads cache: checking app directory for Unity Ads cached files");
            File[] fileList = cacheDirectory.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String filename) {
                    return filename.startsWith(SdkProperties.getCacheFilePrefix());
                }
            });
            if (fileList == null || fileList.length == 0) {
                callback.invoke(new JSONArray());
            }
            try {
                JSONArray files = new JSONArray();
                for (File f : fileList) {
                    String name = f.getName().substring(SdkProperties.getCacheFilePrefix().length());
                    DeviceLog.debug("Unity Ads cache: found " + name + ", " + f.length() + " bytes");
                    files.put(getFileJson(name));
                }
                callback.invoke(files);
            } catch (JSONException e) {
                DeviceLog.exception("Error creating JSON", e);
                callback.error(CacheError.JSON_ERROR, new Object[0]);
            }
        }
    }

    @WebViewExposed
    public static void getFileInfo(String fileId, WebViewCallback callback) {
        try {
            callback.invoke(getFileJson(fileId));
        } catch (JSONException e) {
            DeviceLog.exception("Error creating JSON", e);
            callback.error(CacheError.JSON_ERROR, new Object[0]);
        }
    }

    @WebViewExposed
    public static void getFilePath(String fileId, WebViewCallback callback) {
        if (new File(fileIdToFilename(fileId)).exists()) {
            callback.invoke(fileIdToFilename(fileId));
            return;
        }
        callback.error(CacheError.FILE_NOT_FOUND, new Object[0]);
    }

    @WebViewExposed
    public static void deleteFile(String fileId, WebViewCallback callback) {
        if (new File(fileIdToFilename(fileId)).delete()) {
            callback.invoke(new Object[0]);
        } else {
            callback.error(CacheError.FILE_IO_ERROR, new Object[0]);
        }
    }

    @WebViewExposed
    public static void getHash(String fileId, WebViewCallback callback) {
        callback.invoke(Utilities.Sha256(fileId));
    }

    @WebViewExposed
    public static void setTimeouts(Integer connectTimeout, Integer readTimeout, WebViewCallback callback) {
        CacheThread.setConnectTimeout(connectTimeout.intValue());
        CacheThread.setReadTimeout(readTimeout.intValue());
        callback.invoke(new Object[0]);
    }

    @WebViewExposed
    public static void getTimeouts(WebViewCallback callback) {
        callback.invoke(Integer.valueOf(CacheThread.getConnectTimeout()), Integer.valueOf(CacheThread.getReadTimeout()));
    }

    @WebViewExposed
    public static void setProgressInterval(Integer interval, WebViewCallback callback) {
        CacheThread.setProgressInterval(interval.intValue());
        callback.invoke(new Object[0]);
    }

    @WebViewExposed
    public static void getProgressInterval(WebViewCallback callback) {
        callback.invoke(Integer.valueOf(CacheThread.getProgressInterval()));
    }

    @WebViewExposed
    public static void getFreeSpace(WebViewCallback callback) {
        callback.invoke(Long.valueOf(Device.getFreeSpace(SdkProperties.getCacheDirectory())));
    }

    @WebViewExposed
    public static void getTotalSpace(WebViewCallback callback) {
        callback.invoke(Long.valueOf(Device.getTotalSpace(SdkProperties.getCacheDirectory())));
    }

    private static String fileIdToFilename(String fileId) {
        return SdkProperties.getCacheDirectory() + "/" + SdkProperties.getCacheFilePrefix() + fileId;
    }

    private static JSONObject getFileJson(String fileId) throws JSONException {
        JSONObject fileJson = new JSONObject();
        fileJson.put(ShareConstants.WEB_DIALOG_PARAM_ID, fileId);
        File f = new File(fileIdToFilename(fileId));
        if (f.exists()) {
            fileJson.put("found", true);
            fileJson.put("size", f.length());
            fileJson.put("mtime", f.lastModified());
        } else {
            fileJson.put("found", false);
        }
        return fileJson;
    }
}
