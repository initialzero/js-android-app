package com.jaspersoft.android.jaspermobile.data.cache.report;

import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.sdk.service.rx.report.RxReportExecution;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public final class InMemoryReportCache implements ReportCache {
    private final Map<String, RxReportExecution> mStorage = new HashMap<>();

    @Inject
    public InMemoryReportCache() {
    }

    @Override
    public RxReportExecution get(String uri) {
        return mStorage.get(uri);
    }

    @Override
    public RxReportExecution put(String uri, RxReportExecution execution) {
        return mStorage.put(uri, execution);
    }

    @Override
    public RxReportExecution evict(String uri) {
        return mStorage.remove(uri);
    }
}
