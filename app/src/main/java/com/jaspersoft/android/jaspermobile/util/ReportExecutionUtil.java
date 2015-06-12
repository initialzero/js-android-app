/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
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

import android.accounts.Account;
import android.content.Context;

import com.jaspersoft.android.retrofit.sdk.account.AccountServerData;
import com.jaspersoft.android.retrofit.sdk.account.JasperAccountManager;
import com.jaspersoft.android.retrofit.sdk.server.ServerRelease;
import com.jaspersoft.android.retrofit.sdk.util.JasperSettings;
import com.jaspersoft.android.sdk.client.oxm.report.ExecutionRequest;
import com.jaspersoft.android.sdk.client.oxm.report.ExportExecution;
import com.jaspersoft.android.sdk.client.oxm.report.ReportExecutionRequest;

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

    private ServerRelease mServerRelease;
    private AccountServerData mServerData;

    @AfterInject
    void injectRoboGuiceDependencies() {
        final RoboInjector injector = RoboGuice.getInjector(context);
        injector.injectMembersWithoutViews(this);

        Account account = JasperAccountManager.get(context).getActiveAccount();
        mServerData = AccountServerData.get(context, account);
        mServerRelease = ServerRelease.parseVersion(mServerData.getVersionName());
    }

    public void setupInteractiveness(ReportExecutionRequest executionData) {
        double currentVersion = mServerRelease.code();
        boolean interactive = (currentVersion != ServerRelease.EMERALD_MR3.code());
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
        return mServerRelease == ServerRelease.EMERALD_MR2;
    }

}
