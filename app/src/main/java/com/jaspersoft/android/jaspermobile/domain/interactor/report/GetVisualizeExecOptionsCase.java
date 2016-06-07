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

package com.jaspersoft.android.jaspermobile.domain.interactor.report;

import com.jaspersoft.android.jaspermobile.data.cache.profile.CredentialsCache;
import com.jaspersoft.android.jaspermobile.data.cache.report.ReportParamsCache;
import com.jaspersoft.android.jaspermobile.data.entity.mapper.ReportParamsMapper;
import com.jaspersoft.android.jaspermobile.domain.AppCredentials;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.interactor.AbstractUseCase;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.jaspermobile.ui.model.visualize.VisualizeExecOptions;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func0;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public class GetVisualizeExecOptionsCase extends AbstractUseCase<VisualizeExecOptions.Builder, String> {

    private final Profile mProfile;
    private final CredentialsCache mCredentialsCache;
    private final ReportParamsCache mReportParamsCache;
    private final ReportParamsMapper mReportParamsMapper;

    @Inject
    public GetVisualizeExecOptionsCase(PreExecutionThread preExecutionThread,
                                       PostExecutionThread postExecutionThread,
                                       Profile profile,
                                       CredentialsCache credentialsCache,
                                       ReportParamsCache reportParamsCache,
                                       ReportParamsMapper reportParamsMapper
    ) {
        super(preExecutionThread, postExecutionThread);
        mProfile = profile;
        mCredentialsCache = credentialsCache;
        mReportParamsCache = reportParamsCache;
        mReportParamsMapper = reportParamsMapper;
    }

    @Override
    protected Observable<VisualizeExecOptions.Builder> buildUseCaseObservable(@NotNull final String reportUri) {
        return Observable.defer(new Func0<Observable<VisualizeExecOptions.Builder>>() {
            @Override
            public Observable<VisualizeExecOptions.Builder> call() {
                AppCredentials credentials = mCredentialsCache.get(mProfile);
                List<ReportParameter> reportParameters = mReportParamsCache.get(reportUri);
                String jsonParams = mReportParamsMapper.legacyParamsToJson(reportParameters);
                VisualizeExecOptions.Builder builder = new VisualizeExecOptions.Builder()
                        .setUri(reportUri)
                        .setAppCredentials(credentials)
                        .setParams(jsonParams);
                return Observable.just(builder);
            }
        });
    }
}
