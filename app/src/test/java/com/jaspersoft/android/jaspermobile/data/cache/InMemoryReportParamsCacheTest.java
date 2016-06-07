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

package com.jaspersoft.android.jaspermobile.data.cache;

import com.jaspersoft.android.jaspermobile.data.cache.report.InMemoryReportParamsCache;
import com.jaspersoft.android.jaspermobile.util.InputControlHolder;
import com.jaspersoft.android.jaspermobile.util.ReportParamsStorage;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class InMemoryReportParamsCacheTest {

    private static final List<com.jaspersoft.android.sdk.client.oxm.report.ReportParameter> LEGACY_REPORT_PARAMS = Collections.singletonList(null);
    private static final String REPORT_URI = "/my/uri";

    @Mock
    ReportParamsStorage mParamsStorage;
    @Mock
    InputControlHolder mInputControlHolder;


    private InMemoryReportParamsCache mCache;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        setupMocks();
        mCache = new InMemoryReportParamsCache(mParamsStorage);
    }

    @Test
    public void testPut() throws Exception {
        mCache.put(REPORT_URI, LEGACY_REPORT_PARAMS);
        verify(mParamsStorage).getInputControlHolder(REPORT_URI);
        verify(mInputControlHolder).setReportParams(LEGACY_REPORT_PARAMS);
    }

    @Test
    public void testGet() throws Exception {
        mCache.get(REPORT_URI);
        verify(mParamsStorage).getInputControlHolder(REPORT_URI);
        verify(mInputControlHolder).getReportParams();
    }

    @Test
    public void should_evict_collection_from_holder() throws Exception {
        mCache.evict(REPORT_URI);
        verify(mParamsStorage).clearInputControlHolder(REPORT_URI);
    }

    private void setupMocks() {
        when(mParamsStorage.getInputControlHolder(anyString())).thenReturn(mInputControlHolder);
        when(mInputControlHolder.getReportParams()).thenReturn(LEGACY_REPORT_PARAMS);
    }
}