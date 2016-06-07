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

import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public final class InMemoryReportPropertyCache implements ReportPropertyCache {
    private final Map<String, Holder> mStorage = new HashMap<>();

    @Inject
    public InMemoryReportPropertyCache() {
    }

    @Override
    public void putTotalPages(String reportUri, int totalPages) {
        Holder holder = mStorage.get(reportUri);
        if (holder == null) {
            holder = new Holder();
            mStorage.put(reportUri, holder);
        }
        holder.pages = totalPages;
    }

    @Override
    public Integer getTotalPages(String reportUri) {
        Holder holder = mStorage.get(reportUri);
        if (holder == null) {
            return null;
        }
        return holder.pages;
    }

    @Override
    public void evict(String reportUri) {
        mStorage.remove(reportUri);
    }

    private static class Holder {
        Integer pages;
    }
}
