package com.jaspersoft.android.jaspermobile.internal.di.components;

import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.jaspermobile.internal.di.modules.ProfileModule;
import com.jaspersoft.android.jaspermobile.internal.di.modules.ReportModule;

import dagger.Subcomponent;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
@Subcomponent(
        modules = ProfileModule.class
)
public interface ProfileComponent {
    ReportComponent plus(ReportModule reportModule);
}
