package com.jaspersoft.android.jaspermobile.internal.di.modules;

import android.accounts.AccountManager;
import android.app.Application;
import android.content.Context;

import com.jaspersoft.android.jaspermobile.BackgroundThread;
import com.jaspersoft.android.jaspermobile.UIThread;
import com.jaspersoft.android.jaspermobile.data.network.AuthenticatorFactory;
import com.jaspersoft.android.jaspermobile.data.network.ServerApiFactory;
import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.interactor.CompositeUseCase;
import com.jaspersoft.android.jaspermobile.domain.network.Authenticator;
import com.jaspersoft.android.jaspermobile.domain.network.ServerApi;
import com.jaspersoft.android.jaspermobile.util.JasperSettings;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@Module
public final class AppModule {
    private final Application mApplication;

    public AppModule(Application application) {
        mApplication = application;
    }

    @Provides
    @Singleton
    Context provideApplicationContext() {
        return mApplication;
    }

    @Provides
    @Singleton
    PostExecutionThread providePostExecutionThread(UIThread uiThread) {
        return uiThread;
    }

    @Provides
    @Singleton
    PreExecutionThread providePreExecutionThread(BackgroundThread backgroundThread) {
        return backgroundThread;
    }

    @Provides
    @Singleton
    @Named("accountType")
    String provideAccountType() {
        return JasperSettings.JASPER_ACCOUNT_TYPE;
    }

    @Provides
    @Singleton
    CompositeUseCase provideCompositeUseCase(PostExecutionThread uiThread, PreExecutionThread backgroundThread) {
        return new CompositeUseCase(uiThread, backgroundThread);
    }

    @Provides
    @Singleton
    ServerApi.Factory providesServerInfoFactory(ServerApiFactory apiFactory) {
        return apiFactory;
    }

    @Provides
    @Singleton
    Authenticator.Factory providesAuthenticatorFactory(AuthenticatorFactory factory) {
        return factory;
    }

    @Provides
    @Singleton
    AccountManager providesAccountManager(Context context) {
        return AccountManager.get(context);
    }
}
