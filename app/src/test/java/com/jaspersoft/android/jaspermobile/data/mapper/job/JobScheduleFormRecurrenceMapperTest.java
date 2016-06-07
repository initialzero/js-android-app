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
import com.jaspersoft.android.jaspermobile.domain.entity.job.JobSimpleRecurrence;
import com.jaspersoft.android.sdk.service.data.schedule.CalendarRecurrence;
import com.jaspersoft.android.sdk.service.data.schedule.IntervalRecurrence;
import com.jaspersoft.android.sdk.service.data.schedule.RecurrenceIntervalUnit;
import com.jaspersoft.android.sdk.service.data.schedule.Trigger;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;

/**
 * @author Tom Koptel
 * @since 2.5
 */
public class JobScheduleFormRecurrenceMapperTest {

    @Mock
    JobScheduleFormSimpleRecurrenceMapper simpleRecurrenceMapper;
    @Mock
    JobScheduleFormCalendarRecurrenceMapper calendarRecurrenceMapper;
    @Mock
    JobScheduleFormNoneRecurrenceMapper noneRecurrenceMapper;

    private JobScheduleFormRecurrenceMapper recurrenceMapper;

    @Mock
    JobSimpleRecurrence domainSimpleRecurrence;
    @Mock
    JobCalendarRecurrence domainCalendarRecurrence;
    JobNoneRecurrence domainNoneRecurrence = JobNoneRecurrence.INSTANCE;

    Trigger dataSimpleRecurrence = new Trigger.Builder()
            .withRecurrence(new IntervalRecurrence.Builder()
                    .withInterval(1)
                    .withUnit(RecurrenceIntervalUnit.DAY)
                    .build())
            .build();
    Trigger dataCalendarRecurrence = new Trigger.Builder()
            .withRecurrence(new CalendarRecurrence.Builder()
                    .withAllMonths()
                    .build())
            .build();
    Trigger dataNoneRecurrence = null;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        recurrenceMapper = new JobScheduleFormRecurrenceMapper(simpleRecurrenceMapper, calendarRecurrenceMapper, noneRecurrenceMapper);
    }

    @Test
    public void should_map_domain_simple_recurrence_to_data() throws Exception {
        recurrenceMapper.toDataEntity(domainSimpleRecurrence);

        verify(simpleRecurrenceMapper).toDataEntity(domainSimpleRecurrence);
    }

    @Test
    public void should_map_data_simple_recurrence_to_domain() throws Exception {
        recurrenceMapper.toDomainEntity(dataSimpleRecurrence);

        verify(simpleRecurrenceMapper).toDomainEntity(dataSimpleRecurrence);
    }

    @Test
    public void should_map_domain_none_recurrence_to_data() throws Exception {
        recurrenceMapper.toDataEntity(domainNoneRecurrence);

        verify(noneRecurrenceMapper).toDataEntity(domainNoneRecurrence);
    }

    @Test
    public void should_map_data_none_recurrence_to_domain() throws Exception {
        recurrenceMapper.toDomainEntity(dataNoneRecurrence);

        verify(noneRecurrenceMapper).toDomainEntity(dataNoneRecurrence);
    }

    @Test
    public void should_map_domain_calendar_recurrence_to_data() throws Exception {
        recurrenceMapper.toDataEntity(domainCalendarRecurrence);

        verify(calendarRecurrenceMapper).toDataEntity(domainCalendarRecurrence);
    }

    @Test
    public void should_map_data_calendar_recurrence_to_domain() throws Exception {
        recurrenceMapper.toDomainEntity(dataCalendarRecurrence);

        verify(calendarRecurrenceMapper).toDomainEntity(dataCalendarRecurrence);
    }
}