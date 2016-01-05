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

package com.jaspersoft.android.jaspermobile.domain.interactor;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.data.repository.ProfileDataRepository;
import com.jaspersoft.android.jaspermobile.data.validator.CredentialsValidatorImpl;
import com.jaspersoft.android.jaspermobile.data.validator.ProfileValidatorImpl;
import com.jaspersoft.android.jaspermobile.data.validator.ServerValidatorImpl;
import com.jaspersoft.android.jaspermobile.domain.AppCredentials;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.ProfileForm;
import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.repository.CredentialsRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.JasperServerRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.ProfileRepository;
import com.jaspersoft.android.jaspermobile.domain.validator.CredentialsValidator;
import com.jaspersoft.android.jaspermobile.domain.validator.ProfileValidator;
import com.jaspersoft.android.jaspermobile.domain.validator.ServerValidator;
import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;
import com.jaspersoft.android.jaspermobile.internal.di.modules.CredentialsModule;
import com.jaspersoft.android.jaspermobile.internal.di.modules.JasperServerModule;
import com.jaspersoft.android.jaspermobile.internal.di.modules.ProfileModule;

import rx.Observable;
import rx.functions.Func1;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerActivity
public class SaveProfileUseCase extends AbstractUseCase<Profile, ProfileForm> {
    /**
     * Injected by {@link ProfileModule#providesProfileRepository(ProfileDataRepository)}
     */
    private final ProfileRepository mProfileRepository;
    /**
     * Injected by {@link JasperServerModule#providesServerValidator(ServerValidatorImpl)}
     */
    private final JasperServerRepository mJasperServerRepository;
    /**
     * Injected by {@link CredentialsModule#providesCredentialsValidator(CredentialsValidatorImpl)} ()}
     */
    private final CredentialsRepository mCredentialsDataRepository;

    /**
     * Injected by {@link ProfileModule#provideProfileValidator(ProfileValidatorImpl)}
     */
    private final ProfileValidator mProfileValidator;

    /**
     * Injected by {@link JasperServerModule#providesServerValidator(ServerValidatorImpl)}}
     */
    private final ServerValidator mServerValidator;

    /**
     * Injected by {@link CredentialsModule#providesCredentialsValidator(CredentialsValidatorImpl)}
     */
    private final CredentialsValidator mCredentialsValidator;

    @Inject
    public SaveProfileUseCase(
            PreExecutionThread preExecutionThread,
            PostExecutionThread postExecutionThread,
            ProfileRepository profileRepository,
            JasperServerRepository jasperServerRepository,
            CredentialsRepository credentialsDataRepository,
            ProfileValidator profileValidator,
            ServerValidator serverValidator,
            CredentialsValidator credentialsValidator) {
        super(preExecutionThread, postExecutionThread);
        mProfileRepository = profileRepository;
        mJasperServerRepository = jasperServerRepository;
        mCredentialsDataRepository = credentialsDataRepository;
        mProfileValidator = profileValidator;
        mServerValidator = serverValidator;
        mCredentialsValidator = credentialsValidator;
    }

    @Override
    protected Observable<Profile> buildUseCaseObservable(final ProfileForm form) {
        Profile profile = form.getProfile();
        final String serverUrl = form.getServerUrl();
        AppCredentials credentials = form.getCredentials();

        Observable<Profile> validateProfile = mProfileValidator.validate(profile);
        final Observable<JasperServer> validateServer = mServerValidator.validate(serverUrl);
        final Observable<AppCredentials> validateCredentials = mCredentialsValidator.validate(credentials);

        Observable<Profile> saveProfileAction = mProfileRepository.saveProfile(profile);
        Observable<Profile> saveServerAction = mJasperServerRepository.saveServer(profile, serverUrl);
        Observable<Profile> saveCredentialsAction = mCredentialsDataRepository.saveCredentials(profile, credentials);
        Observable<Profile> activateProfileAction = mProfileRepository.activate(profile);

        final Observable<Profile> saveAction = saveProfileAction
                .concatWith(saveServerAction)
                .concatWith(saveCredentialsAction)
                .concatWith(activateProfileAction)
                .last();

        return validateProfile
                .flatMap(new Func1<Profile, Observable<JasperServer>>() {
                    @Override
                    public Observable<JasperServer> call(Profile profile) {
                        return validateServer;
                    }
                })
                .flatMap(new Func1<JasperServer, Observable<AppCredentials>>() {
                    @Override
                    public Observable<AppCredentials> call(JasperServer server) {
                        return validateCredentials;
                    }
                })
                .flatMap(new Func1<AppCredentials, Observable<Profile>>() {
                    @Override
                    public Observable<Profile> call(AppCredentials appCredentials) {
                        return saveAction;
                    }
                });
    }

}
