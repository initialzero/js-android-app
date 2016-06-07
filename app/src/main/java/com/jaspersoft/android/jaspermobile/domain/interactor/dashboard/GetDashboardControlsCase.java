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

package com.jaspersoft.android.jaspermobile.domain.interactor.dashboard;

import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.interactor.AbstractUseCase;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ControlsRepository;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func1;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public class GetDashboardControlsCase extends AbstractUseCase<Boolean, String> {
    private final ControlsRepository mControlsRepository;

    @Inject
    public GetDashboardControlsCase(PreExecutionThread preExecutionThread,
                                             PostExecutionThread postExecutionThread,
                                             ControlsRepository controlsRepository) {
        super(preExecutionThread, postExecutionThread);
        mControlsRepository = controlsRepository;
    }

    @Override
    protected Observable<Boolean> buildUseCaseObservable(@NonNull final String reportUri) {
        return mControlsRepository.listDashboardControls(reportUri)
                .map(new Func1<List<InputControl>, Boolean>() {
                    @Override
                    public Boolean call(List<InputControl> inputControls) {
                        return !inputControls.isEmpty();
                    }
                });
    }
}
