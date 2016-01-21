package com.jaspersoft.android.jaspermobile.presentation.presenter;


import com.jaspersoft.android.jaspermobile.domain.SimpleSubscriber;
import com.jaspersoft.android.jaspermobile.domain.VisualizeTemplate;
import com.jaspersoft.android.jaspermobile.domain.interactor.report.GetVisualizeTemplateCase;
import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;
import com.jaspersoft.android.jaspermobile.presentation.view.ReportVisualizeView;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerActivity
public class ReportVisualizePresenter implements Presenter<ReportVisualizeView> {

    private final double mDiagonal;
    private final GetVisualizeTemplateCase mGetVisualizeTemplateCase;

    private ReportVisualizeView mView;

    @Inject
    public ReportVisualizePresenter(@Named("screen_diagonal") Double diagonal,
                                    GetVisualizeTemplateCase getVisualizeTemplateCase) {
        mDiagonal = diagonal;
        mGetVisualizeTemplateCase = getVisualizeTemplateCase;
    }

    public void init() {
        mGetVisualizeTemplateCase.execute(mDiagonal, new SimpleSubscriber<VisualizeTemplate>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(VisualizeTemplate item) {
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

    }
}
