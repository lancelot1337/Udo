package org.cocos2dx.lib;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout.LayoutParams;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class Cocos2dxEditBoxHelper {
    private static final String TAG = Cocos2dxEditBoxHelper.class.getSimpleName();
    private static Cocos2dxActivity mCocos2dxActivity;
    private static SparseArray<Cocos2dxEditBox> mEditBoxArray;
    private static ResizeLayout mFrameLayout;
    private static int mViewTag = 0;

    private static native void editBoxEditingChanged(int i, String str);

    private static native void editBoxEditingDidBegin(int i);

    private static native void editBoxEditingDidEnd(int i, String str, int i2);

    public static void __editBoxEditingDidBegin(int index) {
        editBoxEditingDidBegin(index);
    }

    public static void __editBoxEditingChanged(int index, String text) {
        editBoxEditingChanged(index, text);
    }

    public static void __editBoxEditingDidEnd(int index, String text, int action) {
        editBoxEditingDidEnd(index, text, action);
    }

    public Cocos2dxEditBoxHelper(ResizeLayout layout) {
        mFrameLayout = layout;
        mCocos2dxActivity = (Cocos2dxActivity) Cocos2dxActivity.getContext();
        mEditBoxArray = new SparseArray();
    }

    public static int convertToSP(float point) {
        return (int) TypedValue.applyDimension(2, point, mCocos2dxActivity.getResources().getDisplayMetrics());
    }

    public static int createEditBox(int left, int top, int width, int height, float scaleX) {
        final int index = mViewTag;
        final float f = scaleX;
        final int i = height;
        final int i2 = left;
        final int i3 = top;
        final int i4 = width;
        mCocos2dxActivity.runOnUiThread(new Runnable() {
            public void run() {
                final Cocos2dxEditBox editBox = new Cocos2dxEditBox(Cocos2dxEditBoxHelper.mCocos2dxActivity);
                editBox.setFocusable(true);
                editBox.setFocusableInTouchMode(true);
                editBox.setInputFlag(5);
                editBox.setInputMode(6);
                editBox.setReturnType(0);
                editBox.setHintTextColor(-7829368);
                editBox.setVisibility(4);
                editBox.setBackgroundColor(0);
                editBox.setTextColor(-1);
                editBox.setSingleLine();
                editBox.setOpenGLViewScaleX(f);
                float density = Cocos2dxEditBoxHelper.mCocos2dxActivity.getResources().getDisplayMetrics().density;
                int paddingBottom = Cocos2dxEditBoxHelper.convertToSP(((float) ((int) ((((float) i) * 0.33f) / density))) - ((f * 5.0f) / density)) / 2;
                editBox.setPadding(Cocos2dxEditBoxHelper.convertToSP((float) ((int) ((f * 5.0f) / density))), paddingBottom, 0, paddingBottom);
                LayoutParams lParams = new LayoutParams(-2, -2);
                lParams.leftMargin = i2;
                lParams.topMargin = i3;
                lParams.width = i4;
                lParams.height = i;
                lParams.gravity = 51;
                Cocos2dxEditBoxHelper.mFrameLayout.addView(editBox, lParams);
                editBox.addTextChangedListener(new TextWatcher() {
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        final String text = new String(s.toString());
                        Cocos2dxEditBoxHelper.mCocos2dxActivity.runOnGLThread(new Runnable() {
                            public void run() {
                                Cocos2dxEditBoxHelper.__editBoxEditingChanged(index, text);
                            }
                        });
                    }

                    public void afterTextChanged(Editable s) {
                    }
                });
                editBox.setOnFocusChangeListener(new OnFocusChangeListener() {
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            Cocos2dxEditBoxHelper.mCocos2dxActivity.runOnGLThread(new Runnable() {
                                public void run() {
                                    editBox.endAction = 0;
                                    Cocos2dxEditBoxHelper.__editBoxEditingDidBegin(index);
                                }
                            });
                            editBox.setSelection(editBox.getText().length());
                            Cocos2dxEditBoxHelper.mFrameLayout.setEnableForceDoLayout(true);
                            Cocos2dxEditBoxHelper.mCocos2dxActivity.getGLSurfaceView().setSoftKeyboardShown(true);
                            Log.d(Cocos2dxEditBoxHelper.TAG, "edit box get focus");
                            return;
                        }
                        editBox.setVisibility(8);
                        final String text = new String(editBox.getText().toString());
                        Cocos2dxEditBoxHelper.mCocos2dxActivity.runOnGLThread(new Runnable() {
                            public void run() {
                                Cocos2dxEditBoxHelper.__editBoxEditingDidEnd(index, text, editBox.endAction);
                            }
                        });
                        Cocos2dxEditBoxHelper.mCocos2dxActivity.hideVirtualButton();
                        Cocos2dxEditBoxHelper.mFrameLayout.setEnableForceDoLayout(false);
                        Log.d(Cocos2dxEditBoxHelper.TAG, "edit box lose focus");
                    }
                });
                editBox.setOnKeyListener(new OnKeyListener() {
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        if (event.getAction() != 0 || keyCode != 66 || (editBox.getInputType() & 131072) == 131072) {
                            return false;
                        }
                        Cocos2dxEditBoxHelper.closeKeyboardOnUiThread(index);
                        return true;
                    }
                });
                editBox.setOnEditorActionListener(new OnEditorActionListener() {
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == 5) {
                            editBox.endAction = 1;
                            Cocos2dxEditBoxHelper.closeKeyboardOnUiThread(index);
                            return true;
                        }
                        if (actionId == 6) {
                            Cocos2dxEditBoxHelper.closeKeyboardOnUiThread(index);
                        }
                        return false;
                    }
                });
                Cocos2dxEditBoxHelper.mEditBoxArray.put(index, editBox);
            }
        });
        int i5 = mViewTag;
        mViewTag = i5 + 1;
        return i5;
    }

    public static void removeEditBox(final int index) {
        mCocos2dxActivity.runOnUiThread(new Runnable() {
            public void run() {
                Cocos2dxEditBox editBox = (Cocos2dxEditBox) Cocos2dxEditBoxHelper.mEditBoxArray.get(index);
                if (editBox != null) {
                    Cocos2dxEditBoxHelper.mEditBoxArray.remove(index);
                    Cocos2dxEditBoxHelper.mFrameLayout.removeView(editBox);
                    Log.e(Cocos2dxEditBoxHelper.TAG, "remove EditBox");
                }
            }
        });
    }

    public static void setFont(final int index, final String fontName, final float fontSize) {
        mCocos2dxActivity.runOnUiThread(new Runnable() {
            public void run() {
                Cocos2dxEditBox editBox = (Cocos2dxEditBox) Cocos2dxEditBoxHelper.mEditBoxArray.get(index);
                if (editBox != null) {
                    Typeface tf;
                    if (fontName.isEmpty()) {
                        tf = Typeface.DEFAULT;
                    } else if (fontName.endsWith(".ttf")) {
                        try {
                            Cocos2dxEditBoxHelper.mCocos2dxActivity;
                            tf = Cocos2dxTypefaces.get(Cocos2dxActivity.getContext(), fontName);
                        } catch (Exception e) {
                            Log.e("Cocos2dxEditBoxHelper", "error to create ttf type face: " + fontName);
                            tf = Typeface.create(fontName, 0);
                        }
                    } else {
                        tf = Typeface.create(fontName, 0);
                    }
                    if (fontSize >= 0.0f) {
                        editBox.setTextSize(2, fontSize / Cocos2dxEditBoxHelper.mCocos2dxActivity.getResources().getDisplayMetrics().density);
                    }
                    editBox.setTypeface(tf);
                }
            }
        });
    }

    public static void setFontColor(int index, int red, int green, int blue, int alpha) {
        final int i = index;
        final int i2 = alpha;
        final int i3 = red;
        final int i4 = green;
        final int i5 = blue;
        mCocos2dxActivity.runOnUiThread(new Runnable() {
            public void run() {
                Cocos2dxEditBox editBox = (Cocos2dxEditBox) Cocos2dxEditBoxHelper.mEditBoxArray.get(i);
                if (editBox != null) {
                    editBox.setTextColor(Color.argb(i2, i3, i4, i5));
                }
            }
        });
    }

    public static void setPlaceHolderText(final int index, final String text) {
        mCocos2dxActivity.runOnUiThread(new Runnable() {
            public void run() {
                Cocos2dxEditBox editBox = (Cocos2dxEditBox) Cocos2dxEditBoxHelper.mEditBoxArray.get(index);
                if (editBox != null) {
                    editBox.setHint(text);
                }
            }
        });
    }

    public static void setPlaceHolderTextColor(int index, int red, int green, int blue, int alpha) {
        final int i = index;
        final int i2 = alpha;
        final int i3 = red;
        final int i4 = green;
        final int i5 = blue;
        mCocos2dxActivity.runOnUiThread(new Runnable() {
            public void run() {
                Cocos2dxEditBox editBox = (Cocos2dxEditBox) Cocos2dxEditBoxHelper.mEditBoxArray.get(i);
                if (editBox != null) {
                    editBox.setHintTextColor(Color.argb(i2, i3, i4, i5));
                }
            }
        });
    }

    public static void setMaxLength(final int index, final int maxLength) {
        mCocos2dxActivity.runOnUiThread(new Runnable() {
            public void run() {
                Cocos2dxEditBox editBox = (Cocos2dxEditBox) Cocos2dxEditBoxHelper.mEditBoxArray.get(index);
                if (editBox != null) {
                    editBox.setMaxLength(maxLength);
                }
            }
        });
    }

    public static void setVisible(final int index, final boolean visible) {
        mCocos2dxActivity.runOnUiThread(new Runnable() {
            public void run() {
                Cocos2dxEditBox editBox = (Cocos2dxEditBox) Cocos2dxEditBoxHelper.mEditBoxArray.get(index);
                if (editBox != null) {
                    editBox.setVisibility(visible ? 0 : 8);
                }
            }
        });
    }

    public static void setText(final int index, final String text) {
        mCocos2dxActivity.runOnUiThread(new Runnable() {
            public void run() {
                Cocos2dxEditBox editBox = (Cocos2dxEditBox) Cocos2dxEditBoxHelper.mEditBoxArray.get(index);
                if (editBox != null) {
                    editBox.setText(text);
                }
            }
        });
    }

    public static void setReturnType(final int index, final int returnType) {
        mCocos2dxActivity.runOnUiThread(new Runnable() {
            public void run() {
                Cocos2dxEditBox editBox = (Cocos2dxEditBox) Cocos2dxEditBoxHelper.mEditBoxArray.get(index);
                if (editBox != null) {
                    editBox.setReturnType(returnType);
                }
            }
        });
    }

    public static void setTextHorizontalAlignment(final int index, final int alignment) {
        mCocos2dxActivity.runOnUiThread(new Runnable() {
            public void run() {
                Cocos2dxEditBox editBox = (Cocos2dxEditBox) Cocos2dxEditBoxHelper.mEditBoxArray.get(index);
                if (editBox != null) {
                    editBox.setTextHorizontalAlignment(alignment);
                }
            }
        });
    }

    public static void setInputMode(final int index, final int inputMode) {
        mCocos2dxActivity.runOnUiThread(new Runnable() {
            public void run() {
                Cocos2dxEditBox editBox = (Cocos2dxEditBox) Cocos2dxEditBoxHelper.mEditBoxArray.get(index);
                if (editBox != null) {
                    editBox.setInputMode(inputMode);
                }
            }
        });
    }

    public static void setInputFlag(final int index, final int inputFlag) {
        mCocos2dxActivity.runOnUiThread(new Runnable() {
            public void run() {
                Cocos2dxEditBox editBox = (Cocos2dxEditBox) Cocos2dxEditBoxHelper.mEditBoxArray.get(index);
                if (editBox != null) {
                    editBox.setInputFlag(inputFlag);
                }
            }
        });
    }

    public static void setEditBoxViewRect(int index, int left, int top, int maxWidth, int maxHeight) {
        final int i = index;
        final int i2 = left;
        final int i3 = top;
        final int i4 = maxWidth;
        final int i5 = maxHeight;
        mCocos2dxActivity.runOnUiThread(new Runnable() {
            public void run() {
                Cocos2dxEditBox editBox = (Cocos2dxEditBox) Cocos2dxEditBoxHelper.mEditBoxArray.get(i);
                if (editBox != null) {
                    editBox.setEditBoxViewRect(i2, i3, i4, i5);
                }
            }
        });
    }

    public static void openKeyboard(final int index) {
        mCocos2dxActivity.runOnUiThread(new Runnable() {
            public void run() {
                Cocos2dxEditBoxHelper.openKeyboardOnUiThread(index);
            }
        });
    }

    private static void openKeyboardOnUiThread(int index) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            Log.e(TAG, "openKeyboardOnUiThread doesn't run on UI thread!");
            return;
        }
        Cocos2dxActivity cocos2dxActivity = mCocos2dxActivity;
        InputMethodManager imm = (InputMethodManager) Cocos2dxActivity.getContext().getSystemService("input_method");
        Cocos2dxEditBox editBox = (Cocos2dxEditBox) mEditBoxArray.get(index);
        if (editBox != null) {
            editBox.requestFocus();
            imm.showSoftInput(editBox, 0);
            mCocos2dxActivity.getGLSurfaceView().setSoftKeyboardShown(true);
        }
    }

    private static void closeKeyboardOnUiThread(int index) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            Log.e(TAG, "closeKeyboardOnUiThread doesn't run on UI thread!");
            return;
        }
        Cocos2dxActivity cocos2dxActivity = mCocos2dxActivity;
        InputMethodManager imm = (InputMethodManager) Cocos2dxActivity.getContext().getSystemService("input_method");
        Cocos2dxEditBox editBox = (Cocos2dxEditBox) mEditBoxArray.get(index);
        if (editBox != null) {
            imm.hideSoftInputFromWindow(editBox.getWindowToken(), 0);
            mCocos2dxActivity.getGLSurfaceView().setSoftKeyboardShown(false);
            mCocos2dxActivity.getGLSurfaceView().requestFocus();
            mCocos2dxActivity.hideVirtualButton();
        }
    }

    public static void closeKeyboard(final int index) {
        mCocos2dxActivity.runOnUiThread(new Runnable() {
            public void run() {
                Cocos2dxEditBoxHelper.closeKeyboardOnUiThread(index);
            }
        });
    }
}
