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

import com.jaspersoft.android.jaspermobile.domain.entity.job.JobScheduleBundle;
import com.jaspersoft.android.jaspermobile.domain.entity.job.JobScheduleForm;
import com.jaspersoft.android.jaspermobile.domain.entity.job.JobSimpleRecurrence;
import com.jaspersoft.android.jaspermobile.ui.entity.job.CalendarViewRecurrence;
import com.jaspersoft.android.jaspermobile.ui.entity.job.JobFormViewBundle;
import com.jaspersoft.android.jaspermobile.ui.entity.job.JobFormViewEntity;
import com.jaspersoft.android.jaspermobile.ui.entity.job.SimpleViewRecurrence;
import com.jaspersoft.android.jaspermobile.ui.mapper.UiCollectionEntityMapper;
import com.jaspersoft.android.jaspermobile.ui.mapper.UiEntityMapper;

/**
 * @author Tom Koptel
 * @since 2.5
 */
public final class JobUiFormBundleMapper implements UiEntityMapper<JobScheduleBundle, JobFormViewBundle> {

    @NonNull
    private final UiEntityMapper<JobScheduleForm, JobFormViewEntity> uiFormMapper;
    @NonNull
    private final UiCollectionEntityMapper<JobScheduleForm.OutputFormat, JobFormViewEntity.OutputFormat> outputFormatMapper;
    @NonNull
    private final UiCollectionEntityMapper<JobSimpleRecurrence.Unit, SimpleViewRecurrence.Unit> intervalUnitMapper;
    @NonNull
    private final UiCollectionEntityMapper<JobScheduleForm.Recurrence, JobFormViewEntity.Recurrence> recurrenceMapper;
    @NonNull
    private final UiCollectionEntityMapper<Integer, CalendarViewRecurrence.Month> monthMapper;
    @NonNull
    private final UiCollectionEntityMapper<Integer, CalendarViewRecurrence.Day> dayMapper;

    @VisibleForTesting
    JobUiFormBundleMapper(
            @NonNull UiEntityMapper<JobScheduleForm, JobFormViewEntity> uiFormMapper,
            @NonNull UiCollectionEntityMapper<JobScheduleForm.OutputFormat, JobFormViewEntity.OutputFormat> outputFormatMapper,
            @NonNull UiCollectionEntityMapper<JobSimpleRecurrence.Unit, SimpleViewRecurrence.Unit> intervalUnitMapper,
            @NonNull UiCollectionEntityMapper<JobScheduleForm.Recurrence, JobFormViewEntity.Recurrence> recurrenceMapper,
            @NonNull UiCollectionEntityMapper<Integer, CalendarViewRecurrence.Month> monthMapper,
            @NonNull UiCollectionEntityMapper<Integer, CalendarViewRecurrence.Day> dayMapper) {
        this.uiFormMapper = uiFormMapper;
        this.outputFormatMapper = outputFormatMapper;
        this.intervalUnitMapper = intervalUnitMapper;
        this.recurrenceMapper = recurrenceMapper;
        this.monthMapper = monthMapper;
        this.dayMapper = dayMapper;
    }

    @NonNull
    public static JobUiFormBundleMapper create(@NonNull Context context) {
        JobUiFormMapper formMapper = JobUiFormMapper.create(context);
        JobUiOutputFormatMapper outputFormatMapper = JobUiOutputFormatMapper.create(context);
        JobUiRecurrenceUnitMapper unitMapper = JobUiRecurrenceUnitMapper.create(context);
        JobUiRecurrenceMapper recurrenceMapper = JobUiRecurrenceMapper.create(context);
        JobUiCalendarMonthMapper monthMapper = JobUiCalendarMonthMapper.create();
        JobUiCalendarDayMapper dayMapper = JobUiCalendarDayMapper.create();

        return new JobUiFormBundleMapper(
                formMapper,
                outputFormatMapper,
                unitMapper,
                recurrenceMapper,
                monthMapper,
                dayMapper
        );
    }

    @NonNull
    @Override
    public JobFormViewBundle toUiEntity(@NonNull JobScheduleBundle domainEntity) {
        JobFormViewBundle.Builder builder = JobFormViewBundle.builder();
        builder.form(uiFormMapper.toUiEntity(domainEntity.form()));
        builder.allFormats(outputFormatMapper.toUiEntityList(domainEntity.allFormats()));
        builder.allIntervalUnits(intervalUnitMapper.toUiEntityList(domainEntity.allIntervalUnits()));
        builder.allRecurrences(recurrenceMapper.toUiEntityList(domainEntity.allRecurrences()));
        builder.allMonths(monthMapper.toUiEntityList(domainEntity.allMonths()));
        builder.allDays(dayMapper.toUiEntityList(domainEntity.allDays()));
        return builder.build();
    }

    @NonNull
    @Override
    public JobScheduleBundle toDomainEntity(@NonNull JobFormViewBundle uiEntity) {
        JobScheduleBundle.Builder builder = JobScheduleBundle.builder();
        builder.form(uiFormMapper.toDomainEntity(uiEntity.form()));
        builder.allFormats(outputFormatMapper.toDomainEntityList(uiEntity.allFormats()));
        builder.allIntervalUnits(intervalUnitMapper.toDomainEntityList(uiEntity.allIntervalUnits()));
        builder.allRecurrences(recurrenceMapper.toDomainEntityList(uiEntity.allRecurrences()));
        builder.allMonths(monthMapper.toDomainEntityList(uiEntity.allMonths()));
        builder.allDays(dayMapper.toDomainEntityList(uiEntity.allDays()));
        return builder.build();
    }
}
