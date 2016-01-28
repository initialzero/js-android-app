package com.jaspersoft.android.jaspermobile.presentation.presenter;


import android.support.annotation.VisibleForTesting;

import com.jaspersoft.android.jaspermobile.domain.AppCredentials;
import com.jaspersoft.android.jaspermobile.domain.AppResource;
import com.jaspersoft.android.jaspermobile.domain.SimpleSubscriber;
import com.jaspersoft.android.jaspermobile.domain.VisualizeTemplate;
import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.interactor.report.FlushInputControlsCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.report.GetJsonParamsCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.report.GetReportMetadataCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.report.GetReportShowControlsPropertyCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.report.GetVisualizeTemplateCase;
import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;
import com.jaspersoft.android.jaspermobile.network.RequestExceptionHandler;
import com.jaspersoft.android.jaspermobile.presentation.action.ReportActionListener;
import com.jaspersoft.android.jaspermobile.presentation.model.ReportResourceModel;
import com.jaspersoft.android.jaspermobile.presentation.model.mapper.ResourceModelMapper;
import com.jaspersoft.android.jaspermobile.presentation.model.visualize.ErrorEvent;
import com.jaspersoft.android.jaspermobile.presentation.model.visualize.ExecutionReferenceClickEvent;
import com.jaspersoft.android.jaspermobile.presentation.model.visualize.ExternalReferenceClickEvent;
import com.jaspersoft.android.jaspermobile.presentation.model.visualize.LoadCompleteEvent;
import com.jaspersoft.android.jaspermobile.presentation.model.visualize.MultiPageLoadEvent;
import com.jaspersoft.android.jaspermobile.presentation.model.visualize.PageLoadCompleteEvent;
import com.jaspersoft.android.jaspermobile.presentation.model.visualize.PageLoadErrorEvent;
import com.jaspersoft.android.jaspermobile.presentation.model.visualize.ReportCompleteEvent;
import com.jaspersoft.android.jaspermobile.presentation.model.visualize.VisualizeComponent;
import com.jaspersoft.android.jaspermobile.presentation.model.visualize.VisualizeExecOptions;
import com.jaspersoft.android.jaspermobile.presentation.model.visualize.VisualizeViewModel;
import com.jaspersoft.android.jaspermobile.presentation.model.visualize.WebViewErrorEvent;
import com.jaspersoft.android.jaspermobile.presentation.view.ReportVisualizeView;
import com.jaspersoft.android.jaspermobile.visualize.ReportData;

import javax.inject.Inject;
import javax.inject.Named;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerActivity
public class ReportVisualizePresenter implements Presenter<ReportVisualizeView>, ReportActionListener {

    private final double mScreenDiagonal;
    private final String mReportUri;
    private final AppCredentials mCredentials;
    private final PostExecutionThread mPostExecutionThread;
    private final RequestExceptionHandler mRequestExceptionHandler;
    private final ResourceModelMapper mResourceModelMapper;

    private final GetReportShowControlsPropertyCase mGetReportShowControlsPropertyCase;
    private final GetVisualizeTemplateCase mGetVisualizeTemplateCase;
    private final GetJsonParamsCase mGetJsonParamsCase;
    private final FlushInputControlsCase mFlushInputControlsCase;
    private final GetReportMetadataCase mGetReportMetadataCase;

    private CompositeSubscription mCompositeSubscription;
    private ReportVisualizeView mView;

    @Inject
    public ReportVisualizePresenter(
            @Named("screen_diagonal") Double screenDiagonal,
            @Named("report_uri") String reportUri,
            AppCredentials credentials,
            PostExecutionThread postExecutionThread,
            RequestExceptionHandler requestExceptionHandler,
            ResourceModelMapper resourceModelMapper,
            GetReportShowControlsPropertyCase getReportShowControlsPropertyCase,
            GetVisualizeTemplateCase getVisualizeTemplateCase,
            GetJsonParamsCase getJsonParamsCase,
            FlushInputControlsCase flushInputControlsCase,
            GetReportMetadataCase getReportMetadataCase
    ) {
        mScreenDiagonal = screenDiagonal;
        mReportUri = reportUri;
        mCredentials = credentials;
        mPostExecutionThread = postExecutionThread;
        mRequestExceptionHandler = requestExceptionHandler;
        mResourceModelMapper = resourceModelMapper;
        mGetReportShowControlsPropertyCase = getReportShowControlsPropertyCase;
        mGetVisualizeTemplateCase = getVisualizeTemplateCase;
        mGetJsonParamsCase = getJsonParamsCase;
        mFlushInputControlsCase = flushInputControlsCase;
        mGetReportMetadataCase = getReportMetadataCase;
    }

