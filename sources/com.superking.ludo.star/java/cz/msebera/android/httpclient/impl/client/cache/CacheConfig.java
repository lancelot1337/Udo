package cz.msebera.android.httpclient.impl.client.cache;

import com.ironsource.sdk.utils.Constants.RequestParameters;
import cz.msebera.android.httpclient.util.Args;

public class CacheConfig implements Cloneable {
    public static final CacheConfig DEFAULT = new Builder().build();
    public static final boolean DEFAULT_303_CACHING_ENABLED = false;
    public static final int DEFAULT_ASYNCHRONOUS_WORKERS_CORE = 1;
    public static final int DEFAULT_ASYNCHRONOUS_WORKERS_MAX = 1;
    public static final int DEFAULT_ASYNCHRONOUS_WORKER_IDLE_LIFETIME_SECS = 60;
    public static final boolean DEFAULT_HEURISTIC_CACHING_ENABLED = false;
    public static final float DEFAULT_HEURISTIC_COEFFICIENT = 0.1f;
    public static final long DEFAULT_HEURISTIC_LIFETIME = 0;
    public static final int DEFAULT_MAX_CACHE_ENTRIES = 1000;
    public static final int DEFAULT_MAX_OBJECT_SIZE_BYTES = 8192;
    public static final int DEFAULT_MAX_UPDATE_RETRIES = 1;
    public static final int DEFAULT_REVALIDATION_QUEUE_SIZE = 100;
    public static final boolean DEFAULT_WEAK_ETAG_ON_PUTDELETE_ALLOWED = false;
    private boolean allow303Caching;
    private int asynchronousWorkerIdleLifetimeSecs;
    private int asynchronousWorkersCore;
    private int asynchronousWorkersMax;
    private boolean heuristicCachingEnabled;
    private float heuristicCoefficient;
    private long heuristicDefaultLifetime;
    private boolean isSharedCache;
    private int maxCacheEntries;
    private long maxObjectSize;
    private int maxUpdateRetries;
    private boolean neverCacheHTTP10ResponsesWithQuery;
    private int revalidationQueueSize;
    private boolean weakETagOnPutDeleteAllowed;

    public static class Builder {
        private boolean allow303Caching = CacheConfig.DEFAULT_HEURISTIC_CACHING_ENABLED;
        private int asynchronousWorkerIdleLifetimeSecs = CacheConfig.DEFAULT_ASYNCHRONOUS_WORKER_IDLE_LIFETIME_SECS;
        private int asynchronousWorkersCore = CacheConfig.DEFAULT_MAX_UPDATE_RETRIES;
        private int asynchronousWorkersMax = CacheConfig.DEFAULT_MAX_UPDATE_RETRIES;
        private boolean heuristicCachingEnabled = CacheConfig.DEFAULT_HEURISTIC_CACHING_ENABLED;
        private float heuristicCoefficient = CacheConfig.DEFAULT_HEURISTIC_COEFFICIENT;
        private long heuristicDefaultLifetime = CacheConfig.DEFAULT_HEURISTIC_LIFETIME;
        private boolean isSharedCache = true;
        private int maxCacheEntries = CacheConfig.DEFAULT_MAX_CACHE_ENTRIES;
        private long maxObjectSize = 8192;
        private int maxUpdateRetries = CacheConfig.DEFAULT_MAX_UPDATE_RETRIES;
        private boolean neverCacheHTTP10ResponsesWithQuery;
        private int revalidationQueueSize = CacheConfig.DEFAULT_REVALIDATION_QUEUE_SIZE;
        private boolean weakETagOnPutDeleteAllowed = CacheConfig.DEFAULT_HEURISTIC_CACHING_ENABLED;

        Builder() {
        }

        public Builder setMaxObjectSize(long maxObjectSize) {
            this.maxObjectSize = maxObjectSize;
            return this;
        }

