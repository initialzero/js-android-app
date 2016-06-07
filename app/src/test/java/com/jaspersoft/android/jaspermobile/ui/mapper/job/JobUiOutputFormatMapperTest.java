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

import com.jaspersoft.android.jaspermobile.domain.entity.job.JobScheduleForm.OutputFormat;
import com.jaspersoft.android.jaspermobile.ui.entity.job.JobFormViewEntity;
import com.jaspersoft.android.jaspermobile.ui.mapper.EntityLocalizer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Tom Koptel
 * @since 2.5
 */
public class JobUiOutputFormatMapperTest {

    private static final String LOCALIZED_FORMAT = "Localized Format";

    OutputFormat defaultDomainEntity = OutputFormat.CSV;
    JobFormViewEntity.OutputFormat defaultUiEntity = JobFormViewEntity.OutputFormat.create("CSV", LOCALIZED_FORMAT);
    @Mock
    EntityLocalizer<OutputFormat> entityLocalizer;

    private JobUiOutputFormatMapper formatMapper;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(entityLocalizer.localize(any(OutputFormat.class))).thenReturn(LOCALIZED_FORMAT);

        formatMapper = new JobUiOutputFormatMapper(entityLocalizer);
    }

    @Test
    public void testToUiEntity() throws Exception {
        JobFormViewEntity.OutputFormat format = formatMapper.toUiEntity(defaultDomainEntity);

        verify(entityLocalizer).localize(defaultDomainEntity);
        assertThat(format, is(defaultUiEntity));
    }

    @Test
    public void testToDomainEntity() throws Exception {
        OutputFormat format = formatMapper.toDomainEntity(defaultUiEntity);
        assertThat(format, is(defaultDomainEntity));
    }
}