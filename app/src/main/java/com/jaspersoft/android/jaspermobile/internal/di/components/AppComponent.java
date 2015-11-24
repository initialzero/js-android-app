package com.jaspersoft.android.jaspermobile.internal.di.components;

import android.content.Context;

import com.jaspersoft.android.jaspermobile.auth.JasperAuthenticator;
import com.jaspersoft.android.jaspermobile.data.cache.ProfileCache;
import com.jaspersoft.android.jaspermobile.domain.interactor.CompositeUseCase;
import com.jaspersoft.android.jaspermobile.domain.network.Authenticator;
import com.jaspersoft.android.jaspermobile.domain.repository.CredentialsRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.JasperServerRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.ProfileRepository;
import com.jaspersoft.android.jaspermobile.internal.di.modules.AppModule;
import com.jaspersoft.android.jaspermobile.internal.di.modules.ProfileModule;
import com.jaspersoft.android.jaspermobile.presentation.view.activity.BaseActivity;

import javax.inject.Named;
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

    Context appContext();
    CompositeUseCase compositeUseCase();
    Authenticator.Factory authApiFactory();

    // Profile dependencies
    // Repos
    @Named("profileAccountCache")
    ProfileCache profileAccountCache();
    ProfileRepository profileRepository();
    CredentialsRepository credentialsRepository();
    JasperServerRepository serverRepository();

    @Named("accountType")
    String provideAccountType();

}
