package com.jaspersoft.android.jaspermobile.activities.info.fragments;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.jaspersoft.android.jaspermobile.Analytics;
import com.jaspersoft.android.jaspermobile.GraphObject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.info.InfoHeaderView;
import com.jaspersoft.android.jaspermobile.activities.robospice.Nullable;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceFragment;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.interactor.report.option.GetReportOptionsCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.report.option.LoadControlsForOptionCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.resource.GetResourceDetailsCase;
import com.jaspersoft.android.jaspermobile.internal.di.components.ProfileComponent;
import com.jaspersoft.android.jaspermobile.util.resource.JasperResource;
import com.jaspersoft.android.jaspermobile.util.resource.viewbinder.ResourceBinder;
import com.jaspersoft.android.jaspermobile.util.resource.viewbinder.ResourceBinderFactory;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;

import javax.inject.Inject;

import roboguice.inject.InjectView;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
@EFragment(R.layout.fragment_resource_info)
public class SimpleInfoFragment extends RoboSpiceFragment {

    public static final String TAG = ResourceInfoFragment.class.getSimpleName();

    @FragmentArg
    protected JasperResource jasperResource;

    @Inject
    @Nullable
    protected Analytics analytics;
    @Inject
    @Nullable
    protected JasperServer mJasperServer;

    @Inject
    @Nullable
    protected GetResourceDetailsCase mGetResourceDetailsCase;
    @Inject
    @Nullable
    protected GetReportOptionsCase mGetReportOptionsCase;
    @Inject
    @Nullable
    protected LoadControlsForOptionCase mLoadControlsForOptionCase;

    @InjectView(R.id.toolbarImageView)
    protected ImageView toolbarImage;

    @InjectView(R.id.info_collapsing_toolbar)
    protected CollapsingToolbarLayout toolbarLayout;

    private InfoHeaderView infoHeaderView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ProfileComponent profileComponent = GraphObject.Factory.from(getContext())
                .getProfileComponent();
        profileComponent.inject(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setToolbar(view);
        showHeaderView();

        if (savedInstanceState == null) {
            analytics.sendEvent(
                    Analytics.EventCategory.RESOURCE.getValue(),
                    Analytics.EventAction.INFO_VIEWED.getValue(),
                    jasperResource.getResourceType().name()
            );
        }
    }

    @Override
    public void onDestroyView() {
        mGetResourceDetailsCase.unsubscribe();
        mGetReportOptionsCase.unsubscribe();
        mLoadControlsForOptionCase.unsubscribe();
        super.onDestroyView();
    }

    final protected void updateHeaderViewLabel(String label) {
        jasperResource.setLabel(label);
        infoHeaderView.setTitle(label);
    }

    private void showHeaderView() {
        ResourceBinderFactory mResourceBinderFactory = new ResourceBinderFactory(getActivity());
        ResourceBinder resourceBinder = mResourceBinderFactory.create(jasperResource.getResourceType());

        infoHeaderView = new InfoHeaderView(toolbarImage, toolbarLayout);
        resourceBinder.bindView(infoHeaderView, jasperResource);
    }

    private void setToolbar(View infoView) {
        Toolbar toolbar = (Toolbar) infoView.findViewById(R.id.toolbar);

        ActionBarActivity actionBarActivity = (ActionBarActivity) getActivity();
        actionBarActivity.setSupportActionBar(toolbar);

        ActionBar actionBar = actionBarActivity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_close);
        }
    }
}
