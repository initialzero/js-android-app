package com.jaspersoft.android.jaspermobile.data;

import com.jaspersoft.android.sdk.service.rx.filter.RxFiltersService;
import com.jaspersoft.android.sdk.service.rx.report.RxReportService;
import com.jaspersoft.android.sdk.service.rx.report.schedule.RxReportScheduleService;
import com.jaspersoft.android.sdk.service.rx.repository.RxRepositoryService;

import rx.Observable;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface JasperRestClient {
    Observable<RxReportService> reportService();
    Observable<RxRepositoryService> repositoryService();
    Observable<RxFiltersService> filtersService();
    Observable<RxReportScheduleService> scheduleService();
}
