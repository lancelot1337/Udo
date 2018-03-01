package org.cocos2dx.lib;

import cz.msebera.android.httpclient.protocol.HTTP;
import java.io.File;
import java.io.FileInputStream;

public class GameControllerUtils {
    public static void ensureDirectoryExist(String path) {
        File sdkDir = new File(path);
        if (!sdkDir.exists()) {
            sdkDir.mkdirs();
        }
    }

    public static String readJsonFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }
        try {
            FileInputStream is = new FileInputStream(file);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            return new String(buffer, HTTP.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
