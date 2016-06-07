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


import com.jaspersoft.android.jaspermobile.domain.entity.job.JobCalendarRecurrence;
import com.jaspersoft.android.jaspermobile.domain.entity.job.JobNoneRecurrence;
import com.jaspersoft.android.jaspermobile.domain.entity.job.JobScheduleForm;
import com.jaspersoft.android.jaspermobile.domain.entity.job.JobSimpleRecurrence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Tom Koptel
 * @since 2.5
 */
class JobScheduleRecurrenceFactory {

    private final Map<Class<?>, RecurrenceFactory> creators;

    JobScheduleRecurrenceFactory() {
        creators = new LinkedHashMap<>();
        creators.put(JobNoneRecurrence.class, new NoneRecurrenceFactory());
        creators.put(JobSimpleRecurrence.class, new SimpleRecurrenceFactory());
        creators.put(JobCalendarRecurrence.class, new CalendarRecurrenceFactory());
    }

    List<JobScheduleForm.Recurrence> generate(JobScheduleForm.Recurrence userRecurrence) {
        List<JobScheduleForm.Recurrence> recurrences = new ArrayList<>(3);
        Class<? extends JobScheduleForm.Recurrence> userType = userRecurrence.getClass();

        for (Map.Entry<Class<?>, RecurrenceFactory> entry : creators.entrySet()) {
            Class<?> type = entry.getKey();
            RecurrenceFactory recurrenceFactory = entry.getValue();

            if (type.isAssignableFrom(userType)) {
                recurrences.add(userRecurrence);
            } else {
                recurrences.add(recurrenceFactory.createDefault());
            }
        }

        return recurrences;
    }

    private static class SimpleRecurrenceFactory implements RecurrenceFactory {
        @Override
        public JobScheduleForm.Recurrence createDefault() {
            return JobSimpleRecurrence.builder()
                    .interval(1)
                    .unit(JobSimpleRecurrence.Unit.DAY)
                    .build();
        }
    }

    static class CalendarRecurrenceFactory implements RecurrenceFactory {
        static final List<Integer> ALL_MONTHS = Arrays.asList(
                Calendar.JANUARY,
                Calendar.FEBRUARY,
                Calendar.MARCH,
                Calendar.APRIL,
                Calendar.MAY,
                Calendar.JUNE,
                Calendar.JULY,
                Calendar.AUGUST,
                Calendar.SEPTEMBER,
                Calendar.OCTOBER,
                Calendar.NOVEMBER,
                Calendar.DECEMBER
        );
        static final List<Integer> ALL_DAYS = Arrays.asList(
                Calendar.SUNDAY,
                Calendar.MONDAY,
                Calendar.TUESDAY,
                Calendar.WEDNESDAY,
                Calendar.THURSDAY,
                Calendar.FRIDAY,
                Calendar.SATURDAY
        );
        @Override
        public JobScheduleForm.Recurrence createDefault() {
            return JobCalendarRecurrence.builder()
                    .months(Collections.<Integer>emptyList())
                    .daysInWeek(Collections.<Integer>emptyList())
                    .minutes("0")
                    .hours("0")
                    .build();
        }
    }

    private static class NoneRecurrenceFactory implements RecurrenceFactory {
        @Override
        public JobScheduleForm.Recurrence createDefault() {
            return JobNoneRecurrence.INSTANCE;
        }
    }

    private interface RecurrenceFactory {
        JobScheduleForm.Recurrence createDefault();
    }
}
