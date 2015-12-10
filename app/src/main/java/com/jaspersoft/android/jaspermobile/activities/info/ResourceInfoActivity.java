package com.jaspersoft.android.jaspermobile.activities.info;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.jaspersoft.android.jaspermobile.activities.info.fragments.ReportInfoFragment_;
import com.jaspersoft.android.jaspermobile.activities.info.fragments.ResourceInfoFragment;
import com.jaspersoft.android.jaspermobile.activities.info.fragments.ResourceInfoFragment_;
import com.jaspersoft.android.jaspermobile.activities.info.fragments.SavedItemInfoFragment_;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboToolbarActivity;
import com.jaspersoft.android.jaspermobile.util.resource.JasperResource;
import com.jaspersoft.android.jaspermobile.util.resource.JasperResourceType;
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
    protected JasperResource jasperResource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fragment resourceInfoFragment = getSupportFragmentManager().findFragmentByTag(ResourceInfoFragment.TAG);

        if (resourceInfoFragment == null) {
            switch (jasperResource.getResourceType()) {
                case report:
                    resourceInfoFragment = ReportInfoFragment_.builder()
                            .jasperResource(jasperResource)
                            .build();
                    break;
                case saved_item:
                    resourceInfoFragment = SavedItemInfoFragment_.builder()
                            .jasperResource(jasperResource)
                            .build();
                    break;
                default:
                    resourceInfoFragment = ResourceInfoFragment_.builder()
                            .jasperResource(jasperResource)
                            .build();
            }
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
