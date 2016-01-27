package com.jaspersoft.android.jaspermobile.domain.interactor.report;

import com.jaspersoft.android.jaspermobile.domain.repository.report.ControlsRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ReportPageRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ReportPropertyRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ReportRepository;
import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;

import javax.inject.Inject;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerActivity
public class FlushReportCachesCase {
    private final ControlsRepository mControlsRepository;
    private final ReportPageRepository mReportPageRepository;
    private final ReportPropertyRepository mReportPropertyRepository;
    private final ReportRepository mReportRepository;

    @Inject
    public FlushReportCachesCase(
            ControlsRepository controlsRepository,
                                 ReportPageRepository reportPageRepository,
                                 ReportPropertyRepository reportPropertyRepository,
                                 ReportRepository reportRepository
    ) {
        mControlsRepository = controlsRepository;
        mReportPageRepository = reportPageRepository;
        mReportPropertyRepository = reportPropertyRepository;
        mReportRepository = reportRepository;
    }

    public void execute(String reportUri) {
        mReportRepository.flushReport(reportUri);
        mControlsRepository.flushControls(reportUri);
        mReportPageRepository.flushReportPages(reportUri);
        mReportPropertyRepository.flushReportProperties(reportUri);
    }
}
