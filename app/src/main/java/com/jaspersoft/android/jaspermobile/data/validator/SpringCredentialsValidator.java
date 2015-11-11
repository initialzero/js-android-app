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

import com.jaspersoft.android.jaspermobile.domain.network.Authenticator;
import com.jaspersoft.android.jaspermobile.domain.BaseCredentials;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.validator.CredentialsValidationFactory;
import com.jaspersoft.android.jaspermobile.domain.validator.Validation;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.InvalidCredentialsException;
import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;
import com.jaspersoft.android.sdk.network.RestError;

import javax.inject.Inject;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerActivity
public final class SpringCredentialsValidator implements CredentialsValidationFactory {
    private final Authenticator.Factory mAuthFactory;

    @Inject
    public SpringCredentialsValidator(Authenticator.Factory authFactory) {
        mAuthFactory = authFactory;
    }

    @Override
    public Validation<InvalidCredentialsException> create(final JasperServer server, final BaseCredentials credentials) {
        return new Validation<InvalidCredentialsException>() {
            @Override
            public InvalidCredentialsException getCheckedException() {
                return new InvalidCredentialsException(credentials);
            }

            @Override
            public boolean perform() {
                try {
                    mAuthFactory.create(server.getBaseUrl()).authenticate(credentials);
                } catch (RestError restError) {
                    if (restError.code() == 401) {
                        return false;
                    } else {
                        throw restError;
                    }
                }
                return true;
            }
        };
    }
}
