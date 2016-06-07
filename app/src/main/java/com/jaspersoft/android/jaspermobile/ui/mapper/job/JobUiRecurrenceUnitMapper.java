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

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.jaspersoft.android.jaspermobile.domain.entity.job.JobSimpleRecurrence;
import com.jaspersoft.android.jaspermobile.ui.entity.job.SimpleViewRecurrence;
import com.jaspersoft.android.jaspermobile.ui.mapper.EntityLocalizer;
import com.jaspersoft.android.jaspermobile.ui.mapper.UiCollectionEntityMapper;

/**
 * @author Tom Koptel
 * @since 2.5
 */
final class JobUiRecurrenceUnitMapper extends UiCollectionEntityMapper<JobSimpleRecurrence.Unit, SimpleViewRecurrence.Unit> {

    private final EntityLocalizer<JobSimpleRecurrence.Unit> entityLocalizer;

    @VisibleForTesting
    JobUiRecurrenceUnitMapper(EntityLocalizer<JobSimpleRecurrence.Unit> entityLocalizer) {
        this.entityLocalizer = entityLocalizer;
    }

    @NonNull
    public static JobUiRecurrenceUnitMapper create(@NonNull Context context) {
        IntervalUnitLocalizer unitLocalizer = new IntervalUnitLocalizer(context);
        return new JobUiRecurrenceUnitMapper(unitLocalizer);
    }

    @NonNull
    @Override
    public SimpleViewRecurrence.Unit toUiEntity(@NonNull JobSimpleRecurrence.Unit unit) {
        String localizedLabel = entityLocalizer.localize(unit);
        return SimpleViewRecurrence.Unit.create(unit.name(), localizedLabel);
    }

    @NonNull
    @Override
    public JobSimpleRecurrence.Unit toDomainEntity(@NonNull SimpleViewRecurrence.Unit domainEntity) {
        return JobSimpleRecurrence.Unit.valueOf(domainEntity.rawValue());
    }
}
