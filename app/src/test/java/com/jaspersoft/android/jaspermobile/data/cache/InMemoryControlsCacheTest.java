package com.jaspersoft.android.jaspermobile.data.cache;


import com.jaspersoft.android.jaspermobile.data.cache.report.InMemoryControlsCache;
import com.jaspersoft.android.jaspermobile.util.InputControlHolder;
import com.jaspersoft.android.jaspermobile.util.ReportParamsStorage;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;

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
public class InMemoryControlsCacheTest {
    private static final String REPORT_URI = "my/uri";
    public static final List<InputControl> CONTROLS = Collections.emptyList();

    @Mock
    ReportParamsStorage mParamsStorage;
    @Mock
    InputControlHolder mInputControlHolder;

    private InMemoryControlsCache mCache;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        setupMocks();
        mCache = new InMemoryControlsCache(mParamsStorage);
    }

    @Test
    public void should_delegate_put_action_to_report_params_storage() throws Exception {
        mCache.put(REPORT_URI, CONTROLS);
        verify(mParamsStorage).getInputControlHolder(REPORT_URI);
        verify(mInputControlHolder).setInputControls(CONTROLS);
    }

    @Test
    public void should_delegate_get_action_to_report_params_storage() throws Exception {
        mCache.get(REPORT_URI);
        verify(mParamsStorage).getInputControlHolder(REPORT_URI);
        verify(mInputControlHolder).getInputControls();
    }

    private void setupMocks() {
        when(mParamsStorage.getInputControlHolder(anyString())).thenReturn(mInputControlHolder);
    }
}