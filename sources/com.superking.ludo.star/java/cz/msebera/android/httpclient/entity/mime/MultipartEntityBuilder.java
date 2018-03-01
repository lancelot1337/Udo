package cz.msebera.android.httpclient.entity.mime;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.mime.content.ByteArrayBody;
import cz.msebera.android.httpclient.entity.mime.content.ContentBody;
import cz.msebera.android.httpclient.entity.mime.content.FileBody;
import cz.msebera.android.httpclient.entity.mime.content.InputStreamBody;
import cz.msebera.android.httpclient.entity.mime.content.StringBody;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.util.Args;
import io.branch.referral.R;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.cocos2dx.lib.Cocos2dxHandler;

public class MultipartEntityBuilder {
    private static final String DEFAULT_SUBTYPE = "form-data";
    private static final char[] MULTIPART_CHARS = "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private List<FormBodyPart> bodyParts = null;
    private String boundary = null;
    private Charset charset = null;
    private ContentType contentType;
    private HttpMultipartMode mode = HttpMultipartMode.STRICT;

    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$cz$msebera$android$httpclient$entity$mime$HttpMultipartMode = new int[HttpMultipartMode.values().length];

        static {
            try {
                $SwitchMap$cz$msebera$android$httpclient$entity$mime$HttpMultipartMode[HttpMultipartMode.BROWSER_COMPATIBLE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$cz$msebera$android$httpclient$entity$mime$HttpMultipartMode[HttpMultipartMode.RFC6532.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    public static MultipartEntityBuilder create() {
        return new MultipartEntityBuilder();
    }

    MultipartEntityBuilder() {
    }

    public MultipartEntityBuilder setMode(HttpMultipartMode mode) {
        this.mode = mode;
        return this;
    }

    public MultipartEntityBuilder setLaxMode() {
        this.mode = HttpMultipartMode.BROWSER_COMPATIBLE;
        return this;
    }

    public MultipartEntityBuilder setStrictMode() {
        this.mode = HttpMultipartMode.STRICT;
        return this;
    }

    public MultipartEntityBuilder setBoundary(String boundary) {
        this.boundary = boundary;
        return this;
    }

    public MultipartEntityBuilder setMimeSubtype(String subType) {
        Args.notBlank(subType, "MIME subtype");
        this.contentType = ContentType.create("multipart/" + subType);
        return this;
    }

    public MultipartEntityBuilder seContentType(ContentType contentType) {
        Args.notNull(contentType, "Content type");
        this.contentType = contentType;
        return this;
    }

    public MultipartEntityBuilder setCharset(Charset charset) {
        this.charset = charset;
        return this;
    }

    public MultipartEntityBuilder addPart(FormBodyPart bodyPart) {
        if (bodyPart != null) {
            if (this.bodyParts == null) {
                this.bodyParts = new ArrayList();
            }
            this.bodyParts.add(bodyPart);
        }
        return this;
    }

    public MultipartEntityBuilder addPart(String name, ContentBody contentBody) {
        Args.notNull(name, "Name");
        Args.notNull(contentBody, "Content body");
        return addPart(FormBodyPartBuilder.create(name, contentBody).build());
    }

    public MultipartEntityBuilder addTextBody(String name, String text, ContentType contentType) {
        return addPart(name, new StringBody(text, contentType));
    }

    public MultipartEntityBuilder addTextBody(String name, String text) {
        return addTextBody(name, text, ContentType.DEFAULT_TEXT);
    }

    public MultipartEntityBuilder addBinaryBody(String name, byte[] b, ContentType contentType, String filename) {
        return addPart(name, new ByteArrayBody(b, contentType, filename));
    }

    public MultipartEntityBuilder addBinaryBody(String name, byte[] b) {
        return addBinaryBody(name, b, ContentType.DEFAULT_BINARY, null);
    }

    public MultipartEntityBuilder addBinaryBody(String name, File file, ContentType contentType, String filename) {
        return addPart(name, new FileBody(file, contentType, filename));
    }

    public MultipartEntityBuilder addBinaryBody(String name, File file) {
        return addBinaryBody(name, file, ContentType.DEFAULT_BINARY, file != null ? file.getName() : null);
    }

    public MultipartEntityBuilder addBinaryBody(String name, InputStream stream, ContentType contentType, String filename) {
        return addPart(name, new InputStreamBody(stream, contentType, filename));
    }

    public MultipartEntityBuilder addBinaryBody(String name, InputStream stream) {
        return addBinaryBody(name, stream, ContentType.DEFAULT_BINARY, null);
    }

    private String generateBoundary() {
        StringBuilder buffer = new StringBuilder();
        Random rand = new Random();
        int count = rand.nextInt(11) + 30;
        for (int i = 0; i < count; i++) {
            buffer.append(MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)]);
        }
        return buffer.toString();
    }

    MultipartFormEntity buildEntity() {
        ContentType contentTypeCopy;
        List<FormBodyPart> bodyPartsCopy;
        AbstractMultipartForm form;
        String boundaryCopy = this.boundary;
        if (boundaryCopy == null && this.contentType != null) {
            boundaryCopy = this.contentType.getParameter("boundary");
        }
        if (boundaryCopy == null) {
            boundaryCopy = generateBoundary();
        }
        Charset charsetCopy = this.charset;
        if (charsetCopy == null && this.contentType != null) {
            charsetCopy = this.contentType.getCharset();
        }
        List<NameValuePair> paramsList = new ArrayList(2);
        paramsList.add(new BasicNameValuePair("boundary", boundaryCopy));
        if (charsetCopy != null) {
            paramsList.add(new BasicNameValuePair("charset", charsetCopy.name()));
        }
        NameValuePair[] params = (NameValuePair[]) paramsList.toArray(new NameValuePair[paramsList.size()]);
        if (this.contentType != null) {
            contentTypeCopy = this.contentType.withParameters(params);
        } else {
            contentTypeCopy = ContentType.create("multipart/form-data", params);
        }
        if (this.bodyParts != null) {
            bodyPartsCopy = new ArrayList(this.bodyParts);
        } else {
            bodyPartsCopy = Collections.emptyList();
        }
        switch (AnonymousClass1.$SwitchMap$cz$msebera$android$httpclient$entity$mime$HttpMultipartMode[(this.mode != null ? this.mode : HttpMultipartMode.STRICT).ordinal()]) {
            case Cocos2dxHandler.HANDLER_SHOW_DIALOG /*1*/:
                form = new HttpBrowserCompatibleMultipart(charsetCopy, boundaryCopy, bodyPartsCopy);
                break;
            case R.styleable.View_paddingStart /*2*/:
                form = new HttpRFC6532Multipart(charsetCopy, boundaryCopy, bodyPartsCopy);
                break;
            default:
                form = new HttpStrictMultipart(charsetCopy, boundaryCopy, bodyPartsCopy);
                break;
        }
        return new MultipartFormEntity(form, contentTypeCopy, form.getTotalLength());
    }

    public HttpEntity build() {
        return buildEntity();
    }
}
