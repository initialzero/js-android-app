package com.jaspersoft.android.jaspermobile.test.acceptance.library;

import android.app.Activity;
import android.content.ComponentName;
import android.widget.AbsListView;

import com.google.android.apps.common.testing.testrunner.ActivityLifecycleCallback;
import com.google.android.apps.common.testing.testrunner.Stage;
import com.jaspersoft.android.jaspermobile.activities.repository.LibraryActivity_;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class ListViewInjector implements ActivityLifecycleCallback {
    private final AdapterIdlingResource adapterIdlingResource;

    public ListViewInjector(AdapterIdlingResource adapterIdlingResource) {
        this.adapterIdlingResource = adapterIdlingResource;
    }

    @Override
    public void onActivityLifecycleChanged(Activity activity, Stage stage) {
        ComponentName targetComponentName =
                new ComponentName(activity, LibraryActivity_.class.getName());

        ComponentName currentComponentName = activity.getComponentName();
        if (!currentComponentName.equals(targetComponentName)) return;

        switch (stage) {
            case CREATED:
                // We need to wait for the activity to be created before getting a reference
                // to the webview
                AbsListView absListView = (AbsListView) activity.findViewById(android.R.id.list);

                adapterIdlingResource.inject(absListView);
                break;
            case STOPPED:
                // Clean up reference
                if (activity.isFinishing()) adapterIdlingResource.clear();
                break;
            default: // NOP
        }
    }

}
