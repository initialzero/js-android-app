package com.jaspersoft.android.jaspermobile.internal.di.modules.app;

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
public final class ConstantsModule {
    @Provides
    @Singleton
    @Named("accountType")
    String provideAccountType() {
        return JasperSettings.JASPER_ACCOUNT_TYPE;
    }

    @Provides
    @Singleton
    @Named("reserved_account_name")
    String provideReservedAccountName() {
        return JasperSettings.RESERVED_ACCOUNT_NAME;
    }

    @Provides
    @Named("LIMIT")
    @Singleton
    Integer provideLimit() {
        return 100;
    }

    @Provides
    @Named("THRESHOLD")
    @Singleton
    Integer provideThreshold() {
        return 5;
    }
}
