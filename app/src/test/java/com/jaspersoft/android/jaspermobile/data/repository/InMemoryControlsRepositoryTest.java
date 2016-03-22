package com.jaspersoft.android.jaspermobile.data.repository;

import com.jaspersoft.android.jaspermobile.Chain;
import com.jaspersoft.android.jaspermobile.data.JasperRestClient;
import com.jaspersoft.android.jaspermobile.data.cache.report.ControlsCache;
import com.jaspersoft.android.jaspermobile.data.cache.report.ReportParamsCache;
import com.jaspersoft.android.jaspermobile.data.entity.mapper.InputControlsMapper;
import com.jaspersoft.android.jaspermobile.data.entity.mapper.ReportParamsMapper;
import com.jaspersoft.android.jaspermobile.data.repository.report.InMemoryControlsRepository;
import com.jaspersoft.android.sdk.network.entity.control.InputControl;
import com.jaspersoft.android.sdk.network.entity.control.InputControlState;
import com.jaspersoft.android.sdk.network.entity.report.ReportParameter;
import com.jaspersoft.android.sdk.service.rx.filter.RxFiltersService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.observers.TestSubscriber;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class InMemoryControlsRepositoryTest {

    private static final String REPORT_URI = "my/uri";
    private static final List<InputControl> CONTROLS = Collections.emptyList();
    private static final List<InputControlState> STATES = Collections.emptyList();
    private static final List<com.jaspersoft.android.sdk.client.oxm.control.InputControlState> LEGACY_STATES = Collections.emptyList();
    private static final List<com.jaspersoft.android.sdk.client.oxm.control.InputControl> LEGACY_CONTROLS = Collections.emptyList();
    private static final List<com.jaspersoft.android.sdk.client.oxm.report.ReportParameter> LEGACY_REPORT_PARAMETERS = Collections.emptyList();
    private static final List<ReportParameter> REPORT_PARAMETERS = Collections.emptyList();

    @Mock
    RxFiltersService mFiltersService;
    @Mock
    ControlsCache mControlsCache;
    @Mock
    ReportParamsCache mReportParamsCache;
    @Mock
    InputControlsMapper mInputControlsMapper;
    @Mock
    ReportParamsMapper mReportParamsMapper;

    @Mock
    JasperRestClient mJasperRestClient;

    private InMemoryControlsRepository inMemoryControlsRepository;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        setupMocks();
        inMemoryControlsRepository = new InMemoryControlsRepository(
                mJasperRestClient,
                mControlsCache,
                mReportParamsCache,
                mInputControlsMapper,
                mReportParamsMapper
        );
    }

    @Test
    public void returns_cached_item_on_second_run() throws Exception {
        Chain<List<InputControl>> answer = Chain.of(null, CONTROLS);
        when(mControlsCache.get(anyString())).then(answer);

        requestControls();
        verify(mFiltersService).listReportControls(REPORT_URI);

        requestControls();
        verifyNoMoreInteractions(mFiltersService);

        verify(mControlsCache, times(2)).get(REPORT_URI);
    }

    @Test
    public void should_evict_caches() throws Exception {
        inMemoryControlsRepository.flushControls(REPORT_URI);
        verify(mControlsCache).evict(REPORT_URI);
    }

    @Test
    public void should_request_input_control_states() throws Exception {
        when(mControlsCache.get(anyString())).thenReturn(LEGACY_CONTROLS);

        TestSubscriber<List<com.jaspersoft.android.sdk.client.oxm.control.InputControlState>> test = new TestSubscriber<>();
        inMemoryControlsRepository.listControlValues(REPORT_URI).subscribe(test);

        verify(mControlsCache).get(REPORT_URI);
        verify(mReportParamsMapper).legacyControlsToParams(LEGACY_CONTROLS);
        verify(mReportParamsMapper).legacyParamsToRetrofitted(LEGACY_REPORT_PARAMETERS);
        verify(mFiltersService).listControlsStates(REPORT_URI, REPORT_PARAMETERS, true);
        verify(mInputControlsMapper).retrofittedStatesToLegacy(STATES);
    }

    private void requestControls() {
        TestSubscriber<List<com.jaspersoft.android.sdk.client.oxm.control.InputControl>> test = new TestSubscriber<>();
        inMemoryControlsRepository.listReportControls(REPORT_URI).subscribe(test);
        test.assertNoErrors();
        test.assertCompleted();
    }

    private void setupMocks() {
        when(mJasperRestClient.filtersService()).thenReturn(Observable.just(mFiltersService));
        when(mFiltersService.listReportControls(anyString()))
                .thenReturn(Observable.just(CONTROLS));
        when(mFiltersService.validateControls(anyString(), anyListOf(ReportParameter.class), anyBoolean()))
                .thenReturn(Observable.just(STATES));
       when(mFiltersService.listControlsStates(anyString(), anyListOf(ReportParameter.class), anyBoolean()))
                .thenReturn(Observable.just(STATES));

        when(mInputControlsMapper.retrofittedControlsToLegacy(CONTROLS)).thenReturn(LEGACY_CONTROLS);
        when(mControlsCache.get(anyString())).thenReturn(LEGACY_CONTROLS);

        when(mReportParamsCache.get(anyString())).thenReturn(LEGACY_REPORT_PARAMETERS);
        when(mReportParamsMapper.legacyParamsToRetrofitted(LEGACY_REPORT_PARAMETERS)).thenReturn(REPORT_PARAMETERS);
    }
}