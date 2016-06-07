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
import android.support.annotation.VisibleForTesting;

import com.jaspersoft.android.jaspermobile.domain.entity.job.JobScheduleForm;
import com.jaspersoft.android.jaspermobile.ui.entity.job.JobFormViewEntity;
import com.jaspersoft.android.jaspermobile.ui.mapper.EntityLocalizer;
import com.jaspersoft.android.jaspermobile.ui.mapper.UiCollectionEntityMapper;

/**
 * @author Tom Koptel
 * @since 2.5
 */
final class JobUiOutputFormatMapper extends UiCollectionEntityMapper<JobScheduleForm.OutputFormat, JobFormViewEntity.OutputFormat> {

    private final EntityLocalizer<JobScheduleForm.OutputFormat> entityLocalizer;

    @VisibleForTesting
    JobUiOutputFormatMapper(EntityLocalizer<JobScheduleForm.OutputFormat> entityLocalizer) {
        this.entityLocalizer = entityLocalizer;
    }

    @NonNull
    public static JobUiOutputFormatMapper create(@NonNull Context context) {
        JobOutputFormatLocalizer entityLocalizer = new JobOutputFormatLocalizer(context);
        return new JobUiOutputFormatMapper(entityLocalizer);
    }

    @NonNull
    @Override
    public JobFormViewEntity.OutputFormat toUiEntity(@NonNull JobScheduleForm.OutputFormat domainEntity) {
        String localizedLabel = entityLocalizer.localize(domainEntity);
        return JobFormViewEntity.OutputFormat.create(domainEntity.name(), localizedLabel);
    }

    @NonNull
    @Override
    public JobScheduleForm.OutputFormat toDomainEntity(@NonNull JobFormViewEntity.OutputFormat uiEntity) {
        String rawValue = uiEntity.rawValue();
        return JobScheduleForm.OutputFormat.valueOf(rawValue);
    }
}
