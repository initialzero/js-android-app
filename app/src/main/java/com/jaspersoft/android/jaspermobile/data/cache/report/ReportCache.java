package com.jaspersoft.android.jaspermobile.data.cache.report;

import com.jaspersoft.android.sdk.service.rx.report.RxReportExecution;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface ReportCache {
    RxReportExecution put(String reportUri, RxReportExecution execution);

    RxReportExecution get(String reportUri);

    RxReportExecution evict(String uri);
}
