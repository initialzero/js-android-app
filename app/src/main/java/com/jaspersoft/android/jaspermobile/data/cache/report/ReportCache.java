package com.jaspersoft.android.jaspermobile.data.cache.report;

import com.jaspersoft.android.jaspermobile.domain.Report;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface ReportCache {
    Report get(String uri);
    Report put(String uri, Report report);
    Report remove(String uri);
}
