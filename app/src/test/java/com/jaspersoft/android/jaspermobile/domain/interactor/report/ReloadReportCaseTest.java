package com.jaspersoft.android.jaspermobile.domain.interactor.report;

import com.jaspersoft.android.jaspermobile.FakePostExecutionThread;
import com.jaspersoft.android.jaspermobile.FakePreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.PageRequest;
import com.jaspersoft.android.jaspermobile.domain.ReportPage;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ReportPageRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ReportRepository;
import com.jaspersoft.android.sdk.service.rx.report.RxReportExecution;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import rx.Observable;
import rx.observers.TestSubscriber;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class ReloadReportCaseTest {
    private static final String REPORT_URI = "/my/uri";
    private static final String PAGE_POSITION = "100";
    private static final PageRequest PAGE_REQUEST = new PageRequest.Builder()
            .setUri(REPORT_URI)
            .setRange(PAGE_POSITION)
            .asHtml()
            .build();
    private static final ReportPage ANY_PAGE = new ReportPage("page".getBytes(), true);
    @Mock
    ReportRepository mReportRepository;
    @Mock
    ReportPageRepository mReportPageRepository;
    @Mock
    RxReportExecution mRxReportExecution;

    private ReloadReportCase mReloadReportCase;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        mReloadReportCase = new ReloadReportCase(
                FakePreExecutionThread.create(),
                FakePostExecutionThread.create(),
                mReportRepository,
                mReportPageRepository
        );
    }

    @Test
    public void testBuildUseCaseObservable() throws Exception {
        when(mReportRepository.reloadReport(anyString())).thenReturn(Observable.just(mRxReportExecution));
        when(mReportPageRepository.get(any(RxReportExecution.class), any(PageRequest.class))).thenReturn(Observable.just(ANY_PAGE));

        TestSubscriber<ReportPage> test = new TestSubscriber<>();
        mReloadReportCase.execute(PAGE_REQUEST, test);

        verify(mReportRepository).reloadReport(REPORT_URI);
        verify(mReportPageRepository).get(mRxReportExecution, PAGE_REQUEST);
    }
}