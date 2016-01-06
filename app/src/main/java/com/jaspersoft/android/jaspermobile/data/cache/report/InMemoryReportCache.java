package com.jaspersoft.android.jaspermobile.data.cache.report;

import com.jaspersoft.android.jaspermobile.domain.Report;
import com.jaspersoft.android.jaspermobile.internal.di.PerReport;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerReport
public final class InMemoryReportCache implements ReportCache {
    private final Map<String, Report> mStorage = new HashMap<>();

    @Inject
    public InMemoryReportCache() {
    }

    @Override
    public Report get(String uri) {
        return mStorage.get(uri);
    }

    @Override
    public Report put(String uri, Report report) {
        return mStorage.put(uri, report);
    }

    @Override
    public Report remove(String uri) {
        return mStorage.remove(uri);
    }
}
