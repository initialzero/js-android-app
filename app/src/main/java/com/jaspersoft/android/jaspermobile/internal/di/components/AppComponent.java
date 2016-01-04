package com.jaspersoft.android.jaspermobile.internal.di.components;

import com.jaspersoft.android.jaspermobile.auth.JasperAuthenticator;
import com.jaspersoft.android.jaspermobile.internal.di.modules.ActivityModule;
import com.jaspersoft.android.jaspermobile.internal.di.modules.AppModule;
import com.jaspersoft.android.jaspermobile.internal.di.modules.ProfileModule;
import com.jaspersoft.android.jaspermobile.internal.di.modules.ServerClientModule;
import com.jaspersoft.android.jaspermobile.presentation.view.activity.BaseActivity;

import javax.inject.Singleton;

import dagger.Component;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@Singleton
@Component(modules = {
        AppModule.class,
        ProfileModule.class,

})
public interface AppComponent {
    void inject(BaseActivity baseActivity);
    void inject(JasperAuthenticator authenticator);

    AuthenticatorActivityComponent plus(ActivityModule activityModule, ServerClientModule serverClientModule);
}
