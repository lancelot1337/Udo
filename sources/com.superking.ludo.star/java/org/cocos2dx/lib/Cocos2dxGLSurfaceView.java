package org.cocos2dx.lib;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import com.ironsource.sdk.utils.Constants.RequestParameters;
import io.branch.referral.R;

public class Cocos2dxGLSurfaceView extends GLSurfaceView {
    private static final int HANDLER_CLOSE_IME_KEYBOARD = 3;
    private static final int HANDLER_OPEN_IME_KEYBOARD = 2;
    private static final String TAG = Cocos2dxGLSurfaceView.class.getSimpleName();
    private static Cocos2dxGLSurfaceView mCocos2dxGLSurfaceView;
    private static Cocos2dxTextInputWrapper sCocos2dxTextInputWraper;
    private static Handler sHandler;
    private Cocos2dxEditBox mCocos2dxEditText;
    private Cocos2dxRenderer mCocos2dxRenderer;
    private boolean mMultipleTouchEnabled = true;
    private boolean mSoftKeyboardShown = false;

    public boolean isSoftKeyboardShown() {
        return this.mSoftKeyboardShown;
    }

    public void setSoftKeyboardShown(boolean softKeyboardShown) {
        this.mSoftKeyboardShown = softKeyboardShown;
    }

    public boolean isMultipleTouchEnabled() {
        return this.mMultipleTouchEnabled;
    }

    public void setMultipleTouchEnabled(boolean multipleTouchEnabled) {
        this.mMultipleTouchEnabled = multipleTouchEnabled;
    }

    public Cocos2dxGLSurfaceView(Context context) {
        super(context);
        initView();
    }

