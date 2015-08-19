package com.jaspersoft.android.jaspermobile.activities.info;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceFragment;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsMenu;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
@EFragment(R.layout.fragment_resource_info)
@OptionsMenu(R.menu.am_resource_info_menu)
public class ResourceInfoFragment extends RoboSpiceFragment {

    public static final String TAG = ResourceInfoFragment.class.getSimpleName();

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setToolbar(view);

        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) view.findViewById(R.id.info_collapsing_toolbar);
        collapsingToolbar.setTitle("Title");
    }

    private void setToolbar(View infoView) {
        Toolbar toolbar = (Toolbar) infoView.findViewById(R.id.toolbar);
      //  infoView.findViewById(R.id.toolbarContainer).setPadding(0, getStatusBarHeight(), 0, 0);

        ActionBarActivity actionBarActivity = (ActionBarActivity) getActivity();
        actionBarActivity.setSupportActionBar(toolbar);

        ActionBar actionBar = actionBarActivity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_close);
        }
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
