package com.jaspersoft.android.jaspermobile.internal.di.components;

import android.content.Context;

import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.interactor.CompositeUseCase;
import com.jaspersoft.android.jaspermobile.domain.network.Authenticator;
import com.jaspersoft.android.jaspermobile.domain.network.ServerApi;
import com.jaspersoft.android.jaspermobile.internal.di.modules.AppModule;
import com.jaspersoft.android.jaspermobile.presentation.view.activity.BaseActivity;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Component;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {
    void inject(BaseActivity baseActivity);

    Context appContext();
    PreExecutionThread preExecutionThread();
    PostExecutionThread postExecutionThread();
    CompositeUseCase compositeUseCase();
    ServerApi.Factory serverApiFactory();
    Authenticator.Factory authApiFactory();

    @Named("accountType")
    String provideAccountType();
}
