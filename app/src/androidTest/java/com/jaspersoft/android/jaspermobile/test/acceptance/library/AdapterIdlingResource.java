package com.jaspersoft.android.jaspermobile.test.acceptance.library;

import android.database.DataSetObservable;
import android.widget.AbsListView;

import com.jaspersoft.android.jaspermobile.test.utils.espresso.ActivityLifecycleIdlingResource;

import static com.google.android.apps.common.testing.testrunner.util.Checks.checkNotNull;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class AdapterIdlingResource implements ActivityLifecycleIdlingResource<AbsListView> {
    private ResourceCallback callback;
    private AbsListView listView;
    private boolean isIdle = true;

    @Override
    public void inject(AbsListView activityComponent) {
        this.listView = checkNotNull(activityComponent,
                String.format("Trying to instantiate a \'%s\' with a null WebView", getName()));
    }

    @Override
    public void clear() {
        listView = null;
    }

    @Override
    public String getName() {
        return "AdapterView idling resource";
    }

    @Override
    public boolean isIdleNow() {
        if (listView == null) return true;
        return true;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
        this.callback = resourceCallback;
    }

    private class MyDataObservable extends DataSetObservable {
        public void notifyChanged() {
            super.notifyChanged();
        }
    }
}
