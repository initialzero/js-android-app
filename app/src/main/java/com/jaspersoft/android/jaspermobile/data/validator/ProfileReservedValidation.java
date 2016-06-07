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

package com.jaspersoft.android.jaspermobile.data.validator;

import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.validator.ValidationRule;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.ProfileReservedException;
import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerActivity
public final class ProfileReservedValidation implements ValidationRule<Profile, ProfileReservedException> {

    private final String mReservedName;

    @Inject
    public ProfileReservedValidation(@Named("reserved_account_name") String reservedName) {
        mReservedName = reservedName;
    }

    @Override
    public void validate(@NonNull Profile profile) throws ProfileReservedException {
        final String profileName = profile.getKey();
        if (mReservedName.equals(profileName)) {
            throw new ProfileReservedException();
        }
    }
}
