package com.jaspersoft.android.jaspermobile.presentation.presenter;


import android.support.annotation.VisibleForTesting;

import com.jaspersoft.android.jaspermobile.domain.SimpleSubscriber;
import com.jaspersoft.android.jaspermobile.domain.VisualizeTemplate;
import com.jaspersoft.android.jaspermobile.domain.interactor.report.GetReportShowControlsPropertyCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.report.GetVisualizeTemplateCase;
import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;
import com.jaspersoft.android.jaspermobile.network.RequestExceptionHandler;
import com.jaspersoft.android.jaspermobile.presentation.action.ReportActionListener;
import com.jaspersoft.android.jaspermobile.presentation.model.visualize.VisualizeComponent;
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

    private final CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    private ReportVisualizeView mView;

    @Inject
    public ReportVisualizePresenter(@Named("screen_diagonal") Double screenDiagonal,
                                    RequestExceptionHandler requestExceptionHandler,
                                    GetReportShowControlsPropertyCase getReportShowControlsPropertyCase,
                                    GetVisualizeTemplateCase getVisualizeTemplateCase) {
        mScreenDiagonal = screenDiagonal;
        mRequestExceptionHandler = requestExceptionHandler;
        mGetReportShowControlsPropertyCase = getReportShowControlsPropertyCase;
        mGetVisualizeTemplateCase = getVisualizeTemplateCase;
    }

    public void init() {
        if (mView == null) {
            throw new NullPointerException("Please inject view before calling this method");
        }
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

    @Override
    public void runReport() {
        VisualizeComponent visualize = mView.getVisualize().run();
        listenForLoadStart(visualize);
    }

    private void listenForLoadStart(VisualizeComponent visualize) {
        subscribeToEvent(
                visualize.visualizeEvents()
                        .loadStartEvent()
                        .subscribe(new ErrorSubscriber<>(new SimpleSubscriber<Void>() {
                            @Override
                            public void onNext(Void item) {
                                mView.setWebViewVisibility(false);
                                mView.showPageLoader();
                                mView.resetPaginationControl();
                            }
                        }))
        );
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

    private void loadVisualizeTemplate() {
        mGetVisualizeTemplateCase.execute(mScreenDiagonal, new ErrorSubscriber<>(
                new SimpleSubscriber<VisualizeTemplate>() {
                    @Override
                    public void onNext(VisualizeTemplate template) {
                        mView.loadTemplateInView(template);
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
