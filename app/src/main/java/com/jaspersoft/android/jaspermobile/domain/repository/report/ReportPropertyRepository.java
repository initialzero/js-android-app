package com.jaspersoft.android.jaspermobile.domain.repository.report;

import android.support.annotation.NonNull;

import com.jaspersoft.android.sdk.service.rx.report.RxReportExecution;

import rx.Observable;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface ReportPropertyRepository {
    @NonNull
    Observable<Integer> getTotalPagesProperty(@NonNull final RxReportExecution reportExecution, @NonNull final String reportUri);

    void flushReportProperties(String reportUri);
}
