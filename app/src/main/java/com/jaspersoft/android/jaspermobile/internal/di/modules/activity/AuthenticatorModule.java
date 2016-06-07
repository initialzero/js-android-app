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

package com.jaspersoft.android.jaspermobile.internal.di.modules.activity;

import com.jaspersoft.android.jaspermobile.data.validator.ProfileAuthorizedValidation;
import com.jaspersoft.android.jaspermobile.data.validator.ProfileReservedValidation;
import com.jaspersoft.android.jaspermobile.data.validator.ProfileUniquenessValidation;
import com.jaspersoft.android.jaspermobile.data.validator.ServerVersionValidation;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.ProfileForm;
import com.jaspersoft.android.jaspermobile.domain.validator.ValidationRule;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.DuplicateProfileException;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.ProfileReservedException;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.ServerVersionNotSupportedException;
import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;
import com.jaspersoft.android.jaspermobile.ui.contract.AuthenticationContract;
import com.jaspersoft.android.jaspermobile.ui.presenter.AuthenticationPresenter;

import dagger.Module;
import dagger.Provides;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@Module
public final class AuthenticatorModule {
    @PerActivity
    @Provides
    AuthenticationContract.ActionListener provideActionListener(AuthenticationPresenter presenter) {
        return presenter;
    }

    @PerActivity
    @Provides
    ValidationRule<JasperServer, ServerVersionNotSupportedException> providesVersionValidation(ServerVersionValidation validation) {
        return validation;
    }

    @PerActivity
    @Provides
    ValidationRule<Profile, DuplicateProfileException> providesDuplicateProfileValidation(ProfileUniquenessValidation validation) {
        return validation;
    }

    @PerActivity
    @Provides
    ValidationRule<Profile, ProfileReservedException> providesReservedProfileValidation(ProfileReservedValidation validation) {
        return validation;
    }

    @PerActivity
    @Provides
    ValidationRule<ProfileForm, Exception> providesProfileAuthorizedValidation(ProfileAuthorizedValidation validation) {
        return validation;
    }
}
