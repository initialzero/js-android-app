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

package com.jaspersoft.android.jaspermobile.ui.presenter;

import android.support.annotation.VisibleForTesting;

import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.ProfileForm;
import com.jaspersoft.android.jaspermobile.domain.interactor.profile.DemoProfileExistsUseCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.profile.SaveProfileUseCase;
import com.jaspersoft.android.jaspermobile.domain.repository.exception.FailedToSaveCredentials;
import com.jaspersoft.android.jaspermobile.domain.repository.exception.FailedToSaveProfile;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.DuplicateProfileException;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.ProfileReservedException;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.ServerVersionNotSupportedException;
import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;
import com.jaspersoft.android.jaspermobile.network.RequestExceptionHandler;
import com.jaspersoft.android.jaspermobile.ui.contract.AuthenticationContract;
import com.jaspersoft.android.jaspermobile.ui.page.AuthPageState;
import com.jaspersoft.android.jaspermobile.ui.validation.AliasMissingException;
import com.jaspersoft.android.jaspermobile.ui.validation.PasswordMissingException;
import com.jaspersoft.android.jaspermobile.ui.validation.ProfileFormValidation;
import com.jaspersoft.android.jaspermobile.ui.validation.ServerUrlFormatException;
import com.jaspersoft.android.jaspermobile.ui.validation.ServerUrlMissingException;
import com.jaspersoft.android.jaspermobile.ui.validation.UsernameMissingException;
import com.jaspersoft.android.sdk.service.exception.ServiceException;

import javax.inject.Inject;

import rx.Subscriber;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerActivity
public final class AuthenticationPresenter extends LegacyPresenter<AuthenticationContract.View> implements AuthenticationContract.ActionListener {

    private final SaveProfileUseCase mSaveProfileUseCaseUseCase;
    private final ProfileFormValidation mProfileFormValidation;
    private final RequestExceptionHandler mRequestExceptionHandler;
    private final DemoProfileExistsUseCase mDemoProfileExistsUseCase;

    @Inject
    public AuthenticationPresenter(
            SaveProfileUseCase saveProfileUseCaseUseCase,
            ProfileFormValidation profileFormValidation,
            RequestExceptionHandler requestExceptionHandler,
            DemoProfileExistsUseCase demoProfileExistsUseCase
    ) {
        mSaveProfileUseCaseUseCase = saveProfileUseCaseUseCase;
        mProfileFormValidation = profileFormValidation;
        mRequestExceptionHandler = requestExceptionHandler;
        mDemoProfileExistsUseCase = demoProfileExistsUseCase;
    }

    @Override
    public void resume() {
        AuthPageState state = getView().getState();
        if (state.isLoading()) {
            getView().showLoading();
        } else {
            getView().hideLoading();
        }
    }

    @Override
    public void pause() {
        getView().hideLoading();
    }

    @Override
    public void destroy() {
        mSaveProfileUseCaseUseCase.unsubscribe();
    }

    @Override
    public void checkDemoAccountAvailability() {
        boolean hideTryDemo = !mDemoProfileExistsUseCase.execute();
        getView().showTryDemo(hideTryDemo);
    }

    @Override
    public void saveProfile(ProfileForm profileForm) {
        if (isClientDataValid(profileForm)) {
            getView().showLoading();
            mSaveProfileUseCaseUseCase.execute(profileForm, new ProfileSaveListener());
        }
    }

    private boolean isClientDataValid(ProfileForm form) {
        try {
            mProfileFormValidation.validate(form);
            return true;
        } catch (UsernameMissingException e) {
            getView().showUsernameRequiredError();
        } catch (PasswordMissingException e) {
            getView().showPasswordRequiredError();
        } catch (AliasMissingException e) {
            getView().showAliasRequiredError();
        } catch (ServerUrlMissingException e) {
            getView().showServerUrlRequiredError();
        } catch (ServerUrlFormatException e) {
            getView().showServerUrlFormatError();
        }
        return false;
    }

    @VisibleForTesting
    void handleProfileSaveFailure(Throwable e) {
        getView().hideLoading();
        if (e instanceof DuplicateProfileException) {
            getView().showAliasDuplicateError();
        } else if (e instanceof ProfileReservedException) {
            getView().showAliasReservedError();
        } else if (e instanceof ServerVersionNotSupportedException) {
            getView().showServerVersionNotSupported();
        } else if (e instanceof FailedToSaveProfile) {
            getView().showFailedToAddProfile(e.getMessage());
        } else if (e instanceof FailedToSaveCredentials) {
            getView().showFailedToAddProfile(e.getMessage());
        } else if (e instanceof ServiceException) {
            getView().showError(mRequestExceptionHandler.extractMessage(e));
        } else {
            getView().showError(e.getMessage());
        }
    }

    private void setPageLoadingState(boolean loading) {
        AuthPageState state = getView().getState();
        state.setLoading(loading);
    }

    private class ProfileSaveListener extends Subscriber<Profile> {
        @Override
        public void onStart() {
            setPageLoadingState(true);
        }

        @Override
        public void onCompleted() {
            setPageLoadingState(false);
            getView().hideLoading();
        }

        @Override
        public void onError(Throwable e) {
            setPageLoadingState(false);
            handleProfileSaveFailure(e);
            getView().hideLoading();
        }

        @Override
        public void onNext(Profile profile) {
            getView().navigateToApp(profile);
        }
    }
}
