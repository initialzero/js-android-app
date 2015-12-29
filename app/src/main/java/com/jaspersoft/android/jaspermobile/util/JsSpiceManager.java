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

package com.jaspersoft.android.jaspermobile.util;

import android.content.Context;
import android.text.TextUtils;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.legacy.JsServerProfileCompat;
import com.jaspersoft.android.jaspermobile.network.XmlSpiceService;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.JsServerProfile;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;

import timber.log.Timber;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class JsSpiceManager extends SpiceManager {
    private static final String LOG_TAG = JsSpiceManager.class.getSimpleName();

    @Inject
    JsRestClient jsRestClient;
    @Inject
    Context mContext;

    @Inject
    public JsSpiceManager() {
        super(XmlSpiceService.class);
    }

    @Override
    public <T> void execute(final CachedSpiceRequest<T> cachedSpiceRequest, final RequestListener<T> requestListener) {
        tryToSetupRestClient();
        if (isRestClientValid()) {
            super.execute(cachedSpiceRequest, requestListener);
        }
    }

    private boolean isRestClientValid() {
        if (jsRestClient != null) {
            JsServerProfile jsServerProfile = jsRestClient.getServerProfile();
            if (jsServerProfile == null) {
                Timber.w(LOG_TAG, "Server profile is null ignoring request");
                return false;
            } else {
                if (TextUtils.isEmpty(jsServerProfile.getServerUrl())) {
                    Timber.w(LOG_TAG, "Server url is null ignoring request");
                    return false;
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    private void tryToSetupRestClient() {
        if (!isRestClientValid()) {
            JsServerProfileCompat.initLegacyJsRestClient(mContext, jsRestClient);
        }
    }
}
