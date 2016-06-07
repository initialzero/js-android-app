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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.jaspermobile.util.ReportParamsStorage;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;

import java.util.List;

import javax.inject.Inject;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public final class InMemoryControlsCache implements ControlsCache {
    private final ReportParamsStorage mParamsStorage;

    @Inject
    public InMemoryControlsCache(ReportParamsStorage paramsStorage) {
        mParamsStorage = paramsStorage;
    }

    @NonNull
    @Override
    public List<InputControl> put(@NonNull String uri, @NonNull List<InputControl> controls) {
        mParamsStorage.getInputControlHolder(uri).setInputControls(controls);
        return controls;
    }

    @Nullable
    @Override
    public List<InputControl> get(@NonNull String uri) {
        return mParamsStorage.getInputControlHolder(uri).getInputControls();
    }

    @Override
    public void evict(String reportUri) {
        mParamsStorage.clearInputControlHolder(reportUri);
    }
}
