package com.jaspersoft.android.jaspermobile.activities.info;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.report.SaveReportActivity_;
import com.jaspersoft.android.sdk.util.FileUtils;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;

import java.util.ArrayList;

import roboguice.inject.InjectView;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
@OptionsMenu(R.menu.save_item_menu)
@EFragment(R.layout.fragment_resource_info)
public class ReportInfoFragment extends ResourceInfoFragment {

    @InjectView(R.id.ri_report_option_container)
    protected View roContainer;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        showReportOptions();
    }

    @OptionsItem(R.id.saveAction)
    protected void saveReport() {
        if (FileUtils.isExternalStorageWritable()) {
            SaveReportActivity_.intent(this)
                    .resource(resourceLookup)
                    .pageCount(0)
                    .start();
        } else {
            Toast.makeText(getActivity(),
                    R.string.rv_t_external_storage_not_available, Toast.LENGTH_SHORT).show();
        }
    }

    private void showReportOptions() {
        roContainer.setVisibility(View.VISIBLE);

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
}
