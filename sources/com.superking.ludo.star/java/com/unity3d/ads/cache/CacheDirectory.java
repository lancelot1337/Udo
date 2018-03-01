package com.unity3d.ads.cache;

import android.content.Context;
import android.os.Build.VERSION;
import android.os.Environment;
import com.unity3d.ads.log.DeviceLog;
import cz.msebera.android.httpclient.protocol.HTTP;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class CacheDirectory {
    private static final String TEST_FILE_NAME = "UnityAdsTest.txt";
    private String _cacheDirName;
    private File _cacheDirectory = null;
    private boolean _initialized = false;

    public CacheDirectory(String cacheDirName) {
        this._cacheDirName = cacheDirName;
    }

    public File getCacheDirectory(Context context) {
        if (this._initialized) {
            return this._cacheDirectory;
        }
        this._initialized = true;
        if (VERSION.SDK_INT > 18) {
            if ("mounted".equals(Environment.getExternalStorageState())) {
                File externalCache = createCacheDirectory(context.getExternalCacheDir(), this._cacheDirName);
                if (testCacheDirectory(externalCache)) {
                    this._cacheDirectory = externalCache;
                    DeviceLog.debug("Unity Ads is using external cache directory: " + externalCache.getAbsolutePath());
                    return this._cacheDirectory;
                }
            }
            DeviceLog.debug("External media not mounted");
        }
        File internalCache = context.getFilesDir();
        if (testCacheDirectory(internalCache)) {
            this._cacheDirectory = internalCache;
            DeviceLog.debug("Unity Ads is using internal cache directory: " + internalCache.getAbsolutePath());
            return this._cacheDirectory;
        }
        DeviceLog.error("Unity Ads failed to initialize cache directory");
        return null;
    }

    public File createCacheDirectory(File baseDir, String newDir) {
        if (baseDir == null) {
            return null;
        }
        File directory = new File(baseDir, newDir);
        directory.mkdirs();
        if (directory.isDirectory()) {
            return directory;
        }
        return null;
    }

    public boolean testCacheDirectory(File directory) {
        if (directory == null || !directory.isDirectory()) {
            return false;
        }
        try {
            byte[] inData = "test".getBytes(HTTP.UTF_8);
            byte[] outData = new byte[inData.length];
            File testFile = new File(directory, TEST_FILE_NAME);
            FileOutputStream fos = new FileOutputStream(testFile);
            fos.write(inData);
            fos.flush();
            fos.close();
            if (directory.listFiles() == null) {
                DeviceLog.debug("Failed to list files in directory " + directory.getAbsolutePath());
                return false;
            }
            FileInputStream fis = new FileInputStream(testFile);
            int readCount = fis.read(outData, 0, outData.length);
            fis.close();
            if (!testFile.delete()) {
                DeviceLog.debug("Failed to delete testfile " + testFile.getAbsoluteFile());
                return false;
            } else if (readCount != outData.length) {
                DeviceLog.debug("Read buffer size mismatch");
                return false;
            } else if (new String(outData, HTTP.UTF_8).equals("test")) {
                return true;
            } else {
                DeviceLog.debug("Read buffer content mismatch");
                return false;
            }
        } catch (Exception e) {
            DeviceLog.debug("Unity Ads exception while testing cache directory " + directory.getAbsolutePath() + ": " + e.getMessage());
            return false;
        }
    }
}
