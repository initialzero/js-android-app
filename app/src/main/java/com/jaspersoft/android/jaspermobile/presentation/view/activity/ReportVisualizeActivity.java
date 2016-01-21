package com.jaspersoft.android.jaspermobile.presentation.view.activity;

import android.os.Bundle;

import com.jaspersoft.android.jaspermobile.JasperMobileApplication;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboToolbarActivity;
import com.jaspersoft.android.jaspermobile.internal.di.HasComponent;
import com.jaspersoft.android.jaspermobile.internal.di.components.ProfileComponent;
import com.jaspersoft.android.jaspermobile.internal.di.components.ReportComponent;
import com.jaspersoft.android.jaspermobile.internal.di.components.ReportVisualizeActivityComponent;
import com.jaspersoft.android.jaspermobile.internal.di.modules.ReportModule;
import com.jaspersoft.android.jaspermobile.internal.di.modules.activity.ActivityModule;
import com.jaspersoft.android.jaspermobile.presentation.view.fragment.ReportVisualizeFragment;
import com.jaspersoft.android.jaspermobile.presentation.view.fragment.ReportVisualizeFragment_;
import com.jaspersoft.android.jaspermobile.util.ScrollableTitleHelper;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@EActivity(R.layout.report_viewer_layout)
public class ReportVisualizeActivity extends RoboToolbarActivity implements HasComponent<ReportVisualizeActivityComponent> {
    @Extra
    protected ResourceLookup resource;
    @Bean
    protected ScrollableTitleHelper scrollableTitleHelper;

    private JasperMobileApplication graphObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scrollableTitleHelper.injectTitle(resource.getLabel());
        graphObject = JasperMobileApplication.get(this);

        if (graphObject.getReportComponent() == null) {
            ProfileComponent profileComponent = graphObject.getProfileComponent();
            ReportComponent reportComponent = profileComponent.plus(new ReportModule(resource.getUri()));
            graphObject.setReportComponent(reportComponent);
        }

        if (savedInstanceState == null) {
            ReportVisualizeFragment viewFragment = ReportVisualizeFragment_.builder()
                    .build();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.control, viewFragment, ReportVisualizeFragment.TAG)
                    .commit();
        }
    }

    @Override
    public void finish() {
        super.finish();
        graphObject.releaseReportComponent();
    }

    @Override
    public ReportVisualizeActivityComponent getComponent() {
        return JasperMobileApplication.get(this)
                .getReportComponent()
                .plusReportVisualizeActivity(new ActivityModule(this));
    }
}
