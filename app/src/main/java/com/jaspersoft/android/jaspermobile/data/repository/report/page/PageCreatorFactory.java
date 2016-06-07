/*
 * Copyright © 2016 TIBCO Software,Inc.All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software:you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation,either version 3of the License,or
 * (at your option)any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android.If not,see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.data.repository.report.page;

import android.content.Context;
import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.PageRequest;
import com.jaspersoft.android.jaspermobile.internal.di.ApplicationContext;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.sdk.service.report.ReportExecution;

import javax.inject.Inject;

/**
 * @author Tom Koptel
 * @since 2.5
 */
@PerProfile
public class PageCreatorFactory {
    private final Context context;
    private JasperServer server;

    @Inject
    public PageCreatorFactory(@ApplicationContext Context context, JasperServer server) {
        this.context = context;
        this.server = server;
    }

    @NonNull
    public PageCreator create(PageRequest pageRequest, ReportExecution reportExecution) {
        String format = pageRequest.getFormat();
        if ("html".equals(format.toLowerCase())) {
            return new HtmlPageCreator(context, pageRequest, reportExecution, server);
        }
        return new RawPageCreator(pageRequest, reportExecution);
    }
}
