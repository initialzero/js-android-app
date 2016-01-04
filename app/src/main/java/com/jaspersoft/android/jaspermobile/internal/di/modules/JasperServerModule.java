package com.jaspersoft.android.jaspermobile.internal.di.modules;

import com.jaspersoft.android.jaspermobile.data.cache.AccountServerCache;
import com.jaspersoft.android.jaspermobile.data.cache.ServerCache;
import com.jaspersoft.android.jaspermobile.data.repository.JasperServerDataRepository;
import com.jaspersoft.android.jaspermobile.data.validator.ServerValidatorImpl;
import com.jaspersoft.android.jaspermobile.domain.repository.JasperServerRepository;
import com.jaspersoft.android.jaspermobile.domain.validator.ServerValidator;
import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;

import dagger.Module;
import dagger.Provides;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@Module
public final class JasperServerModule {

    @PerActivity
    @Provides
    JasperServerRepository providesServerRepository(JasperServerDataRepository repository) {
        return repository;
    }

    @PerActivity
    @Provides
    ServerCache providesJasperSeverCache(AccountServerCache cache) {
        return cache;
    }

    @PerActivity
    @Provides
    ServerValidator providesServerValidator(ServerValidatorImpl validator) {
        return validator;
    }
}
