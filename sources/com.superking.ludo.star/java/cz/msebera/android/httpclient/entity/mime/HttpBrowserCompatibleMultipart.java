package cz.msebera.android.httpclient.entity.mime;

import cz.msebera.android.httpclient.protocol.HTTP;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;

class HttpBrowserCompatibleMultipart extends AbstractMultipartForm {
    private final List<FormBodyPart> parts;

    public HttpBrowserCompatibleMultipart(Charset charset, String boundary, List<FormBodyPart> parts) {
        super(charset, boundary);
        this.parts = parts;
    }

    public List<FormBodyPart> getBodyParts() {
        return this.parts;
    }

    protected void formatMultipartHeader(FormBodyPart part, OutputStream out) throws IOException {
        Header header = part.getHeader();
        AbstractMultipartForm.writeField(header.getField(MIME.CONTENT_DISPOSITION), this.charset, out);
        if (part.getBody().getFilename() != null) {
            AbstractMultipartForm.writeField(header.getField(HTTP.CONTENT_TYPE), this.charset, out);
        }
    }
}
