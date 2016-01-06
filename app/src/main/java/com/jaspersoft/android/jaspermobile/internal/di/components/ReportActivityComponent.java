package com.jaspersoft.android.jaspermobile.internal.di.components;

import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;
import com.jaspersoft.android.jaspermobile.internal.di.modules.activity.ActivityModule;
import com.jaspersoft.android.jaspermobile.internal.di.modules.activity.ReportActivityModule;
import com.jaspersoft.android.jaspermobile.presentation.view.fragment.ReportViewFragment;

import dagger.Subcomponent;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerActivity
@Subcomponent(
        modules = {
                ReportActivityModule.class,
                ActivityModule.class
        }
)
public interface ReportActivityComponent {
    void inject(ReportViewFragment reportViewFragment);
}
