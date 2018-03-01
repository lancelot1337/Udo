package com.ironsource.sdk.precache;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import com.ironsource.sdk.data.SSAFile;
import com.ironsource.sdk.utils.IronSourceSharedPrefHelper;
import com.ironsource.sdk.utils.IronSourceStorageUtils;
import com.ironsource.sdk.utils.Logger;
import com.ironsource.sdk.utils.SDKUtils;
import cz.msebera.android.httpclient.HttpStatus;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.cache.CacheConfig;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.Callable;

public class DownloadManager {
    public static final String CAMPAIGNS = "campaigns";
    public static final String FILE_ALREADY_EXIST = "file_already_exist";
    protected static final String FILE_NOT_FOUND_EXCEPTION = "file not found exception";
    public static final String GLOBAL_ASSETS = "globalAssets";
    protected static final String HTTP_EMPTY_RESPONSE = "http empty response";
    protected static final String HTTP_ERROR_CODE = "http error code";
    protected static final String HTTP_NOT_FOUND = "http not found";
    protected static final String HTTP_OK = "http ok";
    protected static final String IO_EXCEPTION = "io exception";
    protected static final String MALFORMED_URL_EXCEPTION = "malformed url exception";
    static final int MESSAGE_EMPTY_URL = 1007;
    static final int MESSAGE_FILE_DOWNLOAD_FAIL = 1017;
    static final int MESSAGE_FILE_DOWNLOAD_SUCCESS = 1016;
    static final int MESSAGE_FILE_NOT_FOUND_EXCEPTION = 1018;
    static final int MESSAGE_GENERAL_HTTP_ERROR_CODE = 1011;
    static final int MESSAGE_HTTP_EMPTY_RESPONSE = 1006;
    static final int MESSAGE_HTTP_NOT_FOUND = 1005;
    static final int MESSAGE_INIT_BC_FAIL = 1014;
    static final int MESSAGE_IO_EXCEPTION = 1009;
    static final int MESSAGE_MALFORMED_URL_EXCEPTION = 1004;
    static final int MESSAGE_NUM_OF_BANNERS_TO_CACHE = 1013;
    static final int MESSAGE_NUM_OF_BANNERS_TO_INIT_SUCCESS = 1012;
    static final int MESSAGE_OUT_OF_MEMORY_EXCEPTION = 1019;
    static final int MESSAGE_SOCKET_TIMEOUT_EXCEPTION = 1008;
    static final int MESSAGE_TMP_FILE_RENAME_FAILED = 1020;
    static final int MESSAGE_URI_SYNTAX_EXCEPTION = 1010;
    static final int MESSAGE_ZERO_CAMPAIGNS_TO_INIT_SUCCESS = 1015;
    public static final String NO_DISK_SPACE = "no_disk_space";
    public static final String NO_NETWORK_CONNECTION = "no_network_connection";
    public static final int OPERATION_TIMEOUT = 5000;
    protected static final String OUT_OF_MEMORY_EXCEPTION = "out of memory exception";
    public static final String SETTINGS = "settings";
    protected static final String SOCKET_TIMEOUT_EXCEPTION = "socket timeout exception";
    public static final String STORAGE_UNAVAILABLE = "sotrage_unavailable";
    private static final String TAG = "DownloadManager";
    private static final String TEMP_DIR_FOR_FILES = "temp";
    private static final String TEMP_PREFIX_FOR_FILES = "tmp_";
    private static final String UNABLE_TO_CREATE_FOLDER = "unable_to_create_folder";
    protected static final String URI_SYNTAX_EXCEPTION = "uri syntax exception";
    public static final String UTF8_CHARSET = "UTF-8";
    private static DownloadManager mDownloadManager;
    private String mCacheRootDirectory;
    private DownloadHandler mDownloadHandler = getDownloadHandler();
    private Thread mMobileControllerThread;

    public interface OnPreCacheCompletion {
        void onFileDownloadFail(SSAFile sSAFile);

        void onFileDownloadSuccess(SSAFile sSAFile);
    }

    static class DownloadHandler extends Handler {
        OnPreCacheCompletion mListener;

        DownloadHandler() {
        }

        void setOnPreCacheCompletion(OnPreCacheCompletion listener) {
            if (listener == null) {
                throw new IllegalArgumentException();
            }
            this.mListener = listener;
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DownloadManager.MESSAGE_FILE_DOWNLOAD_SUCCESS /*1016*/:
                    this.mListener.onFileDownloadSuccess((SSAFile) msg.obj);
                    return;
                case DownloadManager.MESSAGE_FILE_DOWNLOAD_FAIL /*1017*/:
                    this.mListener.onFileDownloadFail((SSAFile) msg.obj);
                    return;
                default:
                    return;
            }
        }

