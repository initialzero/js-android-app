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

import com.jaspersoft.android.jaspermobile.FakePostExecutionThread;
import com.jaspersoft.android.jaspermobile.FakePreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.PageRequest;
import com.jaspersoft.android.jaspermobile.domain.ReportPage;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ReportPageRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ReportRepository;
import com.jaspersoft.android.sdk.service.rx.report.RxReportExecution;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import rx.Observable;
import rx.observers.TestSubscriber;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class RunReportCaseTest {

    private static final String REPORT_URI = "/my/uri";
    private static final String PAGE_POSITION = "1";
    private static final PageRequest FIRST_PAGE_REQUEST = new PageRequest.Builder()
            .setUri(REPORT_URI)
            .setRange(PAGE_POSITION)
            .asHtml()
            .build();
    private static final ReportPage ANY_PAGE = new ReportPage("page".getBytes(), true);

    @Mock
    ReportRepository mReportRepository;
    @Mock
    ReportPageRepository mReportPageRepository;
    @Mock
    RxReportExecution mRxReportExecution;

    private RunReportCase mRunReportCase;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        mRunReportCase = new RunReportCase(
                FakePreExecutionThread.create(),
                FakePostExecutionThread.create(),
                mReportRepository,
                mReportPageRepository
        );
    }

    @Test
    public void testBuildUseCaseObservable() throws Exception {
        when(mReportRepository.getReport(anyString())).thenReturn(Observable.just(mRxReportExecution));
        when(mReportPageRepository.get(any(RxReportExecution.class), any(PageRequest.class))).thenReturn(Observable.just(ANY_PAGE));

        TestSubscriber<ReportPage> test = new TestSubscriber<>();
        mRunReportCase.execute(REPORT_URI, test);

        verify(mReportRepository).getReport(REPORT_URI);
        verify(mReportPageRepository).get(mRxReportExecution, FIRST_PAGE_REQUEST);
    }
}