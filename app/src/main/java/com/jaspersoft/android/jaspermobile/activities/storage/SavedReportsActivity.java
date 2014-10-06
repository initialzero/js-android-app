package com.jaspersoft.android.jaspermobile.activities.storage;

import android.app.ActionBar;
import android.os.Bundle;

import com.jaspersoft.android.jaspermobile.activities.robospice.RoboAccentFragmentActivity;
import com.jaspersoft.android.jaspermobile.activities.storage.fragment.SavedItemsControllerFragment;
import com.jaspersoft.android.jaspermobile.activities.storage.fragment.SavedItemsControllerFragment_;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;

/**
 * @author Ivan Gadzhega
 * @author Tom Koptel
 * @since 1.8
 */
@EActivity
public class SavedReportsActivity extends RoboAccentFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            SavedItemsControllerFragment controllerFragment = SavedItemsControllerFragment_.builder().build();
            getSupportFragmentManager().beginTransaction()
                    .add(controllerFragment, SavedItemsControllerFragment.TAG)
                    .commit();
        }
    }

    @OptionsItem(android.R.id.home)
    final void goHome() {
        super.onBackPressed();
    }

}
