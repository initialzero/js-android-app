package com.jaspersoft.android.jaspermobile.data.cache;

import com.jaspersoft.android.jaspermobile.data.cache.report.InMemoryReportParamsCache;
import com.jaspersoft.android.jaspermobile.util.InputControlHolder;
import com.jaspersoft.android.jaspermobile.util.ReportParamsStorage;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class InMemoryReportParamsCacheTest {

    private static final List<com.jaspersoft.android.sdk.client.oxm.report.ReportParameter> LEGACY_REPORT_PARAMS = Collections.singletonList(null);
    private static final String REPORT_URI = "/my/uri";

    @Mock
    ReportParamsStorage mParamsStorage;
    @Mock
    InputControlHolder mInputControlHolder;


    private InMemoryReportParamsCache mCache;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        setupMocks();
        mCache = new InMemoryReportParamsCache(mParamsStorage);
    }

    @Test
    public void testPut() throws Exception {
        mCache.put(REPORT_URI, LEGACY_REPORT_PARAMS);
        verify(mParamsStorage).getInputControlHolder(REPORT_URI);
        verify(mInputControlHolder).setReportParams(LEGACY_REPORT_PARAMS);
    }

    @Test
    public void testGet() throws Exception {
        mCache.get(REPORT_URI);
        verify(mParamsStorage).getInputControlHolder(REPORT_URI);
        verify(mInputControlHolder).getReportParams();
    }

    @Test
    public void should_evict_collection_from_holder() throws Exception {
        mCache.evict(REPORT_URI);
        verify(mParamsStorage).clearInputControlHolder(REPORT_URI);
    }

    private void setupMocks() {
        when(mParamsStorage.getInputControlHolder(anyString())).thenReturn(mInputControlHolder);
        when(mInputControlHolder.getReportParams()).thenReturn(LEGACY_REPORT_PARAMS);
    }
}