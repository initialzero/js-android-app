package com.jaspersoft.android.jaspermobile.data.mapper.job;

import com.jaspersoft.android.jaspermobile.domain.entity.job.JobCalendarRecurrence;
import com.jaspersoft.android.sdk.service.data.schedule.CalendarRecurrence;
import com.jaspersoft.android.sdk.service.data.schedule.DaysInWeek;
import com.jaspersoft.android.sdk.service.data.schedule.DaysType;
import com.jaspersoft.android.sdk.service.data.schedule.HoursTimeFormat;
import com.jaspersoft.android.sdk.service.data.schedule.MinutesTimeFormat;
import com.jaspersoft.android.sdk.service.data.schedule.Trigger;
import com.jaspersoft.android.sdk.service.data.schedule.UntilEndDate;

import org.junit.Test;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

/**
 * @author Tom Koptel
 * @since 2.5
 */
public class JobScheduleFormCalendarRecurrenceMapperTest {
    private static final List<Integer> ALL_DAYS = Arrays.asList(
            Calendar.SUNDAY,
            Calendar.MONDAY,
            Calendar.TUESDAY,
            Calendar.WEDNESDAY,
            Calendar.THURSDAY,
            Calendar.FRIDAY,
            Calendar.SATURDAY
    );

    private JobCalendarRecurrence defaultDomain, mappedDomain;
    private Trigger defaultData, mappedData;
    private JobScheduleFormCalendarRecurrenceMapper recurrenceMapper;

    @Test
    public void testMapsNoDaysTypeToTriggerIfAllDaysSelected() throws Exception {
        givenMapper();
        givenDomainEntityWithAllDays();

        whenMapsToDataEntity();

        CalendarRecurrence recurrence = (CalendarRecurrence) mappedData.getRecurrence();
        DaysType daysType = recurrence.getDaysType();
        assertThat(daysType, is(nullValue()));
    }

    private void givenDomainEntityWithAllDays() {
        givenDomainEntity();
        defaultDomain = defaultDomain.newBuilder()
                .daysInWeek(ALL_DAYS)
                .build();
    }

    @Test
    public void testToDataEntity() throws Exception {
        givenMapper();
        givenDomainEntity();

        whenMapsToDataEntity();

        CalendarRecurrence recurrence = (CalendarRecurrence) mappedData.getRecurrence();
        UntilEndDate endDate = (UntilEndDate) mappedData.getEndDate();
        DaysInWeek daysInWeek = (DaysInWeek) recurrence.getDaysType();

        assertThat(recurrence.getMonths(), hasItem(Calendar.AUGUST));
        assertThat(daysInWeek.getDays(), hasItem(Calendar.FRIDAY));
        assertThat(endDate.getSpecifiedDate(), is(defaultDomain.endDate()));
        assertThat(recurrence.getHours().toString(), is("1,4"));
        assertThat(recurrence.getMinutes().toString(), is("9,12"));
    }

    @Test
    public void testToDomainEntity() throws Exception {
        givenMapper();
        givenDataEntity();

        whenMapsToDomainEntity();

        Date endDate = ((UntilEndDate) defaultData.getEndDate()).getSpecifiedDate();
        assertThat(mappedDomain.months(), hasItem(Calendar.AUGUST));
        assertThat(mappedDomain.daysInWeek(), hasItem(Calendar.FRIDAY));
        assertThat(mappedDomain.hours(), is("1,4"));
        assertThat(mappedDomain.minutes(), is("9,12"));
        assertThat(mappedDomain.endDate(), is(endDate));
    }

    private void givenMapper() {
        recurrenceMapper = new JobScheduleFormCalendarRecurrenceMapper();
    }

    private void givenDomainEntity() {
        defaultDomain = JobCalendarRecurrence.builder()
                .daysInWeek(Collections.singletonList(Calendar.FRIDAY))
                .months(Collections.singletonList(Calendar.AUGUST))
                .minutes("9, 12")
                .hours("1, 4")
                .endDate(new Date())
                .build();
    }

    private void givenDataEntity() {
        CalendarRecurrence recurrence = new CalendarRecurrence.Builder()
                .withMonths(Calendar.AUGUST)
                .withDaysInWeek(Calendar.FRIDAY)
                .withMinutes(MinutesTimeFormat.parse("9,12"))
                .withHours(HoursTimeFormat.parse("1,4"))
                .build();
        defaultData = new Trigger.Builder()
                .withRecurrence(recurrence)
                .withEndDate(new UntilEndDate(new Date()))
                .build();
    }

    private void whenMapsToDomainEntity() {
        mappedDomain = (JobCalendarRecurrence) recurrenceMapper.toDomainEntity(defaultData);
    }

    private void whenMapsToDataEntity() {
        mappedData = recurrenceMapper.toDataEntity(defaultDomain);
    }
}