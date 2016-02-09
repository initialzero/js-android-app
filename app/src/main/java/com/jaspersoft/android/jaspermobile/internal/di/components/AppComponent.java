package com.jaspersoft.android.jaspermobile.internal.di.components;

import com.jaspersoft.android.jaspermobile.JasperMobileApplication;
import com.jaspersoft.android.jaspermobile.auth.JasperAuthenticator;
import com.jaspersoft.android.jaspermobile.internal.di.modules.ProfileModule;
import com.jaspersoft.android.jaspermobile.internal.di.modules.activity.ActivityModule;
import com.jaspersoft.android.jaspermobile.internal.di.modules.activity.StartupActivityModule;
import com.jaspersoft.android.jaspermobile.internal.di.modules.app.AppModule;
import com.jaspersoft.android.jaspermobile.internal.di.modules.app.CacheModule;
import com.jaspersoft.android.jaspermobile.internal.di.modules.app.ConstantsModule;
import com.jaspersoft.android.jaspermobile.internal.di.modules.app.RepoModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@Singleton
@Component(modules = {
        AppModule.class,
        CacheModule.class,
        RepoModule.class,
        ConstantsModule.class,
})
public interface AppComponent {
    void inject(JasperAuthenticator authenticator);
    void inject(JasperMobileApplication application);

    AuthenticatorActivityComponent plus(ActivityModule activityModule);
    StartupActivityComponent plus(StartupActivityModule startupActivityModule);
    ProfileComponent plus(ProfileModule profileModule);
}
