package cz.msebera.android.httpclient.conn.ssl;

import cz.msebera.android.httpclient.message.TokenParser;
import io.branch.referral.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.security.auth.x500.X500Principal;
import org.cocos2dx.lib.BuildConfig;

public final class DistinguishedNameParser {
    private int beg;
    private char[] chars;
    private int cur;
    private final String dn;
    private int end;
    private final int length = this.dn.length();
    private int pos;

    public DistinguishedNameParser(X500Principal principal) {
        this.dn = principal.getName("RFC2253");
    }

    private String nextAT() {
        while (this.pos < this.length && this.chars[this.pos] == TokenParser.SP) {
            this.pos++;
        }
        if (this.pos == this.length) {
            return null;
        }
        this.beg = this.pos;
        this.pos++;
        while (this.pos < this.length && this.chars[this.pos] != '=' && this.chars[this.pos] != TokenParser.SP) {
            this.pos++;
        }
        if (this.pos >= this.length) {
            throw new IllegalStateException("Unexpected end of DN: " + this.dn);
        }
        this.end = this.pos;
        if (this.chars[this.pos] == TokenParser.SP) {
            while (this.pos < this.length && this.chars[this.pos] != '=' && this.chars[this.pos] == TokenParser.SP) {
                this.pos++;
            }
            if (this.chars[this.pos] != '=' || this.pos == this.length) {
                throw new IllegalStateException("Unexpected end of DN: " + this.dn);
            }
        }
        this.pos++;
        while (this.pos < this.length && this.chars[this.pos] == TokenParser.SP) {
            this.pos++;
        }
        if (this.end - this.beg > 4 && this.chars[this.beg + 3] == '.' && ((this.chars[this.beg] == 'O' || this.chars[this.beg] == 'o') && ((this.chars[this.beg + 1] == 'I' || this.chars[this.beg + 1] == 'i') && (this.chars[this.beg + 2] == 'D' || this.chars[this.beg + 2] == 'd')))) {
            this.beg += 4;
        }
        return new String(this.chars, this.beg, this.end - this.beg);
    }

    private String quotedAV() {
        this.pos++;
        this.beg = this.pos;
        this.end = this.beg;
        while (this.pos != this.length) {
            if (this.chars[this.pos] == TokenParser.DQUOTE) {
                this.pos++;
                while (this.pos < this.length && this.chars[this.pos] == TokenParser.SP) {
                    this.pos++;
                }
                return new String(this.chars, this.beg, this.end - this.beg);
            }
            if (this.chars[this.pos] == TokenParser.ESCAPE) {
                this.chars[this.end] = getEscaped();
            } else {
                this.chars[this.end] = this.chars[this.pos];
            }
            this.pos++;
            this.end++;
        }
        throw new IllegalStateException("Unexpected end of DN: " + this.dn);
    }

    private String hexAV() {
        if (this.pos + 4 >= this.length) {
            throw new IllegalStateException("Unexpected end of DN: " + this.dn);
        }
        int hexLen;
        this.beg = this.pos;
        this.pos++;
        while (this.pos != this.length && this.chars[this.pos] != '+' && this.chars[this.pos] != ',' && this.chars[this.pos] != ';') {
            if (this.chars[this.pos] == TokenParser.SP) {
                this.end = this.pos;
                this.pos++;
                while (this.pos < this.length && this.chars[this.pos] == TokenParser.SP) {
                    this.pos++;
                }
                hexLen = this.end - this.beg;
                if (hexLen >= 5 || (hexLen & 1) == 0) {
                    throw new IllegalStateException("Unexpected end of DN: " + this.dn);
                }
                byte[] encoded = new byte[(hexLen / 2)];
                int p = this.beg + 1;
                for (int i = 0; i < encoded.length; i++) {
                    encoded[i] = (byte) getByte(p);
                    p += 2;
                }
                return new String(this.chars, this.beg, hexLen);
            }
            if (this.chars[this.pos] >= 'A' && this.chars[this.pos] <= 'F') {
                char[] cArr = this.chars;
                int i2 = this.pos;
                cArr[i2] = (char) (cArr[i2] + 32);
            }
            this.pos++;
        }
        this.end = this.pos;
        hexLen = this.end - this.beg;
        if (hexLen >= 5) {
        }
        throw new IllegalStateException("Unexpected end of DN: " + this.dn);
    }

