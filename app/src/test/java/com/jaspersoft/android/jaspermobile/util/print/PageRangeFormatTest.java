/*
 * Copyright � 2015 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of Jaspersoft Mobile for Android.
 *
 * Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.util.print;

import android.print.PageRange;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author Tom Koptel
 * @since 2.1
 */
@RunWith(JUnitParamsRunner.class)
public class PageRangeFormatTest {
    private static final int MAX_TOTAL_PAGE = 1000;

    @Mock
    PageRange pageRange;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAcceptZeroTotalPages() {
        new PageRangeFormat(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAcceptNegativeTotalPages() {
        new PageRangeFormat(-1);
    }

    @Test
    @Parameters({"0|10", "5|300"})
    public void shouldAdaptRangeForRange(String startString, String endString) {
        int start = Integer.valueOf(startString);
        int end = Integer.valueOf(endString);
        when(pageRange.getStart()).thenReturn(start);
        when(pageRange.getEnd()).thenReturn(end);

        String range = new PageRangeFormat(MAX_TOTAL_PAGE).format(pageRange);

        assertThat(range, is(String.format("%d-%d", start + 1, end + 1)));
    }

    @Test
    @Parameters({"0|0|10|1", "0|0|1|1", "0|" + Integer.MAX_VALUE + "|1|1", "1|1|10|2", "2|2|10|3"})
    public void shouldAdaptRangeToSinglePage(String start, String end, String totalPages, String resultPage) {
        when(pageRange.getStart()).thenReturn(Integer.valueOf(start));
        when(pageRange.getEnd()).thenReturn(Integer.valueOf(end));

        String range = new PageRangeFormat(Integer.valueOf(totalPages)).format(pageRange);

        assertThat(range, is(resultPage));
    }

    @Test
    public void shouldAdaptRangeForAllPages() {
        // Simulate all pages
        when(pageRange.getStart()).thenReturn(0);
        when(pageRange.getEnd()).thenReturn(Integer.MAX_VALUE);

        String range = new PageRangeFormat(MAX_TOTAL_PAGE).format(pageRange);

        assertThat(range, is("1-1000"));
    }

}
