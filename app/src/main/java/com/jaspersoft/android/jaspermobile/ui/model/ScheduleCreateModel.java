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
import com.jaspersoft.android.jaspermobile.domain.interactor.schedule.SaveJobScheduleUseCase;
import com.jaspersoft.android.jaspermobile.ui.entity.job.JobFormViewBundle;
import com.jaspersoft.android.jaspermobile.ui.mapper.UiEntityMapper;

import rx.Subscriber;

/**
 * @author Tom Koptel
 * @since 2.5
 */
public final class ScheduleCreateModel extends AbstractScheduleModel {
    @NonNull
    private final JobFormViewBundle scheduleBundle;
    @NonNull
    private final UiEntityMapper<JobScheduleBundle, JobFormViewBundle> bundleMapper;
    @NonNull
    private final SaveJobScheduleUseCase saveJobScheduleUseCase;
    @NonNull
    private final Analytics mAnalytics;

    public ScheduleCreateModel(@NonNull JobFormViewBundle scheduleBundle,
                               @NonNull UiEntityMapper<JobScheduleBundle, JobFormViewBundle> bundleMapper,
                               @NonNull SaveJobScheduleUseCase saveJobScheduleUseCase,
                               @NonNull Analytics analytics) {
        this.scheduleBundle = scheduleBundle;
        this.bundleMapper = bundleMapper;
        this.saveJobScheduleUseCase = saveJobScheduleUseCase;
        this.mAnalytics = analytics;
    }

    @Override
    public void load() {
        mCallbacks.onFormLoadSuccess(scheduleBundle);
    }

    @Override
    public void submit(JobFormViewBundle form) {
        JobScheduleBundle jobForm = bundleMapper.toDomainEntity(form);
        saveJobScheduleUseCase.execute(jobForm, createSubmitSubscriber());
    }

    @Override
    public void bind(Callback callbacks) {
        super.bind(callbacks);
        saveJobScheduleUseCase.subscribe(createSubmitSubscriber());
    }

    @Override
    public void unbind() {
        super.unbind();
        saveJobScheduleUseCase.unsubscribe();
    }

    private Subscriber<? super Void> createSubmitSubscriber() {
        return new Subscriber<Void>() {
            @Override
            public void onError(Throwable e) {
                mCallbacks.onFormSubmitError(e);
            }

            @Override
            public void onNext(Void jobData) {
                mCallbacks.onFormSubmitSuccess();
            }

            @Override
            public void onCompleted() {
                logJobCreatedEvent();
            }
        };
    }

    private void logJobCreatedEvent() {
        mAnalytics.sendEvent(
                Analytics.EventCategory.JOB.getValue(),
                Analytics.EventAction.ADDED.getValue(),
                null);
    }
}
