package com.jaspersoft.android.jaspermobile.test.acceptance.library;

import android.database.DataSetObserver;
import android.util.Log;
import android.widget.ListView;

import com.jaspersoft.android.jaspermobile.activities.repository.fragment.ResourcesFragment;
import com.jaspersoft.android.jaspermobile.test.utils.espresso.ActivityLifecycleIdlingResource;

import static com.google.android.apps.common.testing.testrunner.util.Checks.checkNotNull;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class ResourcesFragmentIdlingResource implements ActivityLifecycleIdlingResource<ResourcesFragment> {
    private ResourceCallback callback;
    private ResourcesFragment resourcesFragment;
    private ListView listView;

    @Override
    public void inject(ResourcesFragment activityComponent) {
        this.resourcesFragment = checkNotNull(activityComponent,
                String.format("Trying to instantiate a \'%s\' with a null ResourcesFragment", getName()));
        listView = (ListView) resourcesFragment.getView();
        listView.getAdapter().registerDataSetObserver(new DataObservable());
    }

    @Override
    public void clear() {
        resourcesFragment = null;
    }

    @Override
    public String getName() {
        return "ResourcesFragment adapter idling resource";
    }

    @Override
    public boolean isIdleNow() {
        boolean isIdling = true;
        if (resourcesFragment == null) {
            isIdling = true;
            Log.d("isIdleNow", "(resourcesFragment == null) ============> " + isIdling);
            return true;
        }
        isIdling = (!resourcesFragment.isLoading() && callback != null);
        Log.d("isIdleNow", "(!resourcesFragment.isLoading() && callback != null) ============> " + isIdling);
        return isIdling;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
        this.callback = resourceCallback;
    }

    private class DataObservable extends DataSetObserver {
        public void onChanged() {
            super.onChanged();
            callback.onTransitionToIdle();
        }
    }
}
