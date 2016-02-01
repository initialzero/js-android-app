package com.jaspersoft.android.jaspermobile.domain.interactor;

import com.jaspersoft.android.jaspermobile.FakePostExecutionThread;
import com.jaspersoft.android.jaspermobile.FakePreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.interactor.report.GetReportShowControlsPropertyCase;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ControlsRepository;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.observers.TestSubscriber;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class GetReportShowControlsPropertyCaseTest {
    private static final String REPORT_URI = "/my/uri";

    @Mock
    ControlsRepository mControlsRepository;

    private GetReportShowControlsPropertyCase mGetReportShowControlsPropertyCase;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        mGetReportShowControlsPropertyCase = new GetReportShowControlsPropertyCase(
                FakePreExecutionThread.create(),
                FakePostExecutionThread.create(),
                mControlsRepository
        );
    }

    @Test
    public void execute_fetches_controls() throws Exception {
        when(mControlsRepository.listReportControls(anyString()))
                .thenReturn(Observable.<List<InputControl>>just(Collections.<InputControl>emptyList()));

        TestSubscriber<Boolean> test = new TestSubscriber<>();
        mGetReportShowControlsPropertyCase.execute(REPORT_URI, test);

        verify(mControlsRepository).listReportControls(REPORT_URI);
    }
}