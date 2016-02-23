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

package com.jaspersoft.android.jaspermobile.domain.interactor.profile;

import com.jaspersoft.android.jaspermobile.domain.AppCredentials;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.ProfileForm;
import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.interactor.AbstractUseCase;
import com.jaspersoft.android.jaspermobile.domain.repository.profile.CredentialsRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.profile.JasperServerRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.profile.ProfileRepository;
import com.jaspersoft.android.jaspermobile.domain.validator.ValidationRule;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.DuplicateProfileException;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.ProfileReservedException;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.ServerVersionNotSupportedException;
import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func0;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerActivity
public class SaveProfileUseCase extends AbstractUseCase<Profile, ProfileForm> {

    private final ProfileRepository mProfileRepository;
    private final JasperServerRepository mJasperServerRepository;
    private final CredentialsRepository mCredentialsDataRepository;

    private final ValidationRule<JasperServer, ServerVersionNotSupportedException> mServerVersionValidation;
    private final ValidationRule<Profile, DuplicateProfileException> mDuplicateProfileValidation;
    private final ValidationRule<Profile, ProfileReservedException> mReservedProfileValidation;
    private final ValidationRule<ProfileForm, Exception> mProfileAuthorizedValidation;

    @Inject
    public SaveProfileUseCase(
            PreExecutionThread preExecutionThread,
            PostExecutionThread postExecutionThread,

            ProfileRepository profileRepository,
            JasperServerRepository jasperServerRepository,
            CredentialsRepository credentialsDataRepository,

            ValidationRule<JasperServer, ServerVersionNotSupportedException> serverVersionValidation,
            ValidationRule<Profile, DuplicateProfileException> duplicateProfileValidation,
            ValidationRule<Profile, ProfileReservedException> reservedProfileValidation,
            ValidationRule<ProfileForm, Exception> profileAuthorizedValidation
    ) {
        super(preExecutionThread, postExecutionThread);

        mDuplicateProfileValidation = duplicateProfileValidation;
        mReservedProfileValidation = reservedProfileValidation;
        mProfileAuthorizedValidation = profileAuthorizedValidation;

        mProfileRepository = profileRepository;
        mJasperServerRepository = jasperServerRepository;
        mCredentialsDataRepository = credentialsDataRepository;
        mServerVersionValidation = serverVersionValidation;
    }

    @Override
    protected Observable<Profile> buildUseCaseObservable(final ProfileForm form) {
        return Observable.defer(new Func0<Observable<Profile>>() {
            @Override
            public Observable<Profile> call() {
                Profile profile = form.getProfile();
                String serverUrl = form.getServerUrl();
                AppCredentials credentials = form.getCredentials();

                try {
                    mReservedProfileValidation.validate(profile);
                    mDuplicateProfileValidation.validate(profile);

                    JasperServer server = mJasperServerRepository.loadServer(serverUrl);
                    mServerVersionValidation.validate(server);

                    mProfileAuthorizedValidation.validate(form);

                    mProfileRepository.saveProfile(profile);
                    mJasperServerRepository.saveServer(profile, server);
                    mCredentialsDataRepository.saveCredentials(profile, credentials);

                    return Observable.just(profile);
                } catch (Exception e) {
                    return Observable.error(e);
                }
            }
        });
    }
}
