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

import com.jaspersoft.android.jaspermobile.domain.entity.job.JobScheduleForm;
import com.jaspersoft.android.jaspermobile.ui.entity.job.JobFormViewEntity;
import com.jaspersoft.android.jaspermobile.ui.mapper.UiCollectionEntityMapper;
import com.jaspersoft.android.jaspermobile.ui.mapper.UiEntityMapper;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * @author Tom Koptel
 * @since 2.5
 */
public class JobUiFormMapperTest {

    private static final String DESCRIPTION = "description";
    private JobFormViewEntity defaultUiEntity;
    private JobFormViewEntity mappedUiEntity;
    private JobScheduleForm defaultDomainEntity;
    private JobScheduleForm mappedDomainEntity;
    private JobUiFormMapper jobFormMapper;

    @Mock
    UiEntityMapper<JobScheduleForm.Recurrence, JobFormViewEntity.Recurrence> recurrenceMapper;
    @Mock
    JobScheduleForm.Recurrence domainRecurrence;
    @Mock
    JobFormViewEntity.Recurrence uiRecurrence;

    @Mock
    UiCollectionEntityMapper<JobScheduleForm.OutputFormat, JobFormViewEntity.OutputFormat> formatMapper;

    JobScheduleForm.OutputFormat domainOutPutFormat = JobScheduleForm.OutputFormat.CSV;
    @Mock
    JobFormViewEntity.OutputFormat uiOutputFormat;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(recurrenceMapper.toDomainEntity(any(JobFormViewEntity.Recurrence.class))).thenReturn(domainRecurrence);
        when(recurrenceMapper.toUiEntity(any(JobScheduleForm.Recurrence.class))).thenReturn(uiRecurrence);

        when(formatMapper.toDomainEntity(any(JobFormViewEntity.OutputFormat.class))).thenReturn(domainOutPutFormat);
        when(formatMapper.toUiEntity(any(JobScheduleForm.OutputFormat.class))).thenReturn(uiOutputFormat);
    }

    @Test
    public void should_map_generic_fields_from_domain_to_ui() throws Exception {
        givenMapper();
        givenDomainEntity();
        whenDomainMappedToUi();

        assertThat(mappedUiEntity.id(), is(defaultDomainEntity.id()));
        assertThat(mappedUiEntity.fileName(), is(defaultDomainEntity.fileName()));
        assertThat(mappedUiEntity.description(), is(defaultDomainEntity.description()));
        assertThat(mappedUiEntity.folderUri(), is(defaultDomainEntity.folderUri()));
        assertThat(mappedUiEntity.source(), is(defaultDomainEntity.source()));
        assertThat(mappedUiEntity.startDate(), is(defaultDomainEntity.startDate()));
        assertThat(mappedUiEntity.recurrence(), is(uiRecurrence));
        assertThat(mappedUiEntity.outputFormats(), hasItem(uiOutputFormat));
    }

    @Test
    public void should_map_generic_fields_from_ui_to_domain() throws Exception {
        givenMapper();
        givenUiEntity();
        whenUiMappedToDomain();

        assertThat(mappedDomainEntity.id(), is(defaultUiEntity.id()));
        assertThat(mappedDomainEntity.fileName(), is(defaultUiEntity.fileName()));
        assertThat(mappedDomainEntity.description(), is(defaultUiEntity.description()));
        assertThat(mappedDomainEntity.folderUri(), is(defaultUiEntity.folderUri()));
        assertThat(mappedDomainEntity.source(), is(defaultUiEntity.source()));
        assertThat(mappedDomainEntity.startDate(), is(defaultUiEntity.startDate()));
        assertThat(mappedDomainEntity.recurrence(), is(domainRecurrence));
        assertThat(mappedDomainEntity.outputFormats(), hasItem(domainOutPutFormat));
    }

    private void givenMapper() {
        jobFormMapper = new JobUiFormMapper(recurrenceMapper, formatMapper);
    }

    private void givenDomainEntity() {
        defaultDomainEntity = JobScheduleForm.builder()
                .id(90)
                .version(0)
                .description(DESCRIPTION)
                .source("/report/uri")
                .jobName("Job name")
                .fileName("file name.txt")
                .folderUri("/folder/uri")
                .startDate(new Date())
                .recurrence(domainRecurrence)
                .outputFormats(Collections.singletonList(domainOutPutFormat))
                .build();
    }

    private void whenDomainMappedToUi() {
        mappedUiEntity = jobFormMapper.toUiEntity(defaultDomainEntity);
    }

    private void givenUiEntity() {
        defaultUiEntity = JobFormViewEntity.builder()
                .id(90)
                .version(0)
                .description(DESCRIPTION)
                .source("/report/uri")
                .jobName("Job name")
                .fileName("file name.txt")
                .folderUri("/folder/uri")
                .startDate(new Date())
                .recurrence(uiRecurrence)
                .outputFormats(Collections.singletonList(uiOutputFormat))
                .build();
    }

    private void whenUiMappedToDomain() {
        mappedDomainEntity = jobFormMapper.toDomainEntity(defaultUiEntity);
    }
}