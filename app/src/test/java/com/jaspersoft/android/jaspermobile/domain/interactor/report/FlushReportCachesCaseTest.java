package com.jaspersoft.android.jaspermobile.domain.interactor.report;

import com.jaspersoft.android.jaspermobile.domain.repository.report.ReportRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class FlushReportCachesCaseTest {
    private static final String REPORT_URI = "/my/uri";

    @Mock
    ReportRepository mReportRepository;

    private FlushReportCachesCase mFlushReportCachesCase;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        mFlushReportCachesCase = new FlushReportCachesCase(
                mReportRepository
        );
    }

    @Test
    public void should_delegate_calls_to_repos() throws Exception {
        mFlushReportCachesCase.execute(REPORT_URI);
        verify(mReportRepository).flushReport(REPORT_URI);
    }
}