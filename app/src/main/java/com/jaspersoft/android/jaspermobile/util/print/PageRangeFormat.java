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

package com.jaspersoft.android.jaspermobile.util.print;

import android.print.PageRange;

/**
 * @author Tom Koptel
 * @since 2.1
 */
final class PageRangeFormat {

    private final int mTotalPages;

    public PageRangeFormat(int totalPages) {
        if (totalPages == 0) {
            throw new IllegalArgumentException("Total pages should not be zero");
        }
        if (totalPages < 0) {
            throw new IllegalArgumentException("Total pages should not be less than zero");
        }
        mTotalPages = totalPages;
    }

    /**
     * Adapts range which is manageable by our REST service
     *
     * @param pageRange see {@link PageRange}
     * @return '1-10' for range, '1-${mTotalPages}' for all, 1 for one page
     */
    public String format(PageRange pageRange) {
        int start;
        int end;

        boolean rangeExceedsTotalPages = (pageRange.getEnd() > mTotalPages);
        if (rangeExceedsTotalPages) {
            start = 1;
            end = mTotalPages;
        } else {
            start = pageRange.getStart() + 1;
            end = pageRange.getEnd() + 1;
        }

        boolean isSinglePage = (start == end);
        if (isSinglePage) {
            return String.valueOf(start);
        } else {
            // By default returns simple range incremented by 1
            return String.format("%d-%d", start, end);
        }
    }

}
