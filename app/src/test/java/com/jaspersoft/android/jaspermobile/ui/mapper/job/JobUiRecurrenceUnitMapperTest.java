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

import com.jaspersoft.android.jaspermobile.domain.entity.job.JobSimpleRecurrence;
import com.jaspersoft.android.jaspermobile.ui.entity.job.SimpleViewRecurrence;
import com.jaspersoft.android.jaspermobile.ui.mapper.EntityLocalizer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * @author Tom Koptel
 * @since 2.5
 */
public class JobUiRecurrenceUnitMapperTest {

    public static final String LOCALIZED_UNIT = "LOCALIZED_UNIT";

    @Mock
    EntityLocalizer<JobSimpleRecurrence.Unit> entityLocalizer;

    private JobUiRecurrenceUnitMapper unitMapper;

    JobSimpleRecurrence.Unit defaultDomainUnit, mappedDomainUnit;
    SimpleViewRecurrence.Unit defaultUiUnit, mappedUiUnit;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(entityLocalizer.localize(any(JobSimpleRecurrence.Unit.class))).thenReturn(LOCALIZED_UNIT);
    }

    @Test
    public void testToUiEntity() throws Exception {
        givenUnitMapper();
        givenDomainUnit();
        givenUiUnit();

        whenMapsToUiEntity();

        assertThat(mappedUiUnit.rawValue(), is(defaultDomainUnit.name()));
        assertThat(mappedUiUnit.localizedLabel(), is(LOCALIZED_UNIT));
    }

    @Test
    public void testToDomainEntity() throws Exception {
        givenUnitMapper();
        givenDomainUnit();
        givenUiUnit();

        whenMapsToDomainEntity();

        assertThat(mappedDomainUnit, is(defaultDomainUnit));
    }

    private void givenUnitMapper() {
        unitMapper = new JobUiRecurrenceUnitMapper(entityLocalizer);
    }

    private void givenDomainUnit() {
        defaultDomainUnit = JobSimpleRecurrence.Unit.DAY;
    }

    private void givenUiUnit() {
        defaultUiUnit = SimpleViewRecurrence.Unit.create(defaultDomainUnit.name(), LOCALIZED_UNIT);
    }

    private void whenMapsToUiEntity() {
        mappedUiUnit = unitMapper.toUiEntity(defaultDomainUnit);
    }

    private void whenMapsToDomainEntity() {
        mappedDomainUnit = unitMapper.toDomainEntity(defaultUiUnit);
    }
}