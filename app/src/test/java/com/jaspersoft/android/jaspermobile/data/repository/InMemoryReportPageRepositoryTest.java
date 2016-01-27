package com.jaspersoft.android.jaspermobile.data.repository;

import com.jaspersoft.android.jaspermobile.data.cache.report.ReportPageCache;
import com.jaspersoft.android.jaspermobile.data.repository.report.InMemoryReportPageRepository;
import com.jaspersoft.android.jaspermobile.domain.PageRequest;
import com.jaspersoft.android.jaspermobile.domain.ReportPage;
import com.jaspersoft.android.sdk.service.data.report.PageRange;
import com.jaspersoft.android.sdk.service.data.report.ReportExportOutput;
import com.jaspersoft.android.sdk.service.exception.ServiceException;
import com.jaspersoft.android.sdk.service.exception.StatusCodes;
import com.jaspersoft.android.sdk.service.report.ReportExportOptions;
import com.jaspersoft.android.sdk.service.report.ReportFormat;
import com.jaspersoft.android.sdk.service.rx.report.RxReportExecution;
import com.jaspersoft.android.sdk.service.rx.report.RxReportExport;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import rx.Observable;
import rx.observers.TestSubscriber;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class InMemoryReportPageRepositoryTest {

    private static final String REPORT_URI = "/my/uri";
    private static final String PAGE_POSITION = "100";
    private static final PageRequest PAGE_REQUEST = new PageRequest(REPORT_URI, PAGE_POSITION);
    private static final ReportPage ANY_PAGE = new ReportPage("page", true);

    @Mock
    ReportPageCache mReportPageCache;
    @Mock
    RxReportExecution mReportExecution;
    @Mock
    RxReportExport mReportExport;
    @Mock
    ReportExportOutput mReportExportOutput;

    private InMemoryReportPageRepository mInMemoryReportPageRepository;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        setupMocks();
        mInMemoryReportPageRepository = new InMemoryReportPageRepository(mReportPageCache);
    }

    @Test
    public void should_get_page_from_network_if_cache_empty() throws Exception {
        when(mReportPageCache.get(any(PageRequest.class))).thenReturn(null);

        getReport();

        ReportExportOptions options = ReportExportOptions.builder()
                .withFormat(ReportFormat.HTML)
                .withPageRange(PageRange.parse("100"))
                .build();

        verify(mReportPageCache).get(PAGE_REQUEST);
        verify(mReportExecution).export(options);
        verify(mReportExport).download();
        verify(mReportExportOutput).getStream();
        verify(mReportExportOutput).isFinal();
        verify(mReportPageCache).put(PAGE_REQUEST, ANY_PAGE);
    }

    @Test
    public void should_get_page_from_cache_if_one_not_empty() throws Exception {
        when(mReportPageCache.get(any(PageRequest.class))).thenReturn(ANY_PAGE);

        getReport();

        verify(mReportPageCache).get(PAGE_REQUEST);
        verifyNoMoreInteractions(mReportPageCache);
        verifyZeroInteractions(mReportExecution);
    }

    @Test
    public void should_return_empty_page_if_encountered_error() throws Exception {
        when(mReportExport.download()).thenReturn(
                Observable.<ReportExportOutput>error(new ServiceException(null, null, StatusCodes.EXPORT_EXECUTION_FAILED)));

        TestSubscriber<ReportPage> test = getReport();
        test.assertNoErrors();

        verify(mReportPageCache).put(PAGE_REQUEST, ReportPage.EMPTY);
    }

    @Test
    public void should_evict_caches() throws Exception {
        mInMemoryReportPageRepository.flushReportPages(REPORT_URI);
        verify(mReportPageCache).evict(REPORT_URI);
    }

    private TestSubscriber<ReportPage> getReport() {
        TestSubscriber<ReportPage> test = new TestSubscriber<>();
        mInMemoryReportPageRepository.get(mReportExecution, PAGE_REQUEST).subscribe(test);
        return test;
    }

    private void setupMocks() throws IOException {
        when(mReportExportOutput.isFinal()).thenReturn(true);
        when(mReportExportOutput.getStream()).thenReturn(new ByteArrayInputStream("page".getBytes()));
        when(mReportExport.download()).thenReturn(Observable.just(mReportExportOutput));
        when(mReportExecution.export(any(ReportExportOptions.class))).thenReturn(Observable.just(mReportExport));
    }
}