    private String escapedAV() {
        this.beg = this.pos;
        this.end = this.pos;
        while (this.pos < this.length) {
            char[] cArr;
            int i;
            switch (this.chars[this.pos]) {
                case R.styleable.AppCompatTheme_actionModeCutDrawable /*32*/:
                    this.cur = this.end;
                    this.pos++;
                    cArr = this.chars;
                    i = this.end;
                    this.end = i + 1;
                    cArr[i] = TokenParser.SP;
                    while (this.pos < this.length && this.chars[this.pos] == TokenParser.SP) {
                        cArr = this.chars;
                        i = this.end;
                        this.end = i + 1;
                        cArr[i] = TokenParser.SP;
                        this.pos++;
                    }
                    if (this.pos != this.length && this.chars[this.pos] != ',' && this.chars[this.pos] != '+' && this.chars[this.pos] != ';') {
                        break;
                    }
                    return new String(this.chars, this.beg, this.cur - this.beg);
                case R.styleable.AppCompatTheme_dialogTheme /*43*/:
                case R.styleable.AppCompatTheme_dialogPreferredPadding /*44*/:
                case R.styleable.AppCompatTheme_toolbarStyle /*59*/:
                    return new String(this.chars, this.beg, this.end - this.beg);
                case R.styleable.AppCompatTheme_colorBackgroundFloating /*92*/:
                    cArr = this.chars;
                    i = this.end;
                    this.end = i + 1;
                    cArr[i] = getEscaped();
                    this.pos++;
                    break;
                default:
                    cArr = this.chars;
                    i = this.end;
                    this.end = i + 1;
                    cArr[i] = this.chars[this.pos];
                    this.pos++;
                    break;
            }
        }
        return new String(this.chars, this.beg, this.end - this.beg);
    }

    private char getEscaped() {
        this.pos++;
        if (this.pos == this.length) {
            throw new IllegalStateException("Unexpected end of DN: " + this.dn);
        }
        switch (this.chars[this.pos]) {
            case R.styleable.AppCompatTheme_actionModeCutDrawable /*32*/:
            case R.styleable.AppCompatTheme_actionModePasteDrawable /*34*/:
            case R.styleable.AppCompatTheme_actionModeSelectAllDrawable /*35*/:
            case R.styleable.AppCompatTheme_actionModeFindDrawable /*37*/:
            case R.styleable.AppCompatTheme_textAppearancePopupMenuHeader /*42*/:
            case R.styleable.AppCompatTheme_dialogTheme /*43*/:
            case R.styleable.AppCompatTheme_dialogPreferredPadding /*44*/:
            case R.styleable.AppCompatTheme_toolbarStyle /*59*/:
            case R.styleable.AppCompatTheme_toolbarNavigationButtonStyle /*60*/:
            case R.styleable.AppCompatTheme_popupMenuStyle /*61*/:
            case R.styleable.AppCompatTheme_popupWindowStyle /*62*/:
            case R.styleable.AppCompatTheme_colorBackgroundFloating /*92*/:
            case R.styleable.AppCompatTheme_alertDialogCenterButtons /*95*/:
                return this.chars[this.pos];
            default:
                return getUTF8();
        }
    }

    private char getUTF8() {
        int res = getByte(this.pos);
        this.pos++;
        if (res < 128) {
            return (char) res;
        }
        if (res < 192 || res > 247) {
            return '?';
        }
        int count;
        if (res <= 223) {
            count = 1;
            res &= 31;
        } else if (res <= 239) {
            count = 2;
            res &= 15;
        } else {
            count = 3;
            res &= 7;
        }
        for (int i = 0; i < count; i++) {
            this.pos++;
            if (this.pos == this.length || this.chars[this.pos] != TokenParser.ESCAPE) {
                return '?';
            }
            this.pos++;
            int b = getByte(this.pos);
            this.pos++;
            if ((b & 192) != 128) {
                return '?';
            }
            res = (res << 6) + (b & 63);
        }
        return (char) res;
    }

