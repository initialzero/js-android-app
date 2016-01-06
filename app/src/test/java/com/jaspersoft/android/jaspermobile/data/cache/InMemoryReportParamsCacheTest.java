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
    ReportParamsStorage mReportParamsStorage;
    @Mock
    InputControlHolder mInputControlHolder;


    private InMemoryReportParamsCache mInMemoryReportParamsCache;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        setupMocks();
        mInMemoryReportParamsCache = new InMemoryReportParamsCache(mReportParamsStorage);
    }

    @Test
    public void testPut() throws Exception {
        mInMemoryReportParamsCache.put(REPORT_URI, LEGACY_REPORT_PARAMS);
        verify(mReportParamsStorage).getInputControlHolder(REPORT_URI);
        verify(mInputControlHolder).setReportParams(LEGACY_REPORT_PARAMS);
    }

    @Test
    public void testGet() throws Exception {
        mInMemoryReportParamsCache.get(REPORT_URI);
        verify(mReportParamsStorage).getInputControlHolder(REPORT_URI);
        verify(mInputControlHolder).getReportParams();
    }

    private void setupMocks() {
        when(mReportParamsStorage.getInputControlHolder(anyString())).thenReturn(mInputControlHolder);
        when(mInputControlHolder.getReportParams()).thenReturn(LEGACY_REPORT_PARAMS);
    }
}