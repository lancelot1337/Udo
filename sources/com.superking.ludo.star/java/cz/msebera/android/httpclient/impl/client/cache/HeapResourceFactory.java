package cz.msebera.android.httpclient.impl.client.cache;

import cz.msebera.android.httpclient.annotation.Immutable;
import cz.msebera.android.httpclient.client.cache.Resource;
import cz.msebera.android.httpclient.client.cache.ResourceFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Immutable
public class HeapResourceFactory implements ResourceFactory {
    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public cz.msebera.android.httpclient.client.cache.Resource generate(java.lang.String r9, java.io.InputStream r10, cz.msebera.android.httpclient.client.cache.InputLimit r11) throws java.io.IOException {
        /*
        r8 = this;
        r2 = new java.io.ByteArrayOutputStream;
        r2.<init>();
        r3 = 2048; // 0x800 float:2.87E-42 double:1.012E-320;
        r0 = new byte[r3];
        r4 = 0;
    L_0x000b:
        r1 = r10.read(r0);
        r3 = -1;
        if (r1 == r3) goto L_0x0025;
    L_0x0012:
        r3 = 0;
        r2.write(r0, r3, r1);
        r6 = (long) r1;
        r4 = r4 + r6;
        if (r11 == 0) goto L_0x000b;
    L_0x001a:
        r6 = r11.getValue();
        r3 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1));
        if (r3 <= 0) goto L_0x000b;
    L_0x0022:
        r11.reached();
    L_0x0025:
        r3 = r2.toByteArray();
        r3 = r8.createResource(r3);
        return r3;
        */
        throw new UnsupportedOperationException("Method not decompiled: cz.msebera.android.httpclient.impl.client.cache.HeapResourceFactory.generate(java.lang.String, java.io.InputStream, cz.msebera.android.httpclient.client.cache.InputLimit):cz.msebera.android.httpclient.client.cache.Resource");
    }

    public Resource copy(String requestId, Resource resource) throws IOException {
        byte[] body;
        if (resource instanceof HeapResource) {
            body = ((HeapResource) resource).getByteArray();
        } else {
            ByteArrayOutputStream outstream = new ByteArrayOutputStream();
            IOUtils.copyAndClose(resource.getInputStream(), outstream);
            body = outstream.toByteArray();
        }
        return createResource(body);
    }

    Resource createResource(byte[] buf) {
        return new HeapResource(buf);
    }
}
