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

package com.jaspersoft.android.jaspermobile.domain.interactor.schedule;

import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.interactor.AbstractUseCase2;
import com.jaspersoft.android.jaspermobile.domain.repository.schedule.ScheduleRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.schedule.ScheduleSpecification;
import com.jaspersoft.android.jaspermobile.internal.di.PerScreen;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func0;

/**
 * @author Tom Koptel
 * @since 2.5
 */
@PerScreen
public class DeleteJobScheduleUseCase extends AbstractUseCase2<Void, Integer> {
    private ScheduleRepository mScheduleRepository;

    @Inject
    public DeleteJobScheduleUseCase(
            PreExecutionThread preExecutionThread,
            PostExecutionThread postExecutionThread,
            ScheduleRepository scheduleRepository
    ) {
        super(preExecutionThread, postExecutionThread);
        mScheduleRepository = scheduleRepository;
    }

    @Override
    protected Observable<Void> buildUseCaseObservable(final Integer jobId) {
        return Observable.defer(new Func0<Observable<Void>>() {
            @Override
            public Observable<Void> call() {
                try {
                    ScheduleSpecification scheduleSpecification = new ScheduleSpecification(jobId);
                    mScheduleRepository.remove(scheduleSpecification);
                    return Observable.just(null);
                } catch (Exception e) {
                    return Observable.error(e);
                }
            }
        }).cache();
    }
}
