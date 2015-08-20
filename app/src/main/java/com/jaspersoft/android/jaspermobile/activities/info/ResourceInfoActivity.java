package com.jaspersoft.android.jaspermobile.activities.info;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.jaspersoft.android.jaspermobile.activities.robospice.RoboToolbarActivity;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
@EActivity
public class ResourceInfoActivity extends RoboToolbarActivity {

    @Extra
    protected ResourceLookup resourceLookup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fragment resourceInfoFragment = getSupportFragmentManager().findFragmentByTag(ResourceInfoFragment.TAG);
        if (resourceInfoFragment == null) {
            resourceInfoFragment = ResourceInfoFragment_.builder()
                    .resourceLookup(resourceLookup)
                    .build();
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
