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

package com.jaspersoft.android.jaspermobile.presentation.model.validation;

import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;
import com.jaspersoft.android.jaspermobile.presentation.model.ProfileModel;
import com.jaspersoft.android.jaspermobile.presentation.model.validation.exception.AliasMissingException;
import com.jaspersoft.android.jaspermobile.presentation.model.validation.exception.ServerUrlFormatException;
import com.jaspersoft.android.jaspermobile.presentation.model.validation.exception.ServerUrlMissingException;

import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Inject;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerActivity
public class ProfileClientValidation {
    @Inject
    public ProfileClientValidation() {
    }

    public void validate(ProfileModel profileModel) throws AliasMissingException, ServerUrlMissingException, ServerUrlFormatException {
        String alias = profileModel.getAlias();
        if (alias == null || alias.trim().length() == 0) {
            throw new AliasMissingException();
        }
        String serverUrl = profileModel.getServerUrl();
        if (serverUrl == null || serverUrl.trim().length() == 0) {
            throw new ServerUrlMissingException();
        }

        try {
            new URL(serverUrl);
        } catch (MalformedURLException e) {
            throw new ServerUrlFormatException();
        }
//        if (!URLUtil.isNetworkUrl(serverUrl)) {
//            throw new ServerUrlFormatException();
//        }
    }
}