        public Builder setMaxCacheEntries(int maxCacheEntries) {
            this.maxCacheEntries = maxCacheEntries;
            return this;
        }

        public Builder setMaxUpdateRetries(int maxUpdateRetries) {
            this.maxUpdateRetries = maxUpdateRetries;
            return this;
        }

        public Builder setAllow303Caching(boolean allow303Caching) {
            this.allow303Caching = allow303Caching;
            return this;
        }

        public Builder setWeakETagOnPutDeleteAllowed(boolean weakETagOnPutDeleteAllowed) {
            this.weakETagOnPutDeleteAllowed = weakETagOnPutDeleteAllowed;
            return this;
        }

        public Builder setHeuristicCachingEnabled(boolean heuristicCachingEnabled) {
            this.heuristicCachingEnabled = heuristicCachingEnabled;
            return this;
        }

        public Builder setHeuristicCoefficient(float heuristicCoefficient) {
            this.heuristicCoefficient = heuristicCoefficient;
            return this;
        }

        public Builder setHeuristicDefaultLifetime(long heuristicDefaultLifetime) {
            this.heuristicDefaultLifetime = heuristicDefaultLifetime;
            return this;
        }

        public Builder setSharedCache(boolean isSharedCache) {
            this.isSharedCache = isSharedCache;
            return this;
        }

        public Builder setAsynchronousWorkersMax(int asynchronousWorkersMax) {
            this.asynchronousWorkersMax = asynchronousWorkersMax;
            return this;
        }

        public Builder setAsynchronousWorkersCore(int asynchronousWorkersCore) {
            this.asynchronousWorkersCore = asynchronousWorkersCore;
            return this;
        }

        public Builder setAsynchronousWorkerIdleLifetimeSecs(int asynchronousWorkerIdleLifetimeSecs) {
            this.asynchronousWorkerIdleLifetimeSecs = asynchronousWorkerIdleLifetimeSecs;
            return this;
        }

        public Builder setRevalidationQueueSize(int revalidationQueueSize) {
            this.revalidationQueueSize = revalidationQueueSize;
            return this;
        }

        public Builder setNeverCacheHTTP10ResponsesWithQueryString(boolean neverCacheHTTP10ResponsesWithQuery) {
            this.neverCacheHTTP10ResponsesWithQuery = neverCacheHTTP10ResponsesWithQuery;
            return this;
        }

        public CacheConfig build() {
            return new CacheConfig(this.maxObjectSize, this.maxCacheEntries, this.maxUpdateRetries, this.allow303Caching, this.weakETagOnPutDeleteAllowed, this.heuristicCachingEnabled, this.heuristicCoefficient, this.heuristicDefaultLifetime, this.isSharedCache, this.asynchronousWorkersMax, this.asynchronousWorkersCore, this.asynchronousWorkerIdleLifetimeSecs, this.revalidationQueueSize, this.neverCacheHTTP10ResponsesWithQuery);
        }
    }

    @Deprecated
    public CacheConfig() {
        this.maxObjectSize = 8192;
        this.maxCacheEntries = DEFAULT_MAX_CACHE_ENTRIES;
        this.maxUpdateRetries = DEFAULT_MAX_UPDATE_RETRIES;
        this.allow303Caching = DEFAULT_HEURISTIC_CACHING_ENABLED;
        this.weakETagOnPutDeleteAllowed = DEFAULT_HEURISTIC_CACHING_ENABLED;
        this.heuristicCachingEnabled = DEFAULT_HEURISTIC_CACHING_ENABLED;
        this.heuristicCoefficient = DEFAULT_HEURISTIC_COEFFICIENT;
        this.heuristicDefaultLifetime = DEFAULT_HEURISTIC_LIFETIME;
        this.isSharedCache = true;
        this.asynchronousWorkersMax = DEFAULT_MAX_UPDATE_RETRIES;
        this.asynchronousWorkersCore = DEFAULT_MAX_UPDATE_RETRIES;
        this.asynchronousWorkerIdleLifetimeSecs = DEFAULT_ASYNCHRONOUS_WORKER_IDLE_LIFETIME_SECS;
        this.revalidationQueueSize = DEFAULT_REVALIDATION_QUEUE_SIZE;
    }

