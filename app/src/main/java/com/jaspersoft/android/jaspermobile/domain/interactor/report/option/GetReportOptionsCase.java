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

package com.jaspersoft.android.jaspermobile.domain.interactor.report.option;

import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.interactor.AbstractUseCase;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ReportOptionsRepository;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.sdk.service.data.report.option.ReportOption;

import java.util.Set;

import javax.inject.Inject;

import rx.Observable;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public class GetReportOptionsCase extends AbstractUseCase<Set<ReportOption>, String> {
    private final ReportOptionsRepository mReportOptionsRepository;

    @Inject
    public GetReportOptionsCase(
            PreExecutionThread preExecutionThread,
            PostExecutionThread postExecutionThread,
            ReportOptionsRepository reportOptionsRepository) {
        super(preExecutionThread, postExecutionThread);
        mReportOptionsRepository = reportOptionsRepository;
    }

    @Override
    protected Observable<Set<ReportOption>> buildUseCaseObservable(@NonNull String reportUri) {
        return mReportOptionsRepository.getReportOption(reportUri);
    }
}
