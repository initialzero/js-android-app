package com.jaspersoft.android.jaspermobile.data.repository.report;

import com.jaspersoft.android.jaspermobile.data.JasperRestClient;
import com.jaspersoft.android.jaspermobile.data.entity.mapper.InputControlsMapper;
import com.jaspersoft.android.jaspermobile.data.entity.mapper.ReportParamsMapper;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
import com.jaspersoft.android.sdk.network.entity.control.InputControlState;
import com.jaspersoft.android.sdk.service.data.report.option.ReportOption;
import com.jaspersoft.android.sdk.service.rx.filter.RxFiltersService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import rx.Observable;
import rx.observers.TestSubscriber;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class InMemoryReportOptionsRepositoryTest {

    private static final String REPORT_URI = "/my/uri";
    private static final String OPTION_LABEL = "label";
    private static final String OPTION_ID = "id";

    private static final List<ReportParameter> LEGACY_REPORT_PARAMS = Collections.emptyList();
    private static final List<com.jaspersoft.android.sdk.network.entity.report.ReportParameter> REPORT_PARAMS = Collections.emptyList();
    private static final List<InputControlState> STATES = Collections.emptyList();
    private static final List<com.jaspersoft.android.sdk.client.oxm.control.InputControlState> LEGACY_STATES = Collections.emptyList();

    @Mock
    RxFiltersService mFiltersService;

    @Mock
    ReportParamsMapper mReportParamsMapper;
    @Mock
    InputControlsMapper mControlsMapper;
    @Mock
    ReportOption mReportOption;
    @Mock
    JasperRestClient mJasperRestClient;

    private InMemoryReportOptionsRepository mInMemoryReportOptionsRepository;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        setupMocks();
        mInMemoryReportOptionsRepository = new InMemoryReportOptionsRepository(
                mJasperRestClient,
                mReportParamsMapper,
                mControlsMapper
        );
    }

    private void setupMocks() {
        when(mJasperRestClient.filtersService())
                .thenReturn(Observable.just(mFiltersService));
        when(mFiltersService.createReportOption(anyString(), anyString(),
                anyListOf(com.jaspersoft.android.sdk.network.entity.report.ReportParameter.class), anyBoolean()))
                .thenReturn(Observable.just(mReportOption));
        when(mFiltersService.listResourceStates(anyString(), anyBoolean()))
                .thenReturn(Observable.just(STATES));

        when(mControlsMapper.retrofittedStatesToLegacy(STATES)).thenReturn(LEGACY_STATES);

        Set<ReportOption> options = Collections.singleton(mReportOption);
        when(mFiltersService.listReportOptions(anyString())).thenReturn(Observable.just(options));
        when(mFiltersService.deleteReportOption(anyString(), anyString())).thenReturn(Observable.<Void>just(null));
    }

    @Test
    public void should_list_report_options() throws Exception {
        TestSubscriber<Set<ReportOption>> test = new TestSubscriber<>();
        mInMemoryReportOptionsRepository.getReportOption(REPORT_URI).subscribe(test);
        test.assertNoErrors();
        verify(mFiltersService).listReportOptions(REPORT_URI);
    }

    @Test
    public void should_create_report_option() throws Exception {
        when(mReportParamsMapper.legacyParamsToRetrofitted(anyListOf(ReportParameter.class))).thenReturn(REPORT_PARAMS);

        TestSubscriber<ReportOption> test = new TestSubscriber<>();
        mInMemoryReportOptionsRepository.createReportOptionWithOverride(REPORT_URI, OPTION_LABEL, LEGACY_REPORT_PARAMS).subscribe(test);
        test.assertNoErrors();

        verify(mReportParamsMapper).legacyParamsToRetrofitted(LEGACY_REPORT_PARAMS);
        verify(mFiltersService).createReportOption(REPORT_URI, OPTION_LABEL, REPORT_PARAMS, true);
    }

    @Test
    public void should_return_report_option_states() throws Exception {
        TestSubscriber<List<com.jaspersoft.android.sdk.client.oxm.control.InputControlState>> test = new TestSubscriber<>();
        mInMemoryReportOptionsRepository.getReportOptionStates(REPORT_URI).subscribe(test);
        test.assertNoErrors();

        verify(mControlsMapper).retrofittedStatesToLegacy(STATES);
        verify(mFiltersService).listResourceStates(REPORT_URI, true);
    }

    @Test
    public void should_delete_report_option() throws Exception {
        TestSubscriber<Void> test = new TestSubscriber<>();
        mInMemoryReportOptionsRepository.deleteReportOption(REPORT_URI, OPTION_ID).subscribe(test);
        test.assertNoErrors();

        verify(mFiltersService).deleteReportOption(REPORT_URI, OPTION_ID);
    }
}