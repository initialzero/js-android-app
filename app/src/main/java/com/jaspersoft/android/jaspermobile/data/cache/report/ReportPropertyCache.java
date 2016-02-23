package com.jaspersoft.android.jaspermobile.data.cache.report;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface ReportPropertyCache {
    void putTotalPages(String reportUri, int totalPages);

    Integer getTotalPages(String reportUri);

    void evict(String reportUri);
}
