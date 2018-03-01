package cz.msebera.android.httpclient.client.entity;

import cz.msebera.android.httpclient.HttpEntity;
import java.io.IOException;
import java.io.InputStream;

public class DeflateDecompressingEntity extends DecompressingEntity {
    public DeflateDecompressingEntity(HttpEntity entity) {
        super(entity, new InputStreamFactory() {
            public InputStream create(InputStream instream) throws IOException {
                return new DeflateInputStream(instream);
            }
        });
    }
}