    public Cocos2dxGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    protected void initView() {
        setEGLContextClientVersion(HANDLER_OPEN_IME_KEYBOARD);
        setFocusableInTouchMode(true);
        mCocos2dxGLSurfaceView = this;
        sCocos2dxTextInputWraper = new Cocos2dxTextInputWrapper(this);
        sHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case Cocos2dxGLSurfaceView.HANDLER_OPEN_IME_KEYBOARD /*2*/:
                        if (Cocos2dxGLSurfaceView.this.mCocos2dxEditText != null && Cocos2dxGLSurfaceView.this.mCocos2dxEditText.requestFocus()) {
                            Cocos2dxGLSurfaceView.this.mCocos2dxEditText.removeTextChangedListener(Cocos2dxGLSurfaceView.sCocos2dxTextInputWraper);
                            Cocos2dxGLSurfaceView.this.mCocos2dxEditText.setText(BuildConfig.FLAVOR);
                            String text = msg.obj;
                            Cocos2dxGLSurfaceView.this.mCocos2dxEditText.append(text);
                            Cocos2dxGLSurfaceView.sCocos2dxTextInputWraper.setOriginText(text);
                            Cocos2dxGLSurfaceView.this.mCocos2dxEditText.addTextChangedListener(Cocos2dxGLSurfaceView.sCocos2dxTextInputWraper);
                            ((InputMethodManager) Cocos2dxGLSurfaceView.mCocos2dxGLSurfaceView.getContext().getSystemService("input_method")).showSoftInput(Cocos2dxGLSurfaceView.this.mCocos2dxEditText, 0);
                            Log.d("GLSurfaceView", "showSoftInput");
                            return;
                        }
                        return;
                    case Cocos2dxGLSurfaceView.HANDLER_CLOSE_IME_KEYBOARD /*3*/:
                        if (Cocos2dxGLSurfaceView.this.mCocos2dxEditText != null) {
                            Cocos2dxGLSurfaceView.this.mCocos2dxEditText.removeTextChangedListener(Cocos2dxGLSurfaceView.sCocos2dxTextInputWraper);
                            ((InputMethodManager) Cocos2dxGLSurfaceView.mCocos2dxGLSurfaceView.getContext().getSystemService("input_method")).hideSoftInputFromWindow(Cocos2dxGLSurfaceView.this.mCocos2dxEditText.getWindowToken(), 0);
                            Cocos2dxGLSurfaceView.this.requestFocus();
                            ((Cocos2dxActivity) Cocos2dxGLSurfaceView.mCocos2dxGLSurfaceView.getContext()).hideVirtualButton();
                            Log.d("GLSurfaceView", "HideSoftInput");
                            return;
                        }
                        return;
                    default:
                        return;
                }
            }
        };
    }

    public static Cocos2dxGLSurfaceView getInstance() {
        return mCocos2dxGLSurfaceView;
    }

    public static void queueAccelerometer(float x, float y, float z, long timestamp) {
        final float f = x;
        final float f2 = y;
        final float f3 = z;
        final long j = timestamp;
        mCocos2dxGLSurfaceView.queueEvent(new Runnable() {
            public void run() {
                Cocos2dxAccelerometer.onSensorChanged(f, f2, f3, j);
            }
        });
    }

    public void setCocos2dxRenderer(Cocos2dxRenderer renderer) {
        this.mCocos2dxRenderer = renderer;
        setRenderer(this.mCocos2dxRenderer);
    }

    private String getContentText() {
        return this.mCocos2dxRenderer.getContentText();
    }

    public Cocos2dxEditBox getCocos2dxEditText() {
        return this.mCocos2dxEditText;
    }

    public void setCocos2dxEditText(Cocos2dxEditBox pCocos2dxEditText) {
        this.mCocos2dxEditText = pCocos2dxEditText;
        if (this.mCocos2dxEditText != null && sCocos2dxTextInputWraper != null) {
            this.mCocos2dxEditText.setOnEditorActionListener(sCocos2dxTextInputWraper);
            requestFocus();
        }
    }

    public void onResume() {
        super.onResume();
        setRenderMode(1);
        queueEvent(new Runnable() {
            public void run() {
                Cocos2dxGLSurfaceView.this.mCocos2dxRenderer.handleOnResume();
            }
        });
    }

    public void onPause() {
        queueEvent(new Runnable() {
            public void run() {
                Cocos2dxGLSurfaceView.this.mCocos2dxRenderer.handleOnPause();
            }
        });
        setRenderMode(0);
    }

    public boolean onTouchEvent(MotionEvent pMotionEvent) {
        int i;
        int pointerNumber = pMotionEvent.getPointerCount();
        final int[] ids = new int[pointerNumber];
        float[] xs = new float[pointerNumber];
        float[] ys = new float[pointerNumber];
        if (this.mSoftKeyboardShown) {
            ((InputMethodManager) getContext().getSystemService("input_method")).hideSoftInputFromWindow(((Activity) getContext()).getCurrentFocus().getWindowToken(), 0);
            requestFocus();
            this.mSoftKeyboardShown = false;
        }
        for (i = 0; i < pointerNumber; i++) {
            ids[i] = pMotionEvent.getPointerId(i);
            xs[i] = pMotionEvent.getX(i);
            ys[i] = pMotionEvent.getY(i);
        }
        final float f;
        final float f2;
        final float[] fArr;
        final float[] fArr2;
        switch (pMotionEvent.getAction() & 255) {
            case Cocos2dxEditBox.kEndActionUnknown /*0*/:
                final int idDown = pMotionEvent.getPointerId(0);
                f = xs[0];
                f2 = ys[0];
                queueEvent(new Runnable() {
                    public void run() {
                        Cocos2dxGLSurfaceView.this.mCocos2dxRenderer.handleActionDown(idDown, f, f2);
                    }
                });
                break;
            case Cocos2dxHandler.HANDLER_SHOW_DIALOG /*1*/:
                final int idUp = pMotionEvent.getPointerId(0);
                f = xs[0];
                f2 = ys[0];
                queueEvent(new Runnable() {
                    public void run() {
                        Cocos2dxGLSurfaceView.this.mCocos2dxRenderer.handleActionUp(idUp, f, f2);
                    }
                });
                break;
            case HANDLER_OPEN_IME_KEYBOARD /*2*/:
                if (!this.mMultipleTouchEnabled) {
                    for (i = 0; i < pointerNumber; i++) {
                        if (ids[i] == 0) {
                            final int[] idsMove = new int[]{0};
                            fArr = new float[]{xs[i]};
                            fArr2 = new float[]{ys[i]};
                            queueEvent(new Runnable() {
                                public void run() {
                                    Cocos2dxGLSurfaceView.this.mCocos2dxRenderer.handleActionMove(idsMove, fArr, fArr2);
                                }
                            });
                            break;
                        }
                    }
                    break;
                }
                fArr = xs;
                fArr2 = ys;
                queueEvent(new Runnable() {
                    public void run() {
                        Cocos2dxGLSurfaceView.this.mCocos2dxRenderer.handleActionMove(ids, fArr, fArr2);
                    }
                });
                break;
            case HANDLER_CLOSE_IME_KEYBOARD /*3*/:
                if (!this.mMultipleTouchEnabled) {
                    for (i = 0; i < pointerNumber; i++) {
                        if (ids[i] == 0) {
                            final int[] idsCancel = new int[]{0};
                            fArr = new float[]{xs[i]};
                            fArr2 = new float[]{ys[i]};
                            queueEvent(new Runnable() {
                                public void run() {
                                    Cocos2dxGLSurfaceView.this.mCocos2dxRenderer.handleActionCancel(idsCancel, fArr, fArr2);
                                }
                            });
                            break;
                        }
                    }
                    break;
                }
                fArr = xs;
                fArr2 = ys;
                queueEvent(new Runnable() {
                    public void run() {
                        Cocos2dxGLSurfaceView.this.mCocos2dxRenderer.handleActionCancel(ids, fArr, fArr2);
                    }
                });
                break;
            case R.styleable.Toolbar_contentInsetStart /*5*/:
                int indexPointerDown = pMotionEvent.getAction() >> 8;
                if (this.mMultipleTouchEnabled || indexPointerDown == 0) {
                    final int idPointerDown = pMotionEvent.getPointerId(indexPointerDown);
                    f = pMotionEvent.getX(indexPointerDown);
                    f2 = pMotionEvent.getY(indexPointerDown);
                    queueEvent(new Runnable() {
                        public void run() {
                            Cocos2dxGLSurfaceView.this.mCocos2dxRenderer.handleActionDown(idPointerDown, f, f2);
                        }
                    });
                    break;
                }
            case R.styleable.Toolbar_contentInsetEnd /*6*/:
                int indexPointUp = pMotionEvent.getAction() >> 8;
                if (this.mMultipleTouchEnabled || indexPointUp == 0) {
                    final int idPointerUp = pMotionEvent.getPointerId(indexPointUp);
                    f = pMotionEvent.getX(indexPointUp);
                    f2 = pMotionEvent.getY(indexPointUp);
                    queueEvent(new Runnable() {
                        public void run() {
                            Cocos2dxGLSurfaceView.this.mCocos2dxRenderer.handleActionUp(idPointerUp, f, f2);
                        }
                    });
                    break;
                }
        }
        return true;
    }

    protected void onSizeChanged(int pNewSurfaceWidth, int pNewSurfaceHeight, int pOldSurfaceWidth, int pOldSurfaceHeight) {
        if (!isInEditMode()) {
            this.mCocos2dxRenderer.setScreenWidthAndHeight(pNewSurfaceWidth, pNewSurfaceHeight);
        }
    }

    public boolean onKeyDown(final int pKeyCode, KeyEvent pKeyEvent) {
        switch (pKeyCode) {
            case R.styleable.View_theme /*4*/:
                Cocos2dxVideoHelper.mVideoHandler.sendEmptyMessage(GameControllerDelegate.THUMBSTICK_LEFT_X);
                break;
            case R.styleable.Toolbar_titleMargins /*19*/:
            case R.styleable.Toolbar_maxButtonHeight /*20*/:
            case R.styleable.Toolbar_buttonGravity /*21*/:
            case R.styleable.Toolbar_collapseIcon /*22*/:
            case R.styleable.Toolbar_collapseContentDescription /*23*/:
            case R.styleable.AppCompatTheme_textAppearanceSearchResultTitle /*66*/:
            case R.styleable.AppCompatTheme_listChoiceBackgroundIndicator /*82*/:
            case R.styleable.AppCompatTheme_colorAccent /*85*/:
                break;
            default:
                return super.onKeyDown(pKeyCode, pKeyEvent);
        }
        queueEvent(new Runnable() {
            public void run() {
                Cocos2dxGLSurfaceView.this.mCocos2dxRenderer.handleKeyDown(pKeyCode);
            }
        });
        return true;
    }

    public boolean onKeyUp(final int keyCode, KeyEvent event) {
        switch (keyCode) {
            case R.styleable.View_theme /*4*/:
            case R.styleable.Toolbar_titleMargins /*19*/:
            case R.styleable.Toolbar_maxButtonHeight /*20*/:
            case R.styleable.Toolbar_buttonGravity /*21*/:
            case R.styleable.Toolbar_collapseIcon /*22*/:
            case R.styleable.Toolbar_collapseContentDescription /*23*/:
            case R.styleable.AppCompatTheme_textAppearanceSearchResultTitle /*66*/:
            case R.styleable.AppCompatTheme_listChoiceBackgroundIndicator /*82*/:
            case R.styleable.AppCompatTheme_colorAccent /*85*/:
                queueEvent(new Runnable() {
                    public void run() {
                        Cocos2dxGLSurfaceView.this.mCocos2dxRenderer.handleKeyUp(keyCode);
                    }
                });
                return true;
            default:
                return super.onKeyUp(keyCode, event);
        }
    }

    public static void openIMEKeyboard() {
        Message msg = new Message();
        msg.what = HANDLER_OPEN_IME_KEYBOARD;
        msg.obj = mCocos2dxGLSurfaceView.getContentText();
        sHandler.sendMessage(msg);
    }

    public static void closeIMEKeyboard() {
        Message msg = new Message();
        msg.what = HANDLER_CLOSE_IME_KEYBOARD;
        sHandler.sendMessage(msg);
    }

    public void insertText(final String pText) {
        queueEvent(new Runnable() {
            public void run() {
                Cocos2dxGLSurfaceView.this.mCocos2dxRenderer.handleInsertText(pText);
            }
        });
    }

    public void deleteBackward() {
        queueEvent(new Runnable() {
            public void run() {
                Cocos2dxGLSurfaceView.this.mCocos2dxRenderer.handleDeleteBackward();
            }
        });
    }

    private static void dumpMotionEvent(MotionEvent event) {
        String[] names = new String[]{"DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE", "POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?"};
        StringBuilder sb = new StringBuilder();
        int action = event.getAction();
        int actionCode = action & 255;
        sb.append("event ACTION_").append(names[actionCode]);
        if (actionCode == 5 || actionCode == 6) {
            sb.append("(pid ").append(action >> 8);
            sb.append(")");
        }
        sb.append(RequestParameters.LEFT_BRACKETS);
        for (int i = 0; i < event.getPointerCount(); i++) {
            sb.append("#").append(i);
            sb.append("(pid ").append(event.getPointerId(i));
            sb.append(")=").append((int) event.getX(i));
            sb.append(",").append((int) event.getY(i));
            if (i + 1 < event.getPointerCount()) {
                sb.append(";");
            }
        }
        sb.append(RequestParameters.RIGHT_BRACKETS);
        Log.d(TAG, sb.toString());
    }
}
