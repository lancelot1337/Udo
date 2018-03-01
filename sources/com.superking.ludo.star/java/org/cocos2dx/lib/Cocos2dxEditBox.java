package org.cocos2dx.lib;

import android.content.Context;
import android.graphics.Typeface;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.FrameLayout.LayoutParams;
import cz.msebera.android.httpclient.impl.client.cache.CacheConfig;
import io.branch.referral.R;

public class Cocos2dxEditBox extends EditText {
    private static final int getkTextVerticalAlignmentBottom = 2;
    public static final int kEndActionNext = 1;
    public static final int kEndActionReturn = 3;
    public static final int kEndActionUnknown = 0;
    private static final int kTextHorizontalAlignmentCenter = 1;
    private static final int kTextHorizontalAlignmentLeft = 0;
    private static final int kTextHorizontalAlignmentRight = 2;
    private static final int kTextVerticalAlignmentCenter = 1;
    private static final int kTextVerticalAlignmentTop = 0;
    int endAction = kTextHorizontalAlignmentLeft;
    private final int kEditBoxInputFlagInitialCapsAllCharacters = 4;
    private final int kEditBoxInputFlagInitialCapsSentence = kEndActionReturn;
    private final int kEditBoxInputFlagInitialCapsWord = kTextHorizontalAlignmentRight;
    private final int kEditBoxInputFlagLowercaseAllCharacters = 5;
    private final int kEditBoxInputFlagPassword = kTextHorizontalAlignmentLeft;
    private final int kEditBoxInputFlagSensitive = kTextVerticalAlignmentCenter;
    private final int kEditBoxInputModeAny = kTextHorizontalAlignmentLeft;
    private final int kEditBoxInputModeDecimal = 5;
    private final int kEditBoxInputModeEmailAddr = kTextVerticalAlignmentCenter;
    private final int kEditBoxInputModeNumeric = kTextHorizontalAlignmentRight;
    private final int kEditBoxInputModePhoneNumber = kEndActionReturn;
    private final int kEditBoxInputModeSingleLine = 6;
    private final int kEditBoxInputModeUrl = 4;
    private final int kKeyboardReturnTypeDefault = kTextHorizontalAlignmentLeft;
    private final int kKeyboardReturnTypeDone = kTextVerticalAlignmentCenter;
    private final int kKeyboardReturnTypeGo = 4;
    private final int kKeyboardReturnTypeNext = 5;
    private final int kKeyboardReturnTypeSearch = kEndActionReturn;
    private final int kKeyboardReturnTypeSend = kTextHorizontalAlignmentRight;
    private int mInputFlagConstraints;
    private int mInputModeConstraints;
    private int mMaxLength;
    private float mScaleX;

    public Cocos2dxEditBox(Context context) {
        super(context);
    }

    public void setEditBoxViewRect(int left, int top, int maxWidth, int maxHeight) {
        LayoutParams layoutParams = new LayoutParams(-2, -2);
        layoutParams.leftMargin = left;
        layoutParams.topMargin = top;
        layoutParams.width = maxWidth;
        layoutParams.height = maxHeight;
        layoutParams.gravity = 51;
        setLayoutParams(layoutParams);
    }

    public float getOpenGLViewScaleX() {
        return this.mScaleX;
    }

    public void setOpenGLViewScaleX(float mScaleX) {
        this.mScaleX = mScaleX;
    }

    public void setMaxLength(int maxLength) {
        this.mMaxLength = maxLength;
        InputFilter[] inputFilterArr = new InputFilter[kTextVerticalAlignmentCenter];
        inputFilterArr[kTextHorizontalAlignmentLeft] = new LengthFilter(this.mMaxLength);
        setFilters(inputFilterArr);
    }

    public void setMultilineEnabled(boolean flag) {
        this.mInputModeConstraints |= 131072;
    }

