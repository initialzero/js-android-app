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

package com.jaspersoft.android.jaspermobile.data.repository.job;

import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.data.JasperRestClient;
import com.jaspersoft.android.jaspermobile.data.entity.job.IdentifiedJobForm;
import com.jaspersoft.android.jaspermobile.data.mapper.job.JobDataFormBundleWrapper;
import com.jaspersoft.android.jaspermobile.data.mapper.job.JobDataFormMapper;
import com.jaspersoft.android.jaspermobile.domain.entity.job.JobScheduleBundle;
import com.jaspersoft.android.jaspermobile.domain.entity.job.JobScheduleForm;
import com.jaspersoft.android.jaspermobile.domain.repository.Specification;
import com.jaspersoft.android.jaspermobile.domain.repository.schedule.ScheduleRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.schedule.ScheduleSpecification;
import com.jaspersoft.android.jaspermobile.internal.di.PerScreen;
import com.jaspersoft.android.sdk.service.data.schedule.JobForm;
import com.jaspersoft.android.sdk.service.exception.ServiceException;
import com.jaspersoft.android.sdk.service.report.schedule.ReportScheduleService;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

/**
 * @author Tom Koptel
 * @since 2.5
 */
@PerScreen
public class NetworkScheduleRepository implements ScheduleRepository {
    @NonNull
    private final JasperRestClient restClient;
    @NonNull
    private final JobDataFormBundleWrapper formWrapper;
    @NonNull
    private final JobDataFormMapper formMapper;

    @Inject
    public NetworkScheduleRepository(
            @NonNull JasperRestClient restClient,
            @NonNull JobDataFormBundleWrapper bundleMapper,
            @NonNull JobDataFormMapper formMapper
    ) {
        this.restClient = restClient;
        this.formWrapper = bundleMapper;
        this.formMapper = formMapper;
    }

    @Override
    public void add(JobScheduleBundle item) {
        JobScheduleForm domainForm = item.form();
        IdentifiedJobForm identifiedJobForm = formMapper.toDataEntity(domainForm);
        JobForm dataForm = identifiedJobForm.form();

        ReportScheduleService service = restClient.syncScheduleService();
        try {
            service.createJob(dataForm);
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void add(Iterable<JobScheduleBundle> items) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void update(JobScheduleBundle item) {
        JobScheduleForm domainForm = item.form();
        IdentifiedJobForm identifiedJobForm = formMapper.toDataEntity(domainForm);

        int jobId = identifiedJobForm.id();
        JobForm dataForm = identifiedJobForm.form();

        ReportScheduleService service = restClient.syncScheduleService();
        try {
            service.updateJob(jobId, dataForm);
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remove(JobScheduleBundle item) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void remove(Specification specification) {
        ScheduleSpecification scheduleSpecification = (ScheduleSpecification) specification;
        ReportScheduleService service = restClient.syncScheduleService();

        int jobId = scheduleSpecification.toId();
        try {
            service.deleteJobs(Collections.singleton(jobId));
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<JobScheduleBundle> query(Specification specification) {
        ScheduleSpecification scheduleSpecification = (ScheduleSpecification) specification;
        ReportScheduleService service = restClient.syncScheduleService();
        try {
            int jobId = scheduleSpecification.toId();
            JobForm jobForm = service.readJob(jobId);
            IdentifiedJobForm identifiedJobForm = IdentifiedJobForm.create(jobId, jobForm);

            JobScheduleForm domainForm = formMapper.toDomainEntity(identifiedJobForm);
            JobScheduleBundle domainFormBundle = formWrapper.wrap(domainForm);

            return Collections.singletonList(domainFormBundle);
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        }
    }
}