    public void init() {
        if (mView == null) {
            throw new NullPointerException("Please inject view before calling this method");
        }
        if (!mView.getState().isControlsPageShown()) {
            loadControls();
        }
    }

    private void loadControls() {
        mView.showLoading();
        mGetReportShowControlsPropertyCase.execute(mReportUri, new SimpleSubscriber<Boolean>() {
            @Override
            public void onCompleted() {
                mView.hideLoading();
            }

            @Override
            public void onError(Throwable e) {
                handleError(e);
            }

            @Override
            public void onNext(Boolean needControls) {
                mView.getState().setControlsPageShown(true);
                toggleFiltersAction(needControls);
                resolveNeedControls(needControls);
            }
        });
    }

    @Override
    public void runReport() {
        loadVisualizeTemplate();
    }

    @Override
    public void loadPage(String pageRange) {
        mView.getVisualize().loadPage(pageRange);
    }

    @Override
    public void updateReport() {
        mGetJsonParamsCase.execute(mReportUri, new ErrorSubscriber<>(new SimpleSubscriber<String>() {
            @Override
            public void onNext(String params) {
                mView.resetZoom();
                mView.getVisualize().update(params);
            }
        }));
    }

    @Override
    public void refresh() {
        mView.showLoading();
        mView.getVisualize().refresh();
        mView.resetZoom();
        mView.setWebViewVisibility(false);
        mView.setPaginationVisibility(false);
        mView.resetPaginationControl();
    }

    private void resolveNeedControls(boolean needControls) {
        if (needControls) {
            mView.showInitialFiltersPage();
        } else {
            loadVisualizeTemplate();
        }
    }

    @VisibleForTesting
    void loadVisualizeTemplate() {
        mGetVisualizeTemplateCase.execute(mScreenDiagonal, new ErrorSubscriber<>(
                new SimpleSubscriber<VisualizeTemplate>() {
                    @Override
                    public void onNext(VisualizeTemplate template) {
                        mView.loadTemplateInView(template);
                    }
                }));
    }

    private void runReportOnVisualize() {
        mGetJsonParamsCase.execute(mReportUri, new ErrorSubscriber<>(new SimpleSubscriber<String>() {
            @Override
            public void onNext(String params) {
                VisualizeExecOptions options = new VisualizeExecOptions(
                        mReportUri,
                        params,
                        mCredentials,
                        mScreenDiagonal);
                mView.getVisualize().run(options);
            }
        }));
    }

    @Override
    public void injectView(ReportVisualizeView visualizeReportView) {
        mView = visualizeReportView;
    }

    @Override
    public void resume() {
        mCompositeSubscription = new CompositeSubscription();
        subscribeToVisualizeEvents();
        subscribeToWebViewEvents();
    }

    @Override
    public void pause() {
        mCompositeSubscription.unsubscribe();
    }

    @Override
    public void destroy() {
        mGetReportShowControlsPropertyCase.unsubscribe();
        mGetVisualizeTemplateCase.unsubscribe();
        mGetJsonParamsCase.unsubscribe();
        mFlushInputControlsCase.execute(mReportUri);
    }

    private void subscribeToWebViewEvents() {
        VisualizeViewModel visualize = mView.getVisualize();
        listenForProgressChanges(visualize);
        listenForReceivedError(visualize);
        listenForSessionExpiration(visualize);
    }

    private void listenForProgressChanges(VisualizeViewModel visualize) {
        subscribeToEvent(
                visualize.webViewEvents()
                        .progressChangedEvent()
                        .observeOn(mPostExecutionThread.getScheduler())
                        .subscribe(new ErrorSubscriber<>(new SimpleSubscriber<Integer>() {
                            @Override
                            public void onNext(Integer progress) {
                                mView.updateDeterminateProgress(progress);
                            }
                        }))
        );
    }

    private void listenForReceivedError(VisualizeViewModel visualize) {
        subscribeToEvent(
                visualize.webViewEvents()
                        .receivedErrorEvent()
                        .observeOn(mPostExecutionThread.getScheduler())
                        .subscribe(new ErrorSubscriber<>(new SimpleSubscriber<WebViewErrorEvent>() {
                            @Override
                            public void onNext(WebViewErrorEvent event) {
                                mView.hideLoading();
                                mView.setWebViewVisibility(false);
                                mView.showError("title" + "\n" + "message");
                            }
                        }))
        );
    }

