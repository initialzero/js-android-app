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

import com.jaspersoft.android.jaspermobile.data.mapper.job.JobDataFormBundleWrapper;
import com.jaspersoft.android.jaspermobile.domain.entity.job.JobNoneRecurrence;
import com.jaspersoft.android.jaspermobile.domain.entity.job.JobScheduleBundle;
import com.jaspersoft.android.jaspermobile.domain.entity.job.JobScheduleForm;
import com.jaspersoft.android.jaspermobile.ui.entity.job.JobFormViewBundle;
import com.jaspersoft.android.jaspermobile.ui.mapper.UiEntityMapper;
import com.jaspersoft.android.jaspermobile.util.resource.JasperResource;
import com.jaspersoft.android.jaspermobile.util.resource.JasperResourceType;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Tom Koptel
 * @since 2.5
 */
public class JasperResourceMapperTest {

    public static final String NEW_SCHEDULE = "New schedule";

    @Mock
    JobDataFormBundleWrapper bundleWrapper;
    @Mock
    UiEntityMapper<JobScheduleBundle, JobFormViewBundle> jobUiFormBundleMapper;

    @Mock
    JobScheduleBundle domainFormBundle;
    @Mock
    JobFormViewBundle uiFormBundle;

    JobScheduleForm defaultDomainForm;
    JasperResource defaultJasperResource;

    private JasperResourceMapper resourceMapper;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(bundleWrapper.wrap(any(JobScheduleForm.class))).thenReturn(domainFormBundle);

        resourceMapper = new JasperResourceMapper(bundleWrapper, jobUiFormBundleMapper);
    }

    // FIXME: 8/5/16
//    @Test
//    public void testToUiEntity() throws Exception {
//        givenDefaultDomainForm();
//        givenJasperResource();
//
//        resourceMapper.toUiEntity(defaultJasperResource);
//
//        verify(bundleWrapper).wrap(defaultDomainForm);
//    }

    private void givenDefaultDomainForm() {
        defaultDomainForm = JobScheduleForm.builder()
                .id(0)
                .version(0)
                .outputFormats(Collections.singletonList(JobScheduleForm.OutputFormat.PDF))
                .fileName("All_accounts")
                .folderUri("/my/report")
                .source("/my/report/uri")
                .jobName(NEW_SCHEDULE)
                .recurrence(JobNoneRecurrence.INSTANCE)
                .build();
    }

    private void givenJasperResource() {
        defaultJasperResource = new Resource("/my/report/uri", "All accounts", null);
    }

    private static class Resource extends JasperResource {
        public Resource(String id, String label, String description) {
            super(id, label, description);
        }

        @Override
        public JasperResourceType getResourceType() {
            return JasperResourceType.job;
        }
    }
}