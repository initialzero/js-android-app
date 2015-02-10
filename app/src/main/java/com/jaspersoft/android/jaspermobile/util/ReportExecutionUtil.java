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

package com.jaspersoft.android.jaspermobile.util;

import android.content.Context;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.legacy.JsServerProfileCompat;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.JsServerProfile;
import com.jaspersoft.android.sdk.client.oxm.report.ExecutionRequest;
import com.jaspersoft.android.sdk.client.oxm.server.ServerInfo;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;

@EBean
public class ReportExecutionUtil {
    private static final String ATTACHMENT_PREFIX_5_6 = "/reportExecutions/{reportExecutionId}/exports/{exportExecutionId}/attachments/";
    private static final String ATTACHMENT_PREFIX_5_0 = "/reportExecutions/{reportExecutionId}/exports/{exportOptions}/attachments/";

    @RootContext
    Context context;

    @Inject
    JsRestClient jsRestClient;

    @AfterInject
    void injectRoboGuiceDependencies() {
        final RoboInjector injector = RoboGuice.getInjector(context);
        injector.injectMembersWithoutViews(this);
    }

    public <T extends ExecutionRequest>  void setupAttachmentPrefix(T executionData, double versionCode) {
        String prefix = (versionCode == ServerInfo.VERSION_CODES.EMERALD_TWO) ? ATTACHMENT_PREFIX_5_0 : ATTACHMENT_PREFIX_5_6;
        JsServerProfileCompat.initLegacyJsRestClient(context, jsRestClient);
        JsServerProfile jsServerProfile = jsRestClient.getServerProfile();
        String attachmentsPrefix = (jsServerProfile.getServerUrl() + JsRestClient.REST_SERVICES_V2_URI + prefix);
        executionData.setAttachmentsPrefix(attachmentsPrefix);
    }

    public <T extends ExecutionRequest> void setupBaseUrl(T executionData) {
        JsServerProfileCompat.initLegacyJsRestClient(context, jsRestClient);
        JsServerProfile jsServerProfile = jsRestClient.getServerProfile();
        executionData.setBaseUrl(jsServerProfile.getServerUrl());
    }
}
