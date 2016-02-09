package com.jaspersoft.android.jaspermobile.internal.di.components;

import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;
import com.jaspersoft.android.jaspermobile.internal.di.modules.activity.ActivityModule;
import com.jaspersoft.android.jaspermobile.presentation.view.activity.ToolbarActivity;

import dagger.Subcomponent;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerActivity
@Subcomponent(
        modules = {
                ActivityModule.class
        }
)
public interface BaseActivityComponent {
    void inject(ToolbarActivity toolbarActivity);
}
