package cz.msebera.android.httpclient.client.entity;

import cz.msebera.android.httpclient.HttpEntity;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public class GzipDecompressingEntity extends DecompressingEntity {
    public GzipDecompressingEntity(HttpEntity entity) {
        super(entity, new InputStreamFactory() {
            public InputStream create(InputStream instream) throws IOException {
                return new GZIPInputStream(instream);
            }
        });
    }
}
