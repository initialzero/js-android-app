package com.jaspersoft.android.jaspermobile.test.acceptance.library;

import android.app.Activity;
import android.content.ComponentName;

import com.google.android.apps.common.testing.testrunner.ActivityLifecycleCallback;
import com.google.android.apps.common.testing.testrunner.Stage;
import com.jaspersoft.android.jaspermobile.activities.repository.LibraryActivity;
import com.jaspersoft.android.jaspermobile.activities.repository.LibraryActivity_;
import com.jaspersoft.android.jaspermobile.activities.repository.fragment.ResourcesControllerFragment;
import com.jaspersoft.android.jaspermobile.activities.repository.fragment.ResourcesFragment;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class ResourceFragmentInjector implements ActivityLifecycleCallback {
    private final ResourcesFragmentIdlingResource adapterIdlingResource;

    public ResourceFragmentInjector(ResourcesFragmentIdlingResource adapterIdlingResource) {
        this.adapterIdlingResource = adapterIdlingResource;
    }

    @Override
    public void onActivityLifecycleChanged(Activity activity, Stage stage) {
        ComponentName targetComponentName =
                new ComponentName(activity, LibraryActivity_.class.getName());

        ComponentName currentComponentName = activity.getComponentName();
        if (!currentComponentName.equals(targetComponentName)) return;

        switch (stage) {
            case STARTED:
                LibraryActivity libraryActivity = (LibraryActivity) activity;
                ResourcesFragment resourcesFragment =
                        (ResourcesFragment) libraryActivity.getSupportFragmentManager()
                                .findFragmentByTag(ResourcesControllerFragment.CONTENT_TAG);
                adapterIdlingResource.inject(resourcesFragment);
                break;
            case STOPPED:
                // Clean up reference
                if (activity.isFinishing()) adapterIdlingResource.clear();
                break;
            default: // NOP
        }
    }

}
