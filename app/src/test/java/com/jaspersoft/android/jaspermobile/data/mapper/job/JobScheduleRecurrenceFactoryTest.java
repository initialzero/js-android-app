/*
 * Copyright � 2016 TIBCO Software,Inc.All rights reserved.
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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

/**
 * @author Tom Koptel
 * @since 2.5
 */
public class JobScheduleRecurrenceFactoryTest {

    private JobScheduleRecurrenceFactory recurrenceFactory;

    JobSimpleRecurrence simpleRecurrence = JobSimpleRecurrence.builder()
            .interval(100)
            .unit(JobSimpleRecurrence.Unit.MINUTE)
            .build();
    @Mock
    JobCalendarRecurrence calendarRecurrence;
    JobNoneRecurrence noneRecurrence = JobNoneRecurrence.INSTANCE;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        recurrenceFactory = new JobScheduleRecurrenceFactory();
    }

    @Test
    public void should_reuse_simple_recurrence() throws Exception {
        List<JobScheduleForm.Recurrence> recurrences = recurrenceFactory.generate(simpleRecurrence);
        assertThat(recurrences, hasItem(simpleRecurrence));
    }

    @Test
    public void should_reuse_calendar_recurrence() throws Exception {
        List<JobScheduleForm.Recurrence> recurrences = recurrenceFactory.generate(calendarRecurrence);
        assertThat(recurrences, hasItem(calendarRecurrence));
    }

    @Test
    public void should_reuse_none_recurrence() throws Exception {
        List<JobScheduleForm.Recurrence> recurrences = recurrenceFactory.generate(noneRecurrence);
        assertThat(recurrences, hasItem(noneRecurrence));
    }
}