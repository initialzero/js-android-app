/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.domain.interactor;

import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.repository.ReportRepository;

import rx.Observable;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public final class GetReportTotalPagesCase extends AbstractSimpleUseCase<Integer> {
    private final ReportRepository mReportRepository;

    public GetReportTotalPagesCase(PreExecutionThread preExecutionThread,
                                   PostExecutionThread postExecutionThread,
                                   ReportRepository reportRepository) {
        super(preExecutionThread, postExecutionThread);
        mReportRepository = reportRepository;
    }

    @Override
    protected Observable<Integer> buildUseCaseObservable() {
        return mReportRepository.getTotalPages();
    }
}
