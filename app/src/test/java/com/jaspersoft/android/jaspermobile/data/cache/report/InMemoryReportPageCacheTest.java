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

package com.jaspersoft.android.jaspermobile.data.cache.report;

import com.jaspersoft.android.jaspermobile.domain.PageRequest;
import com.jaspersoft.android.jaspermobile.domain.ReportPage;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class InMemoryReportPageCacheTest {
    private static final String REPORT_URI = "/my/uri";
    private static final String PAGE_POSITION = "100";
    private static final PageRequest PAGE_REQUEST = new PageRequest.Builder()
            .setUri(REPORT_URI)
            .setRange(PAGE_POSITION)
            .asHtml()
            .build();
    private static final ReportPage ANY_PAGE = new ReportPage("page".getBytes(), true);

    private InMemoryReportPageCache mInMemoryReportPageCache;

    @Before
    public void setUp() throws Exception {
        mInMemoryReportPageCache = new InMemoryReportPageCache();
    }

    @Test
    public void should_effectively_persist() throws Exception {
        mInMemoryReportPageCache.put(PAGE_REQUEST, ANY_PAGE);
        ReportPage reportPage = mInMemoryReportPageCache.get(PAGE_REQUEST);
        assertThat(reportPage, is(ANY_PAGE));
        mInMemoryReportPageCache.evictAll();
        ReportPage reportPage1 = mInMemoryReportPageCache.get(PAGE_REQUEST);
        assertThat(reportPage1, is(nullValue()));
    }
}