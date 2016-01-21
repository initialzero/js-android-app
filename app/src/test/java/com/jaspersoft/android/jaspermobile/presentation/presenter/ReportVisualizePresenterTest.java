package com.jaspersoft.android.jaspermobile.presentation.presenter;

import com.jaspersoft.android.jaspermobile.FakePostExecutionThread;
import com.jaspersoft.android.jaspermobile.FakePreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.VisualizeTemplate;
import com.jaspersoft.android.jaspermobile.domain.interactor.report.GetVisualizeTemplateCase;
import com.jaspersoft.android.jaspermobile.network.RequestExceptionHandler;
import com.jaspersoft.android.jaspermobile.presentation.page.ReportPageState;
import com.jaspersoft.android.jaspermobile.presentation.view.ReportVisualizeView;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import rx.Observable;
import rx.Subscriber;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class ReportVisualizePresenterTest {

    public static final VisualizeTemplate VIS_TEMPLATE = new VisualizeTemplate("content", "url");
    public static final double SCREEN_DIAGONAL = 10.1;

    private ReportVisualizePresenter mReportVisualizePresenter;
    private FakeGetVisualizeTemplateCase mGetVisualizeTemplateCase;
    private FakeGetReportShowControlsPropertyCase mGetReportShowControlsPropertyCase;

    @Mock
    RequestExceptionHandler mExceptionHandler;
    @Mock
    ReportVisualizeView mView;

    private ReportPageState fakeState;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        setUpMocks();
        mReportVisualizePresenter = new ReportVisualizePresenter(
                SCREEN_DIAGONAL,
                mExceptionHandler,
                mGetReportShowControlsPropertyCase,
                mGetVisualizeTemplateCase
        );
        mReportVisualizePresenter.injectView(mView);
    }

    @Test
    public void should_request_input_controls_on_second_run() throws Exception {
        fakeState.setControlsPageShown(false);

        mReportVisualizePresenter.init();

        verify(mView).showLoading();
        verify(mGetReportShowControlsPropertyCase).execute(any(Subscriber.class));
        verify(mView).hideLoading();
    }

    @Test
    public void should_display_controls_page_if_report_requires_controls() throws Exception {
        fakeState.setControlsPageShown(false);
        mGetReportShowControlsPropertyCase.setNeedParams(true);

        mReportVisualizePresenter.init();

        verify(fakeState).setControlsPageShown(true);
        verify(mView).setFilterActionVisibility(true);
        verify(mView).showInitialFiltersPage();
    }

    @Test
    public void should_load_template_if_report_not_requires_controls() throws Exception {
        fakeState.setControlsPageShown(false);
        mGetReportShowControlsPropertyCase.setNeedParams(false);

        mReportVisualizePresenter.init();

        verify(mGetVisualizeTemplateCase).execute(eq(SCREEN_DIAGONAL), any(Subscriber.class));

        verify(fakeState).setControlsPageShown(true);
        verify(mView).setFilterActionVisibility(false);
        verify(mView).loadTemplateInView(VIS_TEMPLATE);
    }

    private void setUpMocks() {
        fakeState = spy(new ReportPageState());
        mGetVisualizeTemplateCase = spy(new FakeGetVisualizeTemplateCase());
        mGetReportShowControlsPropertyCase = spy(new FakeGetReportShowControlsPropertyCase());

        when(mView.getState()).thenReturn(fakeState);
    }

    private static class FakeGetVisualizeTemplateCase extends GetVisualizeTemplateCase {
        public FakeGetVisualizeTemplateCase() {
            super(FakePreExecutionThread.create(), FakePostExecutionThread.create(), null, null);
        }

        @Override
        protected Observable<VisualizeTemplate> buildUseCaseObservable(Double diagonal) {
            return Observable.just(VIS_TEMPLATE);
        }
    }
}