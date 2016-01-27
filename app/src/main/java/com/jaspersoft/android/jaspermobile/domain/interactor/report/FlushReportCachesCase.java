package com.jaspersoft.android.jaspermobile.domain.interactor.report;

import com.jaspersoft.android.jaspermobile.domain.repository.report.ReportRepository;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;

import javax.inject.Inject;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public class FlushReportCachesCase {
    private final ReportRepository mReportRepository;

    @Inject
    public FlushReportCachesCase(
                                 ReportRepository reportRepository
    ) {
        mReportRepository = reportRepository;
    }

    public void execute(String reportUri) {
        mReportRepository.flushReport(reportUri);
    }
}
