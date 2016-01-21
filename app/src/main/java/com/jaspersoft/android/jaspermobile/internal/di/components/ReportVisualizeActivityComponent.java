package com.jaspersoft.android.jaspermobile.internal.di.components;

import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;
import com.jaspersoft.android.jaspermobile.internal.di.modules.activity.ActivityModule;
import com.jaspersoft.android.jaspermobile.internal.di.modules.activity.ReportVisualizeActivityModule;
import com.jaspersoft.android.jaspermobile.presentation.view.fragment.ReportVisualizeFragment;

import dagger.Subcomponent;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerActivity
@Subcomponent(
        modules = {
                ReportVisualizeActivityModule.class,
                ActivityModule.class
        }
)
public interface ReportVisualizeActivityComponent {
    void inject(ReportVisualizeFragment reportVisualizeFragment);
}
