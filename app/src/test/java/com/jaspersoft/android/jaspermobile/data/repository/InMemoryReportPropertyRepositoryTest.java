package com.jaspersoft.android.jaspermobile.data.repository;

import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.data.cache.report.ReportPropertyCache;
import com.jaspersoft.android.jaspermobile.data.repository.report.InMemoryReportPropertyRepository;
import com.jaspersoft.android.jaspermobile.domain.ReportPage;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ReportPageRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ReportRepository;
import com.jaspersoft.android.sdk.service.data.report.ReportMetadata;
import com.jaspersoft.android.sdk.service.rx.report.RxReportExecution;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import rx.Observable;
import rx.observers.TestSubscriber;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
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

    private static final String REPORT_URI = "/my/uri";
    private static final int TOTAL_PAGES = 100;

    @Mock
    RxReportExecution mReportExecution;
    @Mock
    ReportPropertyCache mReportPropertyCache;

    @Mock
    ReportPageRepository mReportPageRepository;

    private InMemoryReportPropertyRepository mInMemoryReportPropertyRepository;
    private final ReportMetadata fakeReportMetadata = new ReportMetadata(REPORT_URI, TOTAL_PAGES);

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        setupMocks();
        mInMemoryReportPropertyRepository = new InMemoryReportPropertyRepository(
                mReportPropertyCache
        );
    }

    @Test
    public void should_perform_additional_import_if_total_pages_property_missing() throws Exception {
        when(mReportPropertyCache.getTotalPages(anyString())).thenReturn(null);

        TestSubscriber<Integer> test = getTotalPagesProperty();

        test.assertNoErrors();
        assertThat(test.getOnNextEvents(), hasItem(TOTAL_PAGES));

        verify(mReportExecution).waitForReportCompletion();
        verify(mReportPropertyCache).putTotalPages(REPORT_URI, 100);
    }

    @Test
    public void should_return_cached_value_total_pages() throws Exception {
        when(mReportPropertyCache.getTotalPages(anyString())).thenReturn(200);

        TestSubscriber<Integer> test = getTotalPagesProperty();

        test.assertNoErrors();
        assertThat(test.getOnNextEvents(), hasItem(200));

        verifyZeroInteractions(mReportExecution);
    }


    @Test
    public void should_evict_caches() throws Exception {
        mInMemoryReportPropertyRepository.flushReportProperties(REPORT_URI);
        verify(mReportPropertyCache).evict(REPORT_URI);
    }

    @NonNull
    private TestSubscriber<Integer> getTotalPagesProperty() {
        TestSubscriber<Integer> test = new TestSubscriber<>();
        mInMemoryReportPropertyRepository.getTotalPagesProperty(mReportExecution, REPORT_URI).subscribe(test);
        return test;
    }

    private void setupMocks() {
        when(mReportExecution.waitForReportCompletion()).thenReturn(Observable.just(fakeReportMetadata));
    }
}