    private void listenForSessionExpiration(VisualizeViewModel visualize) {
        subscribeToEvent(
                visualize.webViewEvents()
                        .sessionExpiredEvent()
                        .observeOn(mPostExecutionThread.getScheduler())
                        .subscribe(new ErrorSubscriber<>(new SimpleSubscriber<Void>() {
                            @Override
                            public void onNext(Void item) {
                                mView.handleSessionExpiration();
                            }
                        }))
        );
    }

    private void subscribeToVisualizeEvents() {
        VisualizeViewModel visualize = mView.getVisualize();
        listenForLoadStartEvent(visualize);
        listenScriptLoadedEvent(visualize);
        listenForLoadCompleteEvent(visualize);
        listenForLoadErrorEvent(visualize);
        listenForReportCompleteEvent(visualize);
        listenForPageLoadCompleteEvent(visualize);
        listenForPageLoadErrorEvent(visualize);
        listenForMultiPageLoadEvent(visualize);
        listenForExternalPageEvent(visualize);
        listenForExecutionEvent(visualize);
        listenForWindowErrorEvent(visualize);
    }

    private void listenForLoadStartEvent(VisualizeComponent visualize) {
        subscribeToEvent(
                visualize.visualizeEvents()
                        .loadStartEvent()
                        .observeOn(mPostExecutionThread.getScheduler())
                        .subscribe(new ErrorSubscriber<>(new SimpleSubscriber<Void>() {
                            @Override
                            public void onNext(Void item) {
                                mView.showLoading();
                                mView.setWebViewVisibility(false);
                                mView.resetPaginationControl();
                            }
                        }))
        );
    }

    private void listenScriptLoadedEvent(VisualizeComponent visualize) {
        subscribeToEvent(
                visualize.visualizeEvents()
                        .scriptLoadedEvent()
                        .observeOn(mPostExecutionThread.getScheduler())
                        .subscribe(new ErrorSubscriber<>(new SimpleSubscriber<Void>() {
                            @Override
                            public void onNext(Void item) {
                                runReportOnVisualize();
                            }
                        }))
        );
    }

    private void listenForLoadCompleteEvent(VisualizeViewModel visualize) {
        subscribeToEvent(
                visualize.visualizeEvents()
                        .loadCompleteEvent()
                        .observeOn(mPostExecutionThread.getScheduler())
                        .subscribe(new ErrorSubscriber<>(new SimpleSubscriber<LoadCompleteEvent>() {
                            @Override
                            public void onNext(LoadCompleteEvent completeEvent) {
                                mView.hideLoading();
                                mView.setWebViewVisibility(true);
                            }
                        }))
        );
    }

    private void listenForLoadErrorEvent(VisualizeViewModel visualize) {
        subscribeToEvent(
                visualize.visualizeEvents()
                        .loadErrorEvent()
                        .observeOn(mPostExecutionThread.getScheduler())
                        .subscribe(new ErrorSubscriber<>(new SimpleSubscriber<ErrorEvent>() {
                            @Override
                            public void onNext(ErrorEvent errorEvent) {
                                mView.hideLoading();
                                mView.showError(errorEvent.getErrorMessage());
                            }
                        }))
        );
    }


    private void listenForReportCompleteEvent(VisualizeViewModel visualize) {
        subscribeToEvent(
                visualize.visualizeEvents()
                        .reportCompleteEvent()
                        .observeOn(mPostExecutionThread.getScheduler())
                        .subscribe(new ErrorSubscriber<>(new SimpleSubscriber<ReportCompleteEvent>() {
                            @Override
                            public void onNext(ReportCompleteEvent event) {
                                int totalPages = event.getTotalPages();

                                boolean hasContent = totalPages > 0;
                                toggleSaveAction(hasContent);

                                if (hasContent) {
                                    mView.hideEmptyPageMessage();

                                    boolean multiPage = totalPages > 1;
                                    mView.setPaginationVisibility(multiPage);

                                    if (multiPage) {
                                        mView.setPaginationTotalPages(totalPages);
                                    }
                                } else {
                                    mView.setPaginationVisibility(false);
                                    mView.showEmptyPageMessage();
                                }
                            }
                        }))
        );
    }

    private void listenForPageLoadCompleteEvent(VisualizeViewModel visualize) {
        subscribeToEvent(
                visualize.visualizeEvents()
                        .pageLoadCompleteEvent()
                        .observeOn(mPostExecutionThread.getScheduler())
                        .subscribe(new ErrorSubscriber<>(new SimpleSubscriber<PageLoadCompleteEvent>() {
                            @Override
                            public void onNext(PageLoadCompleteEvent event) {
                                mView.setPaginationEnabled(true);
                                mView.setPaginationCurrentPage(event.getPage());
                            }
                        }))
        );
    }

