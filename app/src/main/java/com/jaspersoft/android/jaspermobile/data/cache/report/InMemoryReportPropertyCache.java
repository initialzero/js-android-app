package com.jaspersoft.android.jaspermobile.data.cache.report;

import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public final class InMemoryReportPropertyCache implements ReportPropertyCache {
    private final Map<String, Holder> mStorage = new HashMap<>();

    @Inject
    public InMemoryReportPropertyCache() {
    }

    @Override
    public void putTotalPages(String reportUri, int totalPages) {
        Holder holder = mStorage.get(reportUri);
        if (holder == null) {
            holder = new Holder();
            mStorage.put(reportUri, holder);
        }
        holder.pages = totalPages;
    }

    @Override
    public Integer getTotalPages(String reportUri) {
        Holder holder = mStorage.get(reportUri);
        if (holder == null) {
            return null;
        }
        return holder.pages;
    }

    @Override
    public void evict(String reportUri) {
        mStorage.remove(reportUri);
    }

    private static class Holder {
        Integer pages;
    }
}
