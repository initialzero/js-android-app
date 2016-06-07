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

package com.jaspersoft.android.jaspermobile.internal.di.modules.screen.job;

import com.jaspersoft.android.jaspermobile.Analytics;
import com.jaspersoft.android.jaspermobile.domain.entity.job.JobScheduleBundle;
import com.jaspersoft.android.jaspermobile.domain.interactor.schedule.GetJobScheduleUseCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.schedule.SaveJobScheduleUseCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.schedule.UpdateJobScheduleUseCase;
import com.jaspersoft.android.jaspermobile.internal.di.PerScreen;
import com.jaspersoft.android.jaspermobile.ui.contract.ScheduleFormContract;
import com.jaspersoft.android.jaspermobile.ui.entity.job.JobFormViewBundle;
import com.jaspersoft.android.jaspermobile.ui.mapper.UiEntityMapper;
import com.jaspersoft.android.jaspermobile.ui.model.ScheduleCreateModel;
import com.jaspersoft.android.jaspermobile.ui.model.ScheduleUpdateModel;
import com.jaspersoft.android.jaspermobile.util.resource.JasperResource;

import dagger.Module;
import dagger.Provides;

/**
 * @author Tom Koptel
 * @since 2.5
 */
@Module
public class JobFormScreenModule extends JobScreenBaseModule {
    private static final int NO_ID = Integer.MIN_VALUE;

    private final JasperResource mResource;
    private final int mJobId;

    public JobFormScreenModule(JasperResource resource) {
        mResource = resource;
        mJobId = NO_ID;
    }

    public JobFormScreenModule(int id) {
        mResource = null;
        mJobId = id;
    }

    @Provides
    @PerScreen
    ScheduleFormContract.Model providesModel(
            Analytics analytics,
            GetJobScheduleUseCase getJobScheduleUseCase,
            UpdateJobScheduleUseCase updateJobScheduleUseCase,
            SaveJobScheduleUseCase saveJobScheduleUseCase,
            UiEntityMapper<JobScheduleBundle, JobFormViewBundle> formMapper,
            UiEntityMapper<JasperResource, JobFormViewBundle> resourceMapper
    ) {
        if (mResource == null) {
            return new ScheduleUpdateModel(mJobId, formMapper, getJobScheduleUseCase, updateJobScheduleUseCase, analytics);
        }
        JobFormViewBundle viewBundle = resourceMapper.toUiEntity(mResource);
        return new ScheduleCreateModel(viewBundle, formMapper, saveJobScheduleUseCase, analytics);
    }
}
