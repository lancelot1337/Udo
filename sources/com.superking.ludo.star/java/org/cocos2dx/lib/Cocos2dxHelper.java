package org.cocos2dx.lib;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Environment;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.os.Vibrator;
import android.preference.PreferenceManager.OnActivityResultListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import com.android.vending.expansion.zipfile.APKExpansionSupport;
import com.android.vending.expansion.zipfile.ZipResourceFile;
import com.enhance.gameservice.IGameTuningService;
import com.enhance.gameservice.IGameTuningService.Stub;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public class Cocos2dxHelper {
    private static final int BOOST_TIME = 7;
    private static final String PREFS_NAME = "Cocos2dxPrefsFile";
    private static final int RUNNABLES_PER_FRAME = 5;
    private static final String TAG = Cocos2dxHelper.class.getSimpleName();
    private static ServiceConnection connection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            Cocos2dxHelper.mGameServiceBinder = Stub.asInterface(service);
            Cocos2dxHelper.fastLoading(Cocos2dxHelper.BOOST_TIME);
        }

        public void onServiceDisconnected(ComponentName name) {
            Cocos2dxHelper.sActivity.getApplicationContext().unbindService(Cocos2dxHelper.connection);
        }
    };
    private static IGameTuningService mGameServiceBinder = null;
    private static Set<OnActivityResultListener> onActivityResultListeners = new LinkedHashSet();
    private static boolean sAccelerometerEnabled;
    private static Activity sActivity = null;
    private static boolean sActivityVisible;
    private static AssetManager sAssetManager;
    private static String sAssetsPath = BuildConfig.FLAVOR;
    private static Cocos2dxMusic sCocos2dMusic;
    private static Cocos2dxSound sCocos2dSound;
    private static Cocos2dxAccelerometer sCocos2dxAccelerometer;
    private static Cocos2dxHelperListener sCocos2dxHelperListener;
    private static boolean sCompassEnabled;
    private static String sFileDirectory;
    private static boolean sInited = false;
    private static ZipResourceFile sOBBFile = null;
    private static String sPackageName;
    private static Vibrator sVibrateService = null;

    public interface Cocos2dxHelperListener {
        void runOnGLThread(Runnable runnable);

        void showDialog(String str, String str2);
    }

    private static native void nativeSetApkPath(String str);

    private static native void nativeSetAudioDeviceInfo(boolean z, int i, int i2);

    private static native void nativeSetContext(Context context, AssetManager assetManager);

    private static native void nativeSetEditTextDialogResult(byte[] bArr);

    public static void runOnGLThread(Runnable r) {
        ((Cocos2dxActivity) sActivity).runOnGLThread(r);
    }

    public static void init(Activity activity) {
        sActivity = activity;
        sCocos2dxHelperListener = (Cocos2dxHelperListener) activity;
        if (!sInited) {
            boolean isSupportLowLatency = activity.getPackageManager().hasSystemFeature("android.hardware.audio.low_latency");
            Log.d(TAG, "isSupportLowLatency:" + isSupportLowLatency);
            int sampleRate = 44100;
            int bufferSizeInFrames = 192;
            if (VERSION.SDK_INT >= 17) {
                AudioManager am = (AudioManager) activity.getSystemService("audio");
                Class audioManagerClass = AudioManager.class;
                Class[] clsArr = new Class[]{String.class};
                String strSampleRate = (String) Cocos2dxReflectionHelper.invokeInstanceMethod(am, "getProperty", clsArr, new Object[]{Cocos2dxReflectionHelper.getConstantValue(audioManagerClass, "PROPERTY_OUTPUT_SAMPLE_RATE")});
                clsArr = new Class[]{String.class};
                String strBufferSizeInFrames = (String) Cocos2dxReflectionHelper.invokeInstanceMethod(am, "getProperty", clsArr, new Object[]{Cocos2dxReflectionHelper.getConstantValue(audioManagerClass, "PROPERTY_OUTPUT_FRAMES_PER_BUFFER")});
                try {
                    sampleRate = Integer.parseInt(strSampleRate);
                    bufferSizeInFrames = Integer.parseInt(strBufferSizeInFrames);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "parseInt failed", e);
                }
                Log.d(TAG, "sampleRate: " + sampleRate + ", framesPerBuffer: " + bufferSizeInFrames);
            } else {
                Log.d(TAG, "android version is lower than 17");
            }
            nativeSetAudioDeviceInfo(isSupportLowLatency, sampleRate, bufferSizeInFrames);
            sPackageName = activity.getApplicationInfo().packageName;
            sFileDirectory = activity.getFilesDir().getAbsolutePath();
            nativeSetApkPath(getAssetsPath());
            sCocos2dxAccelerometer = new Cocos2dxAccelerometer(activity);
            sCocos2dMusic = new Cocos2dxMusic(activity);
            sCocos2dSound = new Cocos2dxSound(activity);
            sAssetManager = activity.getAssets();
            nativeSetContext(activity, sAssetManager);
            Cocos2dxBitmap.setContext(activity);
            sVibrateService = (Vibrator) activity.getSystemService("vibrator");
            sInited = true;
            Intent serviceIntent = new Intent(IGameTuningService.class.getName());
            serviceIntent.setPackage("com.enhance.gameservice");
            boolean suc = activity.getApplicationContext().bindService(serviceIntent, connection, 1);
            int versionCode = 1;
            try {
                versionCode = Cocos2dxActivity.getContext().getPackageManager().getPackageInfo(getCocos2dxPackageName(), 0).versionCode;
            } catch (NameNotFoundException e2) {
                e2.printStackTrace();
            }
            try {
                sOBBFile = APKExpansionSupport.getAPKExpansionZipFile(Cocos2dxActivity.getContext(), versionCode, 0);
            } catch (IOException e3) {
                e3.printStackTrace();
            }
        }
    }

    public static String getAssetsPath() {
        if (sAssetsPath == BuildConfig.FLAVOR) {
            int versionCode = 1;
            try {
                versionCode = sActivity.getPackageManager().getPackageInfo(sPackageName, 0).versionCode;
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
            String pathToOBB = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/obb/" + sPackageName + "/main." + versionCode + "." + sPackageName + ".obb";
            if (new File(pathToOBB).exists()) {
                sAssetsPath = pathToOBB;
            } else {
                sAssetsPath = sActivity.getApplicationInfo().sourceDir;
            }
        }
        return sAssetsPath;
    }

    public static ZipResourceFile getObbFile() {
        return sOBBFile;
    }

    public static Activity getActivity() {
        return sActivity;
    }

    public static void addOnActivityResultListener(OnActivityResultListener listener) {
        onActivityResultListeners.add(listener);
    }

    public static Set<OnActivityResultListener> getOnActivityResultListeners() {
        return onActivityResultListeners;
    }

    public static boolean isActivityVisible() {
        return sActivityVisible;
    }

    public static String getCocos2dxPackageName() {
        return sPackageName;
    }

    public static String getCocos2dxWritablePath() {
        return sFileDirectory;
    }

    public static String getCurrentLanguage() {
        return Locale.getDefault().getLanguage();
    }

    public static String getDeviceModel() {
        return Build.MODEL;
    }

    public static AssetManager getAssetManager() {
        return sAssetManager;
    }

    public static void enableAccelerometer() {
        sAccelerometerEnabled = true;
        sCocos2dxAccelerometer.enableAccel();
    }

    public static void enableCompass() {
        sCompassEnabled = true;
        sCocos2dxAccelerometer.enableCompass();
    }

    public static void setAccelerometerInterval(float interval) {
        sCocos2dxAccelerometer.setInterval(interval);
    }

    public static void disableAccelerometer() {
        sAccelerometerEnabled = false;
        sCocos2dxAccelerometer.disable();
    }

    public static void setKeepScreenOn(boolean value) {
        ((Cocos2dxActivity) sActivity).setKeepScreenOn(value);
    }

    public static void vibrate(float duration) {
        sVibrateService.vibrate((long) (1000.0f * duration));
    }

    public static String getVersion() {
        try {
            return Cocos2dxActivity.getContext().getPackageManager().getPackageInfo(Cocos2dxActivity.getContext().getPackageName(), 0).versionName;
        } catch (Exception e) {
            return BuildConfig.FLAVOR;
        }
    }

    public static boolean openURL(String url) {
        try {
            Intent i = new Intent("android.intent.action.VIEW");
            i.setData(Uri.parse(url));
            sActivity.startActivity(i);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static long[] getObbAssetFileDescriptor(String path) {
        long[] array = new long[3];
        if (sOBBFile != null) {
            AssetFileDescriptor descriptor = sOBBFile.getAssetFileDescriptor(path);
            if (descriptor != null) {
                try {
                    ParcelFileDescriptor parcel = descriptor.getParcelFileDescriptor();
                    array[0] = (long) ((Integer) parcel.getClass().getMethod("getFd", new Class[0]).invoke(parcel, new Object[0])).intValue();
                    array[1] = descriptor.getStartOffset();
                    array[2] = descriptor.getLength();
                } catch (NoSuchMethodException e) {
                    Log.e(TAG, "Accessing file descriptor directly from the OBB is only supported from Android 3.1 (API level 12) and above.");
                } catch (IllegalAccessException e2) {
                    Log.e(TAG, e2.toString());
                } catch (InvocationTargetException e3) {
                    Log.e(TAG, e3.toString());
                }
            }
        }
        return array;
    }

    public static void preloadBackgroundMusic(String pPath) {
        sCocos2dMusic.preloadBackgroundMusic(pPath);
    }

    public static void playBackgroundMusic(String pPath, boolean isLoop) {
        sCocos2dMusic.playBackgroundMusic(pPath, isLoop);
    }

    public static void resumeBackgroundMusic() {
        sCocos2dMusic.resumeBackgroundMusic();
    }

    public static void pauseBackgroundMusic() {
        sCocos2dMusic.pauseBackgroundMusic();
    }

    public static void stopBackgroundMusic() {
        sCocos2dMusic.stopBackgroundMusic();
    }

    public static void rewindBackgroundMusic() {
        sCocos2dMusic.rewindBackgroundMusic();
    }

    public static boolean willPlayBackgroundMusic() {
        return sCocos2dMusic.willPlayBackgroundMusic();
    }

    public static boolean isBackgroundMusicPlaying() {
        return sCocos2dMusic.isBackgroundMusicPlaying();
    }

    public static float getBackgroundMusicVolume() {
        return sCocos2dMusic.getBackgroundVolume();
    }

    public static void setBackgroundMusicVolume(float volume) {
        sCocos2dMusic.setBackgroundVolume(volume);
    }

    public static void preloadEffect(String path) {
        sCocos2dSound.preloadEffect(path);
    }

    public static int playEffect(String path, boolean isLoop, float pitch, float pan, float gain) {
        return sCocos2dSound.playEffect(path, isLoop, pitch, pan, gain);
    }

    public static void resumeEffect(int soundId) {
        sCocos2dSound.resumeEffect(soundId);
    }

    public static void pauseEffect(int soundId) {
        sCocos2dSound.pauseEffect(soundId);
    }

    public static void stopEffect(int soundId) {
        sCocos2dSound.stopEffect(soundId);
    }

    public static float getEffectsVolume() {
        return sCocos2dSound.getEffectsVolume();
    }

    public static void setEffectsVolume(float volume) {
        sCocos2dSound.setEffectsVolume(volume);
    }

    public static void unloadEffect(String path) {
        sCocos2dSound.unloadEffect(path);
    }

    public static void pauseAllEffects() {
        sCocos2dSound.pauseAllEffects();
    }

    public static void resumeAllEffects() {
        sCocos2dSound.resumeAllEffects();
    }

    public static void stopAllEffects() {
        sCocos2dSound.stopAllEffects();
    }

    public static void end() {
        sCocos2dMusic.end();
        sCocos2dSound.end();
    }

    public static void onResume() {
        sActivityVisible = true;
        if (sAccelerometerEnabled) {
            sCocos2dxAccelerometer.enableAccel();
        }
        if (sCompassEnabled) {
            sCocos2dxAccelerometer.enableCompass();
        }
    }

    public static void onPause() {
        sActivityVisible = false;
        if (sAccelerometerEnabled) {
            sCocos2dxAccelerometer.disable();
        }
    }

    public static void onEnterBackground() {
        sCocos2dSound.onEnterBackground();
        sCocos2dMusic.onEnterBackground();
    }

    public static void onEnterForeground() {
        sCocos2dSound.onEnterForeground();
        sCocos2dMusic.onEnterForeground();
    }

    public static void terminateProcess() {
        Process.killProcess(Process.myPid());
    }

    private static void showDialog(String pTitle, String pMessage) {
        sCocos2dxHelperListener.showDialog(pTitle, pMessage);
    }

    public static void setEditTextDialogResult(String pResult) {
        try {
            final byte[] bytesUTF8 = pResult.getBytes("UTF8");
            sCocos2dxHelperListener.runOnGLThread(new Runnable() {
                public void run() {
                    Cocos2dxHelper.nativeSetEditTextDialogResult(bytesUTF8);
                }
            });
        } catch (UnsupportedEncodingException e) {
        }
    }

    public static int getDPI() {
        if (sActivity != null) {
            DisplayMetrics metrics = new DisplayMetrics();
            WindowManager wm = sActivity.getWindowManager();
            if (wm != null) {
                Display d = wm.getDefaultDisplay();
                if (d != null) {
                    d.getMetrics(metrics);
                    return (int) (metrics.density * 160.0f);
                }
            }
        }
        return -1;
    }

    public static boolean getBoolForKey(String key, boolean defaultValue) {
        boolean z = true;
        SharedPreferences settings = sActivity.getSharedPreferences(PREFS_NAME, 0);
        try {
            return settings.getBoolean(key, defaultValue);
        } catch (Exception ex) {
            ex.printStackTrace();
            Object value = settings.getAll().get(key);
            if (value instanceof String) {
                return Boolean.parseBoolean(value.toString());
            }
            if (value instanceof Integer) {
                if (((Integer) value).intValue() == 0) {
                    return false;
                }
                return z;
            } else if (!(value instanceof Float)) {
                return defaultValue;
            } else {
                if (((Float) value).floatValue() == 0.0f) {
                    return false;
                }
                return z;
            }
        }
    }

    public static int getIntegerForKey(String key, int defaultValue) {
        SharedPreferences settings = sActivity.getSharedPreferences(PREFS_NAME, 0);
        try {
            return settings.getInt(key, defaultValue);
        } catch (Exception ex) {
            ex.printStackTrace();
            Object value = settings.getAll().get(key);
            if (value instanceof String) {
                return Integer.parseInt(value.toString());
            }
            if (value instanceof Float) {
                return ((Float) value).intValue();
            }
            if ((value instanceof Boolean) && ((Boolean) value).booleanValue()) {
                return 1;
            }
            return defaultValue;
        }
    }

    public static float getFloatForKey(String key, float defaultValue) {
        SharedPreferences settings = sActivity.getSharedPreferences(PREFS_NAME, 0);
        try {
            return settings.getFloat(key, defaultValue);
        } catch (Exception ex) {
            ex.printStackTrace();
            Object value = settings.getAll().get(key);
            if (value instanceof String) {
                return Float.parseFloat(value.toString());
            }
            if (value instanceof Integer) {
                return ((Integer) value).floatValue();
            }
            if ((value instanceof Boolean) && ((Boolean) value).booleanValue()) {
                return 1.0f;
            }
            return defaultValue;
        }
    }

    public static double getDoubleForKey(String key, double defaultValue) {
        return (double) getFloatForKey(key, (float) defaultValue);
    }

    public static String getStringForKey(String key, String defaultValue) {
        SharedPreferences settings = sActivity.getSharedPreferences(PREFS_NAME, 0);
        try {
            return settings.getString(key, defaultValue);
        } catch (Exception ex) {
            ex.printStackTrace();
            return settings.getAll().get(key).toString();
        }
    }

    public static void setBoolForKey(String key, boolean value) {
        Editor editor = sActivity.getSharedPreferences(PREFS_NAME, 0).edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static void setIntegerForKey(String key, int value) {
        Editor editor = sActivity.getSharedPreferences(PREFS_NAME, 0).edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static void setFloatForKey(String key, float value) {
        Editor editor = sActivity.getSharedPreferences(PREFS_NAME, 0).edit();
        editor.putFloat(key, value);
        editor.apply();
    }

    public static void setDoubleForKey(String key, double value) {
        Editor editor = sActivity.getSharedPreferences(PREFS_NAME, 0).edit();
        editor.putFloat(key, (float) value);
        editor.apply();
    }

    public static void setStringForKey(String key, String value) {
        Editor editor = sActivity.getSharedPreferences(PREFS_NAME, 0).edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static void deleteValueForKey(String key) {
        Editor editor = sActivity.getSharedPreferences(PREFS_NAME, 0).edit();
        editor.remove(key);
        editor.apply();
    }

    public static byte[] conversionEncoding(byte[] text, String fromCharset, String newCharset) {
        try {
            return new String(text, fromCharset).getBytes(newCharset);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int setResolutionPercent(int per) {
        int i = -1;
        try {
            if (mGameServiceBinder != null) {
                i = mGameServiceBinder.setPreferredResolution(per);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return i;
    }

    public static int setFPS(int fps) {
        int i = -1;
        try {
            if (mGameServiceBinder != null) {
                i = mGameServiceBinder.setFramePerSecond(fps);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return i;
    }

    public static int fastLoading(int sec) {
        int i = -1;
        try {
            if (mGameServiceBinder != null) {
                i = mGameServiceBinder.boostUp(sec);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return i;
    }

    public static int getTemperature() {
        int i = -1;
        try {
            if (mGameServiceBinder != null) {
                i = mGameServiceBinder.getAbstractTemperature();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return i;
    }

    public static int setLowPowerMode(boolean enable) {
        int i = -1;
        try {
            if (mGameServiceBinder != null) {
                i = mGameServiceBinder.setGamePowerSaving(enable);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return i;
    }

    public static float[] getAccelValue() {
        return sCocos2dxAccelerometer.accelerometerValues;
    }

    public static float[] getCompassValue() {
        return sCocos2dxAccelerometer.compassFieldValues;
    }

    public static int getSDKVersion() {
        return VERSION.SDK_INT;
    }
}
