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

import android.accounts.Account;
import android.content.Context;

import com.jaspersoft.android.jaspermobile.util.account.AccountServerData;
import com.jaspersoft.android.jaspermobile.util.account.JasperAccountManager;
import com.jaspersoft.android.sdk.client.oxm.report.ExecutionRequest;
import com.jaspersoft.android.sdk.client.oxm.report.ExportExecution;
import com.jaspersoft.android.sdk.client.oxm.report.ReportExecutionRequest;
import com.jaspersoft.android.sdk.service.data.server.ServerVersion;

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

    private ServerVersion mServerVersion;
    private AccountServerData mServerData;

    @AfterInject
    void injectRoboGuiceDependencies() {
        final RoboInjector injector = RoboGuice.getInjector(context);
        injector.injectMembersWithoutViews(this);

        Account account = JasperAccountManager.get(context).getActiveAccount();
        mServerData = AccountServerData.get(context, account);
        mServerVersion = ServerVersion.defaultParser().parse(mServerData.getVersionName());
    }

    public void setupInteractiveness(ReportExecutionRequest executionData) {
        double currentVersion = mServerVersion.getVersionCode();
        boolean interactive = (currentVersion != ServerVersion.EMERALD_MR3.getVersionCode());
        executionData.setInteractive(interactive);
    }

    public <T extends ExecutionRequest> void setupAttachmentPrefix(T executionData) {
        String prefix = isEmeraldMr2() ? ATTACHMENT_PREFIX_5_0 : ATTACHMENT_PREFIX_5_6;
        String attachmentsPrefix = (mServerData.getServerUrl() + JasperSettings.DEFAULT_REST_VERSION + prefix);
        executionData.setAttachmentsPrefix(attachmentsPrefix);
    }

    public <T extends ExecutionRequest> void setupBaseUrl(T executionData) {
        executionData.setBaseUrl(mServerData.getServerUrl());
    }

    public String createExecutionId(ExportExecution response, String pages) {
        String executionId = response.getId();
        if (isEmeraldMr2()) {
            executionId = ("html;pages=" + pages);
        }
        return executionId;
    }

    private boolean isEmeraldMr2() {
        return mServerVersion == ServerVersion.EMERALD_MR2;
    }

}
