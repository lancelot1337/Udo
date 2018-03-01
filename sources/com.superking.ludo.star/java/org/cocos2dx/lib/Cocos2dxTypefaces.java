package org.cocos2dx.lib;

import android.content.Context;
import android.graphics.Typeface;
import java.util.HashMap;

public class Cocos2dxTypefaces {
    private static final HashMap<String, Typeface> sTypefaceCache = new HashMap();

    public static synchronized Typeface get(Context context, String assetName) {
        Typeface typeface;
        synchronized (Cocos2dxTypefaces.class) {
            if (!sTypefaceCache.containsKey(assetName)) {
                Typeface typeface2;
                if (assetName.startsWith("/")) {
                    typeface2 = Typeface.createFromFile(assetName);
                } else {
                    typeface2 = Typeface.createFromAsset(context.getAssets(), assetName);
                }
                sTypefaceCache.put(assetName, typeface2);
            }
            typeface = (Typeface) sTypefaceCache.get(assetName);
        }
        return typeface;
    }
}
