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

import com.jaspersoft.android.jaspermobile.domain.entity.job.JobCalendarRecurrence;
import com.jaspersoft.android.jaspermobile.domain.entity.job.JobNoneRecurrence;
import com.jaspersoft.android.jaspermobile.domain.entity.job.JobScheduleForm;
import com.jaspersoft.android.jaspermobile.domain.entity.job.JobSimpleRecurrence;
import com.jaspersoft.android.jaspermobile.ui.entity.job.CalendarViewRecurrence;
import com.jaspersoft.android.jaspermobile.ui.entity.job.JobFormViewEntity;
import com.jaspersoft.android.jaspermobile.ui.entity.job.NoneViewRecurrence;
import com.jaspersoft.android.jaspermobile.ui.entity.job.SimpleViewRecurrence;
import com.jaspersoft.android.jaspermobile.ui.mapper.UiCollectionEntityMapper;
import com.jaspersoft.android.jaspermobile.ui.mapper.UiEntityMapper;

/**
 * @author Tom Koptel
 * @since 2.5
 */
final class JobUiRecurrenceMapper extends UiCollectionEntityMapper<JobScheduleForm.Recurrence, JobFormViewEntity.Recurrence> {

    @NonNull
    private final UiEntityMapper<JobScheduleForm.Recurrence, JobFormViewEntity.Recurrence> noneMapper;
    @NonNull
    private final UiEntityMapper<JobScheduleForm.Recurrence, JobFormViewEntity.Recurrence> simpleMapper;
    @NonNull
    private final UiEntityMapper<JobScheduleForm.Recurrence, JobFormViewEntity.Recurrence> calendarMapper;

    JobUiRecurrenceMapper(
            @NonNull UiEntityMapper<JobScheduleForm.Recurrence, JobFormViewEntity.Recurrence> noneMapper,
            @NonNull UiEntityMapper<JobScheduleForm.Recurrence, JobFormViewEntity.Recurrence> simpleMapper,
            @NonNull UiEntityMapper<JobScheduleForm.Recurrence, JobFormViewEntity.Recurrence> calendarMapper
    ) {
        this.noneMapper = noneMapper;
        this.simpleMapper = simpleMapper;
        this.calendarMapper = calendarMapper;
    }

    @NonNull
    public static JobUiRecurrenceMapper create(@NonNull Context context) {
        JobUiNoneRecurrenceMapper noneMapper = JobUiNoneRecurrenceMapper.create(context);
        JobUiSimpleRecurrenceMapper simpleMapper = JobUiSimpleRecurrenceMapper.create(context);
        JobUiCalendarRecurrenceMapper calendarMapper = JobUiCalendarRecurrenceMapper.create(context);

        return new JobUiRecurrenceMapper(noneMapper, simpleMapper, calendarMapper);
    }

    @NonNull
    @Override
    public JobFormViewEntity.Recurrence toUiEntity(@NonNull JobScheduleForm.Recurrence recurrence) {
        if (recurrence instanceof JobSimpleRecurrence) {
            return simpleMapper.toUiEntity(recurrence);
        } else if (recurrence instanceof JobNoneRecurrence) {
            return noneMapper.toUiEntity(recurrence);
        } else if (recurrence instanceof JobCalendarRecurrence) {
            return calendarMapper.toUiEntity(recurrence);
        }
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @NonNull
    @Override
    public JobScheduleForm.Recurrence toDomainEntity(@NonNull JobFormViewEntity.Recurrence recurrence) {
        if (recurrence instanceof SimpleViewRecurrence) {
            return simpleMapper.toDomainEntity(recurrence);
        } else if (recurrence instanceof NoneViewRecurrence) {
            return noneMapper.toDomainEntity(recurrence);
        } else if (recurrence instanceof CalendarViewRecurrence) {
            return calendarMapper.toDomainEntity(recurrence);
        }
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
