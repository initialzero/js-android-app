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
import com.jaspersoft.android.jaspermobile.data.cache.profile.CredentialsCache;
import com.jaspersoft.android.jaspermobile.data.cache.report.ReportParamsCache;
import com.jaspersoft.android.jaspermobile.data.entity.mapper.ReportParamsMapper;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.ui.model.visualize.VisualizeExecOptions;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Collections;
import java.util.List;

import rx.observers.TestSubscriber;

import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class GetVisualizeExecOptionsCaseTest {
    private static final String REPORT_URI = "/my/uri";
    private static final String EMPTY_JSON = "{}";
    private static final List<ReportParameter> REPORT_PARAMS = Collections.emptyList();

    @Mock
    ReportParamsCache mReportParamsCache;
    @Mock
    ReportParamsMapper mReportParamsMapper;
    @Mock
    Profile profile;
    @Mock
    CredentialsCache credentialsCache;

    private GetVisualizeExecOptionsCase mGetVisualizeExecOptionsCase;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        setUpMocks();
        mGetVisualizeExecOptionsCase = new GetVisualizeExecOptionsCase(
                FakePreExecutionThread.create(),
                FakePostExecutionThread.create(),
                profile,
                credentialsCache,
                mReportParamsCache,
                mReportParamsMapper
        );
    }

    private void setUpMocks() {
        when(mReportParamsCache.get(anyString())).thenReturn(REPORT_PARAMS);
        when(mReportParamsMapper.legacyParamsToJson(anyListOf(ReportParameter.class))).thenReturn(EMPTY_JSON);
    }

    @Test
    public void testBuildUseCaseObservable() throws Exception {
        TestSubscriber<VisualizeExecOptions.Builder> test = new TestSubscriber<>();
        mGetVisualizeExecOptionsCase.execute(REPORT_URI, test);

        verify(mReportParamsCache).get(REPORT_URI);
        verify(mReportParamsMapper).legacyParamsToJson(REPORT_PARAMS);
    }
}