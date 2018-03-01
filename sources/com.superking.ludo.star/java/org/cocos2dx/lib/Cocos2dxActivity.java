package org.cocos2dx.lib;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.opengl.GLSurfaceView.EGLConfigChooser;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager.OnActivityResultListener;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import com.facebook.internal.ServerProtocol;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;
import org.cocos2dx.lib.Cocos2dxHandler.DialogMessage;
import org.cocos2dx.lib.Cocos2dxHelper.Cocos2dxHelperListener;

public abstract class Cocos2dxActivity extends Activity implements Cocos2dxHelperListener {
    private static final String TAG = Cocos2dxActivity.class.getSimpleName();
    private static Cocos2dxActivity sContext = null;
    private boolean hasFocus = false;
    private Cocos2dxEditBoxHelper mEditBoxHelper = null;
    protected ResizeLayout mFrameLayout = null;
    private int[] mGLContextAttrs = null;
    private Cocos2dxGLSurfaceView mGLSurfaceView = null;
    private Cocos2dxHandler mHandler = null;
    private Cocos2dxVideoHelper mVideoHelper = null;
    private Cocos2dxWebViewHelper mWebViewHelper = null;

    public class Cocos2dxEGLConfigChooser implements EGLConfigChooser {
        protected int[] configAttribs;

        class ConfigValue implements Comparable<ConfigValue> {
            public EGLConfig config = null;
            public int[] configAttribs = null;
            public int value = 0;

            private void calcValue() {
                if (this.configAttribs[4] > 0) {
                    this.value = (this.value + 536870912) + ((this.configAttribs[4] % 64) << 6);
                }
                if (this.configAttribs[5] > 0) {
                    this.value = (this.value + 268435456) + (this.configAttribs[5] % 64);
                }
                if (this.configAttribs[3] > 0) {
                    this.value = (this.value + 1073741824) + ((this.configAttribs[3] % 16) << 24);
                }
                if (this.configAttribs[1] > 0) {
                    this.value += (this.configAttribs[1] % 16) << 20;
                }
                if (this.configAttribs[2] > 0) {
                    this.value += (this.configAttribs[2] % 16) << 16;
                }
                if (this.configAttribs[0] > 0) {
                    this.value += (this.configAttribs[0] % 16) << 12;
                }
            }

            public ConfigValue(int[] attribs) {
                this.configAttribs = attribs;
                calcValue();
            }

            public ConfigValue(EGL10 egl, EGLDisplay display, EGLConfig config) {
                this.config = config;
                this.configAttribs = new int[6];
                this.configAttribs[0] = Cocos2dxEGLConfigChooser.this.findConfigAttrib(egl, display, config, 12324, 0);
                this.configAttribs[1] = Cocos2dxEGLConfigChooser.this.findConfigAttrib(egl, display, config, 12323, 0);
                this.configAttribs[2] = Cocos2dxEGLConfigChooser.this.findConfigAttrib(egl, display, config, 12322, 0);
                this.configAttribs[3] = Cocos2dxEGLConfigChooser.this.findConfigAttrib(egl, display, config, 12321, 0);
                this.configAttribs[4] = Cocos2dxEGLConfigChooser.this.findConfigAttrib(egl, display, config, 12325, 0);
                this.configAttribs[5] = Cocos2dxEGLConfigChooser.this.findConfigAttrib(egl, display, config, 12326, 0);
                calcValue();
            }

            public int compareTo(ConfigValue another) {
                if (this.value < another.value) {
                    return -1;
                }
                if (this.value > another.value) {
                    return 1;
                }
                return 0;
            }

            public String toString() {
                return "{ color: " + this.configAttribs[3] + this.configAttribs[2] + this.configAttribs[1] + this.configAttribs[0] + "; depth: " + this.configAttribs[4] + "; stencil: " + this.configAttribs[5] + ";}";
            }
        }

        public Cocos2dxEGLConfigChooser(int redSize, int greenSize, int blueSize, int alphaSize, int depthSize, int stencilSize) {
            this.configAttribs = new int[]{redSize, greenSize, blueSize, alphaSize, depthSize, stencilSize};
        }

        public Cocos2dxEGLConfigChooser(int[] attribs) {
            this.configAttribs = attribs;
        }

