package cz.msebera.android.httpclient.impl.client.cache;

import cz.msebera.android.httpclient.client.cache.HttpCacheInvalidator;
import cz.msebera.android.httpclient.client.cache.HttpCacheStorage;
import cz.msebera.android.httpclient.client.cache.ResourceFactory;
import cz.msebera.android.httpclient.impl.client.HttpClientBuilder;
import cz.msebera.android.httpclient.impl.execchain.ClientExecChain;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;

public class CachingHttpClientBuilder extends HttpClientBuilder {
    private CacheConfig cacheConfig;
    private File cacheDir;
    private boolean deleteCache = true;
    private HttpCacheInvalidator httpCacheInvalidator;
    private ResourceFactory resourceFactory;
    private SchedulingStrategy schedulingStrategy;
    private HttpCacheStorage storage;

    public static CachingHttpClientBuilder create() {
        return new CachingHttpClientBuilder();
    }

    protected CachingHttpClientBuilder() {
    }

    public final CachingHttpClientBuilder setResourceFactory(ResourceFactory resourceFactory) {
        this.resourceFactory = resourceFactory;
        return this;
    }

    public final CachingHttpClientBuilder setHttpCacheStorage(HttpCacheStorage storage) {
        this.storage = storage;
        return this;
    }

    public final CachingHttpClientBuilder setCacheDir(File cacheDir) {
        this.cacheDir = cacheDir;
        return this;
    }

    public final CachingHttpClientBuilder setCacheConfig(CacheConfig cacheConfig) {
        this.cacheConfig = cacheConfig;
        return this;
    }

    public final CachingHttpClientBuilder setSchedulingStrategy(SchedulingStrategy schedulingStrategy) {
        this.schedulingStrategy = schedulingStrategy;
        return this;
    }

    public final CachingHttpClientBuilder setHttpCacheInvalidator(HttpCacheInvalidator cacheInvalidator) {
        this.httpCacheInvalidator = cacheInvalidator;
        return this;
    }

    public CachingHttpClientBuilder setDeleteCache(boolean deleteCache) {
        this.deleteCache = deleteCache;
        return this;
    }

    protected ClientExecChain decorateMainExec(ClientExecChain mainExec) {
        CacheConfig config = this.cacheConfig != null ? this.cacheConfig : CacheConfig.DEFAULT;
        ResourceFactory resourceFactoryCopy = this.resourceFactory;
        if (resourceFactoryCopy == null) {
            if (this.cacheDir == null) {
                resourceFactoryCopy = new HeapResourceFactory();
            } else {
                resourceFactoryCopy = new FileResourceFactory(this.cacheDir);
            }
        }
        HttpCacheStorage storageCopy = this.storage;
        if (storageCopy == null) {
            if (this.cacheDir == null) {
                storageCopy = new BasicHttpCacheStorage(config);
            } else {
                final ManagedHttpCacheStorage managedStorage = new ManagedHttpCacheStorage(config);
                if (this.deleteCache) {
                    addCloseable(new Closeable() {
                        public void close() throws IOException {
                            managedStorage.shutdown();
                        }
                    });
                } else {
                    addCloseable(managedStorage);
                }
                Object storageCopy2 = managedStorage;
            }
        }
        AsynchronousValidator revalidator = createAsynchronousRevalidator(config);
        CacheKeyGenerator uriExtractor = new CacheKeyGenerator();
        HttpCacheInvalidator cacheInvalidator = this.httpCacheInvalidator;
        if (cacheInvalidator == null) {
            cacheInvalidator = new CacheInvalidator(uriExtractor, storageCopy);
        }
        return new CachingExec(mainExec, new BasicHttpCache(resourceFactoryCopy, storageCopy, config, uriExtractor, cacheInvalidator), config, revalidator);
    }

    private AsynchronousValidator createAsynchronousRevalidator(CacheConfig config) {
        if (config.getAsynchronousWorkersMax() <= 0) {
            return null;
        }
        AsynchronousValidator revalidator = new AsynchronousValidator(createSchedulingStrategy(config));
        addCloseable(revalidator);
        return revalidator;
    }

    private SchedulingStrategy createSchedulingStrategy(CacheConfig config) {
        return this.schedulingStrategy != null ? this.schedulingStrategy : new ImmediateSchedulingStrategy(config);
    }
}
