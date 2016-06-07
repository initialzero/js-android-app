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

package com.jaspersoft.android.jaspermobile.ui.navigation;

import android.content.Context;
import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.domain.entity.job.JobResource;
import com.jaspersoft.android.jaspermobile.internal.di.ActivityContext;
import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;
import com.jaspersoft.android.jaspermobile.util.resource.JasperResource;

import javax.inject.Inject;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerActivity
public class PageFactory {
    private final Context mContext;

    @Inject
    public PageFactory(@ActivityContext Context context) {
        mContext = context;
    }

    @NonNull
    public Page createMainPage() {
        return new MainPage(mContext);
    }

    @NonNull
    public Page createSignUpPage() {
        return new SignUpPage(mContext);
    }

    @NonNull
    public Page createChooseJobPage() {
        return new ChooseJobPage(mContext);
    }

    @NonNull
    public Page createJobEditPage(int jobId) {
        return new EditJobPage(mContext, jobId);
    }

    @NonNull
    public Page createNewJobPage(JasperResource jasperResource) {
        return new CreateJobPage(mContext, jasperResource);
    }

    @NonNull
    public Page createJobInfoPage(JobResource job) {
        return new JobInfoPage(mContext, job);
    }
}
