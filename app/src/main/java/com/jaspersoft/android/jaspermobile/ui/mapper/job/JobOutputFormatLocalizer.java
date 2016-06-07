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

package com.jaspersoft.android.jaspermobile.ui.mapper.job;

import android.content.Context;
import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.domain.entity.job.JobScheduleForm;
import com.jaspersoft.android.jaspermobile.internal.di.ApplicationContext;
import com.jaspersoft.android.jaspermobile.ui.mapper.EntityLocalizer;
import com.jaspersoft.android.sdk.service.data.schedule.JobOutputFormat;

/**
 * @author Tom Koptel
 * @since 2.5
 */
final class JobOutputFormatLocalizer implements EntityLocalizer<JobScheduleForm.OutputFormat> {

    private final Context context;

    public JobOutputFormatLocalizer(@ApplicationContext Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public String localize(@NonNull JobScheduleForm.OutputFormat jobOutputFormat) {
        switch (jobOutputFormat) {
            case XLS_NOPAG:
                return context.getString(R.string.file_format_xls);
            case XLSX_NOPAG:
                return JobOutputFormat.XLSX.name();
            case XLS:
                return context.getString(R.string.sch_format_paginated, context.getString(R.string.file_format_xls));
            case XLSX:
                return context.getString(R.string.sch_format_paginated, jobOutputFormat.name());
            default:
                return jobOutputFormat.name();
        }
    }
}
