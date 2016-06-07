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

package com.jaspersoft.android.jaspermobile.data.repository;

import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.data.cache.report.ReportPageCache;
import com.jaspersoft.android.jaspermobile.data.cache.report.ReportPropertyCache;
import com.jaspersoft.android.jaspermobile.data.repository.report.InMemoryReportPropertyRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ReportPageRepository;
import com.jaspersoft.android.sdk.service.data.report.ReportMetadata;
import com.jaspersoft.android.sdk.service.rx.report.RxReportExecution;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import rx.Observable;
import rx.observers.TestSubscriber;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class InMemoryReportPropertyRepositoryTest {

    private static final String REPORT_URI = "/my/uri";
    private static final int TOTAL_PAGES = 100;

    @Mock
    RxReportExecution mReportExecution;
    @Mock
    ReportPropertyCache mReportPropertyCache;
    @Mock
    ReportPageCache mReportPageCache;

    @Mock
    ReportPageRepository mReportPageRepository;

    private InMemoryReportPropertyRepository mInMemoryReportPropertyRepository;
    private final ReportMetadata fakeReportMetadata = new ReportMetadata(REPORT_URI, TOTAL_PAGES);

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        setupMocks();
        mInMemoryReportPropertyRepository = new InMemoryReportPropertyRepository(
                mReportPropertyCache
        );
    }

    @Test
    public void should_perform_additional_import_if_total_pages_property_missing() throws Exception {
        when(mReportPropertyCache.getTotalPages(anyString())).thenReturn(null);

        TestSubscriber<Integer> test = getTotalPagesProperty();

        test.assertNoErrors();
        assertThat(test.getOnNextEvents(), hasItem(TOTAL_PAGES));

        verify(mReportExecution).waitForReportCompletion();
        verify(mReportPropertyCache).putTotalPages(REPORT_URI, 100);
    }

    @Test
    public void should_return_cached_value_total_pages() throws Exception {
        when(mReportPropertyCache.getTotalPages(anyString())).thenReturn(200);

        TestSubscriber<Integer> test = getTotalPagesProperty();

        test.assertNoErrors();
        assertThat(test.getOnNextEvents(), hasItem(200));

        verifyZeroInteractions(mReportExecution);
    }

    @NonNull
    private TestSubscriber<Integer> getTotalPagesProperty() {
        TestSubscriber<Integer> test = new TestSubscriber<>();
        mInMemoryReportPropertyRepository.getTotalPagesProperty(mReportExecution, REPORT_URI).subscribe(test);
        return test;
    }

    private void setupMocks() {
        when(mReportExecution.waitForReportCompletion()).thenReturn(Observable.just(fakeReportMetadata));
    }
}