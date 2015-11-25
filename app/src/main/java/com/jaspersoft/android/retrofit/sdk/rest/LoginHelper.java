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

package com.jaspersoft.android.retrofit.sdk.rest;

import com.jaspersoft.android.jaspermobile.network.RestClient;
import com.jaspersoft.android.sdk.service.auth.Credentials;
import com.jaspersoft.android.sdk.service.data.server.ServerInfo;
import com.jaspersoft.android.sdk.service.exception.ServiceException;

import rx.Observable;
import rx.functions.Func0;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class LoginHelper {
    public static Observable<LoginResponse> loginAsObservable(final RestClient restFactory, final Credentials credentials) {
        return Observable.defer(new Func0<Observable<LoginResponse>>() {
            @Override
            public Observable<LoginResponse> call() {
                try {
                    return Observable.just(
                            login(restFactory, credentials)
                    );
                } catch (ServiceException e) {
                    return Observable.error(e);
                }
            }
        });
    }

    public static LoginResponse login(RestClient restFactory, Credentials credentials) throws ServiceException {
        String token = restFactory.authApi().authenticate(credentials);
        ServerInfo serverInfo = restFactory.infoApi().requestServerInfo();
        return new LoginResponse(token, serverInfo);
    }
}
