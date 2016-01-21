package com.jaspersoft.android.jaspermobile.presentation.presenter;


import android.support.annotation.VisibleForTesting;

import com.jaspersoft.android.jaspermobile.domain.SimpleSubscriber;
import com.jaspersoft.android.jaspermobile.domain.VisualizeTemplate;
import com.jaspersoft.android.jaspermobile.domain.interactor.report.GetReportShowControlsPropertyCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.report.GetVisualizeTemplateCase;
import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;
import com.jaspersoft.android.jaspermobile.network.RequestExceptionHandler;
import com.jaspersoft.android.jaspermobile.presentation.view.ReportVisualizeView;

import javax.inject.Inject;
import javax.inject.Named;

import timber.log.Timber;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerActivity
public class ReportVisualizePresenter implements Presenter<ReportVisualizeView> {

    private final double mScreenDiagonal;
    private final RequestExceptionHandler mRequestExceptionHandler;
    private final GetReportShowControlsPropertyCase mGetReportShowControlsPropertyCase;
    private final GetVisualizeTemplateCase mGetVisualizeTemplateCase;

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

    private void resolveNeedControls(boolean needControls) {
        if (needControls) {
            mView.showInitialFiltersPage();
        } else {
            loadVisualizeTemplate();
        }
    }

    private void loadVisualizeTemplate() {
        mGetVisualizeTemplateCase.execute(mScreenDiagonal, new SimpleSubscriber<VisualizeTemplate>() {
            @Override
            public void onError(Throwable e) {
                handleError(e);
            }

            @Override
            public void onNext(VisualizeTemplate template) {
                mView.loadTemplateInView(template);
            }
        });
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
}
