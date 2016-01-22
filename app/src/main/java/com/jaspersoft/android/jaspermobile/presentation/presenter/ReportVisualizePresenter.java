package com.jaspersoft.android.jaspermobile.presentation.presenter;


import android.support.annotation.VisibleForTesting;

import com.jaspersoft.android.jaspermobile.domain.SimpleSubscriber;
import com.jaspersoft.android.jaspermobile.domain.VisualizeTemplate;
import com.jaspersoft.android.jaspermobile.domain.interactor.report.GetReportShowControlsPropertyCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.report.GetVisualizeTemplateCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.report.GetJsonParamsCase;
import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;
import com.jaspersoft.android.jaspermobile.network.RequestExceptionHandler;
import com.jaspersoft.android.jaspermobile.presentation.action.ReportActionListener;
import com.jaspersoft.android.jaspermobile.presentation.model.visualize.ErrorEvent;
import com.jaspersoft.android.jaspermobile.presentation.model.visualize.LoadCompleteEvent;
import com.jaspersoft.android.jaspermobile.presentation.model.visualize.ReportCompleteEvent;
import com.jaspersoft.android.jaspermobile.presentation.model.visualize.VisualizeComponent;
import com.jaspersoft.android.jaspermobile.presentation.model.visualize.VisualizeViewModel;
import com.jaspersoft.android.jaspermobile.presentation.view.ReportVisualizeView;

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
    private final RequestExceptionHandler mRequestExceptionHandler;
    private final GetReportShowControlsPropertyCase mGetReportShowControlsPropertyCase;
    private final GetVisualizeTemplateCase mGetVisualizeTemplateCase;
    private final GetJsonParamsCase mGetJsonParamsCase;

    private final CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    private ReportVisualizeView mView;

    @Inject
    public ReportVisualizePresenter(@Named("screen_diagonal") Double screenDiagonal,
                                    RequestExceptionHandler requestExceptionHandler,
                                    GetReportShowControlsPropertyCase getReportShowControlsPropertyCase,
                                    GetVisualizeTemplateCase getVisualizeTemplateCase,
                                    GetJsonParamsCase getJsonParamsCase
    ) {
        mScreenDiagonal = screenDiagonal;
        mRequestExceptionHandler = requestExceptionHandler;
        mGetReportShowControlsPropertyCase = getReportShowControlsPropertyCase;
        mGetVisualizeTemplateCase = getVisualizeTemplateCase;
        mGetJsonParamsCase = getJsonParamsCase;
    }

    public void init() {
        if (mView == null) {
            throw new NullPointerException("Please inject view before calling this method");
        }
        subscribeToVisualizeEvents();
        subscribeToWebViewEvents();
        showLoading();
        mGetReportShowControlsPropertyCase.execute(new SimpleSubscriber<Boolean>() {
            @Override
            public void onCompleted() {
                hideLoading();
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

    private void subscribeToWebViewEvents() {
        VisualizeViewModel visualize = mView.getVisualize();
        listenForProgressChanges(visualize);
    }

    private void listenForProgressChanges(VisualizeViewModel visualize) {
        subscribeToEvent(
                visualize.webViewEvents()
                .progressChangedEvent()
                .subscribe(new ErrorSubscriber<>(new SimpleSubscriber<Integer>() {
                    @Override
                    public void onNext(Integer progress) {
                        mView.updateDeterminateProgress(10);
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
    }


    private void listenForLoadStartEvent(VisualizeComponent visualize) {
        subscribeToEvent(
                visualize.visualizeEvents()
                        .loadStartEvent()
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
                        .subscribe(new ErrorSubscriber<>(new SimpleSubscriber<ReportCompleteEvent>() {
                            @Override
                            public void onNext(ReportCompleteEvent event) {
                                int totalPages = event.getTotalPages();

                                boolean hasContent = totalPages > 0;
                                toggleSaveAction(hasContent);

                                if (hasContent) {
                                    mView.hideEmptyPageMessage();

                                    boolean multiPage = totalPages > 1;
                                    mView.setPaginationControlVisibility(multiPage);

                                    if (multiPage) {
                                        mView.showTotalPages(totalPages);
                                    }
                                } else {
                                    mView.setPaginationControlVisibility(false);
                                    mView.showEmptyPageMessage();
                                }
                            }
                        }))
        );
    }

    @Override
    public void runReport() {

    }

    private void subscribeToEvent(Subscription subscription) {
        mCompositeSubscription.add(subscription);
    }

    @Override
    public void loadPage(String pageRange) {

    }

    @Override
    public void updateReport() {

    }

    @Override
    public void refresh() {

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
        mGetJsonParamsCase.execute(new ErrorSubscriber<>(new SimpleSubscriber<String>() {
            @Override
            public void onNext(String params) {
                mView.getVisualize().run(params);
            }
        }));
    }

    @Override
    public void injectView(ReportVisualizeView visualizeReportView) {
        mView = visualizeReportView;
    }

    @Override
    public void resume() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void destroy() {
        mGetReportShowControlsPropertyCase.unsubscribe();
        mGetVisualizeTemplateCase.unsubscribe();
        mGetJsonParamsCase.unsubscribe();
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

    private void hideLoading() {
        mView.hideLoading();
    }

    private void showLoading() {
        mView.showLoading();
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
