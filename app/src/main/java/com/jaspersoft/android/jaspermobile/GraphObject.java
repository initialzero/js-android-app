package com.jaspersoft.android.jaspermobile;

import com.jaspersoft.android.jaspermobile.internal.di.components.AppComponent;
import com.jaspersoft.android.jaspermobile.internal.di.components.ProfileComponent;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface GraphObject {

    AppComponent getComponent();

    void setProfileComponent(ProfileComponent profileComponent);

    ProfileComponent getProfileComponent();
}
