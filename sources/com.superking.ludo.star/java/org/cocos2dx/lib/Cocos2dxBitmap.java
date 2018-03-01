package org.cocos2dx.lib;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class Cocos2dxBitmap {
    private static final int HORIZONTAL_ALIGN_CENTER = 3;
    private static final int HORIZONTAL_ALIGN_LEFT = 1;
    private static final int HORIZONTAL_ALIGN_RIGHT = 2;
    private static final int VERTICAL_ALIGN_BOTTOM = 2;
    private static final int VERTICAL_ALIGN_CENTER = 3;
    private static final int VERTICAL_ALIGN_TOP = 1;
    private static Context sContext;

    private static native void nativeInitBitmapDC(int i, int i2, byte[] bArr);

    public static void setContext(Context context) {
        sContext = context;
    }

    public static int getTextHeight(String text, int maxWidth, float textSize, Typeface typeface) {
        TextPaint paint = new TextPaint(129);
        paint.setTextSize(textSize);
        paint.setTypeface(typeface);
        int lineCount = 0;
        int index = 0;
        int length = text.length();
        while (index < length) {
            index += paint.breakText(text, index, length, true, (float) maxWidth, null);
            lineCount += VERTICAL_ALIGN_TOP;
        }
        return (int) Math.floor((double) (((float) lineCount) * (Math.abs(paint.ascent()) + Math.abs(paint.descent()))));
    }

    public static Typeface calculateShrinkTypeFace(String text, int width, int height, Alignment hAlignment, float textSize, TextPaint paint, boolean enableWrap) {
        if (width == 0 || height == 0) {
            return paint.getTypeface();
        }
        float actualWidth = (float) (width + VERTICAL_ALIGN_TOP);
        float actualHeight = (float) (height + VERTICAL_ALIGN_TOP);
        float fontSize = textSize + 1.0f;
        if (enableWrap) {
            do {
                if (actualHeight <= ((float) height) && actualWidth <= ((float) width)) {
                    break;
                }
                fontSize -= 1.0f;
                Layout layout = new StaticLayout(text, paint, width, hAlignment, 1.0f, 0.0f, false);
                actualWidth = (float) layout.getWidth();
                actualHeight = (float) layout.getLineTop(layout.getLineCount());
                paint.setTextSize(fontSize);
            } while (fontSize > 0.0f);
            paint.setTextSize(textSize);
        } else {
            do {
                if (actualWidth <= ((float) width) && actualHeight <= ((float) height)) {
                    break;
                }
                fontSize -= 1.0f;
                actualWidth = (float) ((int) Math.ceil((double) StaticLayout.getDesiredWidth(text, paint)));
                actualHeight = (float) getTextHeight(text, (int) actualWidth, fontSize, paint.getTypeface());
                paint.setTextSize(fontSize);
            } while (fontSize > 0.0f);
            paint.setTextSize(textSize);
        }
        return paint.getTypeface();
    }

    public static boolean createTextBitmapShadowStroke(byte[] bytes, String fontName, int fontSize, int fontTintR, int fontTintG, int fontTintB, int fontTintA, int alignment, int width, int height, boolean shadow, float shadowDX, float shadowDY, float shadowBlur, float shadowOpacity, boolean stroke, int strokeR, int strokeG, int strokeB, int strokeA, float strokeSize, boolean enableWrap, int overflow) {
        if (bytes == null || bytes.length == 0) {
            return false;
        }
        Layout layout;
        String string = new String(bytes);
        Alignment hAlignment = Alignment.ALIGN_NORMAL;
        int horizontalAlignment = alignment & 15;
        switch (horizontalAlignment) {
            case VERTICAL_ALIGN_BOTTOM /*2*/:
                hAlignment = Alignment.ALIGN_OPPOSITE;
                break;
            case VERTICAL_ALIGN_CENTER /*3*/:
                hAlignment = Alignment.ALIGN_CENTER;
                break;
        }
        TextPaint paint = newPaint(fontName, fontSize);
        if (stroke) {
            paint.setStyle(Style.STROKE);
            paint.setStrokeWidth(strokeSize);
        }
        int maxWidth = width;
        if (maxWidth <= 0) {
            maxWidth = (int) Math.ceil((double) StaticLayout.getDesiredWidth(string, paint));
        }
        if (overflow != VERTICAL_ALIGN_TOP || enableWrap) {
            if (overflow == VERTICAL_ALIGN_BOTTOM) {
                calculateShrinkTypeFace(string, width, height, hAlignment, (float) fontSize, paint, enableWrap);
            }
            Layout staticLayout = new StaticLayout(string, paint, maxWidth, hAlignment, 1.0f, 0.0f, false);
        } else {
            layout = new StaticLayout(string, paint, (int) Math.ceil((double) StaticLayout.getDesiredWidth(string, paint)), hAlignment, 1.0f, 0.0f, false);
        }
        int layoutWidth = layout.getWidth();
        int layoutHeight = layout.getLineTop(layout.getLineCount());
        int bitmapWidth = Math.max(layoutWidth, width);
        int bitmapHeight = layoutHeight;
        if (height > 0) {
            bitmapHeight = height;
        }
        if (overflow == VERTICAL_ALIGN_TOP && !enableWrap && width > 0) {
            bitmapWidth = width;
        }
        if (bitmapWidth == 0 || bitmapHeight == 0) {
            return false;
        }
        int offsetX = 0;
        if (horizontalAlignment == VERTICAL_ALIGN_CENTER) {
            offsetX = (bitmapWidth - layoutWidth) / VERTICAL_ALIGN_BOTTOM;
        } else if (horizontalAlignment == VERTICAL_ALIGN_BOTTOM) {
            offsetX = bitmapWidth - layoutWidth;
        }
        int offsetY = 0;
        switch ((alignment >> 4) & 15) {
            case VERTICAL_ALIGN_BOTTOM /*2*/:
                offsetY = bitmapHeight - layoutHeight;
                break;
            case VERTICAL_ALIGN_CENTER /*3*/:
                offsetY = (bitmapHeight - layoutHeight) / VERTICAL_ALIGN_BOTTOM;
                break;
        }
        Bitmap bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.translate((float) offsetX, (float) offsetY);
        if (stroke) {
            paint.setARGB(strokeA, strokeR, strokeG, strokeB);
            layout.draw(canvas);
        }
        paint.setStyle(Style.FILL);
        paint.setARGB(fontTintA, fontTintR, fontTintG, fontTintB);
        layout.draw(canvas);
        initNativeObject(bitmap);
        return true;
    }

    private static TextPaint newPaint(String fontName, int fontSize) {
        TextPaint paint = new TextPaint();
        paint.setTextSize((float) fontSize);
        paint.setAntiAlias(true);
        if (fontName.endsWith(".ttf")) {
            try {
                paint.setTypeface(Cocos2dxTypefaces.get(sContext, fontName));
            } catch (Exception e) {
                Log.e("Cocos2dxBitmap", "error to create ttf type face: " + fontName);
                paint.setTypeface(Typeface.create(fontName, 0));
            }
        } else {
            paint.setTypeface(Typeface.create(fontName, 0));
        }
        return paint;
    }

    private static void initNativeObject(Bitmap bitmap) {
        byte[] pixels = getPixels(bitmap);
        if (pixels != null) {
            nativeInitBitmapDC(bitmap.getWidth(), bitmap.getHeight(), pixels);
        }
    }

    private static byte[] getPixels(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        byte[] pixels = new byte[((bitmap.getWidth() * bitmap.getHeight()) * 4)];
        ByteBuffer buf = ByteBuffer.wrap(pixels);
        buf.order(ByteOrder.nativeOrder());
        bitmap.copyPixelsToBuffer(buf);
        return pixels;
    }

    public static int getFontSizeAccordingHeight(int height) {
        TextPaint paint = new TextPaint();
        Rect bounds = new Rect();
        paint.setTypeface(Typeface.DEFAULT);
        int text_size = VERTICAL_ALIGN_TOP;
        boolean found_desired_size = false;
        while (!found_desired_size) {
            paint.setTextSize((float) text_size);
            String text = "SghMNy";
            paint.getTextBounds(text, 0, text.length(), bounds);
            text_size += VERTICAL_ALIGN_TOP;
            if (height - bounds.height() <= VERTICAL_ALIGN_BOTTOM) {
                found_desired_size = true;
            }
        }
        return text_size;
    }

    private static String getStringWithEllipsis(String string, float width, float fontSize) {
        if (TextUtils.isEmpty(string)) {
            return BuildConfig.FLAVOR;
        }
        TextPaint paint = new TextPaint();
        paint.setTypeface(Typeface.DEFAULT);
        paint.setTextSize(fontSize);
        return TextUtils.ellipsize(string, paint, width, TruncateAt.END).toString();
    }
}
