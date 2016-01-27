package com.jaspersoft.android.jaspermobile.data.repository;

import com.jaspersoft.android.jaspermobile.Chain;
import com.jaspersoft.android.jaspermobile.data.cache.report.ControlsCache;
import com.jaspersoft.android.jaspermobile.data.entity.mapper.InputControlsMapper;
import com.jaspersoft.android.jaspermobile.data.repository.report.InMemoryControlsRepository;
import com.jaspersoft.android.sdk.network.entity.control.InputControl;
import com.jaspersoft.android.sdk.service.rx.report.RxFiltersService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.observers.TestSubscriber;

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
    public static final List<InputControl> CONTROLS = Collections.emptyList();
    public static final List<com.jaspersoft.android.sdk.client.oxm.control.InputControl> LEGACY_CONTROLS = Collections.emptyList();

    @Mock
    RxFiltersService mFiltersService;
    @Mock
    ControlsCache mControlsCache;
    @Mock
    InputControlsMapper mInputControlsMapper;


    private InMemoryControlsRepository inMemoryControlsRepository;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        setupMocks();
        inMemoryControlsRepository =
                new InMemoryControlsRepository(mFiltersService, mControlsCache, mInputControlsMapper);
    }

    @Test
    public void controls_listing_fetches_network() throws Exception {
        requestControls();
        verify(mFiltersService).listControls(REPORT_URI);
    }

    @Test
    public void caches_controls_on_run() throws Exception {
        requestControls();
        verify(mControlsCache).put(REPORT_URI, LEGACY_CONTROLS);
    }

    @Test
    public void returns_cached_item_on_second_run() throws Exception {
        requestControls();
        verify(mFiltersService).listControls(REPORT_URI);

        requestControls();
        verifyNoMoreInteractions(mFiltersService);

        verify(mControlsCache, times(2)).get(REPORT_URI);
    }

    @Test
    public void should_evict_caches() throws Exception {
        inMemoryControlsRepository.flushControls(REPORT_URI);
        verify(mControlsCache).evict(REPORT_URI);
    }

    private void requestControls() {
        TestSubscriber<List<com.jaspersoft.android.sdk.client.oxm.control.InputControl>> test = new TestSubscriber<>();
        inMemoryControlsRepository.listControls(REPORT_URI).subscribe(test);
        test.assertNoErrors();
        test.assertCompleted();
    }

    private void setupMocks() {
        when(mFiltersService.listControls(anyString()))
                .thenReturn(Observable.just(CONTROLS));
        when(mInputControlsMapper.transform(CONTROLS)).thenReturn(LEGACY_CONTROLS);

        Chain<List<InputControl>> answer = Chain.of(null, CONTROLS);
        when(mControlsCache.get(anyString())).then(answer);
    }
}