/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.internal.di.modules;

import com.jaspersoft.android.jaspermobile.data.cache.ProfileCache;
import com.jaspersoft.android.jaspermobile.data.cache.ProfileCacheImpl;
import com.jaspersoft.android.jaspermobile.data.entity.mapper.ServerInfoDataMapper;
import com.jaspersoft.android.jaspermobile.data.repository.ProfileDataRepository;
import com.jaspersoft.android.jaspermobile.data.server.JasperServerFactoryImpl;
import com.jaspersoft.android.jaspermobile.data.validator.JasperServerValidatorImpl;
import com.jaspersoft.android.jaspermobile.data.validator.ProfileValidatorImpl;
import com.jaspersoft.android.jaspermobile.data.validator.SpringCredentialsValidator;
import com.jaspersoft.android.jaspermobile.domain.BaseCredentials;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.interactor.SaveProfile;
import com.jaspersoft.android.jaspermobile.domain.interactor.UseCase;
import com.jaspersoft.android.jaspermobile.domain.repository.ProfileRepository;
import com.jaspersoft.android.jaspermobile.domain.server.JasperServerFactory;
import com.jaspersoft.android.jaspermobile.domain.validator.CredentialsValidator;
import com.jaspersoft.android.jaspermobile.domain.validator.JasperServerValidator;
import com.jaspersoft.android.jaspermobile.domain.validator.ProfileValidator;
import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;
import com.jaspersoft.android.sdk.service.auth.JrsAuthenticator;
import com.jaspersoft.android.sdk.service.server.ServerInfoService;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@Module
public final class SaveProfileModule {
    private final String mBaseUrl;
    private final Profile mProfile;
    private final BaseCredentials mCredentials;

    public SaveProfileModule(String baseUrl, Profile profile, BaseCredentials credentials) {
        mBaseUrl = baseUrl;
        mProfile = profile;
        mCredentials = credentials;
    }

    @Provides
    @Named("baseUrl")
    String providesBaseUrl() {
        return mBaseUrl;
    }

    @Provides
    BaseCredentials provideCredentials() {
        return mCredentials;
    }

    @Provides
    Profile providesProfile() {
        return mProfile;
    }

    @Provides
    ProfileValidator provideProfileValidator(ProfileValidatorImpl profileValidator) {
        return profileValidator;
    }

    @Provides
    @PerActivity
    JasperServerValidator providesServerValidator(JasperServerValidatorImpl validator) {
        return validator;
    }

    @Provides
    CredentialsValidator providesCredentialsValidator(BaseCredentials baseCredentials,
                                                      JrsAuthenticator authenticator) {
        return new SpringCredentialsValidator(baseCredentials, authenticator);
    }

    @Provides
    JasperServerFactory provideServerFactory(@Named("baseUrl") String baseUrl,
                                             ServerInfoService infoService,
                                             ServerInfoDataMapper dataMapper) {
        return new JasperServerFactoryImpl(baseUrl, infoService, dataMapper);
    }

    @PerActivity
    @Provides
    ProfileCache provideProfileCache(ProfileCacheImpl profileCache) {
        return profileCache;
    }

    @PerActivity
    @Provides
    ProfileRepository providesProfileRepository(ProfileDataRepository dataRepository) {
        return dataRepository;
    }

    @Provides
    @PerActivity
    @Named("saveProfile")
    UseCase provideAddProfileUseCase(
            JasperServerFactory serverFactory,
            CredentialsValidator credentialsValidator,
            JasperServerValidator serverValidator,
            ProfileValidator profileValidator,
            ProfileRepository repository,
            PreExecutionThread threadExecutor,
            PostExecutionThread postExecutionThread) {
        return new SaveProfile(
                serverFactory,
                credentialsValidator,
                serverValidator,
                profileValidator,
                repository,
                threadExecutor,
                postExecutionThread);
    }
}
