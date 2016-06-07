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

import com.jaspersoft.android.jaspermobile.data.validator.ProfileAuthorizedValidation;
import com.jaspersoft.android.jaspermobile.domain.AppCredentials;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.ProfileForm;
import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.interactor.AbstractSimpleUseCase;
import com.jaspersoft.android.jaspermobile.domain.repository.profile.CredentialsRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.profile.JasperServerRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.profile.ProfileRepository;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.sdk.network.AuthenticationLifecycle;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func0;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public class AuthorizeSessionUseCase extends AbstractSimpleUseCase<Void> {

    private final ProfileRepository profileRepository;
    private final JasperServerRepository serverRepository;
    private final CredentialsRepository credentialsRepository;
    private final AuthenticationLifecycle authenticationLifecycle;

    // TODO revise authorization approach. Remove there should be no data package reference in domain
    private final ProfileAuthorizedValidation profileAuthorizedValidation;

    @Inject
    public AuthorizeSessionUseCase(
            PreExecutionThread preExecutionThread,
            PostExecutionThread postExecutionThread,
            ProfileRepository profileRepository,
            JasperServerRepository serverRepository,
            CredentialsRepository credentialsRepository,
            AuthenticationLifecycle authenticationLifecycle,
            ProfileAuthorizedValidation profileAuthorizedValidation
    ) {
        super(preExecutionThread, postExecutionThread);
        this.profileRepository = profileRepository;
        this.serverRepository = serverRepository;
        this.credentialsRepository = credentialsRepository;
        this.authenticationLifecycle = authenticationLifecycle;
        this.profileAuthorizedValidation = profileAuthorizedValidation;
    }

    @Override
    protected Observable<Void> buildUseCaseObservable() {
        return Observable.defer(new Func0<Observable<Void>>() {
            @Override
            public Observable<Void> call() {
                Profile profile = profileRepository.getActiveProfile();
                JasperServer server = serverRepository.getServer(profile);
                AppCredentials credentials = credentialsRepository.getCredentials(profile);

                ProfileForm profileForm = new ProfileForm.Builder()
                        .setBaseUrl(server.getBaseUrl())
                        .setAlias(profile.getKey())
                        .setCredentials(credentials)
                        .build();
                try {
                    authenticationLifecycle.beforeSessionReload();
                    profileAuthorizedValidation.validate(profileForm);
                    authenticationLifecycle.afterSessionReload();
                    return Observable.just(null);
                } catch (Exception e) {
                    return Observable.error(e);
                }
            }
        });
    }
}
