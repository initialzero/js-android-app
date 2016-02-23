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
public class GetReportPageContentCaseTest {
    private static final String REPORT_URI = "/my/uri";
    private static final PageRequest PAGE_REQUEST = new PageRequest.Builder()
            .setUri(REPORT_URI)
            .setRange("10")
            .asHtml()
            .build();

    @Mock
    ReportRepository mReportRepository;
    @Mock
    ReportPageRepository mReportPageRepository;
    @Mock
    RxReportExecution mRxReportExecution;

    private GetReportPageContentCase mPageContentCase;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        mPageContentCase = new GetReportPageContentCase(
                FakePreExecutionThread.create(),
                FakePostExecutionThread.create(),
                mReportRepository,
                mReportPageRepository
        );
    }

    @Test
    public void testBuildUseCaseObservable() throws Exception {
        when(mReportRepository.getReport(anyString())).thenReturn(Observable.just(mRxReportExecution));
        when(mReportPageRepository.get(any(RxReportExecution.class), any(PageRequest.class))).thenReturn(Observable.<ReportPage>just(null));

        PageRequest pageRequest = new PageRequest.Builder()
                .setUri(REPORT_URI)
                .setRange("10")
                .asHtml().build();
        TestSubscriber<ReportPage> test = new TestSubscriber<>();
        mPageContentCase.execute(pageRequest, test);

        verify(mReportRepository).getReport(REPORT_URI);
        verify(mReportPageRepository).get(mRxReportExecution, PAGE_REQUEST);
    }
}