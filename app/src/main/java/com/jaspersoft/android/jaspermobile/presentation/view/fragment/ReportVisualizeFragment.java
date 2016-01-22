package com.jaspersoft.android.jaspermobile.presentation.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.jaspersoft.android.jaspermobile.JasperMobileApplication;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.domain.VisualizeTemplate;
import com.jaspersoft.android.jaspermobile.internal.di.modules.activity.ActivityModule;
import com.jaspersoft.android.jaspermobile.internal.di.modules.activity.ReportVisualizeActivityModule;
import com.jaspersoft.android.jaspermobile.presentation.model.visualize.VisualizeViewModel;
import com.jaspersoft.android.jaspermobile.presentation.page.ReportPageState;
import com.jaspersoft.android.jaspermobile.presentation.presenter.ReportVisualizePresenter;
import com.jaspersoft.android.jaspermobile.presentation.view.ReportVisualizeView;
import com.jaspersoft.android.jaspermobile.visualize.ReportData;
import com.jaspersoft.android.jaspermobile.widget.JSWebView;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;

import javax.inject.Inject;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@EFragment(R.layout.report_html_viewer)
public class ReportVisualizeFragment extends BaseFragment implements ReportVisualizeView {

    public static final String TAG = "ReportVisualizeFragment";

    @InstanceState
    protected ReportPageState mState;

    @ViewById
    protected JSWebView webView;

    @Inject
    protected ReportVisualizePresenter mPresenter;
    @Inject
    protected VisualizeViewModel mVisualizeViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mState == null) {
            mState = new ReportPageState();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        injectComponents();
    }

    private void injectComponents() {
        JasperMobileApplication.get(getContext())
                .getReportComponent()
                .plusReportVisualizeActivity(
                        new ActivityModule(getActivity()),
                        new ReportVisualizeActivityModule(webView)
                );
        mPresenter.injectView(this);
    }

    @Override
    public void setFilterActionVisibility(boolean visibilityFlag) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void setSaveActionVisibility(boolean visibilityFlag) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void reloadMenu() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void showInitialFiltersPage() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void showPage(String pageContent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void setPaginationVisibility(boolean visibility) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void setPaginationEnabled(boolean enabled) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void setPaginationTotalPages(int totalPages) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int getPaginationTotalPages() {
        return 0;
    }

    @Override
    public void setPaginationCurrentPage(int page) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void setWebViewVisibility(boolean visibility) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void resetPaginationControl() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void showPageOutOfRangeError() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void showEmptyPageMessage() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void hideEmptyPageMessage() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void loadTemplateInView(VisualizeTemplate template) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void updateDeterminateProgress(int progress) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void showExternalLink(String externalLink) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void executeReport(ReportData reportData) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public ReportPageState getState() {
        return mState;
    }

    @Override
    public VisualizeViewModel getVisualize() {
        return mVisualizeViewModel;
    }

    @Override
    public void showLoading() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void hideLoading() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void showError(String message) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void showNotification(String message) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void hideError() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
