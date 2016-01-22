package com.jaspersoft.android.jaspermobile.presentation.presenter;

import com.jaspersoft.android.jaspermobile.FakePostExecutionThread;
import com.jaspersoft.android.jaspermobile.FakePreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.VisualizeTemplate;
import com.jaspersoft.android.jaspermobile.domain.interactor.report.GetJsonParamsCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.report.GetVisualizeTemplateCase;
import com.jaspersoft.android.jaspermobile.network.RequestExceptionHandler;
import com.jaspersoft.android.jaspermobile.presentation.model.visualize.ErrorEvent;
import com.jaspersoft.android.jaspermobile.presentation.model.visualize.ExecutionReferenceClickEvent;
import com.jaspersoft.android.jaspermobile.presentation.model.visualize.ExternalReferenceClickEvent;
import com.jaspersoft.android.jaspermobile.presentation.model.visualize.LoadCompleteEvent;
import com.jaspersoft.android.jaspermobile.presentation.model.visualize.MultiPageLoadEvent;
import com.jaspersoft.android.jaspermobile.presentation.model.visualize.PageLoadCompleteEvent;
import com.jaspersoft.android.jaspermobile.presentation.model.visualize.PageLoadErrorEvent;
import com.jaspersoft.android.jaspermobile.presentation.model.visualize.ReportCompleteEvent;
import com.jaspersoft.android.jaspermobile.presentation.model.visualize.VisualizeEvents;
import com.jaspersoft.android.jaspermobile.presentation.model.visualize.VisualizeViewModel;
import com.jaspersoft.android.jaspermobile.presentation.model.visualize.WebViewEvents;
import com.jaspersoft.android.jaspermobile.presentation.page.ReportPageState;
import com.jaspersoft.android.jaspermobile.presentation.view.ReportVisualizeView;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import rx.Observable;
import rx.Subscriber;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
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

    private static final VisualizeTemplate VIS_TEMPLATE = new VisualizeTemplate("content", "url");
    private static final double SCREEN_DIAGONAL = 10.1;
    private static final String JSON_REPORT_PARAMS = "{}";

    private ReportVisualizePresenter mReportVisualizePresenter;
    private FakeGetVisualizeTemplateCase mGetVisualizeTemplateCase;
    private FakeGetReportShowControlsPropertyCase mGetReportShowControlsPropertyCase;
    private FakeGetJsonParamsCase mRunVisualizeReportCase;

    @Mock
    RequestExceptionHandler mExceptionHandler;
    @Mock
    ReportVisualizeView mView;
    @Mock
    VisualizeViewModel mVisualizeViewModel;
    @Mock
    VisualizeEvents mVisualizeEvents;
    @Mock
    WebViewEvents mWebViewEvents;

    private ReportPageState fakeState;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        setUpMocks();
        mReportVisualizePresenter = new ReportVisualizePresenter(
                SCREEN_DIAGONAL,
                mExceptionHandler,
                mGetReportShowControlsPropertyCase,
                mGetVisualizeTemplateCase,
                mRunVisualizeReportCase
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

    @Test
    public void on_init_should_subscribe_to_visualize_start_load_event() throws Exception {
        when(mVisualizeEvents.loadStartEvent()).thenReturn(Observable.<Void>just(null));

        mReportVisualizePresenter.init();

        verify(mView).setWebViewVisibility(false);
        verify(mView).resetPaginationControl();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void on_init_should_subscribe_to_visualize_script_loaded_event() throws Exception {
        when(mVisualizeEvents.scriptLoadedEvent()).thenReturn(Observable.<Void>just(null));

        mReportVisualizePresenter.init();

        verify(mRunVisualizeReportCase).execute(any(Subscriber.class));
    }

    @Test
    public void on_init_should_subscribe_to_webview_on_progress_event() throws Exception {
        when(mWebViewEvents.progressChangedEvent()).thenReturn(Observable.just(10));

        mReportVisualizePresenter.init();

        verify(mView).updateDeterminateProgress(10);
    }

    private void setUpMocks() {
        fakeState = spy(new ReportPageState());
        mGetVisualizeTemplateCase = spy(new FakeGetVisualizeTemplateCase());
        mGetReportShowControlsPropertyCase = spy(new FakeGetReportShowControlsPropertyCase());
        mRunVisualizeReportCase = spy(new FakeGetJsonParamsCase());

        when(mView.getState()).thenReturn(fakeState);
        when(mView.getVisualize()).thenReturn(mVisualizeViewModel);

        when(mVisualizeViewModel.visualizeEvents()).thenReturn(mVisualizeEvents);
        when(mVisualizeViewModel.webViewEvents()).thenReturn(mWebViewEvents);

        when(mWebViewEvents.progressChangedEvent()).thenReturn(Observable.<Integer>empty());
        
        when(mVisualizeViewModel.run(anyString())).thenReturn(mVisualizeViewModel);
        when(mVisualizeViewModel.refresh()).thenReturn(mVisualizeViewModel);
        when(mVisualizeViewModel.loadPage(anyInt())).thenReturn(mVisualizeViewModel);
        when(mVisualizeViewModel.update(anyString())).thenReturn(mVisualizeViewModel);

        when(mVisualizeEvents.loadStartEvent()).thenReturn(Observable.<Void>empty());
        when(mVisualizeEvents.scriptLoadedEvent()).thenReturn(Observable.<Void>empty());
        when(mVisualizeEvents.loadCompleteEvent()).thenReturn(Observable.<LoadCompleteEvent>empty());
        when(mVisualizeEvents.loadErrorEvent()).thenReturn(Observable.<ErrorEvent>empty());
        when(mVisualizeEvents.reportCompleteEvent()).thenReturn(Observable.<ReportCompleteEvent>empty());
        when(mVisualizeEvents.pageLoadCompleteEvent()).thenReturn(Observable.<PageLoadCompleteEvent>empty());
        when(mVisualizeEvents.pageLoadErrorEvent()).thenReturn(Observable.<PageLoadErrorEvent>empty());
        when(mVisualizeEvents.multiPageLoadEvent()).thenReturn(Observable.<MultiPageLoadEvent>empty());
        when(mVisualizeEvents.externalReferenceClickEvent()).thenReturn(Observable.<ExternalReferenceClickEvent>empty());
        when(mVisualizeEvents.executionReferenceClickEvent()).thenReturn(Observable.<ExecutionReferenceClickEvent>empty());
        when(mVisualizeEvents.windowErrorEvent()).thenReturn(Observable.<ErrorEvent>empty());
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

    private class FakeGetJsonParamsCase extends GetJsonParamsCase {
        public FakeGetJsonParamsCase() {
            super(FakePreExecutionThread.create(), FakePostExecutionThread.create(), null, null, null);
        }

        @Override
        protected Observable<String> buildUseCaseObservable() {
            return Observable.just(JSON_REPORT_PARAMS);
        }
    }
}