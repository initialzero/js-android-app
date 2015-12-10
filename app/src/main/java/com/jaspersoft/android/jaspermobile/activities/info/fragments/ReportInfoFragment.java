package com.jaspersoft.android.jaspermobile.activities.info.fragments;

import android.widget.Toast;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.save.SaveReportActivity_;
import com.jaspersoft.android.sdk.util.FileUtils;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
@OptionsMenu(R.menu.save_item_menu)
@EFragment(R.layout.fragment_resource_info)
public class ReportInfoFragment extends ResourceInfoFragment {

    @OptionsItem(R.id.saveAction)
    protected void saveReport() {
        if (FileUtils.isExternalStorageWritable()) {
            SaveReportActivity_.intent(this)
                    .resource(mResourceLookup)
                    .pageCount(0)
                    .start();
        } else {
            Toast.makeText(getActivity(),
                    R.string.rv_t_external_storage_not_available, Toast.LENGTH_SHORT).show();
        }
    }
}
