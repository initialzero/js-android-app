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

package com.jaspersoft.android.jaspermobile.data.repository;

import com.jaspersoft.android.jaspermobile.domain.service.ReportExecutionService;
import com.jaspersoft.android.jaspermobile.domain.service.ReportService;
import com.jaspersoft.android.jaspermobile.data.mapper.ReportParamsTransformer;
import com.jaspersoft.android.jaspermobile.util.InputControlHolder;
import com.jaspersoft.android.jaspermobile.util.ReportParamsStorage;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rx.Observable;
import rx.observers.TestSubscriber;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class InMemoryReportRepositoryTest {
    private static final Map<String, Set<String>> FAKE_PARAMS = Collections.emptyMap();
    private static final String FAKE_PAGE = "<html></html>";
    private static final Integer FAKE_TOTAL_PAGES = 1;

    @Mock
    ReportService mReportService;
    @Mock
    ReportExecutionService mReportExecutionService;
    @Mock
    ReportParamsTransformer mReportParamsTransformer;

    @Mock
    ReportParamsStorage mReportParamsStorage;
    @Mock
    InputControlHolder mInputControlHolder;
    @Mock
    List<InputControl> mControls;

    private InMemoryReportRepository repoUnderTest;
    private final String reportUri = "/my/uri";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        repoUnderTest = new InMemoryReportRepository(reportUri, mReportService, mReportParamsStorage, mReportParamsTransformer);

        when(mReportParamsTransformer.transform(anyList())).thenReturn(FAKE_PARAMS);
        when(mReportParamsStorage.getInputControlHolder(anyString())).thenReturn(mInputControlHolder);

        when(mReportService.runReport(anyString(), anyMap())).thenReturn(Observable.just(mReportExecutionService));
        when(mReportService.loadControls(anyString())).thenReturn(Observable.just(mControls));

        when(mReportExecutionService.downloadExport(anyString())).thenReturn(Observable.just(FAKE_PAGE));
        when(mReportExecutionService.loadTotalPages()).thenReturn(Observable.just(FAKE_TOTAL_PAGES));
    }

    @Test
    public void testGetPage() throws Exception {
        TestSubscriber<String> pageSubscriber1 = new TestSubscriber<>();
        TestSubscriber<String> pageSubscriber2 = new TestSubscriber<>();

        repoUnderTest.getPage("1-10").subscribe(pageSubscriber1);

        pageSubscriber1.assertNoErrors();
        assertThat(pageSubscriber1.getOnNextEvents(), hasItem(FAKE_PAGE));

        verify(mReportService).runReport(reportUri, FAKE_PARAMS);
        verify(mReportExecutionService).downloadExport("1-10");

        repoUnderTest.getPage("1-10").subscribe(pageSubscriber2);
        pageSubscriber2.assertNoErrors();
        assertThat(pageSubscriber2.getOnNextEvents(), hasItem(FAKE_PAGE));

        // Should retrieve from memory cache
        verifyNoMoreInteractions(mReportExecutionService);
    }

    @Test
    public void testGetControls() throws Exception {
        TestSubscriber<List<InputControl>> testSubscriber = new TestSubscriber<>();

        repoUnderTest.getControls().subscribe(testSubscriber);
        testSubscriber.assertNoErrors();
        assertThat(testSubscriber.getOnNextEvents(), hasItem(mControls));

        verify(mReportService).loadControls(reportUri);
        verify(mReportParamsStorage).getInputControlHolder(reportUri);
        verify(mInputControlHolder).setInputControls(anyList());
    }

    @Test
    public void testGetTotalPages() throws Exception {
        TestSubscriber<Integer> testSubscriber1 = new TestSubscriber<>();

        repoUnderTest.getTotalPages().subscribe(testSubscriber1);
        testSubscriber1.assertNoErrors();
        assertThat(testSubscriber1.getOnNextEvents(), hasItem(FAKE_TOTAL_PAGES));

        verify(mReportExecutionService).loadTotalPages();
  }
}