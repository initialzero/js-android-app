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
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.repository.CredentialsRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.ProfileRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.exception.FailedToSaveCredentials;
import com.jaspersoft.android.jaspermobile.domain.repository.exception.FailedToSaveProfile;
import com.jaspersoft.android.jaspermobile.domain.server.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.server.JasperServerFactory;
import com.jaspersoft.android.jaspermobile.domain.validator.CredentialsValidator;
import com.jaspersoft.android.jaspermobile.domain.validator.JasperServerValidator;
import com.jaspersoft.android.jaspermobile.domain.validator.ProfileValidator;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.DuplicateProfileException;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.InvalidCredentialsException;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.ServerVersionNotSupportedException;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func0;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public final class SaveProfile extends UseCase {
    private final CredentialsValidator mCredentialsValidator;
    private final JasperServerValidator mServerValidator;
    private final ProfileValidator mProfileValidator;
    private final JasperServerFactory mServerFactory;
    private final ProfileRepository mProfileRepository;
    private final CredentialsRepository mCredentialsRepository;

    @Inject
    public SaveProfile(
            JasperServerFactory jasperServerFactory,
            CredentialsValidator credentialsValidator,
            JasperServerValidator serverValidator,
            ProfileValidator profileValidator,
            ProfileRepository profileRepository,
            CredentialsRepository credentialsRepository,
            PreExecutionThread threadExecutor,
            PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        mServerFactory = jasperServerFactory;
        mCredentialsValidator = credentialsValidator;
        mServerValidator = serverValidator;
        mProfileValidator = profileValidator;
        mProfileRepository = profileRepository;
        mCredentialsRepository = credentialsRepository;
    }

    @Override
    protected Observable buildUseCaseObservable() {
        return Observable.defer(new Func0<Observable<Profile>>() {
            @Override
            public rx.Observable<Profile> call() {
                try {
                    return Observable.just(performAddition());
                } catch (InvalidCredentialsException |
                        ServerVersionNotSupportedException |
                        DuplicateProfileException |
                        FailedToSaveProfile |
                        FailedToSaveCredentials e) {
                    return Observable.error(e);
                }
            }
        });
    }

    public Profile performAddition()
            throws InvalidCredentialsException,
            ServerVersionNotSupportedException,
            DuplicateProfileException,
            FailedToSaveProfile,
            FailedToSaveCredentials {
        Profile profile = mProfileValidator.validate();
        JasperServer server = mServerFactory.create();
        mServerValidator.validate(server);
        BaseCredentials credentials = mCredentialsValidator.validate();

        boolean isProfileSaved = mProfileRepository.saveProfile(profile);
        if (!isProfileSaved) {
            throw new FailedToSaveProfile(profile);
        }
        boolean isCredentialsSaved = mCredentialsRepository.saveCredentials(profile, credentials);
        if (!isCredentialsSaved) {
            throw new FailedToSaveCredentials(credentials);
        }

        mProfileRepository.activate(profile);
        return profile;
    }
}
