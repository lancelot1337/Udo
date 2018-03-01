package io.branch.referral;

import com.google.firebase.analytics.FirebaseAnalytics.Param;
import com.ironsource.sdk.utils.Constants.ParametersKeys;
import cz.msebera.android.httpclient.HttpHost;

class ApkParser {
    public static int endDocTag = 1048833;
    public static int endTag = 1048835;
    public static int startTag = 1048834;

    ApkParser() {
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.String decompressXML(byte[] r18) {
        /*
        r17 = this;
        r15 = 16;
        r0 = r17;
        r1 = r18;
        r9 = r0.LEW(r1, r15);
        r11 = 36;
        r15 = r9 * 4;
        r12 = r11 + r15;
        r15 = 12;
        r0 = r17;
        r1 = r18;
        r14 = r0.LEW(r1, r15);
        r7 = r14;
    L_0x001b:
        r0 = r18;
        r15 = r0.length;
        r15 = r15 + -4;
        if (r7 >= r15) goto L_0x0031;
    L_0x0022:
        r0 = r17;
        r1 = r18;
        r15 = r0.LEW(r1, r7);
        r16 = startTag;
        r0 = r16;
        if (r15 != r0) goto L_0x0096;
    L_0x0030:
        r14 = r7;
    L_0x0031:
        r10 = r14;
    L_0x0032:
        r0 = r18;
        r15 = r0.length;
        if (r10 >= r15) goto L_0x00c0;
    L_0x0037:
        r0 = r17;
        r1 = r18;
        r13 = r0.LEW(r1, r10);
        r15 = startTag;
        if (r13 != r15) goto L_0x00b4;
    L_0x0043:
        r15 = r10 + 28;
        r0 = r17;
        r1 = r18;
        r8 = r0.LEW(r1, r15);
        r10 = r10 + 36;
        r7 = 0;
    L_0x0050:
        if (r7 >= r8) goto L_0x0032;
    L_0x0052:
        r15 = r10 + 4;
        r0 = r17;
        r1 = r18;
        r3 = r0.LEW(r1, r15);
        r15 = r10 + 8;
        r0 = r17;
        r1 = r18;
        r6 = r0.LEW(r1, r15);
        r15 = r10 + 16;
        r0 = r17;
        r1 = r18;
        r4 = r0.LEW(r1, r15);
        r10 = r10 + 20;
        r0 = r17;
        r1 = r18;
        r2 = r0.compXmlString(r1, r11, r12, r3);
        r15 = "scheme";
        r15 = r2.equals(r15);
        if (r15 == 0) goto L_0x00b1;
    L_0x0082:
        r15 = -1;
        if (r6 == r15) goto L_0x0099;
    L_0x0085:
        r0 = r17;
        r1 = r18;
        r5 = r0.compXmlString(r1, r11, r12, r6);
    L_0x008d:
        r0 = r17;
        r15 = r0.validURI(r5);
        if (r15 == 0) goto L_0x00b1;
    L_0x0095:
        return r5;
    L_0x0096:
        r7 = r7 + 4;
        goto L_0x001b;
    L_0x0099:
        r15 = new java.lang.StringBuilder;
        r15.<init>();
        r16 = "resourceID 0x";
        r15 = r15.append(r16);
        r16 = java.lang.Integer.toHexString(r4);
        r15 = r15.append(r16);
        r5 = r15.toString();
        goto L_0x008d;
    L_0x00b1:
        r7 = r7 + 1;
        goto L_0x0050;
    L_0x00b4:
        r15 = endTag;
        if (r13 != r15) goto L_0x00bc;
    L_0x00b8:
        r10 = r10 + 24;
        goto L_0x0032;
    L_0x00bc:
        r15 = endDocTag;
        if (r13 != r15) goto L_0x00c0;
    L_0x00c0:
        r5 = "bnc_no_value";
        goto L_0x0095;
        */
        throw new UnsupportedOperationException("Method not decompiled: io.branch.referral.ApkParser.decompressXML(byte[]):java.lang.String");
    }

    private boolean validURI(String value) {
        if (value == null || value.equals(HttpHost.DEFAULT_SCHEME_NAME) || value.equals("https") || value.equals("geo") || value.equals("*") || value.equals("package") || value.equals("sms") || value.equals("smsto") || value.equals("mms") || value.equals("mmsto") || value.equals("tel") || value.equals("voicemail") || value.equals(ParametersKeys.FILE) || value.equals(Param.CONTENT) || value.equals("mailto")) {
            return false;
        }
        return true;
    }

    public String compXmlString(byte[] xml, int sitOff, int stOff, int strInd) {
        if (strInd < 0) {
            return null;
        }
        return compXmlStringAt(xml, stOff + LEW(xml, (strInd * 4) + sitOff));
    }

    public String compXmlStringAt(byte[] arr, int strOff) {
        int strLen = ((arr[strOff + 1] << 8) & 65280) | (arr[strOff] & 255);
        byte[] chars = new byte[strLen];
        for (int ii = 0; ii < strLen; ii++) {
            chars[ii] = arr[(strOff + 2) + (ii * 2)];
        }
        return new String(chars);
    }

    public int LEW(byte[] arr, int off) {
        return ((((arr[off + 3] << 24) & -16777216) | ((arr[off + 2] << 16) & 16711680)) | ((arr[off + 1] << 8) & 65280)) | (arr[off] & 255);
    }
}
