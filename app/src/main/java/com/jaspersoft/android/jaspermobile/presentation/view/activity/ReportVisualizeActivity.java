package com.jaspersoft.android.jaspermobile.presentation.view.activity;

import android.os.Bundle;

import com.jaspersoft.android.jaspermobile.R;
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
public class ReportVisualizeActivity extends ToolbarActivity {
    @Extra
    protected ResourceLookup resource;
    @Bean
    protected ScrollableTitleHelper scrollableTitleHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scrollableTitleHelper.injectTitle(resource.getLabel());

        if (savedInstanceState == null) {
            ReportVisualizeFragment viewFragment = ReportVisualizeFragment_.builder()
                    .resource(resource)
                    .build();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.control, viewFragment, ReportVisualizeFragment.TAG)
                    .commit();
        }
    }
}
