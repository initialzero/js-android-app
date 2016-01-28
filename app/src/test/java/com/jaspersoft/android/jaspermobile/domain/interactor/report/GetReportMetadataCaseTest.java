package com.jaspersoft.android.jaspermobile.domain.interactor.report;

import com.jaspersoft.android.jaspermobile.FakePostExecutionThread;
import com.jaspersoft.android.jaspermobile.FakePreExecutionThread;
import com.jaspersoft.android.jaspermobile.data.cache.report.ReportParamsCache;
import com.jaspersoft.android.jaspermobile.data.entity.mapper.ReportParamsMapper;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ReportPageRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ReportRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ResourceRepository;
import com.jaspersoft.android.jaspermobile.visualize.ReportData;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
import com.jaspersoft.android.sdk.service.data.report.ReportResource;
import com.jaspersoft.android.sdk.service.rx.report.RxReportExecution;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rx.Observable;
import rx.observers.TestSubscriber;

import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class GetReportMetadataCaseTest {

    private static final String REPORT_URI = "/my/uri";
    private static final Map<String, Set<String>> REPORT_PARAMS = Collections.emptyMap();
    private static final List<ReportParameter> LEGACY_REPORT_PARAMS = Collections.emptyList();

    @Mock
    ReportRepository mReportRepository;
    @Mock
    ReportPageRepository mReportPageRepository;
    @Mock
    RxReportExecution mRxReportExecution;
    @Mock
    ReportParamsMapper mReportParamsMapper;

    @Mock
    ResourceRepository mResourceRepository;
    @Mock
    ReportParamsCache mReportParamsCache;

    @Mock
    ReportData mReportData;
    @Mock
    ReportResource mReportResource;

    private GetReportMetadataCase mGetReportMetadataCase;


    @Before
    public void setUp() throws Exception {
        initMocks(this);
        setupMocks();
        mGetReportMetadataCase = new GetReportMetadataCase(
                FakePreExecutionThread.create(),
                FakePostExecutionThread.create(),
                mResourceRepository,
                mReportParamsCache,
                mReportParamsMapper
        );
    }

    @Test
    public void should_save_in_cache() throws Exception {
        performExecute();

        verify(mReportParamsMapper).mapToLegacyParams(REPORT_PARAMS);
        verify(mReportParamsCache).put(REPORT_URI, LEGACY_REPORT_PARAMS);
    }

    @Test
    public void should_request_report_Details() throws Exception {
        performExecute();

        verify(mResourceRepository).getReportResource(REPORT_URI);
    }

    private void performExecute() {
        TestSubscriber<ReportResource> test = new TestSubscriber<>();
        mGetReportMetadataCase.execute(mReportData, test);
        test.assertNoErrors();
    }

    private void setupMocks() {
        when(mResourceRepository.getReportResource(anyString()))
                .thenReturn(Observable.just(mReportResource));
        when(mReportData.getResource()).thenReturn(REPORT_URI);
        when(mReportData.getParams()).thenReturn(REPORT_PARAMS);
        when(mReportParamsMapper.mapToLegacyParams(anyMap()))
                .thenReturn(LEGACY_REPORT_PARAMS);
    }
}