    private int getByte(int position) {
        if (position + 1 >= this.length) {
            throw new IllegalStateException("Malformed DN: " + this.dn);
        }
        int b1 = this.chars[position];
        if (b1 >= 48 && b1 <= 57) {
            b1 -= 48;
        } else if (b1 >= 97 && b1 <= R.styleable.AppCompatTheme_buttonStyle) {
            b1 -= 87;
        } else if (b1 < 65 || b1 > 70) {
            throw new IllegalStateException("Malformed DN: " + this.dn);
        } else {
            b1 -= 55;
        }
        int b2 = this.chars[position + 1];
        if (b2 >= 48 && b2 <= 57) {
            b2 -= 48;
        } else if (b2 >= 97 && b2 <= R.styleable.AppCompatTheme_buttonStyle) {
            b2 -= 87;
        } else if (b2 < 65 || b2 > 70) {
            throw new IllegalStateException("Malformed DN: " + this.dn);
        } else {
            b2 -= 55;
        }
        return (b1 << 4) + b2;
    }

    public String findMostSpecific(String attributeType) {
        this.pos = 0;
        this.beg = 0;
        this.end = 0;
        this.cur = 0;
        this.chars = this.dn.toCharArray();
        String attType = nextAT();
        if (attType == null) {
            return null;
        }
        do {
            String attValue = BuildConfig.FLAVOR;
            if (this.pos == this.length) {
                return null;
            }
            switch (this.chars[this.pos]) {
                case R.styleable.AppCompatTheme_actionModePasteDrawable /*34*/:
                    attValue = quotedAV();
                    break;
                case R.styleable.AppCompatTheme_actionModeSelectAllDrawable /*35*/:
                    attValue = hexAV();
                    break;
                case R.styleable.AppCompatTheme_dialogTheme /*43*/:
                case R.styleable.AppCompatTheme_dialogPreferredPadding /*44*/:
                case R.styleable.AppCompatTheme_toolbarStyle /*59*/:
                    break;
                default:
                    attValue = escapedAV();
                    break;
            }
            if (attributeType.equalsIgnoreCase(attType)) {
                return attValue;
            }
            if (this.pos >= this.length) {
                return null;
            }
            if (this.chars[this.pos] == ',' || this.chars[this.pos] == ';' || this.chars[this.pos] == '+') {
                this.pos++;
                attType = nextAT();
            } else {
                throw new IllegalStateException("Malformed DN: " + this.dn);
            }
        } while (attType != null);
        throw new IllegalStateException("Malformed DN: " + this.dn);
    }

    public List<String> getAllMostSpecificFirst(String attributeType) {
        this.pos = 0;
        this.beg = 0;
        this.end = 0;
        this.cur = 0;
        this.chars = this.dn.toCharArray();
        List<String> emptyList = Collections.emptyList();
        String attType = nextAT();
        if (attType == null) {
            return emptyList;
        }
        while (this.pos < this.length) {
            String attValue = BuildConfig.FLAVOR;
            switch (this.chars[this.pos]) {
                case R.styleable.AppCompatTheme_actionModePasteDrawable /*34*/:
                    attValue = quotedAV();
                    break;
                case R.styleable.AppCompatTheme_actionModeSelectAllDrawable /*35*/:
                    attValue = hexAV();
                    break;
                case R.styleable.AppCompatTheme_dialogTheme /*43*/:
                case R.styleable.AppCompatTheme_dialogPreferredPadding /*44*/:
                case R.styleable.AppCompatTheme_toolbarStyle /*59*/:
                    break;
                default:
                    attValue = escapedAV();
                    break;
            }
            if (attributeType.equalsIgnoreCase(attType)) {
                if (emptyList.isEmpty()) {
                    emptyList = new ArrayList();
                }
                emptyList.add(attValue);
            }
            if (this.pos >= this.length) {
                return emptyList;
            }
            if (this.chars[this.pos] == ',' || this.chars[this.pos] == ';' || this.chars[this.pos] == '+') {
                this.pos++;
                attType = nextAT();
                if (attType == null) {
                    throw new IllegalStateException("Malformed DN: " + this.dn);
                }
            }
            throw new IllegalStateException("Malformed DN: " + this.dn);
        }
        return emptyList;
    }
}