    public void setReturnType(int returnType) {
        switch (returnType) {
            case kTextHorizontalAlignmentLeft /*0*/:
                setImeOptions(268435457);
                return;
            case kTextVerticalAlignmentCenter /*1*/:
                setImeOptions(268435462);
                return;
            case kTextHorizontalAlignmentRight /*2*/:
                setImeOptions(268435460);
                return;
            case kEndActionReturn /*3*/:
                setImeOptions(268435459);
                return;
            case R.styleable.View_theme /*4*/:
                setImeOptions(268435458);
                return;
            case R.styleable.Toolbar_contentInsetStart /*5*/:
                setImeOptions(268435461);
                return;
            default:
                setImeOptions(268435457);
                return;
        }
    }

    public void setTextHorizontalAlignment(int alignment) {
        int gravity = getGravity();
        switch (alignment) {
            case kTextHorizontalAlignmentLeft /*0*/:
                gravity |= kEndActionReturn;
                break;
            case kTextVerticalAlignmentCenter /*1*/:
                gravity |= 17;
                break;
            case kTextHorizontalAlignmentRight /*2*/:
                gravity |= 5;
                break;
            default:
                gravity |= kEndActionReturn;
                break;
        }
        setGravity(gravity);
    }

    public void setTextVerticalAlignment(int alignment) {
        int gravity = getGravity();
        switch (alignment) {
            case kTextHorizontalAlignmentLeft /*0*/:
                gravity |= 48;
                break;
            case kTextVerticalAlignmentCenter /*1*/:
                gravity |= 16;
                break;
            case kTextHorizontalAlignmentRight /*2*/:
                gravity |= 80;
                break;
            default:
                gravity |= 16;
                break;
        }
        setGravity(gravity);
    }

    public void setInputMode(int inputMode) {
        setTextHorizontalAlignment(kTextHorizontalAlignmentLeft);
        setTextVerticalAlignment(kTextVerticalAlignmentCenter);
        switch (inputMode) {
            case kTextHorizontalAlignmentLeft /*0*/:
                setTextVerticalAlignment(kTextHorizontalAlignmentLeft);
                this.mInputModeConstraints = 131073;
                break;
            case kTextVerticalAlignmentCenter /*1*/:
                this.mInputModeConstraints = 33;
                break;
            case kTextHorizontalAlignmentRight /*2*/:
                this.mInputModeConstraints = 4098;
                break;
            case kEndActionReturn /*3*/:
                this.mInputModeConstraints = kEndActionReturn;
                break;
            case R.styleable.View_theme /*4*/:
                this.mInputModeConstraints = 17;
                break;
            case R.styleable.Toolbar_contentInsetStart /*5*/:
                this.mInputModeConstraints = 12290;
                break;
            case R.styleable.Toolbar_contentInsetEnd /*6*/:
                this.mInputModeConstraints = kTextVerticalAlignmentCenter;
                break;
        }
        setInputType(this.mInputModeConstraints | this.mInputFlagConstraints);
    }

    public boolean onKeyDown(int pKeyCode, KeyEvent pKeyEvent) {
        switch (pKeyCode) {
            case R.styleable.View_theme /*4*/:
                ((Cocos2dxActivity) getContext()).getGLSurfaceView().requestFocus();
                return true;
            default:
                return super.onKeyDown(pKeyCode, pKeyEvent);
        }
    }

    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        return super.onKeyPreIme(keyCode, event);
    }

    public void setInputFlag(int inputFlag) {
        switch (inputFlag) {
            case kTextHorizontalAlignmentLeft /*0*/:
                this.mInputFlagConstraints = 129;
                setTypeface(Typeface.DEFAULT);
                setTransformationMethod(new PasswordTransformationMethod());
                break;
            case kTextVerticalAlignmentCenter /*1*/:
                this.mInputFlagConstraints = 524288;
                break;
            case kTextHorizontalAlignmentRight /*2*/:
                this.mInputFlagConstraints = CacheConfig.DEFAULT_MAX_OBJECT_SIZE_BYTES;
                break;
            case kEndActionReturn /*3*/:
                this.mInputFlagConstraints = 16384;
                break;
            case R.styleable.View_theme /*4*/:
                this.mInputFlagConstraints = 4096;
                break;
            case R.styleable.Toolbar_contentInsetStart /*5*/:
                this.mInputFlagConstraints = kTextVerticalAlignmentCenter;
                break;
        }
        setInputType(this.mInputFlagConstraints | this.mInputModeConstraints);
    }
}
