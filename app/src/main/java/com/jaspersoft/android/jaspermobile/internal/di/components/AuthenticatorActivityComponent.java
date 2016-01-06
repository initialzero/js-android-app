package com.jaspersoft.android.jaspermobile.internal.di.components;

import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;
import com.jaspersoft.android.jaspermobile.internal.di.modules.activity.ActivityModule;
import com.jaspersoft.android.jaspermobile.internal.di.modules.activity.AuthenticatorModule;
import com.jaspersoft.android.jaspermobile.internal.di.modules.activity.ServerClientModule;
import com.jaspersoft.android.jaspermobile.presentation.view.fragment.AuthenticatorFragment;

import dagger.Subcomponent;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerActivity
@Subcomponent(
        modules = {
                AuthenticatorModule.class,
                ServerClientModule.class,
                ActivityModule.class
        }
)
public interface AuthenticatorActivityComponent {
    AuthenticatorFragment inject(AuthenticatorFragment authenticatorFragment);
}
