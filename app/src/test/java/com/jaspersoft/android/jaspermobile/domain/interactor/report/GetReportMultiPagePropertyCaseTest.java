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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class GetReportMultiPagePropertyCaseTest {
    private static final String REPORT_URI = "/my/uri";
    private static final PageRequest SECOND_PAGE_REQUEST = new PageRequest(REPORT_URI, "2");
    private static final ReportPage ANY_PAGE = new ReportPage("page", true);

    @Mock
    ReportRepository mReportRepository;
    @Mock
    ReportPageRepository mReportPageRepository;
    @Mock
    RxReportExecution mRxReportExecution;

    private GetReportMultiPagePropertyCase mMultiPagePropertyCase;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        mMultiPagePropertyCase = new GetReportMultiPagePropertyCase(FakePreExecutionThread.create(),
                FakePostExecutionThread.create(),
                mReportRepository,
                mReportPageRepository
        );
    }

    @Test
    public void testBuildUseCaseObservable() throws Exception {
        when(mReportRepository.getReport(anyString())).thenReturn(Observable.just(mRxReportExecution));
        when(mReportPageRepository.get(any(RxReportExecution.class), any(PageRequest.class))).thenReturn(Observable.just(ANY_PAGE));

        TestSubscriber<Boolean> test = new TestSubscriber<>();
        mMultiPagePropertyCase.execute(REPORT_URI, test);
        assertThat(test.getOnNextEvents(), hasItem(true));

        verify(mReportRepository).getReport(REPORT_URI);
        verify(mReportPageRepository).get(mRxReportExecution, SECOND_PAGE_REQUEST);
    }
}