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

import com.jaspersoft.android.jaspermobile.ui.entity.job.CalendarViewRecurrence;
import com.jaspersoft.android.jaspermobile.ui.mapper.EntityLocalizer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Calendar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Tom Koptel
 * @since 2.5
 */
public class JobUiCalendarDayMapperTest {

    public static final String LOCALIZED_MONTH = "Localized month";
    public static final int MONDAY = Calendar.MONDAY;
    @Mock
    EntityLocalizer<Integer> localizer;
    private JobUiCalendarDayMapper monthMapper;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        monthMapper = new JobUiCalendarDayMapper(localizer);
        when(localizer.localize(anyInt())).thenReturn(LOCALIZED_MONTH);
    }

    @Test
    public void testToUiEntity() throws Exception {
        CalendarViewRecurrence.Day uiEntity = CalendarViewRecurrence.Day.create(LOCALIZED_MONTH, MONDAY);
        Integer domainEntity = monthMapper.toDomainEntity(uiEntity);
        assertThat(domainEntity, is(MONDAY));
    }

    @Test
    public void testToDomainEntity() throws Exception {
        CalendarViewRecurrence.Day day = monthMapper.toUiEntity(MONDAY);

        verify(localizer).localize(MONDAY);
        assertThat(day.localizedLabel(), is(LOCALIZED_MONTH));
        assertThat(day.rawValue(), is(MONDAY));
    }
}