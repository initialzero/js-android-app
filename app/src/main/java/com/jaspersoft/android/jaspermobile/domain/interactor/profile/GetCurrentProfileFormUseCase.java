/*
 * Copyright © 2016 TIBCO Software,Inc.All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software:you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation,either version 3of the License,or
 * (at your option)any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android.If not,see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.domain.interactor.profile;

import com.jaspersoft.android.jaspermobile.domain.AppCredentials;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.ProfileForm;
import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.interactor.AbstractSimpleUseCase;
import com.jaspersoft.android.jaspermobile.domain.repository.profile.CredentialsRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.profile.ProfileRepository;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func0;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public class GetCurrentProfileFormUseCase extends AbstractSimpleUseCase<ProfileForm> {

    private final ProfileRepository mProfileRepository;
    private final CredentialsRepository mCredentialsRepository;

    @Inject
    public GetCurrentProfileFormUseCase(
            PreExecutionThread preExecutionThread,
            PostExecutionThread postExecutionThread,
            ProfileRepository profileRepository, CredentialsRepository credentialsRepository
    ) {
        super(preExecutionThread, postExecutionThread);
        mProfileRepository = profileRepository;
        mCredentialsRepository = credentialsRepository;
    }

    @Override
    protected Observable<ProfileForm> buildUseCaseObservable() {
        return Observable.defer(new Func0<Observable<ProfileForm>>() {
            @Override
            public Observable<ProfileForm> call() {
                Profile activeProfile = mProfileRepository.getActiveProfile();
                AppCredentials credentials = mCredentialsRepository.getCredentials(activeProfile);

                ProfileForm form = new ProfileForm.Builder()
                        .setAlias(activeProfile.getKey())
                        .setCredentials(credentials)
                        .build();
                return Observable.just(form);
            }
        });
    }
}
