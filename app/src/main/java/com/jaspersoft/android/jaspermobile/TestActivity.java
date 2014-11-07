/*
 * Copyright © 2014 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of Jaspersoft Mobile for Android.
 *
 * Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile;

import android.widget.TextView;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceFragmentActivity;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.request.cacheable.GetServerInfoRequest;
import com.jaspersoft.android.sdk.client.oxm.server.ServerInfo;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;


@EActivity(R.layout.activity_test)
public class TestActivity extends RoboSpiceFragmentActivity {

    @ViewById(android.R.id.text1)
    TextView statusText;

    @Inject
    JsRestClient jsRestClient;

    @Click(android.R.id.button1)
    final void testHack() {
        getSpiceManager().execute(new GetServerInfoRequest(jsRestClient), new GetServerInfoRequestListener());
    }

    private class GetServerInfoRequestListener implements RequestListener<ServerInfo> {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            statusText.setText("Failure");
        }

        @Override
        public void onRequestSuccess(ServerInfo serverInfo) {
            statusText.setText("Success");
        }
    }

}
