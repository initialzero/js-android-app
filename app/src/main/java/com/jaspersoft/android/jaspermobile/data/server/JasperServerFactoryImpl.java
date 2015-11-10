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

package com.jaspersoft.android.jaspermobile.data.server;

import com.jaspersoft.android.jaspermobile.data.entity.mapper.ServerInfoDataMapper;
import com.jaspersoft.android.jaspermobile.domain.server.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.server.JasperServerFactory;
import com.jaspersoft.android.sdk.service.data.server.ServerInfo;
import com.jaspersoft.android.sdk.service.server.ServerInfoService;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class JasperServerFactoryImpl implements JasperServerFactory {
    private final String mBaseUrl;
    private final ServerInfoService mService;
    private final ServerInfoDataMapper mDataMapper;

    @Inject
    public JasperServerFactoryImpl(
            @Named("baseUrl") String baseUrl,
            ServerInfoService infoService,
            ServerInfoDataMapper dataMapper) {
        mBaseUrl = baseUrl;
        mService = infoService;
        mDataMapper = dataMapper;
    }

    @Override
    public JasperServer create() {
        ServerInfo info = mService.requestServerInfo();
        return mDataMapper.transform(mBaseUrl, info);
    }
}
