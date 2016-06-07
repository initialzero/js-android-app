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

import com.jaspersoft.android.jaspermobile.data.mapper.DataEntityMapper;
import com.jaspersoft.android.jaspermobile.domain.entity.job.JobCalendarRecurrence;
import com.jaspersoft.android.jaspermobile.domain.entity.job.JobScheduleForm;
import com.jaspersoft.android.sdk.service.data.schedule.CalendarRecurrence;
import com.jaspersoft.android.sdk.service.data.schedule.DaysInMonth;
import com.jaspersoft.android.sdk.service.data.schedule.DaysInWeek;
import com.jaspersoft.android.sdk.service.data.schedule.DaysType;
import com.jaspersoft.android.sdk.service.data.schedule.HoursTimeFormat;
import com.jaspersoft.android.sdk.service.data.schedule.MinutesTimeFormat;
import com.jaspersoft.android.sdk.service.data.schedule.Trigger;
import com.jaspersoft.android.sdk.service.data.schedule.UntilEndDate;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author Tom Koptel
 * @since 2.5
 */
class JobScheduleFormCalendarRecurrenceMapper implements DataEntityMapper<JobScheduleForm.Recurrence, Trigger> {

    @NonNull
    @Override
    public Trigger toDataEntity(@NonNull JobScheduleForm.Recurrence domainEntity) {
        JobCalendarRecurrence domainRecurrence = (JobCalendarRecurrence) domainEntity;
        CalendarRecurrence.Builder recurrenceBuilder = new CalendarRecurrence.Builder();

        Set<Integer> months = new HashSet<>(domainRecurrence.months());
        recurrenceBuilder.withMonths(months);

        List<Integer> daysInWeek = domainRecurrence.daysInWeek();
        if (daysInWeek != null) {
            List<Integer> allDays = JobScheduleRecurrenceFactory.CalendarRecurrenceFactory.ALL_DAYS;

            Set<Integer> days = new HashSet<>(daysInWeek);
            boolean allWeekDaysSelected = days.containsAll(allDays);
            if (!allWeekDaysSelected) {
                recurrenceBuilder.withDaysInWeek(days);
            }
        }

        String daysInMonth = domainRecurrence.daysInMonth();
        if (daysInMonth != null) {
            daysInMonth = daysInMonth.replace(" ", "");
            recurrenceBuilder.withDaysInMonth(DaysInMonth.valueOf(daysInMonth));
        }

        String hours = domainRecurrence.hours();
        hours = hours.replace(" ", "");
        recurrenceBuilder.withHours(HoursTimeFormat.parse(hours));

        String minutes = domainRecurrence.minutes();
        minutes = minutes.replace(" ", "");
        recurrenceBuilder.withMinutes(MinutesTimeFormat.parse(minutes));

        CalendarRecurrence dataRecurrence = recurrenceBuilder.build();
        Trigger.CalendarTriggerBuilder trigger = new Trigger.Builder()
                .withRecurrence(dataRecurrence);
        Date endDate = domainRecurrence.endDate();
        if (endDate != null) {
            trigger.withEndDate(new UntilEndDate(endDate));
        }

        return trigger.build();
    }

    @NonNull
    @Override
    public JobScheduleForm.Recurrence toDomainEntity(@NonNull Trigger dataEntity) {
        CalendarRecurrence recurrence = (CalendarRecurrence) dataEntity.getRecurrence();
        UntilEndDate endDate = (UntilEndDate) dataEntity.getEndDate();

        JobCalendarRecurrence.Builder builder = JobCalendarRecurrence.builder();

        LinkedList<Integer> months = new LinkedList<>();
        months.addAll(recurrence.getMonths());
        Collections.sort(months);
        builder.months(months);

        if (endDate != null) {
            builder.endDate(endDate.getSpecifiedDate());
        }

        DaysType daysType = recurrence.getDaysType();
        if (daysType != null) {
            if (daysType instanceof DaysInWeek) {
                DaysInWeek daysInWeek = (DaysInWeek) daysType;

                List<Integer> daysInWeekList = new LinkedList<>();
                daysInWeekList.addAll(daysInWeek.getDays());
                Collections.sort(daysInWeekList);
                builder.daysInWeek(daysInWeekList);
            }

            if (daysType instanceof DaysInMonth) {
                DaysInMonth daysInMonth = (DaysInMonth) daysType;
                builder.daysInMonth(daysInMonth.toString());
            }
        }
        builder.hours(recurrence.getHours().toString());
        builder.minutes(recurrence.getMinutes().toString());

        return builder.build();
    }
}
