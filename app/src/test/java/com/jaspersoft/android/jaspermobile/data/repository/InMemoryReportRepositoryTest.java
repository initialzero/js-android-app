package com.jaspersoft.android.jaspermobile.data.repository;

import com.jaspersoft.android.jaspermobile.data.JasperRestClient;
import com.jaspersoft.android.jaspermobile.data.cache.report.ReportCache;
import com.jaspersoft.android.jaspermobile.data.cache.report.ReportPageCache;
import com.jaspersoft.android.jaspermobile.data.cache.report.ReportParamsCache;
import com.jaspersoft.android.jaspermobile.data.entity.mapper.ReportParamsMapper;
import com.jaspersoft.android.jaspermobile.data.repository.report.InMemoryReportRepository;
import com.jaspersoft.android.sdk.network.entity.report.ReportParameter;
import com.jaspersoft.android.sdk.service.report.ReportExecutionOptions;
import com.jaspersoft.android.sdk.service.report.ReportFormat;
import com.jaspersoft.android.sdk.service.rx.report.RxReportExecution;
import com.jaspersoft.android.sdk.service.rx.report.RxReportService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.observers.TestSubscriber;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class InMemoryReportRepositoryTest {

    private static final String REPORT_URI = "/my/uri";
    private static final List<com.jaspersoft.android.sdk.client.oxm.report.ReportParameter> LEGACY_REPORT_PARAMS = Collections.singletonList(null);
    private static final List<ReportParameter> REPORT_PARAMS = Collections.singletonList(null);

    @Mock
    ReportPageCache mReportPageCache;
    @Mock
    RxReportService mRxReportService;
    @Mock
    RxReportExecution mRxReportExecution;
    @Mock
    ReportParamsMapper mReportParamsMapper;
    @Mock
    ReportCache mReportCache;

    @Mock
    ReportParamsCache mReportParamsCache;
    @Mock
    JasperRestClient mJasperRestClient;

    private InMemoryReportRepository inMemoryReportRepository;
    public static final ReportExecutionOptions EXECUTION_OPTIONS = ReportExecutionOptions.builder()
            .withFormat(ReportFormat.HTML)
            .withFreshData(false)
            .withParams(REPORT_PARAMS)
            .build();

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        setupMocks();
        inMemoryReportRepository = new InMemoryReportRepository(
                mJasperRestClient,
                mReportPageCache,
                mReportParamsCache,
                mReportParamsMapper,
                mReportCache
        );
    }

    @Test
    public void repository_fetches_from_network_when_cache_is_empty() throws Exception {
        inMemoryReportRepository.getReport(REPORT_URI).subscribe();

        verify(mRxReportService).run(REPORT_URI, EXECUTION_OPTIONS);
        verify(mReportCache).get(REPORT_URI);
        verify(mReportCache).put(eq(REPORT_URI), any(RxReportExecution.class));
    }

    @Test
    public void repository_fetches_from_cache_if_not_empty() throws Exception {
        when(mReportCache.get(anyString())).thenReturn(mRxReportExecution);

        inMemoryReportRepository.getReport(REPORT_URI).subscribe();

        verifyZeroInteractions(mRxReportService);
        verify(mReportCache).get(REPORT_URI);
        verifyNoMoreInteractions(mReportCache);
    }

    @Test
    public void should_evict_caches_while_reload_and_get_new_report() throws Exception {
        TestSubscriber<RxReportExecution> test = new TestSubscriber<>();

        inMemoryReportRepository.reloadReport(REPORT_URI).subscribe(test);

        verify(mReportPageCache).evict(REPORT_URI);
        verify(mReportCache).evict(REPORT_URI);
        verify(mRxReportService).run(REPORT_URI, EXECUTION_OPTIONS);
        verify(mReportCache).put(eq(REPORT_URI), any(RxReportExecution.class));
    }

    @Test
    public void should_update_report_object_and_evict_caches() throws Exception {
        TestSubscriber<RxReportExecution> test = new TestSubscriber<>();
        inMemoryReportRepository.updateReport(REPORT_URI).subscribe(test);

        verify(mReportPageCache).evict(REPORT_URI);
        verify(mReportCache).evict(REPORT_URI);
        verify(mRxReportExecution).updateExecution(REPORT_PARAMS);
        verify(mReportCache).put(eq(REPORT_URI), any(RxReportExecution.class));
    }

    @Test
    public void should_evict_caches() throws Exception {
        inMemoryReportRepository.flushReport(REPORT_URI);
        verify(mReportPageCache).evict(REPORT_URI);
        verify(mReportParamsCache).evict(REPORT_URI);
    }

    private void setupMocks() {
        when(mJasperRestClient.reportService()).thenReturn(Observable.just(mRxReportService));
        when(mRxReportService.run(anyString(), any(ReportExecutionOptions.class)))
                .thenReturn(Observable.just(mRxReportExecution));
        when(mReportParamsMapper.legacyParamsToRetrofitted(anyList())).thenReturn(REPORT_PARAMS);
        when(mReportCache.get(anyString())).thenReturn(null);
        when(mReportParamsCache.get(anyString())).thenReturn(LEGACY_REPORT_PARAMS);
    }
}