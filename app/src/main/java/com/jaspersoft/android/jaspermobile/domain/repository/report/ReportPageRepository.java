package com.jaspersoft.android.jaspermobile.domain.repository.report;

import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.domain.PageRequest;
import com.jaspersoft.android.jaspermobile.domain.ReportPage;
import com.jaspersoft.android.sdk.service.rx.report.RxReportExecution;

import rx.Observable;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface ReportPageRepository {
    @NonNull
    Observable<ReportPage> get(@NonNull final RxReportExecution execution, @NonNull final PageRequest pageRequest);

    void flushReportPages(@NonNull String reportUri);
}
