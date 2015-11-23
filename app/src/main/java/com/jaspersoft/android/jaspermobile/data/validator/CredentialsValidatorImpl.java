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

import com.jaspersoft.android.jaspermobile.domain.BaseCredentials;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.network.Authenticator;
import com.jaspersoft.android.jaspermobile.domain.network.RestStatusException;
import com.jaspersoft.android.jaspermobile.domain.validator.CredentialsValidator;
import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;

import javax.inject.Inject;

/**
 * Perform network call and authorize user. If passed than user fine to go
 *
 * @author Tom Koptel
 * @since 2.3
 */
@PerActivity
public final class CredentialsValidatorImpl implements CredentialsValidator {
    private final Authenticator.Factory mAuthFactory;

    @Inject
    public CredentialsValidatorImpl(Authenticator.Factory authFactory) {
        mAuthFactory = authFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(JasperServer server, BaseCredentials credentials) throws RestStatusException {
        mAuthFactory.create(server.getBaseUrl()).authenticate(credentials);
    }
}
