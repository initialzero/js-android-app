package com.jaspersoft.android.jaspermobile.data.cache.report;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;

import com.jaspersoft.android.jaspermobile.domain.PageRequest;
import com.jaspersoft.android.jaspermobile.domain.ReportPage;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;

import javax.inject.Inject;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public final class InMemoryReportPageCache implements ReportPageCache {
    private final LruCache<String, ReportPage> mStorage = new LruCache<>(10);

    @Inject
    public InMemoryReportPageCache() {
    }

    @Nullable
    @Override
    public ReportPage get(@NonNull PageRequest pageRequest) {
        String id = pageRequest.getIdentifier();
        return mStorage.get(id);
    }

    @Override
    @NonNull
    public ReportPage put(@NonNull PageRequest pageRequest, @NonNull ReportPage content) {
        String id = pageRequest.getIdentifier();
        mStorage.put(id, content);
        return content;
    }

    @Override
    public void evict(String uri) {
        mStorage.remove(uri);
    }
}
