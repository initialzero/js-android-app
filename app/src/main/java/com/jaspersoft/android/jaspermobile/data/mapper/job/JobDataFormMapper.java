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

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.jaspersoft.android.jaspermobile.data.entity.job.IdentifiedJobForm;
import com.jaspersoft.android.jaspermobile.data.mapper.DataEntityMapper;
import com.jaspersoft.android.jaspermobile.domain.entity.job.JobScheduleForm;
import com.jaspersoft.android.sdk.service.data.schedule.JobForm;
import com.jaspersoft.android.sdk.service.data.schedule.JobOutputFormat;
import com.jaspersoft.android.sdk.service.data.schedule.JobSource;
import com.jaspersoft.android.sdk.service.data.schedule.RepositoryDestination;
import com.jaspersoft.android.sdk.service.data.schedule.Trigger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Tom Koptel
 * @since 2.5
 */
public class JobDataFormMapper implements DataEntityMapper<JobScheduleForm, IdentifiedJobForm> {

    @NonNull
    private final JobScheduleFormRecurrenceMapper recurrenceMapper;
    @NonNull
    private final JobScheduleFormOutputFormatMapper outputFormatMapper;

    @VisibleForTesting
    JobDataFormMapper(
            @NonNull JobScheduleFormRecurrenceMapper recurrenceMapper,
            @NonNull JobScheduleFormOutputFormatMapper outputFormatMapper
    ) {
        this.recurrenceMapper = recurrenceMapper;
        this.outputFormatMapper = outputFormatMapper;
    }

    @NonNull
    public static JobDataFormMapper create() {
        JobScheduleFormRecurrenceMapper recurrenceMapper = JobScheduleFormRecurrenceMapper.create();
        JobScheduleFormOutputFormatMapper formatMapper = new JobScheduleFormOutputFormatMapper();
        return new JobDataFormMapper(recurrenceMapper, formatMapper);
    }

    @NonNull
    @Override
    public IdentifiedJobForm toDataEntity(@NonNull JobScheduleForm domainForm) {
        JobScheduleForm.Recurrence recurrence = domainForm.recurrence();
        Trigger dataRecurrence = recurrenceMapper.toDataEntity(recurrence);

        JobSource source =  extractJobSource(domainForm)
                .withUri(domainForm.source())
                .build();

        RepositoryDestination destination = extractDestinationBuilder(domainForm)
                .withFolderUri(domainForm.folderUri())
                .build();
        List<JobOutputFormat> formats = outputFormatMapper.toDataEntityList(domainForm.outputFormats());

        JobForm.Builder formBuilder = new JobForm.Builder();
        formBuilder.withVersion(domainForm.version());
        formBuilder.withLabel(domainForm.jobName());
        formBuilder.withDescription(domainForm.description());
        formBuilder.withBaseOutputFilename(domainForm.fileName());
        formBuilder.withStartDate(domainForm.startDate());
        formBuilder.withRepositoryDestination(destination);
        formBuilder.withMailNotification(domainForm.rawMailNotification());
        formBuilder.withJobAlert(domainForm.rawAlert());
        formBuilder.withJobSource(source);
        formBuilder.withOutputFormats(formats);
        formBuilder.withTrigger(dataRecurrence);
        formBuilder.build();

        return IdentifiedJobForm.create(domainForm.id(), formBuilder.build());
    }

    private JobSource.Builder extractJobSource(JobScheduleForm domainForm) {
        JobSource jobSource = domainForm.rawSource();
        if (jobSource == null) {
            return new JobSource.Builder();
        }
        return jobSource.newBuilder();
    }

    @NonNull
    @Override
    public JobScheduleForm toDomainEntity(@NonNull IdentifiedJobForm dataForm) {
        JobForm form = dataForm.form();

        JobScheduleForm.Builder builder = JobScheduleForm.builder();
        builder.id(dataForm.id());

        int version = form.getVersion() == null ? 0 : form.getVersion();
        builder.version(version);

        builder.jobName(form.getLabel());
        builder.description(form.getDescription());
        builder.fileName(form.getBaseOutputFilename());
        builder.startDate(form.getStartDate());
        builder.folderUri(form.getRepositoryDestination().getFolderUri());
        builder.source(form.getSource().getUri());
        builder.rawDestination(form.getRepositoryDestination());
        builder.rawMailNotification(form.getMailNotification());
        builder.rawAlert(form.getJobAlert());
        builder.rawSource(form.getSource());

        Set<JobOutputFormat> outputFormats = form.getOutputFormats();
        List<JobOutputFormat> dataFormats = new ArrayList<>(outputFormats.size());
        dataFormats.addAll(outputFormats);
        List<JobScheduleForm.OutputFormat> formats = outputFormatMapper.toDomainEntityList(dataFormats);
        builder.outputFormats(formats);

        Trigger trigger = form.getTrigger();
        JobScheduleForm.Recurrence recurrence = recurrenceMapper.toDomainEntity(trigger);
        builder.recurrence(recurrence);

        return builder.build();
    }

    @NonNull
    private RepositoryDestination.Builder extractDestinationBuilder(@NonNull JobScheduleForm domainForm) {
        RepositoryDestination destination = domainForm.rawDestination();
        if (destination == null) {
            return new RepositoryDestination.Builder();
        } else {
            return destination.newBuilder();
        }
    }
}
