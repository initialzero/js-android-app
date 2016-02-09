package com.jaspersoft.android.jaspermobile.presentation.view.fragment;

import com.jaspersoft.android.jaspermobile.internal.di.components.ProfileComponent;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface ComponentProvider {
    <C> C getComponent(Class<C> componentType);

    ProfileComponent getProfileComponent();
}
