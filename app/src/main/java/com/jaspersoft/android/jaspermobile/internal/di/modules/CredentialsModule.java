package com.jaspersoft.android.jaspermobile.internal.di.modules;

import android.accounts.AccountManager;
import android.content.Context;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.data.cache.AccountCredentialsCache;
import com.jaspersoft.android.jaspermobile.data.cache.CredentialsCache;
import com.jaspersoft.android.jaspermobile.data.entity.mapper.AccountDataMapper;
import com.jaspersoft.android.jaspermobile.data.repository.CredentialsDataRepository;
import com.jaspersoft.android.jaspermobile.data.validator.CredentialsValidatorImpl;
import com.jaspersoft.android.jaspermobile.domain.repository.CredentialsRepository;
import com.jaspersoft.android.jaspermobile.domain.validator.CredentialsValidator;
import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;
import com.jaspersoft.android.jaspermobile.util.security.PasswordManager;

import dagger.Module;
import dagger.Provides;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@Module
public final class CredentialsModule {
    @PerActivity
    @Provides
    CredentialsRepository providesCredentialsDataRepository(CredentialsDataRepository repository) {
        return repository;
    }

    @PerActivity
    @Provides
    CredentialsCache provideCredentialsCache(Context context, AccountManager accountManager, AccountDataMapper accountDataMapper) {
        String secret = context.getString(R.string.password_salt_key);
        PasswordManager passwordManager = PasswordManager.init(context, secret);
        return new AccountCredentialsCache(accountManager, passwordManager, accountDataMapper);
    }

    @PerActivity
    @Provides
    CredentialsValidator providesCredentialsValidator(CredentialsValidatorImpl validator) {
        return validator;
    }
}
