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

import com.google.gson.Gson;
import com.jaspersoft.android.jaspermobile.data.cache.report.ReportParamsCache;
import com.jaspersoft.android.jaspermobile.data.entity.ReportData;
import com.jaspersoft.android.jaspermobile.data.entity.mapper.ReportParamsMapper;
import com.jaspersoft.android.jaspermobile.data.entity.mapper.ResourceMapper;
import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.interactor.AbstractUseCase;
import com.jaspersoft.android.jaspermobile.domain.repository.resource.ResourceRepository;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.service.data.repository.Resource;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;

/**
 * TODO: remove data package related dependencies
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public class GetReportMetadataCase extends AbstractUseCase<ResourceLookup, String> {

    private final ResourceRepository mResourceRepository;
    private final ReportParamsCache mReportParamsCache;
    private final ReportParamsMapper mReportParamsMapper;
    private final ResourceMapper mResourceMapper;

    @Inject
    public GetReportMetadataCase(
            PreExecutionThread preExecutionThread,
            PostExecutionThread postExecutionThread,
            ResourceRepository resourceRepository,
            ReportParamsCache reportParamsCache,
            ReportParamsMapper reportParamsMapper,
            ResourceMapper resourceMapper) {
        super(preExecutionThread, postExecutionThread);
        mResourceRepository = resourceRepository;
        mReportParamsCache = reportParamsCache;
        mReportParamsMapper = reportParamsMapper;
        mResourceMapper = resourceMapper;
    }

    @Override
    protected Observable<ResourceLookup> buildUseCaseObservable(final String data) {
        return Observable.defer(new Func0<Observable<Resource>>() {
            @Override
            public Observable<Resource> call() {
                ReportData reportData = new Gson().fromJson(data, ReportData.class);
                List<ReportParameter> reportParameters =
                        mReportParamsMapper.mapToLegacyParams(reportData.getParams());
                String uri = reportData.getResource();
                mReportParamsCache.put(uri, reportParameters);
                return mResourceRepository.getResourceByType(uri, "reportUnit");
            }
        }).map(new Func1<Resource, ResourceLookup>(){
            @Override
            public ResourceLookup call(Resource resource) {
                ResourceLookup lookup = new ResourceLookup();
                mResourceMapper.toLegacyResource(resource, lookup);
                return lookup;
            }
        });
    }
}
