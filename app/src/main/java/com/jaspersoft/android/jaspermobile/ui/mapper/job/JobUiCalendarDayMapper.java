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

package com.jaspersoft.android.jaspermobile.ui.mapper.job;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.jaspersoft.android.jaspermobile.ui.entity.job.CalendarViewRecurrence;
import com.jaspersoft.android.jaspermobile.ui.mapper.EntityLocalizer;
import com.jaspersoft.android.jaspermobile.ui.mapper.UiCollectionEntityMapper;

/**
 * @author Tom Koptel
 * @since 2.5
 */
final class JobUiCalendarDayMapper extends UiCollectionEntityMapper<Integer, CalendarViewRecurrence.Day> {
    @NonNull
    private final EntityLocalizer<Integer> localizer;

    @VisibleForTesting
    JobUiCalendarDayMapper(@NonNull EntityLocalizer<Integer> localizer) {
        this.localizer = localizer;
    }

    @NonNull
    public static JobUiCalendarDayMapper create() {
        DayLocalizer dayLocalizer = new DayLocalizer();
        return new JobUiCalendarDayMapper(dayLocalizer);
    }

    @NonNull
    @Override
    public CalendarViewRecurrence.Day toUiEntity(@NonNull Integer domainEntity) {
        String localize = localizer.localize(domainEntity);
        return CalendarViewRecurrence.Day.create(localize, domainEntity);
    }

    @NonNull
    @Override
    public Integer toDomainEntity(@NonNull CalendarViewRecurrence.Day uiEntity) {
        return uiEntity.rawValue();
    }
}
