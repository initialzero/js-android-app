package com.jaspersoft.android.jaspermobile.internal.di.components;

import android.content.Context;

import com.jaspersoft.android.jaspermobile.internal.di.modules.AppModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {

    Context appContext();
}
