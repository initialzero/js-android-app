package com.jaspersoft.android.jaspermobile.data.repository;

import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.data.repository.report.InMemoryReportPropertyRepository;
import com.jaspersoft.android.jaspermobile.domain.Report;
import com.jaspersoft.android.jaspermobile.domain.ReportPage;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ReportPageRepository;
import com.jaspersoft.android.sdk.service.data.report.ReportMetadata;
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
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class InMemoryReportPropertyRepositoryTest {

    private static final ReportPage ANY_PAGE = new ReportPage("page", false);
    private static final String REPORT_URI = "/my/uri";
    private static final int TOTAL_PAGES = 100;

    @Mock
    Report mReport;
    @Mock
    RxReportExecution mReportExecution;

    @Mock
    ReportPageRepository mReportPageRepository;

    private InMemoryReportPropertyRepository mInMemoryReportPropertyRepository;
    private final ReportMetadata fakeReportMetadata = new ReportMetadata(REPORT_URI, TOTAL_PAGES);

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        setupMocks();
        mInMemoryReportPropertyRepository = new InMemoryReportPropertyRepository(mReportPageRepository);
    }

    @Test
    public void should_perform_additional_import_if_multipage_property_missing() throws Exception {
        when(mReport.getMultiPage()).thenReturn(null);

        TestSubscriber<Boolean> test = getMultiPageProperty();

        test.assertNoErrors();
        assertThat(test.getOnNextEvents(), hasItem(true));

        verify(mReportPageRepository).get(mReport, "2");
        verify(mReport).setMultiPage(true);
    }

    @Test
    public void should_return_cached_value_multipage() throws Exception {
        when(mReport.getMultiPage()).thenReturn(true);

        TestSubscriber<Boolean> test = getMultiPageProperty();

        test.assertNoErrors();
        assertThat(test.getOnNextEvents(), hasItem(true));

        verifyZeroInteractions(mReportPageRepository);
    }

    @Test
    public void should_perform_additional_import_if_total_pages_property_missing() throws Exception {
        when(mReport.getTotalPages()).thenReturn(null);

        TestSubscriber<Integer> test = getTotalPagesProperty();

        test.assertNoErrors();
        assertThat(test.getOnNextEvents(), hasItem(TOTAL_PAGES));

        verify(mReportExecution).waitForReportCompletion();
        verify(mReport).setTotalPages(TOTAL_PAGES);
    }

    @Test
    public void should_return_cached_value_total_pages() throws Exception {
        when(mReport.getTotalPages()).thenReturn(200);

        TestSubscriber<Integer> test = getTotalPagesProperty();

        test.assertNoErrors();
        assertThat(test.getOnNextEvents(), hasItem(200));

        verifyZeroInteractions(mReportExecution);
    }

    @Test
    public void should_return_negative_result_if_empty_page_returned() throws Exception {
        when(mReportPageRepository.get(any(Report.class), anyString()))
                .thenReturn(Observable.just(ReportPage.EMPTY));

        TestSubscriber<Boolean> test = getMultiPageProperty();

        test.assertNoErrors();
        assertThat(test.getOnNextEvents(), hasItem(false));
    }

    @NonNull
    private TestSubscriber<Boolean> getMultiPageProperty() {
        TestSubscriber<Boolean> test = new TestSubscriber<>();
        mInMemoryReportPropertyRepository.getMultiPageProperty(mReport).subscribe(test);
        return test;
    }

    @NonNull
    private TestSubscriber<Integer> getTotalPagesProperty() {
        TestSubscriber<Integer> test = new TestSubscriber<>();
        mInMemoryReportPropertyRepository.getTotalPagesProperty(mReport).subscribe(test);
        return test;
    }

    private void setupMocks() {
        when(mReportPageRepository.get(any(Report.class), anyString()))
                .thenReturn(Observable.just(ANY_PAGE));
        when(mReport.getExecution()).thenReturn(mReportExecution);
        when(mReportExecution.waitForReportCompletion()).thenReturn(Observable.just(fakeReportMetadata));
    }
}