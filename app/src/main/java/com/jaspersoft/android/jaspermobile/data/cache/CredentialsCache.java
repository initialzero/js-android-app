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

package com.jaspersoft.android.jaspermobile.data.cache;

import com.jaspersoft.android.jaspermobile.domain.AppCredentials;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.util.security.PasswordManager;

/**
 * Abstraction around caching of {@link AppCredentials} instance.
 * Following interface implemented by {@link AccountCredentialsCache}
 *
 * @author Tom Koptel
 * @since 2.3
 */
public interface CredentialsCache {
    /**
     * Saves credentials in cache, presumably encoding sensitive data(e.g. password)
     *
     * @param profile the target profile we use to associate with credentials
     * @param credentials we are going to put inside repository
     * @throws PasswordManager.EncryptionException thrown if unexpected encoding exception raised
     */
    void put(Profile profile, AppCredentials credentials) throws PasswordManager.EncryptionException;

    /**
     * Retrieves credentials from cache, presumably decoding sensitive data(e.g. password)
     *
     * @param profile the target profile we use to associate with credentials
     * @throws PasswordManager.DecryptionException thrown if unexpected decoding exception raised
     */
    AppCredentials get(Profile profile) throws PasswordManager.DecryptionException;
}
