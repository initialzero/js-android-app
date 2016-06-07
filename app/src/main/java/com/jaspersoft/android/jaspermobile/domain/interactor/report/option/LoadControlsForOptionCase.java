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

import com.jaspersoft.android.jaspermobile.data.cache.report.ReportParamsCache;
import com.jaspersoft.android.jaspermobile.data.entity.mapper.ReportParamsMapper;
import com.jaspersoft.android.jaspermobile.domain.LoadOptionParamsRequest;
import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.interactor.AbstractUseCase;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ReportOptionsRepository;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlState;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func1;

/**
 * TODO revise interactor after release 2.3. There should be no mentioning of data layer here
 *
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public class LoadControlsForOptionCase extends AbstractUseCase<Void, LoadOptionParamsRequest> {
    private final ReportOptionsRepository mReportOptionsRepository;
    private final ReportParamsCache mReportParamsCache;
    private final ReportParamsMapper mReportParamsMapper;

    @Inject
    public LoadControlsForOptionCase(
            PreExecutionThread preExecutionThread,
            PostExecutionThread postExecutionThread,
            ReportOptionsRepository reportOptionsRepository,
            ReportParamsCache reportParamsCache,
            ReportParamsMapper reportParamsMapper
    ) {
        super(preExecutionThread, postExecutionThread);
        mReportOptionsRepository = reportOptionsRepository;
        mReportParamsCache = reportParamsCache;
        mReportParamsMapper = reportParamsMapper;
    }

    @Override
    protected Observable<Void> buildUseCaseObservable(final LoadOptionParamsRequest request) {
        return mReportOptionsRepository.getReportOptionStates(request.getOptionUri())
                .flatMap(new Func1<List<InputControlState>, Observable<Void>>() {
                    @Override
                    public Observable<Void> call(List<InputControlState> states) {
                        List<ReportParameter> parameters = mReportParamsMapper.mapStatesToLegacyParams(states);
                        mReportParamsCache.put(request.getReportUri(), parameters);
                        return Observable.just(null);
                    }
                });
    }
}
