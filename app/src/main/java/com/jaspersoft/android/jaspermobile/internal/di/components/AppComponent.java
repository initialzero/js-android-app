package com.jaspersoft.android.jaspermobile.internal.di.components;

import com.jaspersoft.android.jaspermobile.JasperMobileModule;
import com.jaspersoft.android.jaspermobile.auth.JasperAuthenticator;
import com.jaspersoft.android.jaspermobile.internal.di.modules.ProfileModule;
import com.jaspersoft.android.jaspermobile.internal.di.modules.activity.ActivityModule;
import com.jaspersoft.android.jaspermobile.internal.di.modules.app.AppModule;
import com.jaspersoft.android.jaspermobile.internal.di.modules.app.CacheModule;
import com.jaspersoft.android.jaspermobile.internal.di.modules.app.RepoModule;
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
        CacheModule.class,
        RepoModule.class,
})
public interface AppComponent {
    void inject(BaseActivity baseActivity);
    void inject(JasperAuthenticator authenticator);
    void inject(JasperMobileModule module);

    AuthenticatorActivityComponent plus(ActivityModule activityModule);
    ProfileComponent plus(ProfileModule profileModule);
}
