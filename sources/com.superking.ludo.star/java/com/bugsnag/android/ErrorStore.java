package com.bugsnag.android;

import android.content.Context;
import com.bugsnag.android.JsonStream.Streamable;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Locale;

class ErrorStore {
    private static final int MAX_STORED_ERRORS = 100;
    private static final String UNSENT_ERROR_PATH = "/bugsnag-errors/";
    final Configuration config;
    final String path;

    ErrorStore(Configuration config, Context appContext) {
        String path;
        this.config = config;
        try {
            path = appContext.getCacheDir().getAbsolutePath() + UNSENT_ERROR_PATH;
            File outFile = new File(path);
            outFile.mkdirs();
            if (!outFile.exists()) {
                Logger.warn("Could not prepare error storage directory");
                path = null;
            }
        } catch (Exception e) {
            Logger.warn("Could not prepare error storage directory", e);
            path = null;
        }
        this.path = path;
    }

    void flush() {
        if (this.path != null) {
            Async.run(new Runnable() {
                public void run() {
                    int i = 0;
                    File exceptionDir = new File(ErrorStore.this.path);
                    if (exceptionDir.exists() && exceptionDir.isDirectory()) {
                        File[] errorFiles = exceptionDir.listFiles();
                        if (errorFiles != null && errorFiles.length > 0) {
                            Logger.info(String.format(Locale.US, "Sending %d saved error(s) to Bugsnag", new Object[]{Integer.valueOf(errorFiles.length)}));
                            int length = errorFiles.length;
                            while (i < length) {
                                File errorFile = errorFiles[i];
                                try {
                                    HttpClient.post(ErrorStore.this.config.getEndpoint(), new Report(ErrorStore.this.config.getApiKey(), errorFile));
                                    Logger.info("Deleting sent error file " + errorFile.getName());
                                    if (!errorFile.delete()) {
                                        errorFile.deleteOnExit();
                                    }
                                } catch (NetworkException e) {
                                    Logger.warn("Could not send previously saved error(s) to Bugsnag, will try again later", e);
                                } catch (Exception e2) {
                                    Logger.warn("Problem sending unsent error from disk", e2);
                                    if (!errorFile.delete()) {
                                        errorFile.deleteOnExit();
                                    }
                                }
                                i++;
                            }
                        }
                    }
                }
            });
        }
    }

    void write(Error error) {
        Exception e;
        Throwable th;
        if (this.path != null) {
            File exceptionDir = new File(this.path);
            if (exceptionDir.isDirectory()) {
                File[] files = exceptionDir.listFiles();
                if (files.length >= MAX_STORED_ERRORS) {
                    Arrays.sort(files);
                    Logger.warn(String.format("Discarding oldest error as stored error limit reached (%s)", new Object[]{files[0].getPath()}));
                    if (!files[0].delete()) {
                        files[0].deleteOnExit();
                    }
                }
            }
            Writer out = null;
            try {
                Writer out2 = new FileWriter(String.format(Locale.US, "%s%d.json", new Object[]{this.path, Long.valueOf(System.currentTimeMillis())}));
                try {
                    JsonStream stream = new JsonStream(out2);
                    stream.value((Streamable) error);
                    stream.close();
                    Logger.info(String.format("Saved unsent error to disk (%s) ", new Object[]{filename}));
                    IOUtils.closeQuietly(out2);
                    out = out2;
                } catch (Exception e2) {
                    e = e2;
                    out = out2;
                    try {
                        Logger.warn(String.format("Couldn't save unsent error to disk (%s) ", new Object[]{filename}), e);
                        IOUtils.closeQuietly(out);
                    } catch (Throwable th2) {
                        th = th2;
                        IOUtils.closeQuietly(out);
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    out = out2;
                    IOUtils.closeQuietly(out);
                    throw th;
                }
            } catch (Exception e3) {
                e = e3;
                Logger.warn(String.format("Couldn't save unsent error to disk (%s) ", new Object[]{filename}), e);
                IOUtils.closeQuietly(out);
            }
        }
    }
}
