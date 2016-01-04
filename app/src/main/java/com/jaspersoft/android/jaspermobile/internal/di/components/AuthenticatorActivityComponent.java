package com.jaspersoft.android.jaspermobile.internal.di.components;

import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;
import com.jaspersoft.android.jaspermobile.internal.di.modules.ActivityModule;
import com.jaspersoft.android.jaspermobile.internal.di.modules.AnonymousServicesModule;
import com.jaspersoft.android.jaspermobile.internal.di.modules.AuthenticatorModule;
import com.jaspersoft.android.jaspermobile.internal.di.modules.CredentialsModule;
import com.jaspersoft.android.jaspermobile.internal.di.modules.JasperServerModule;
import com.jaspersoft.android.jaspermobile.internal.di.modules.ServerClientModule;
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
                ActivityModule.class,
                AnonymousServicesModule.class,
                CredentialsModule.class,
                JasperServerModule.class,
        }
)
public interface AuthenticatorActivityComponent {
    AuthenticatorFragment inject(AuthenticatorFragment authenticatorFragment);
}
