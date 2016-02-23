package com.jaspersoft.android.jaspermobile.internal.di.components;

import com.jaspersoft.android.jaspermobile.activities.viewer.html.dashboard.Amber2DashboardActivity;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.dashboard.BaseDashboardActivity;
import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;
import com.jaspersoft.android.jaspermobile.internal.di.modules.activity.ActivityModule;
import com.jaspersoft.android.jaspermobile.internal.di.modules.activity.DashboardModule;

import dagger.Subcomponent;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerActivity
@Subcomponent(
        modules = {
                ActivityModule.class,
                DashboardModule.class,
        }
)
public interface DashboardActivityComponent {
    void inject(BaseDashboardActivity activity);
    void inject(Amber2DashboardActivity amber2DashboardActivity);
}
