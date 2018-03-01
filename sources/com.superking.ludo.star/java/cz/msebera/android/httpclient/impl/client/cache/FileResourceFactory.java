package cz.msebera.android.httpclient.impl.client.cache;

import cz.msebera.android.httpclient.annotation.Immutable;
import cz.msebera.android.httpclient.client.cache.Resource;
import cz.msebera.android.httpclient.client.cache.ResourceFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Immutable
public class FileResourceFactory implements ResourceFactory {
    private final File cacheDir;
    private final BasicIdGenerator idgen = new BasicIdGenerator();

    public FileResourceFactory(File cacheDir) {
        this.cacheDir = cacheDir;
    }

    private File generateUniqueCacheFile(String requestId) {
        StringBuilder buffer = new StringBuilder();
        this.idgen.generate(buffer);
        buffer.append('.');
        int len = Math.min(requestId.length(), 100);
        for (int i = 0; i < len; i++) {
            char ch = requestId.charAt(i);
            if (Character.isLetterOrDigit(ch) || ch == '.') {
                buffer.append(ch);
            } else {
                buffer.append('-');
            }
        }
        return new File(this.cacheDir, buffer.toString());
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public cz.msebera.android.httpclient.client.cache.Resource generate(java.lang.String r9, java.io.InputStream r10, cz.msebera.android.httpclient.client.cache.InputLimit r11) throws java.io.IOException {
        /*
        r8 = this;
        r1 = r8.generateUniqueCacheFile(r9);
        r3 = new java.io.FileOutputStream;
        r3.<init>(r1);
        r6 = 2048; // 0x800 float:2.87E-42 double:1.012E-320;
        r0 = new byte[r6];	 Catch:{ all -> 0x0032 }
        r4 = 0;
    L_0x000f:
        r2 = r10.read(r0);	 Catch:{ all -> 0x0032 }
        r6 = -1;
        if (r2 == r6) goto L_0x0029;
    L_0x0016:
        r6 = 0;
        r3.write(r0, r6, r2);	 Catch:{ all -> 0x0032 }
        r6 = (long) r2;	 Catch:{ all -> 0x0032 }
        r4 = r4 + r6;
        if (r11 == 0) goto L_0x000f;
    L_0x001e:
        r6 = r11.getValue();	 Catch:{ all -> 0x0032 }
        r6 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1));
        if (r6 <= 0) goto L_0x000f;
    L_0x0026:
        r11.reached();	 Catch:{ all -> 0x0032 }
    L_0x0029:
        r3.close();
        r6 = new cz.msebera.android.httpclient.impl.client.cache.FileResource;
        r6.<init>(r1);
        return r6;
    L_0x0032:
        r6 = move-exception;
        r3.close();
        throw r6;
        */
        throw new UnsupportedOperationException("Method not decompiled: cz.msebera.android.httpclient.impl.client.cache.FileResourceFactory.generate(java.lang.String, java.io.InputStream, cz.msebera.android.httpclient.client.cache.InputLimit):cz.msebera.android.httpclient.client.cache.Resource");
    }

    public Resource copy(String requestId, Resource resource) throws IOException {
        File file = generateUniqueCacheFile(requestId);
        if (resource instanceof FileResource) {
            IOUtils.copyFile(((FileResource) resource).getFile(), file);
        } else {
            IOUtils.copyAndClose(resource.getInputStream(), new FileOutputStream(file));
        }
        return new FileResource(file);
    }
}