        private int findConfigAttrib(EGL10 egl, EGLDisplay display, EGLConfig config, int attribute, int defaultValue) {
            int[] value = new int[1];
            if (egl.eglGetConfigAttrib(display, config, attribute, value)) {
                return value[0];
            }
            return defaultValue;
        }

        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
            EGLConfig[] configs = new EGLConfig[1];
            int[] numConfigs = new int[1];
            if (egl.eglChooseConfig(display, new int[]{12324, this.configAttribs[0], 12323, this.configAttribs[1], 12322, this.configAttribs[2], 12321, this.configAttribs[3], 12325, this.configAttribs[4], 12326, this.configAttribs[5], 12352, 4, 12344}, configs, 1, numConfigs) && numConfigs[0] > 0) {
                return configs[0];
            }
            int[] EGLV2attribs = new int[]{12352, 4, 12344};
            if (!egl.eglChooseConfig(display, EGLV2attribs, null, 0, numConfigs) || numConfigs[0] <= 0) {
                Log.e("device_policy", "Can not select an EGLConfig for rendering.");
                return null;
            }
            int num = numConfigs[0];
            ConfigValue[] cfgVals = new ConfigValue[num];
            configs = new EGLConfig[num];
            egl.eglChooseConfig(display, EGLV2attribs, configs, num, numConfigs);
            for (int i = 0; i < num; i++) {
                cfgVals[i] = new ConfigValue(egl, display, configs[i]);
            }
            ConfigValue configValue = new ConfigValue(this.configAttribs);
            int lo = 0;
            int hi = num;
            while (lo < hi - 1) {
                int mi = (lo + hi) / 2;
                if (configValue.compareTo(cfgVals[mi]) < 0) {
                    hi = mi;
                } else {
                    lo = mi;
                }
            }
            if (lo != num - 1) {
                lo++;
            }
            Log.w("cocos2d", "Can't find EGLConfig match: " + configValue + ", instead of closest one:" + cfgVals[lo]);
            return cfgVals[lo].config;
        }
    }

    private static native int[] getGLContextAttrs();

    public Cocos2dxGLSurfaceView getGLSurfaceView() {
        return this.mGLSurfaceView;
    }

    public static Context getContext() {
        return sContext;
    }

    public void setKeepScreenOn(boolean value) {
        final boolean newValue = value;
        runOnUiThread(new Runnable() {
            public void run() {
                Cocos2dxActivity.this.mGLSurfaceView.setKeepScreenOn(newValue);
            }
        });
    }

    protected void onLoadNativeLibraries() {
        try {
            System.loadLibrary(getPackageManager().getApplicationInfo(getPackageName(), 128).metaData.getString("android.app.lib_name"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideVirtualButton();
        onLoadNativeLibraries();
        sContext = this;
        this.mHandler = new Cocos2dxHandler(this);
        Cocos2dxHelper.init(this);
        this.mGLContextAttrs = getGLContextAttrs();
        init();
        if (this.mVideoHelper == null) {
            this.mVideoHelper = new Cocos2dxVideoHelper(this, this.mFrameLayout);
        }
        if (this.mWebViewHelper == null) {
            this.mWebViewHelper = new Cocos2dxWebViewHelper(this.mFrameLayout);
        }
        if (this.mEditBoxHelper == null) {
            this.mEditBoxHelper = new Cocos2dxEditBoxHelper(this.mFrameLayout);
        }
        getWindow().setSoftInputMode(32);
        setVolumeControlStream(3);
    }

    protected void onResume() {
        Log.d(TAG, "onResume()");
        super.onResume();
        hideVirtualButton();
        resumeIfHasFocus();
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        Log.d(TAG, "onWindowFocusChanged() hasFocus=" + hasFocus);
        super.onWindowFocusChanged(hasFocus);
        this.hasFocus = hasFocus;
        resumeIfHasFocus();
    }

    private void resumeIfHasFocus() {
        if (this.hasFocus) {
            hideVirtualButton();
            Cocos2dxHelper.onResume();
            this.mGLSurfaceView.onResume();
        }
    }

    protected void onPause() {
        Log.d(TAG, "onPause()");
        super.onPause();
        Cocos2dxHelper.onPause();
        this.mGLSurfaceView.onPause();
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    public void showDialog(String pTitle, String pMessage) {
        Message msg = new Message();
        msg.what = 1;
        msg.obj = new DialogMessage(pTitle, pMessage);
        this.mHandler.sendMessage(msg);
    }

    public void runOnGLThread(Runnable pRunnable) {
        this.mGLSurfaceView.queueEvent(pRunnable);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        for (OnActivityResultListener listener : Cocos2dxHelper.getOnActivityResultListeners()) {
            listener.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void init() {
        LayoutParams framelayout_params = new LayoutParams(-1, -1);
        this.mFrameLayout = new ResizeLayout(this);
        this.mFrameLayout.setLayoutParams(framelayout_params);
        LayoutParams edittext_layout_params = new LayoutParams(-1, -2);
        Cocos2dxEditBox edittext = new Cocos2dxEditBox(this);
        edittext.setLayoutParams(edittext_layout_params);
        this.mFrameLayout.addView(edittext);
        this.mGLSurfaceView = onCreateView();
        this.mFrameLayout.addView(this.mGLSurfaceView);
        if (isAndroidEmulator()) {
            this.mGLSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        }
        this.mGLSurfaceView.setCocos2dxRenderer(new Cocos2dxRenderer());
        this.mGLSurfaceView.setCocos2dxEditText(edittext);
        setContentView(this.mFrameLayout);
    }

    public Cocos2dxGLSurfaceView onCreateView() {
        Cocos2dxGLSurfaceView glSurfaceView = new Cocos2dxGLSurfaceView(this);
        glSurfaceView.setMultipleTouchEnabled(false);
        if (this.mGLContextAttrs[3] > 0) {
            glSurfaceView.getHolder().setFormat(-3);
        }
        glSurfaceView.setEGLConfigChooser(new Cocos2dxEGLConfigChooser(this.mGLContextAttrs));
        return glSurfaceView;
    }

    protected void hideVirtualButton() {
        if (VERSION.SDK_INT >= 19) {
            Class viewClass = View.class;
            try {
                int SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION = ((Integer) Cocos2dxReflectionHelper.getConstantValue(viewClass, "SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION")).intValue();
                int SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN = ((Integer) Cocos2dxReflectionHelper.getConstantValue(viewClass, "SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN")).intValue();
                int SYSTEM_UI_FLAG_HIDE_NAVIGATION = ((Integer) Cocos2dxReflectionHelper.getConstantValue(viewClass, "SYSTEM_UI_FLAG_HIDE_NAVIGATION")).intValue();
                int SYSTEM_UI_FLAG_FULLSCREEN = ((Integer) Cocos2dxReflectionHelper.getConstantValue(viewClass, "SYSTEM_UI_FLAG_FULLSCREEN")).intValue();
                int SYSTEM_UI_FLAG_IMMERSIVE_STICKY = ((Integer) Cocos2dxReflectionHelper.getConstantValue(viewClass, "SYSTEM_UI_FLAG_IMMERSIVE_STICKY")).intValue();
                Class[] clsArr = new Class[]{Integer.TYPE};
                Cocos2dxReflectionHelper.invokeInstanceMethod(getWindow().getDecorView(), "setSystemUiVisibility", clsArr, new Object[]{Integer.valueOf(((((((Integer) Cocos2dxReflectionHelper.getConstantValue(viewClass, "SYSTEM_UI_FLAG_LAYOUT_STABLE")).intValue() | SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION) | SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN) | SYSTEM_UI_FLAG_HIDE_NAVIGATION) | SYSTEM_UI_FLAG_FULLSCREEN) | SYSTEM_UI_FLAG_IMMERSIVE_STICKY)});
            } catch (NullPointerException e) {
                Log.e(TAG, "hideVirtualButton", e);
            }
        }
    }

    private static final boolean isAndroidEmulator() {
        Log.d(TAG, "model=" + Build.MODEL);
        String product = Build.PRODUCT;
        Log.d(TAG, "product=" + product);
        boolean isEmulator = false;
        if (product != null) {
            isEmulator = product.equals(ServerProtocol.DIALOG_PARAM_SDK_VERSION) || product.contains("_sdk") || product.contains("sdk_");
        }
        Log.d(TAG, "isEmulator=" + isEmulator);
        return isEmulator;
    }
}
