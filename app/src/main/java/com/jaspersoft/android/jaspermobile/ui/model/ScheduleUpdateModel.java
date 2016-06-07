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

package com.jaspersoft.android.jaspermobile.ui.model;

import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.Analytics;
import com.jaspersoft.android.jaspermobile.domain.entity.job.JobScheduleBundle;
import com.jaspersoft.android.jaspermobile.domain.entity.job.JobScheduleForm;
import com.jaspersoft.android.jaspermobile.domain.interactor.schedule.GetJobScheduleUseCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.schedule.UpdateJobScheduleUseCase;
import com.jaspersoft.android.jaspermobile.internal.di.PerScreen;
import com.jaspersoft.android.jaspermobile.ui.entity.job.JobFormViewBundle;
import com.jaspersoft.android.jaspermobile.ui.mapper.UiEntityMapper;

import rx.Subscriber;

/**
 * @author Tom Koptel
 * @since 2.5
 */
@PerScreen
public final class ScheduleUpdateModel extends AbstractScheduleModel {
    @NonNull
    private final UiEntityMapper<JobScheduleBundle, JobFormViewBundle> bundleMapper;
    @NonNull
    private final GetJobScheduleUseCase getJobScheduleUseCase;
    @NonNull
    private final UpdateJobScheduleUseCase updateJobScheduleUseCase;
    @NonNull
    private final Analytics analytics;
    private final int jobId;
    private boolean loadDataEventConsumed;

    private JobScheduleForm initialDomainForm;

    public ScheduleUpdateModel(
            int jobId,
            @NonNull UiEntityMapper<JobScheduleBundle, JobFormViewBundle> bundleMapper,
            @NonNull GetJobScheduleUseCase getJobScheduleUseCase,
            @NonNull UpdateJobScheduleUseCase updateJobScheduleUseCase,
            @NonNull Analytics analytics
    ) {
        this.jobId = jobId;
        this.bundleMapper = bundleMapper;
        this.getJobScheduleUseCase = getJobScheduleUseCase;
        this.updateJobScheduleUseCase = updateJobScheduleUseCase;
        this.analytics = analytics;
    }

    @Override
    public void load() {
        getJobScheduleUseCase.execute(jobId, createReadFormSubscriber());
    }

    @Override
    public void submit(JobFormViewBundle viewEntity) {
        JobScheduleBundle formBundle = bundleMapper.toDomainEntity(viewEntity);
        JobScheduleForm form = formBundle.form()
                .newBuilder()
                .rawDestination(initialDomainForm.rawDestination())
                .rawMailNotification(initialDomainForm.rawMailNotification())
                .rawAlert(initialDomainForm.rawAlert())
                .rawSource(initialDomainForm.rawSource())
                .build();
        JobScheduleBundle updatedBundle = formBundle.newBuilder()
                .form(form)
                .build();
        updateJobScheduleUseCase.execute(updatedBundle, createUpdateFormSubscriber());
    }

    @Override
    public void bind(Callback callbacks) {
        super.bind(callbacks);
        if (!loadDataEventConsumed) {
            getJobScheduleUseCase.subscribe(createReadFormSubscriber());
        }
        updateJobScheduleUseCase.subscribe(createUpdateFormSubscriber());
    }

    private Subscriber<? super JobScheduleBundle> createReadFormSubscriber() {
        return new Subscriber<JobScheduleBundle>() {
            @Override
            public void onCompleted() {
                logJobCreatedEvent();
            }

            @Override
            public void onError(Throwable e) {
                mCallbacks.onFormLoadError(e);
            }

            @Override
            public void onNext(JobScheduleBundle domainBundle) {
                loadDataEventConsumed = true;
                initialDomainForm = domainBundle.form();
                JobFormViewBundle form = bundleMapper.toUiEntity(domainBundle);
                mCallbacks.onFormLoadSuccess(form);
            }
        };
    }

    private Subscriber<? super Void> createUpdateFormSubscriber() {
        return new Subscriber<Void>() {
            @Override
            public void onCompleted() {
                logJobViewedEvent();
            }

            @Override
            public void onError(Throwable e) {
                mCallbacks.onFormSubmitError(e);
            }

            @Override
            public void onNext(Void item) {
                mCallbacks.onFormSubmitSuccess();
            }
        };
    }

    @Override
    public void unbind() {
        super.unbind();
        getJobScheduleUseCase.unsubscribe();
        updateJobScheduleUseCase.unsubscribe();
    }

    private void logJobViewedEvent() {
        analytics.sendEvent(
                Analytics.EventCategory.JOB.getValue(),
                Analytics.EventAction.VIEWED.getValue(),
                null);
    }

    private void logJobCreatedEvent() {
        analytics.sendEvent(
                Analytics.EventCategory.JOB.getValue(),
                Analytics.EventAction.CHANGED.getValue(),
                null);
    }
}
