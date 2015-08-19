package com.jaspersoft.android.jaspermobile.activities.info;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.jaspersoft.android.jaspermobile.activities.robospice.RoboToolbarActivity;

import org.androidannotations.annotations.EActivity;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
@EActivity
public class ResourceInfoActivity extends RoboToolbarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fragment resourceInfoFragment = getSupportFragmentManager().findFragmentByTag(ResourceInfoFragment.TAG);
        if (resourceInfoFragment == null) {
            resourceInfoFragment = ResourceInfoFragment_.builder().build();
        }
        commitContent(resourceInfoFragment, ResourceInfoFragment.TAG);
    }

    private void commitContent(@NonNull Fragment directFragment, String fragmentTag) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction
                .replace(android.R.id.content, directFragment, fragmentTag)
                .commit();
    }


}