    CacheConfig(long maxObjectSize, int maxCacheEntries, int maxUpdateRetries, boolean allow303Caching, boolean weakETagOnPutDeleteAllowed, boolean heuristicCachingEnabled, float heuristicCoefficient, long heuristicDefaultLifetime, boolean isSharedCache, int asynchronousWorkersMax, int asynchronousWorkersCore, int asynchronousWorkerIdleLifetimeSecs, int revalidationQueueSize, boolean neverCacheHTTP10ResponsesWithQuery) {
        this.maxObjectSize = maxObjectSize;
        this.maxCacheEntries = maxCacheEntries;
        this.maxUpdateRetries = maxUpdateRetries;
        this.allow303Caching = allow303Caching;
        this.weakETagOnPutDeleteAllowed = weakETagOnPutDeleteAllowed;
        this.heuristicCachingEnabled = heuristicCachingEnabled;
        this.heuristicCoefficient = heuristicCoefficient;
        this.heuristicDefaultLifetime = heuristicDefaultLifetime;
        this.isSharedCache = isSharedCache;
        this.asynchronousWorkersMax = asynchronousWorkersMax;
        this.asynchronousWorkersCore = asynchronousWorkersCore;
        this.asynchronousWorkerIdleLifetimeSecs = asynchronousWorkerIdleLifetimeSecs;
        this.revalidationQueueSize = revalidationQueueSize;
    }

    @Deprecated
    public int getMaxObjectSizeBytes() {
        return this.maxObjectSize > 2147483647L ? Integer.MAX_VALUE : (int) this.maxObjectSize;
    }

    @Deprecated
    public void setMaxObjectSizeBytes(int maxObjectSizeBytes) {
        if (maxObjectSizeBytes > Integer.MAX_VALUE) {
            this.maxObjectSize = 2147483647L;
        } else {
            this.maxObjectSize = (long) maxObjectSizeBytes;
        }
    }

    public long getMaxObjectSize() {
        return this.maxObjectSize;
    }

    @Deprecated
    public void setMaxObjectSize(long maxObjectSize) {
        this.maxObjectSize = maxObjectSize;
    }

    public boolean isNeverCacheHTTP10ResponsesWithQuery() {
        return this.neverCacheHTTP10ResponsesWithQuery;
    }

    public int getMaxCacheEntries() {
        return this.maxCacheEntries;
    }

    @Deprecated
    public void setMaxCacheEntries(int maxCacheEntries) {
        this.maxCacheEntries = maxCacheEntries;
    }

    public int getMaxUpdateRetries() {
        return this.maxUpdateRetries;
    }

    @Deprecated
    public void setMaxUpdateRetries(int maxUpdateRetries) {
        this.maxUpdateRetries = maxUpdateRetries;
    }

    public boolean is303CachingEnabled() {
        return this.allow303Caching;
    }

    public boolean isWeakETagOnPutDeleteAllowed() {
        return this.weakETagOnPutDeleteAllowed;
    }

    public boolean isHeuristicCachingEnabled() {
        return this.heuristicCachingEnabled;
    }

    @Deprecated
    public void setHeuristicCachingEnabled(boolean heuristicCachingEnabled) {
        this.heuristicCachingEnabled = heuristicCachingEnabled;
    }

    public float getHeuristicCoefficient() {
        return this.heuristicCoefficient;
    }

    @Deprecated
    public void setHeuristicCoefficient(float heuristicCoefficient) {
        this.heuristicCoefficient = heuristicCoefficient;
    }

    public long getHeuristicDefaultLifetime() {
        return this.heuristicDefaultLifetime;
    }

