package com.jaspersoft.android.jaspermobile.data.cache.report;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.jaspersoft.android.jaspermobile.domain.PageRequest;
import com.jaspersoft.android.jaspermobile.domain.ReportPage;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.octo.android.robospice.persistence.memory.LruCache;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public final class InMemoryReportPageCache implements ReportPageCache {
    private final Map<String, LruCache<String, ReportPage>> mStorage = new HashMap<>();

    @Inject
    public InMemoryReportPageCache() {
    }

    @Nullable
    @Override
    public ReportPage get(@NonNull PageRequest pageRequest) {
        LruCache<String, ReportPage> cache = getInternalCache(pageRequest.getUri());
        return cache.get(pageRequest.getRange());
    }

    @Override
    @NonNull
    public ReportPage put(@NonNull PageRequest pageRequest, @NonNull ReportPage content) {
        LruCache<String, ReportPage> cache = getInternalCache(pageRequest.getUri());
        cache.put(pageRequest.getRange(), content);
        return content;
    }

    @NonNull
    private LruCache<String, ReportPage> getInternalCache(String uri) {
        LruCache<String, ReportPage> cache = mStorage.get(uri);
        if (cache == null) {
            cache = new LruCache<>(10);
            mStorage.put(uri, cache);
        }
        return cache;
    }

    @Override
    public void evict(String uri) {
        mStorage.remove(uri);
    }
}
