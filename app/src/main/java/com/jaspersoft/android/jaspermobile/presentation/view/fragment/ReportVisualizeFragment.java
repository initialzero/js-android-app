package com.jaspersoft.android.jaspermobile.presentation.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.jaspersoft.android.jaspermobile.domain.VisualizeTemplate;
import com.jaspersoft.android.jaspermobile.internal.di.components.ReportVisualizeActivityComponent;
import com.jaspersoft.android.jaspermobile.presentation.page.ReportPageState;
import com.jaspersoft.android.jaspermobile.presentation.presenter.ReportVisualizePresenter;
import com.jaspersoft.android.jaspermobile.presentation.view.ReportVisualizeView;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;

import javax.inject.Inject;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@EFragment
public class ReportVisualizeFragment extends BaseFragment implements ReportVisualizeView {

    public static final String TAG = "ReportVisualizeFragment";

    @InstanceState
    ReportPageState mState;

    @Inject
    ReportVisualizePresenter mPresenter;

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
        getComponent(ReportVisualizeActivityComponent.class).inject(this);
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
    public void setPaginationControlVisibility(boolean visibility) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void resetPaginationControl() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void showTotalPages(int totalPages) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void showCurrentPage(int page) {
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
    public void loadTemplateInView(VisualizeTemplate template) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public ReportPageState getState() {
        return mState;
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
