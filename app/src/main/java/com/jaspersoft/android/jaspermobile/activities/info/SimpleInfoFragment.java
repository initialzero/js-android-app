package com.jaspersoft.android.jaspermobile.activities.info;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceFragment;
import com.jaspersoft.android.jaspermobile.widget.TopCropImageView;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;

import roboguice.inject.InjectView;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
@EFragment(R.layout.fragment_resource_info)
public class SimpleInfoFragment extends RoboSpiceFragment {

    public static final String TAG = ResourceInfoFragment.class.getSimpleName();

    @FragmentArg
    protected ResourceLookup resourceLookup;

    @InjectView(R.id.toolbarImageView)
    protected TopCropImageView toolbarImage;

    @InjectView(R.id.info_collapsing_toolbar)
    protected CollapsingToolbarLayout toolbarLayout;

    @InjectView(R.id.ri_report_option)
    protected Spinner reportOption;

    @InjectView(R.id.ri_type)
    protected TextView resType;

    @InjectView(R.id.ri_label)
    protected TextView resLabel;

    @InjectView(R.id.ri_descritpion)
    protected TextView resDescription;

    @InjectView(R.id.ri_uri)
    protected TextView resUri;

    @InjectView(R.id.ri_modified_date)
    protected TextView resModifiedDate;

    @InjectView(R.id.ri_creation_date)
    protected TextView resCreationDate;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setToolbar(view);
        fillWithData();
    }

    private void fillWithData() {
        resType.setText(resourceLookup.getResourceType().toString());
        resLabel.setText(resourceLookup.getLabel());
        resDescription.setText(resourceLookup.getDescription());
        resUri.setText(resourceLookup.getUri());
        resModifiedDate.setText(resourceLookup.getUpdateDate());
        resCreationDate.setText(resourceLookup.getCreationDate());
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