        public void release() {
            this.mListener = null;
        }
    }

    static class FileWorkerThread implements Callable<Result> {
        private long mConnectionRetries;
        private String mDirectory;
        private String mFileName;
        private String mFileUrl;
        private String mTmpFilesDirectory;

        public FileWorkerThread(String url, String directory, String fileName, long connectionRetries, String tmpFilesDirectory) {
            this.mFileUrl = url;
            this.mDirectory = directory;
            this.mFileName = fileName;
            this.mConnectionRetries = connectionRetries;
            this.mTmpFilesDirectory = tmpFilesDirectory;
        }

        int saveFile(byte[] data, String destFileName) throws Exception {
            return IronSourceStorageUtils.saveFile(data, destFileName);
        }

        boolean renameFile(String fromName, String toName) throws Exception {
            return IronSourceStorageUtils.renameFile(fromName, toName);
        }

        byte[] getBytes(InputStream in) throws IOException {
            return DownloadManager.getBytes(in);
        }

        public Result call() {
            Result results = null;
            if (this.mConnectionRetries == 0) {
                this.mConnectionRetries = 1;
            }
            for (int tryIndex = 0; ((long) tryIndex) < this.mConnectionRetries; tryIndex++) {
                results = downloadContent(this.mFileUrl, tryIndex);
                int responseCode = results.responseCode;
                if (responseCode != DownloadManager.MESSAGE_SOCKET_TIMEOUT_EXCEPTION && responseCode != DownloadManager.MESSAGE_IO_EXCEPTION) {
                    break;
                }
            }
            if (!(results == null || results.body == null)) {
                String origFileName = this.mDirectory + File.separator + this.mFileName;
                String tmpFileName = this.mTmpFilesDirectory + File.separator + DownloadManager.TEMP_PREFIX_FOR_FILES + this.mFileName;
                try {
                    if (saveFile(results.body, tmpFileName) == 0) {
                        results.responseCode = DownloadManager.MESSAGE_HTTP_EMPTY_RESPONSE;
                    } else if (!renameFile(tmpFileName, origFileName)) {
                        results.responseCode = DownloadManager.MESSAGE_TMP_FILE_RENAME_FAILED;
                    }
                } catch (FileNotFoundException e) {
                    results.responseCode = DownloadManager.MESSAGE_FILE_NOT_FOUND_EXCEPTION;
                } catch (Exception e2) {
                    if (!TextUtils.isEmpty(e2.getMessage())) {
                        Logger.i(DownloadManager.TAG, e2.getMessage());
                    }
                    results.responseCode = DownloadManager.MESSAGE_IO_EXCEPTION;
                } catch (Error err) {
                    if (!TextUtils.isEmpty(err.getMessage())) {
                        Logger.i(DownloadManager.TAG, err.getMessage());
                    }
                    results.responseCode = DownloadManager.MESSAGE_OUT_OF_MEMORY_EXCEPTION;
                }
            }
            return results;
        }

        Result downloadContent(String url, int tryNumber) {
            Result results = new Result();
            HttpURLConnection connection = null;
            int responseCode = 0;
            if (TextUtils.isEmpty(url)) {
                results.url = url;
                results.responseCode = DownloadManager.MESSAGE_EMPTY_URL;
            } else {
                InputStream is = null;
                try {
                    URL mUrl = new URL(url);
                    mUrl.toURI();
                    connection = (HttpURLConnection) mUrl.openConnection();
                    connection.setRequestMethod(HttpGet.METHOD_NAME);
                    connection.setConnectTimeout(DownloadManager.OPERATION_TIMEOUT);
                    connection.setReadTimeout(DownloadManager.OPERATION_TIMEOUT);
                    connection.connect();
                    responseCode = connection.getResponseCode();
                    if (responseCode < HttpStatus.SC_OK || responseCode >= HttpStatus.SC_BAD_REQUEST) {
                        responseCode = DownloadManager.MESSAGE_GENERAL_HTTP_ERROR_CODE;
                    } else {
                        is = connection.getInputStream();
                        results.body = getBytes(is);
                    }
                    if (responseCode != HttpStatus.SC_OK) {
                        Logger.i(DownloadManager.TAG, " RESPONSE CODE: " + responseCode + " URL: " + url + " ATTEMPT: " + tryNumber);
                    }
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                    results.url = url;
                    results.responseCode = responseCode;
                } catch (MalformedURLException e2) {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e3) {
                            e3.printStackTrace();
                        }
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                    results.url = url;
                    results.responseCode = DownloadManager.MESSAGE_MALFORMED_URL_EXCEPTION;
                } catch (URISyntaxException e4) {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e32) {
                            e32.printStackTrace();
                        }
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                    results.url = url;
                    results.responseCode = DownloadManager.MESSAGE_URI_SYNTAX_EXCEPTION;
                } catch (SocketTimeoutException e5) {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e322) {
                            e322.printStackTrace();
                        }
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                    results.url = url;
                    results.responseCode = DownloadManager.MESSAGE_SOCKET_TIMEOUT_EXCEPTION;
                } catch (FileNotFoundException e6) {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e3222) {
                            e3222.printStackTrace();
                        }
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                    results.url = url;
                    results.responseCode = DownloadManager.MESSAGE_FILE_NOT_FOUND_EXCEPTION;
                } catch (Exception e7) {
                    if (!TextUtils.isEmpty(e7.getMessage())) {
                        Logger.i(DownloadManager.TAG, e7.getMessage());
                    }
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e32222) {
                            e32222.printStackTrace();
                        }
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                    results.url = url;
                    results.responseCode = DownloadManager.MESSAGE_IO_EXCEPTION;
                } catch (Error err) {
                    responseCode = DownloadManager.MESSAGE_OUT_OF_MEMORY_EXCEPTION;
                    if (!TextUtils.isEmpty(err.getMessage())) {
                        Logger.i(DownloadManager.TAG, err.getMessage());
                    }
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e322222) {
                            e322222.printStackTrace();
                        }
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                    results.url = url;
                    results.responseCode = DownloadManager.MESSAGE_OUT_OF_MEMORY_EXCEPTION;
                } catch (Throwable th) {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e3222222) {
                            e3222222.printStackTrace();
                        }
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                    results.url = url;
                    results.responseCode = responseCode;
                }
            }
            return results;
        }
    }

    static class Result {
        byte[] body;
        int responseCode;
        public String url;

        Result() {
        }
    }

    static class SingleFileWorkerThread implements Runnable {
        private String mCacheRootDirectory;
        private long mConnectionRetries = getConnectionRetries();
        Handler mDownloadHandler;
        private String mFile;
        private String mFileName = guessFileName(this.mFile);
        private String mPath;
        private final String mTempFilesDirectory;

        SingleFileWorkerThread(SSAFile file, Handler downloadHandler, String cacheRootDir, String tempFilesDirectory) {
            this.mFile = file.getFile();
            this.mPath = file.getPath();
            this.mCacheRootDirectory = cacheRootDir;
            this.mDownloadHandler = downloadHandler;
            this.mTempFilesDirectory = tempFilesDirectory;
        }

        String guessFileName(String file) {
            return SDKUtils.getFileName(this.mFile);
        }

        FileWorkerThread getFileWorkerThread(String url, String directory, String fileName, long connectionRetries, String tmpFilesDirectory) {
            return new FileWorkerThread(url, directory, fileName, connectionRetries, tmpFilesDirectory);
        }

        Message getMessage() {
            return new Message();
        }

        String makeDir(String cacheRootDirectory, String directory) {
            return IronSourceStorageUtils.makeDir(cacheRootDirectory, directory);
        }

        public void run() {
            SSAFile ssaFile = new SSAFile(this.mFileName, this.mPath);
            Message msg = getMessage();
            msg.obj = ssaFile;
            String folderName = makeDir(this.mCacheRootDirectory, this.mPath);
            if (folderName == null) {
                msg.what = DownloadManager.MESSAGE_FILE_DOWNLOAD_FAIL;
                ssaFile.setErrMsg(DownloadManager.UNABLE_TO_CREATE_FOLDER);
                this.mDownloadHandler.sendMessage(msg);
                return;
            }
            int code = getFileWorkerThread(this.mFile, folderName, ssaFile.getFile(), this.mConnectionRetries, this.mTempFilesDirectory).call().responseCode;
            switch (code) {
                case HttpStatus.SC_OK /*200*/:
                    msg.what = DownloadManager.MESSAGE_FILE_DOWNLOAD_SUCCESS;
                    this.mDownloadHandler.sendMessage(msg);
                    return;
                case HttpStatus.SC_NOT_FOUND /*404*/:
                case DownloadManager.MESSAGE_MALFORMED_URL_EXCEPTION /*1004*/:
                case DownloadManager.MESSAGE_HTTP_NOT_FOUND /*1005*/:
                case DownloadManager.MESSAGE_HTTP_EMPTY_RESPONSE /*1006*/:
                case DownloadManager.MESSAGE_SOCKET_TIMEOUT_EXCEPTION /*1008*/:
                case DownloadManager.MESSAGE_IO_EXCEPTION /*1009*/:
                case DownloadManager.MESSAGE_URI_SYNTAX_EXCEPTION /*1010*/:
                case DownloadManager.MESSAGE_GENERAL_HTTP_ERROR_CODE /*1011*/:
                case DownloadManager.MESSAGE_FILE_NOT_FOUND_EXCEPTION /*1018*/:
                case DownloadManager.MESSAGE_OUT_OF_MEMORY_EXCEPTION /*1019*/:
                    String errMsg = convertErrorCodeToMessage(code);
                    msg.what = DownloadManager.MESSAGE_FILE_DOWNLOAD_FAIL;
                    ssaFile.setErrMsg(errMsg);
                    this.mDownloadHandler.sendMessage(msg);
                    return;
                default:
                    return;
            }
        }

        String convertErrorCodeToMessage(int errorCode) {
            String errMsg = "not defined message for " + errorCode;
            switch (errorCode) {
                case HttpStatus.SC_NOT_FOUND /*404*/:
                case DownloadManager.MESSAGE_HTTP_NOT_FOUND /*1005*/:
                    return DownloadManager.HTTP_NOT_FOUND;
                case DownloadManager.MESSAGE_MALFORMED_URL_EXCEPTION /*1004*/:
                    return DownloadManager.MALFORMED_URL_EXCEPTION;
                case DownloadManager.MESSAGE_HTTP_EMPTY_RESPONSE /*1006*/:
                    return DownloadManager.HTTP_EMPTY_RESPONSE;
                case DownloadManager.MESSAGE_SOCKET_TIMEOUT_EXCEPTION /*1008*/:
                    return DownloadManager.SOCKET_TIMEOUT_EXCEPTION;
                case DownloadManager.MESSAGE_IO_EXCEPTION /*1009*/:
                    return DownloadManager.IO_EXCEPTION;
                case DownloadManager.MESSAGE_URI_SYNTAX_EXCEPTION /*1010*/:
                    return DownloadManager.URI_SYNTAX_EXCEPTION;
                case DownloadManager.MESSAGE_GENERAL_HTTP_ERROR_CODE /*1011*/:
                    return DownloadManager.HTTP_ERROR_CODE;
                case DownloadManager.MESSAGE_FILE_NOT_FOUND_EXCEPTION /*1018*/:
                    return DownloadManager.FILE_NOT_FOUND_EXCEPTION;
                case DownloadManager.MESSAGE_OUT_OF_MEMORY_EXCEPTION /*1019*/:
                    return DownloadManager.OUT_OF_MEMORY_EXCEPTION;
                default:
                    return errMsg;
            }
        }

        public long getConnectionRetries() {
            return Long.parseLong(IronSourceSharedPrefHelper.getSupersonicPrefHelper().getConnectionRetries());
        }
    }

    private DownloadManager(String cacheRootDirectory) {
        this.mCacheRootDirectory = cacheRootDirectory;
        IronSourceStorageUtils.deleteFolder(this.mCacheRootDirectory, TEMP_DIR_FOR_FILES);
        IronSourceStorageUtils.makeDir(this.mCacheRootDirectory, TEMP_DIR_FOR_FILES);
    }

    public static synchronized DownloadManager getInstance(String cacheRootDirectory) {
        DownloadManager downloadManager;
        synchronized (DownloadManager.class) {
            if (mDownloadManager == null) {
                mDownloadManager = new DownloadManager(cacheRootDirectory);
            }
            downloadManager = mDownloadManager;
        }
        return downloadManager;
    }

    DownloadHandler getDownloadHandler() {
        return new DownloadHandler();
    }

    public void setOnPreCacheCompletion(OnPreCacheCompletion listener) {
        this.mDownloadHandler.setOnPreCacheCompletion(listener);
    }

    public void release() {
        mDownloadManager = null;
        this.mDownloadHandler.release();
        this.mDownloadHandler = null;
    }

    public void downloadFile(SSAFile file) {
        new Thread(new SingleFileWorkerThread(file, this.mDownloadHandler, this.mCacheRootDirectory, getTempFilesDirectory())).start();
    }

    public void downloadMobileControllerFile(SSAFile file) {
        this.mMobileControllerThread = new Thread(new SingleFileWorkerThread(file, this.mDownloadHandler, this.mCacheRootDirectory, getTempFilesDirectory()));
        this.mMobileControllerThread.start();
    }

    public boolean isMobileControllerThreadLive() {
        return this.mMobileControllerThread != null && this.mMobileControllerThread.isAlive();
    }

    String getTempFilesDirectory() {
        return this.mCacheRootDirectory + File.separator + TEMP_DIR_FOR_FILES;
    }

    static byte[] getBytes(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[CacheConfig.DEFAULT_MAX_OBJECT_SIZE_BYTES];
        while (true) {
            int bytesRead = in.read(data, 0, data.length);
            if (bytesRead != -1) {
                buffer.write(data, 0, bytesRead);
            } else {
                buffer.flush();
                return buffer.toByteArray();
            }
        }
    }
}
