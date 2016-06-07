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

package com.jaspersoft.android.jaspermobile.data.mapper.job;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.jaspersoft.android.jaspermobile.domain.entity.job.JobScheduleBundle;
import com.jaspersoft.android.jaspermobile.domain.entity.job.JobScheduleForm;
import com.jaspersoft.android.jaspermobile.domain.entity.job.JobSimpleRecurrence;

import java.util.List;

/**
 * @author Tom Koptel
 * @since 2.5
 */
public class JobDataFormBundleWrapper {
    @NonNull
    private final JobScheduleRecurrenceFactory recurrenceFactory;
    @NonNull
    private final JobScheduleIntervalUnitFactory intervalUnitFactory;
    @NonNull
    private final JobScheduleFormatsFactory scheduleFormatsFactory;

    @VisibleForTesting
    JobDataFormBundleWrapper(
            @NonNull JobScheduleRecurrenceFactory recurrenceFactory,
            @NonNull JobScheduleIntervalUnitFactory intervalUnitFactory,
            @NonNull JobScheduleFormatsFactory scheduleFormatsFactory) {
        this.recurrenceFactory = recurrenceFactory;
        this.intervalUnitFactory = intervalUnitFactory;
        this.scheduleFormatsFactory = scheduleFormatsFactory;
    }

    @NonNull
    public static JobDataFormBundleWrapper create() {
        JobScheduleRecurrenceFactory recurrenceFactory = new JobScheduleRecurrenceFactory();
        JobScheduleIntervalUnitFactory intervalUnitFactory = new JobScheduleIntervalUnitFactory();
        JobScheduleFormatsFactory scheduleFormatsFactory = new JobScheduleFormatsFactory();
        return new JobDataFormBundleWrapper(
                recurrenceFactory,
                intervalUnitFactory,
                scheduleFormatsFactory
        );
    }

    @NonNull
    public JobScheduleBundle wrap(@NonNull JobScheduleForm form) {
        List<JobScheduleForm.Recurrence> recurrences = recurrenceFactory.generate(form.recurrence());
        List<JobSimpleRecurrence.Unit> units = intervalUnitFactory.generate();
        List<JobScheduleForm.OutputFormat> formats = scheduleFormatsFactory.generate();

        JobScheduleBundle.Builder builder = JobScheduleBundle.builder();
        builder.allRecurrences(recurrences);
        builder.allIntervalUnits(units);
        builder.allFormats(formats);
        builder.form(form);
        builder.allDays(JobScheduleRecurrenceFactory.CalendarRecurrenceFactory.ALL_DAYS);
        builder.allMonths(JobScheduleRecurrenceFactory.CalendarRecurrenceFactory.ALL_MONTHS);

        return builder.build();
    }
}
