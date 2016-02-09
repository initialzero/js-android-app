package com.jaspersoft.android.jaspermobile.presentation.view.fragment;

import android.app.Activity;
import android.content.Intent;

import com.jaspersoft.android.jaspermobile.GraphObject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.navigation.NavigationActivity_;
import com.jaspersoft.android.jaspermobile.internal.di.HasComponent;
import com.jaspersoft.android.jaspermobile.internal.di.components.ProfileComponent;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public enum ComponentProviderDelegate {
    INSTANCE;

    @SuppressWarnings("unchecked")
    public <C> C getComponent(Activity activity, Class<C> componentType) {
        return componentType.cast(((HasComponent<C>) activity).getComponent());
    }

    public ProfileComponent getProfileComponent(Activity activity) {
        GraphObject graphObject = GraphObject.Factory.from(activity);
        ProfileComponent profileComponent = graphObject.getProfileComponent();
        if (profileComponent == null) {
            NavigationActivity_.intent(activity)
                    .currentSelection(R.id.vg_saved_items)
                    .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    .start();
            activity.finish();
        }
        return profileComponent;
    }
}
