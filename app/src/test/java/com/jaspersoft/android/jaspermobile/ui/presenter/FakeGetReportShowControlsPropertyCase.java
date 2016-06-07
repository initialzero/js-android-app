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

package com.jaspersoft.android.jaspermobile.ui.presenter;

import com.jaspersoft.android.jaspermobile.FakePostExecutionThread;
import com.jaspersoft.android.jaspermobile.FakePreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.ReportControlFlags;
import com.jaspersoft.android.jaspermobile.domain.interactor.report.GetReportShowControlsPropertyCase;

import rx.Observable;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class FakeGetReportShowControlsPropertyCase extends GetReportShowControlsPropertyCase {
    private ReportControlFlags mFakeResult;

    public FakeGetReportShowControlsPropertyCase() {
        super(FakePreExecutionThread.create(), FakePostExecutionThread.create(), null, null);
    }

    public void setNeedParams(boolean hasControls) {
        mFakeResult = new ReportControlFlags(true, hasControls);
    }

    public void setNeedPrompt(boolean needPrompt) {
        mFakeResult = new ReportControlFlags(needPrompt, false);
    }

    @Override
    protected Observable<ReportControlFlags> buildUseCaseObservable(String reportUri) {
        return Observable.just(mFakeResult);
    }
}