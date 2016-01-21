package com.jaspersoft.android.jaspermobile.presentation.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.jaspersoft.android.jaspermobile.internal.di.components.ReportVisualizeActivityComponent;
import com.jaspersoft.android.jaspermobile.presentation.presenter.ReportVisualizePresenter;
import com.jaspersoft.android.jaspermobile.presentation.view.ReportVisualizeView;

import org.androidannotations.annotations.EFragment;

import javax.inject.Inject;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@EFragment
public class ReportVisualizeFragment extends BaseFragment implements ReportVisualizeView {

    public static final String TAG = "ReportVisualizeFragment";

    @Inject
    ReportVisualizePresenter mPresenter;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        injectComponents();
    }

    private void injectComponents() {
        getComponent(ReportVisualizeActivityComponent.class).inject(this);
        mPresenter.injectView(this);
    }
}
