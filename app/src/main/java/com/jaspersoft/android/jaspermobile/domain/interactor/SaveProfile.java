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

import com.jaspersoft.android.jaspermobile.domain.BaseCredentials;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.network.RestStatusException;
import com.jaspersoft.android.jaspermobile.domain.network.ServerApi;
import com.jaspersoft.android.jaspermobile.domain.repository.CredentialsRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.JasperServerRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.ProfileRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.exception.FailedToSaveCredentials;
import com.jaspersoft.android.jaspermobile.domain.repository.exception.FailedToSaveProfile;
import com.jaspersoft.android.jaspermobile.domain.validator.CredentialsValidator;
import com.jaspersoft.android.jaspermobile.domain.validator.ProfileValidator;
import com.jaspersoft.android.jaspermobile.domain.validator.ServerValidator;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.DuplicateProfileException;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.InvalidCredentialsException;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.ProfileReservedException;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.ServerVersionNotSupportedException;
import com.jaspersoft.android.jaspermobile.util.security.PasswordManager;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func0;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class SaveProfile {
    private final ServerApi.Factory mServerFactory;
    private final CredentialsValidator mCredentialsValidator;
    private final ServerValidator mServerValidator;
    private final ProfileValidator mProfileValidator;
    private final ProfileRepository mProfileRepository;
    private final CredentialsRepository mCredentialsRepository;
    private final JasperServerRepository mServerRepository;

    private final CompositeUseCase mCompositeUseCase;

    @Inject
    public SaveProfile(
            ServerApi.Factory jasperServerFactory,
            CredentialsValidator credentialsValidator,
            ServerValidator serverValidator,
            ProfileValidator profileValidator,
            ProfileRepository profileRepository,
            CredentialsRepository credentialsRepository,
            JasperServerRepository serverRepository,
            CompositeUseCase compositeUseCase) {
        mServerFactory = jasperServerFactory;
        mCredentialsValidator = credentialsValidator;
        mServerValidator = serverValidator;
        mProfileValidator = profileValidator;
        mProfileRepository = profileRepository;
        mCredentialsRepository = credentialsRepository;
        mServerRepository = serverRepository;
        mCompositeUseCase = compositeUseCase;
    }

    public void execute(String baseUrl, Profile profile, BaseCredentials credentials, rx.Subscriber useCaseSubscriber) {
        rx.Observable observable = buildUseCaseObservable(baseUrl, profile, credentials);
        mCompositeUseCase.execute(observable, useCaseSubscriber);
    }

    private Observable buildUseCaseObservable(final String baseUrl,
                                              final Profile profile,
                                              final BaseCredentials credentials) {
        return Observable.defer(new Func0<Observable<Profile>>() {
            @Override
            public rx.Observable<Profile> call() {
                try {
                    return Observable.just(performAddition(baseUrl, profile, credentials));
                } catch (Exception e) {
                    return Observable.error(e);
                }
            }
        });
    }

    public void unsubscribe() {
        mCompositeUseCase.unsubscribe();
    }

    private Profile performAddition(String baseUrl, Profile profile,
                                    BaseCredentials credentials) throws Exception {
        validateProfile(profile);
        JasperServer server = mServerFactory.create(baseUrl).requestServer();
        validateServer(server);
        validateCredentials(server, credentials);

        saveProfile(profile);
        saveCredentials(profile, credentials);
        saveServer(profile, server);

        activateProfile(profile);

        return profile;
    }

    private void validateProfile(Profile profile) throws DuplicateProfileException, ProfileReservedException {
        mProfileValidator.validate(profile);
    }

    private void validateServer(JasperServer server) throws ServerVersionNotSupportedException {
        mServerValidator.validate(server);
    }

    private void validateCredentials(JasperServer server, BaseCredentials credentials)
            throws RestStatusException, InvalidCredentialsException {
        mCredentialsValidator.validate(server, credentials);
    }

    private void saveProfile(Profile profile) throws FailedToSaveProfile {
        boolean isProfileSaved = mProfileRepository.saveProfile(profile);
        if (!isProfileSaved) {
            throw new FailedToSaveProfile(profile);
        }
    }

    private void saveCredentials(Profile profile, BaseCredentials credentials) throws FailedToSaveCredentials {
        try {
            mCredentialsRepository.saveCredentials(profile, credentials);
        } catch (PasswordManager.EncryptionException encryptionException) {
            throw new FailedToSaveCredentials(credentials);
        }
    }

    private void saveServer(Profile profile, JasperServer server) {
        mServerRepository.saveServer(profile, server);
    }

    private void activateProfile(Profile profile) {
        mProfileRepository.activate(profile);
    }
}
