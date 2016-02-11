package com.jaspersoft.android.jaspermobile.presentation.view.fragment;

import android.app.Activity;

import com.jaspersoft.android.jaspermobile.GraphObject;
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
            throw new IllegalStateException("Profile component missing it looks like it was GC or manually nulled");
        }
        return profileComponent;
    }
}