    private void listenForPageLoadErrorEvent(VisualizeViewModel visualize) {
        subscribeToEvent(
                visualize.visualizeEvents()
                        .pageLoadErrorEvent()
                        .observeOn(mPostExecutionThread.getScheduler())
                        .subscribe(new ErrorSubscriber<>(new SimpleSubscriber<PageLoadErrorEvent>() {
                            @Override
                            public void onNext(PageLoadErrorEvent event) {
                                mView.setPaginationEnabled(true);
                                mView.setPaginationCurrentPage(event.getPage());
                                mView.showError(event.getErrorMessage());
                            }
                        }))
        );
    }

    private void listenForMultiPageLoadEvent(VisualizeViewModel visualize) {
        subscribeToEvent(
                visualize.visualizeEvents()
                        .multiPageLoadEvent()
                        .observeOn(mPostExecutionThread.getScheduler())
                        .subscribe(new ErrorSubscriber<>(new SimpleSubscriber<MultiPageLoadEvent>() {
                            @Override
                            public void onNext(MultiPageLoadEvent event) {
                                boolean needToShowPagination = event.isMultiPage() && mView.getPaginationTotalPages() > 0;
                                mView.setPaginationVisibility(needToShowPagination);
                            }
                        }))
        );
    }

    private void listenForExternalPageEvent(VisualizeViewModel visualize) {
        subscribeToEvent(
                visualize.visualizeEvents()
                        .externalReferenceClickEvent()
                        .observeOn(mPostExecutionThread.getScheduler())
                        .subscribe(new ErrorSubscriber<>(new SimpleSubscriber<ExternalReferenceClickEvent>() {
                            @Override
                            public void onNext(ExternalReferenceClickEvent event) {
                                mView.showExternalLink(event.getExternalReference());
                            }
                        }))
        );
    }

    private void listenForExecutionEvent(VisualizeViewModel visualize) {
        subscribeToEvent(
                visualize.visualizeEvents()
                        .executionReferenceClickEvent()
                        .observeOn(mPostExecutionThread.getScheduler())
                        .subscribe(new ErrorSubscriber<>(new SimpleSubscriber<ExecutionReferenceClickEvent>() {
                            @Override
                            public void onNext(ExecutionReferenceClickEvent event) {
                                loadReportMetadata(event.getReportData());
                            }
                        }))
        );
    }

    private void loadReportMetadata(ReportData reportData) {
        mView.showLoading();
        mGetReportMetadataCase.execute(reportData, new ErrorSubscriber<>(new SimpleSubscriber<AppResource>() {
            @Override
            public void onCompleted() {
                mView.hideLoading();
            }

            @Override
            public void onNext(AppResource item) {
                ReportResourceModel model = mResourceModelMapper.mapReportModel(item);
                mView.executeReport(model);
            }
        }));
    }

    private void listenForWindowErrorEvent(VisualizeViewModel visualize) {
        subscribeToEvent(
                visualize.visualizeEvents()
                        .windowErrorEvent()
                        .observeOn(mPostExecutionThread.getScheduler())
                        .subscribe(new ErrorSubscriber<>(new SimpleSubscriber<ErrorEvent>() {
                            @Override
                            public void onNext(ErrorEvent event) {
                                mView.showError(event.getErrorMessage());
                            }
                        }))
        );
    }

    @VisibleForTesting
    void handleError(Throwable error) {
        Timber.e(error, "Presenter received unexpected error");
        mView.hideLoading();
        mView.showError(mRequestExceptionHandler.extractMessage(error));
    }

    private void toggleFiltersAction(boolean visibility) {
        mView.setFilterActionVisibility(visibility);
        mView.reloadMenu();
    }

    private void toggleSaveAction(boolean visibility) {
        mView.setSaveActionVisibility(visibility);
        mView.reloadMenu();
    }

    private void subscribeToEvent(Subscription subscription) {
        mCompositeSubscription.add(subscription);
    }

    private final class ErrorSubscriber<R> extends SimpleSubscriber<R> {
        private final SimpleSubscriber<R> mDelegate;

        private ErrorSubscriber(SimpleSubscriber<R> delegate) {
            mDelegate = delegate;
        }

        @Override
        public void onError(Throwable e) {
            handleError(e);
        }

        @Override
        public void onCompleted() {
            mDelegate.onCompleted();
        }

        @Override
        public void onNext(R item) {
            mDelegate.onNext(item);
        }
    }
}
