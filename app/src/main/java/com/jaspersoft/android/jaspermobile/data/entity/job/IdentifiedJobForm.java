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

package com.jaspersoft.android.jaspermobile.data.entity.job;

import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;
import com.jaspersoft.android.sdk.service.data.schedule.JobForm;

/**
 * @author Tom Koptel
 * @since 2.5
 */
@AutoValue
public abstract class IdentifiedJobForm {
    public abstract int id();

    @NonNull
    public abstract JobForm form();

    public static IdentifiedJobForm create(int id, JobForm form) {
        return new AutoValue_IdentifiedJobForm(id, form);
    }
}
