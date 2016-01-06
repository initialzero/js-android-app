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

package com.jaspersoft.android.jaspermobile.data.validator;

import com.jaspersoft.android.jaspermobile.data.entity.mapper.CredentialsMapper;
import com.jaspersoft.android.jaspermobile.domain.AppCredentials;
import com.jaspersoft.android.jaspermobile.domain.validator.CredentialsValidator;
import com.jaspersoft.android.sdk.network.Credentials;
import com.jaspersoft.android.sdk.service.rx.auth.RxAuthorizationService;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func1;

/**
 * Perform network call and authorize user. If passed than user fine to go
 *
 * @author Tom Koptel
 * @since 2.3
 */
public final class CredentialsValidatorImpl implements CredentialsValidator {

    private final RxAuthorizationService mAuthorizationService;
    private final CredentialsMapper mCredentialsMapper;

    @Inject
    public CredentialsValidatorImpl(RxAuthorizationService authorizationService, CredentialsMapper credentialsMapper) {
        mAuthorizationService = authorizationService;
        mCredentialsMapper = credentialsMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<AppCredentials> validate(final AppCredentials credentials) {
        Credentials spring = mCredentialsMapper.toNetworkModel(credentials);
        return mAuthorizationService.authorize(spring)
                .flatMap(new Func1<Credentials, Observable<AppCredentials>>() {
                    @Override
                    public Observable<AppCredentials> call(Credentials aCredentials) {
                        return Observable.just(credentials);
                    }
                });
    }
}
