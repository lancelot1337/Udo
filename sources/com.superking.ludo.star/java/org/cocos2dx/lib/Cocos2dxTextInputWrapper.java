package org.cocos2dx.lib;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class Cocos2dxTextInputWrapper implements TextWatcher, OnEditorActionListener {
    private static final String TAG = Cocos2dxTextInputWrapper.class.getSimpleName();
    private final Cocos2dxGLSurfaceView mCocos2dxGLSurfaceView;
    private String mOriginText;
    private String mText;

    public Cocos2dxTextInputWrapper(Cocos2dxGLSurfaceView pCocos2dxGLSurfaceView) {
        this.mCocos2dxGLSurfaceView = pCocos2dxGLSurfaceView;
    }

    private boolean isFullScreenEdit() {
        return ((InputMethodManager) this.mCocos2dxGLSurfaceView.getCocos2dxEditText().getContext().getSystemService("input_method")).isFullscreenMode();
    }

    public void setOriginText(String pOriginText) {
        this.mOriginText = pOriginText;
    }

    public void afterTextChanged(Editable s) {
        if (!isFullScreenEdit()) {
            int old_i = 0;
            int new_i = 0;
            while (old_i < this.mText.length() && new_i < s.length() && this.mText.charAt(old_i) == s.charAt(new_i)) {
                old_i++;
                new_i++;
            }
            while (old_i < this.mText.length()) {
                this.mCocos2dxGLSurfaceView.deleteBackward();
                old_i++;
            }
            if (s.length() - new_i > 0) {
                this.mCocos2dxGLSurfaceView.insertText(s.subSequence(new_i, s.length()).toString());
            }
            this.mText = s.toString();
        }
    }

    public void beforeTextChanged(CharSequence pCharSequence, int start, int count, int after) {
        this.mText = pCharSequence.toString();
    }

    public void onTextChanged(CharSequence pCharSequence, int start, int before, int count) {
    }

    public boolean onEditorAction(TextView pTextView, int pActionID, KeyEvent pKeyEvent) {
        if (this.mCocos2dxGLSurfaceView.getCocos2dxEditText() == pTextView && isFullScreenEdit()) {
            if (this.mOriginText != null) {
                for (int i = this.mOriginText.length(); i > 0; i--) {
                    this.mCocos2dxGLSurfaceView.deleteBackward();
                }
            }
            String text = pTextView.getText().toString();
            if (text != null) {
                if (text.compareTo(BuildConfig.FLAVOR) == 0) {
                    text = "\n";
                }
                if ('\n' != text.charAt(text.length() - 1)) {
                    text = text + '\n';
                }
            }
            this.mCocos2dxGLSurfaceView.insertText(text);
        }
        if (pActionID == 6) {
            this.mCocos2dxGLSurfaceView.requestFocus();
        }
        return false;
    }
}
