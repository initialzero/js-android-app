/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
 *  http://community.jaspersoft.com/project/jaspermobile-android
 *
 *  Unless you have purchased a commercial license agreement from Jaspersoft,
 *  the following license terms apply:
 *
 *  This program is part of Jaspersoft Mobile for Android.
 *
 *  Jaspersoft Mobile is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Jaspersoft Mobile is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Jaspersoft Mobile for Android. If not, see
 *  <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.auth;

import android.content.Intent;
import android.os.IBinder;

import com.google.inject.Inject;
import com.jaspersoft.android.sdk.client.JsRestClient;

import roboguice.service.RoboService;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class JasperAuthenticatorService extends RoboService {
    private JasperAuthenticator mAuthenticator;

    @Inject
    JsRestClient jsRestClient;

    @Override
    public void onCreate() {
        super.onCreate();
        mAuthenticator = new JasperAuthenticator(this, jsRestClient);
    }

    public JasperAuthenticator getAuthenticator() {
        return mAuthenticator;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
