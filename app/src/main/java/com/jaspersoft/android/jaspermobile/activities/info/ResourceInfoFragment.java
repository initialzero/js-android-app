package com.jaspersoft.android.jaspermobile.activities.info;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceFragment;
import com.jaspersoft.android.jaspermobile.util.resource.viewbinder.ResourceViewHelper;
import com.jaspersoft.android.jaspermobile.widget.TopCropImageView;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OptionsMenu;

import java.util.ArrayList;

import roboguice.inject.InjectView;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
@EFragment(R.layout.fragment_resource_info)
@OptionsMenu(R.menu.am_resource_info_menu)
public class ResourceInfoFragment extends RoboSpiceFragment {

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
    protected TextView resModidiedDate;

    @InjectView(R.id.ri_creation_date)
    protected TextView resCreationDate;

    @Inject
    protected JsRestClient jsRestClient;

    private ResourceViewHelper viewHelper;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setToolbar(view);

        showReportOptions();
        fillWithData();

        viewHelper = new ResourceViewHelper(getActivity());
        viewHelper.populateView(new InfoHeaderView(toolbarImage, toolbarLayout), resourceLookup);
    }

    private void fillWithData() {
        resType.setText(resourceLookup.getResourceType().toString());
        resLabel.setText(resourceLookup.getLabel());
        resDescription.setText(resourceLookup.getDescription());
        resUri.setText(resourceLookup.getUri());
        resModidiedDate.setText(resourceLookup.getUpdateDate());
        resCreationDate.setText(resourceLookup.getCreationDate());
    }

    private void showReportOptions() {
        ArrayList<String> reportOptions = new ArrayList<>();
        reportOptions.add("New report options");
        reportOptions.add("Test option");

        // It's a hack to make spinner width as a selected item width
        ArrayAdapter<String> reportOptionAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, reportOptions) {
            @Override
            public View getView(final int position, final View convertView,
                                final ViewGroup parent) {
                int selectedItemPosition = reportOption.getSelectedItemPosition();
                return super.getView(selectedItemPosition, convertView, parent);
            }
        };
        reportOptionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reportOption.setAdapter(reportOptionAdapter);
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