    @Deprecated
    public void setHeuristicDefaultLifetime(long heuristicDefaultLifetimeSecs) {
        this.heuristicDefaultLifetime = heuristicDefaultLifetimeSecs;
    }

    public boolean isSharedCache() {
        return this.isSharedCache;
    }

    @Deprecated
    public void setSharedCache(boolean isSharedCache) {
        this.isSharedCache = isSharedCache;
    }

    public int getAsynchronousWorkersMax() {
        return this.asynchronousWorkersMax;
    }

    @Deprecated
    public void setAsynchronousWorkersMax(int max) {
        this.asynchronousWorkersMax = max;
    }

    public int getAsynchronousWorkersCore() {
        return this.asynchronousWorkersCore;
    }

    @Deprecated
    public void setAsynchronousWorkersCore(int min) {
        this.asynchronousWorkersCore = min;
    }

    public int getAsynchronousWorkerIdleLifetimeSecs() {
        return this.asynchronousWorkerIdleLifetimeSecs;
    }

    @Deprecated
    public void setAsynchronousWorkerIdleLifetimeSecs(int secs) {
        this.asynchronousWorkerIdleLifetimeSecs = secs;
    }

    public int getRevalidationQueueSize() {
        return this.revalidationQueueSize;
    }

    @Deprecated
    public void setRevalidationQueueSize(int size) {
        this.revalidationQueueSize = size;
    }

    protected CacheConfig clone() throws CloneNotSupportedException {
        return (CacheConfig) super.clone();
    }

    public static Builder custom() {
        return new Builder();
    }

    public static Builder copy(CacheConfig config) {
        Args.notNull(config, "Cache config");
        return new Builder().setMaxObjectSize(config.getMaxObjectSize()).setMaxCacheEntries(config.getMaxCacheEntries()).setMaxUpdateRetries(config.getMaxUpdateRetries()).setHeuristicCachingEnabled(config.isHeuristicCachingEnabled()).setHeuristicCoefficient(config.getHeuristicCoefficient()).setHeuristicDefaultLifetime(config.getHeuristicDefaultLifetime()).setSharedCache(config.isSharedCache()).setAsynchronousWorkersMax(config.getAsynchronousWorkersMax()).setAsynchronousWorkersCore(config.getAsynchronousWorkersCore()).setAsynchronousWorkerIdleLifetimeSecs(config.getAsynchronousWorkerIdleLifetimeSecs()).setRevalidationQueueSize(config.getRevalidationQueueSize()).setNeverCacheHTTP10ResponsesWithQueryString(config.isNeverCacheHTTP10ResponsesWithQuery());
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[maxObjectSize=").append(this.maxObjectSize).append(", maxCacheEntries=").append(this.maxCacheEntries).append(", maxUpdateRetries=").append(this.maxUpdateRetries).append(", 303CachingEnabled=").append(this.allow303Caching).append(", weakETagOnPutDeleteAllowed=").append(this.weakETagOnPutDeleteAllowed).append(", heuristicCachingEnabled=").append(this.heuristicCachingEnabled).append(", heuristicCoefficient=").append(this.heuristicCoefficient).append(", heuristicDefaultLifetime=").append(this.heuristicDefaultLifetime).append(", isSharedCache=").append(this.isSharedCache).append(", asynchronousWorkersMax=").append(this.asynchronousWorkersMax).append(", asynchronousWorkersCore=").append(this.asynchronousWorkersCore).append(", asynchronousWorkerIdleLifetimeSecs=").append(this.asynchronousWorkerIdleLifetimeSecs).append(", revalidationQueueSize=").append(this.revalidationQueueSize).append(", neverCacheHTTP10ResponsesWithQuery=").append(this.neverCacheHTTP10ResponsesWithQuery).append(RequestParameters.RIGHT_BRACKETS);
        return builder.toString();
    }
}
