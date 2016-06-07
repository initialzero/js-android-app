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
import com.jaspersoft.android.jaspermobile.domain.entity.job.JobSimpleRecurrence;
import com.jaspersoft.android.jaspermobile.ui.entity.job.JobFormViewEntity;
import com.jaspersoft.android.jaspermobile.ui.entity.job.SimpleViewRecurrence;
import com.jaspersoft.android.jaspermobile.ui.mapper.EntityLocalizer;
import com.jaspersoft.android.jaspermobile.ui.mapper.UiEntityMapper;


/**
 * @author Tom Koptel
 * @since 2.5
 */
final class JobUiSimpleRecurrenceMapper implements UiEntityMapper<JobScheduleForm.Recurrence, JobFormViewEntity.Recurrence> {
    private final UiEntityMapper<JobSimpleRecurrence.Unit, SimpleViewRecurrence.Unit> unitMapper;
    private final EntityLocalizer<JobScheduleForm.Recurrence> localizer;

    @VisibleForTesting
    JobUiSimpleRecurrenceMapper(
            @NonNull UiEntityMapper<JobSimpleRecurrence.Unit, SimpleViewRecurrence.Unit> unitMapper,
            @NonNull EntityLocalizer<JobScheduleForm.Recurrence> localizer
    ) {
        this.unitMapper = unitMapper;
        this.localizer = localizer;
    }

    @NonNull
    public static JobUiSimpleRecurrenceMapper create(@NonNull Context context) {
        JobUiRecurrenceUnitMapper unitMapper = JobUiRecurrenceUnitMapper.create(context);
        RecurrenceEntityLocalizer localizer = new RecurrenceEntityLocalizer(context);
        return new JobUiSimpleRecurrenceMapper(unitMapper, localizer);
    }

    @NonNull
    @Override
    public JobFormViewEntity.Recurrence toUiEntity(@NonNull JobScheduleForm.Recurrence recurrence) {
        JobSimpleRecurrence domainEntity = (JobSimpleRecurrence) recurrence;
        SimpleViewRecurrence.Builder builder = SimpleViewRecurrence.builder();

        builder.interval(domainEntity.interval());
        builder.occurrence(domainEntity.occurrence());
        builder.untilDate(domainEntity.untilDate());

        SimpleViewRecurrence.Unit unit = unitMapper.toUiEntity(domainEntity.unit());
        builder.unit(unit);

        String localizedLabel = localizer.localize(recurrence);
        builder.localizedLabel(localizedLabel);

        return builder.build();
    }

    @NonNull
    @Override
    public JobScheduleForm.Recurrence toDomainEntity(@NonNull JobFormViewEntity.Recurrence recurrence) {
        SimpleViewRecurrence uiEntity = (SimpleViewRecurrence) recurrence;
        JobSimpleRecurrence.Builder builder = JobSimpleRecurrence.builder();

        builder.interval(uiEntity.interval());
        builder.occurrence(uiEntity.occurrence());
        builder.untilDate(uiEntity.untilDate());

        JobSimpleRecurrence.Unit unit = unitMapper.toDomainEntity(uiEntity.unit());
        builder.unit(unit);

        return builder.build();
    }
}
