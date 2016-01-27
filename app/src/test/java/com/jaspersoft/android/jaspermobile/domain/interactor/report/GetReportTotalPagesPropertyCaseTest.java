package com.jaspersoft.android.jaspermobile.domain.interactor.report;

import com.jaspersoft.android.jaspermobile.FakePostExecutionThread;
import com.jaspersoft.android.jaspermobile.FakePreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.Report;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ReportPropertyRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ReportRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import rx.Observable;
import rx.observers.TestSubscriber;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class GetReportTotalPagesPropertyCaseTest {
    private static final String REPORT_URI = "/my/uri";

    @Mock
    ReportRepository mReportRepository;
    @Mock
    ReportPropertyRepository mReportPropertyRepository;
    @Mock
    Report mReport;
    private GetReportTotalPagesPropertyCase mTotalPagesPropertyCase;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        mTotalPagesPropertyCase = new GetReportTotalPagesPropertyCase(FakePreExecutionThread.create(),
                FakePostExecutionThread.create(),
                mReportRepository,
                mReportPropertyRepository
        );
    }

    @Test
    public void testBuildUseCaseObservable() throws Exception {
        when(mReportRepository.getReport(anyString())).thenReturn(Observable.just(mReport));

        TestSubscriber<Integer> test = new TestSubscriber<>();
        mTotalPagesPropertyCase.execute(REPORT_URI, test);

        verify(mReportRepository).getReport(REPORT_URI);
        verify(mReportPropertyRepository).getTotalPagesProperty(mReport);
    }
}