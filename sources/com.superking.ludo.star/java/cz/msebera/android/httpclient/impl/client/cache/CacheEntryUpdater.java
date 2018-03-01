package cz.msebera.android.httpclient.impl.client.cache;

import com.ironsource.sdk.utils.Constants.ErrorCodes;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.HttpStatus;
import cz.msebera.android.httpclient.annotation.Immutable;
import cz.msebera.android.httpclient.client.cache.HeaderConstants;
import cz.msebera.android.httpclient.client.cache.HttpCacheEntry;
import cz.msebera.android.httpclient.client.cache.Resource;
import cz.msebera.android.httpclient.client.cache.ResourceFactory;
import cz.msebera.android.httpclient.client.utils.DateUtils;
import cz.msebera.android.httpclient.protocol.HTTP;
import cz.msebera.android.httpclient.util.Args;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

@Immutable
class CacheEntryUpdater {
    private final ResourceFactory resourceFactory;

    CacheEntryUpdater() {
        this(new HeapResourceFactory());
    }

    CacheEntryUpdater(ResourceFactory resourceFactory) {
        this.resourceFactory = resourceFactory;
    }

    public HttpCacheEntry updateCacheEntry(String requestId, HttpCacheEntry entry, Date requestDate, Date responseDate, HttpResponse response) throws IOException {
        Args.check(response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_MODIFIED, "Response must have 304 status code");
        Header[] mergedHeaders = mergeHeaders(entry, response);
        Resource resource = null;
        if (entry.getResource() != null) {
            resource = this.resourceFactory.copy(requestId, entry.getResource());
        }
        return new HttpCacheEntry(requestDate, responseDate, entry.getStatusLine(), mergedHeaders, resource, entry.getRequestMethod());
    }

    protected Header[] mergeHeaders(HttpCacheEntry entry, HttpResponse response) {
        if (entryAndResponseHaveDateHeader(entry, response) && entryDateHeaderNewerThenResponse(entry, response)) {
            return entry.getAllHeaders();
        }
        List<Header> cacheEntryHeaderList = new ArrayList(Arrays.asList(entry.getAllHeaders()));
        removeCacheHeadersThatMatchResponse(cacheEntryHeaderList, response);
        removeCacheEntry1xxWarnings(cacheEntryHeaderList, entry);
        cacheEntryHeaderList.addAll(Arrays.asList(response.getAllHeaders()));
        return (Header[]) cacheEntryHeaderList.toArray(new Header[cacheEntryHeaderList.size()]);
    }

    private void removeCacheHeadersThatMatchResponse(List<Header> cacheEntryHeaderList, HttpResponse response) {
        for (Header responseHeader : response.getAllHeaders()) {
            ListIterator<Header> cacheEntryHeaderListIter = cacheEntryHeaderList.listIterator();
            while (cacheEntryHeaderListIter.hasNext()) {
                if (((Header) cacheEntryHeaderListIter.next()).getName().equals(responseHeader.getName())) {
                    cacheEntryHeaderListIter.remove();
                }
            }
        }
    }

    private void removeCacheEntry1xxWarnings(List<Header> cacheEntryHeaderList, HttpCacheEntry entry) {
        ListIterator<Header> cacheEntryHeaderListIter = cacheEntryHeaderList.listIterator();
        while (cacheEntryHeaderListIter.hasNext()) {
            if (HeaderConstants.WARNING.equals(((Header) cacheEntryHeaderListIter.next()).getName())) {
                for (Header cacheEntryWarning : entry.getHeaders(HeaderConstants.WARNING)) {
                    if (cacheEntryWarning.getValue().startsWith(ErrorCodes.FOLDER_NOT_EXIST_CODE)) {
                        cacheEntryHeaderListIter.remove();
                    }
                }
            }
        }
    }

    private boolean entryDateHeaderNewerThenResponse(HttpCacheEntry entry, HttpResponse response) {
        Date entryDate = DateUtils.parseDate(entry.getFirstHeader(HTTP.DATE_HEADER).getValue());
        Date responseDate = DateUtils.parseDate(response.getFirstHeader(HTTP.DATE_HEADER).getValue());
        if (entryDate == null || responseDate == null || !entryDate.after(responseDate)) {
            return false;
        }
        return true;
    }

    private boolean entryAndResponseHaveDateHeader(HttpCacheEntry entry, HttpResponse response) {
        if (entry.getFirstHeader(HTTP.DATE_HEADER) == null || response.getFirstHeader(HTTP.DATE_HEADER) == null) {
            return false;
        }
        return true;
    }
}
