/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.presentation.presenter;

import com.jaspersoft.android.jaspermobile.data.network.RestErrorAdapter;
import com.jaspersoft.android.jaspermobile.domain.AppCredentials;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.ProfileForm;
import com.jaspersoft.android.jaspermobile.domain.interactor.SaveProfileUseCase;
import com.jaspersoft.android.jaspermobile.domain.network.RestErrorCodes;
import com.jaspersoft.android.jaspermobile.domain.network.RestStatusException;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.DuplicateProfileException;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.ProfileReservedException;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.ServerVersionNotSupportedException;
import com.jaspersoft.android.jaspermobile.presentation.validation.AliasMissingException;
import com.jaspersoft.android.jaspermobile.presentation.validation.PasswordMissingException;
import com.jaspersoft.android.jaspermobile.presentation.validation.ProfileFormValidation;
import com.jaspersoft.android.jaspermobile.presentation.validation.ServerUrlFormatException;
import com.jaspersoft.android.jaspermobile.presentation.validation.ServerUrlMissingException;
import com.jaspersoft.android.jaspermobile.presentation.validation.UsernameMissingException;
import com.jaspersoft.android.jaspermobile.presentation.view.AuthenticationView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import rx.Subscriber;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class AuthenticationPresenterTest {

    @Mock
    ProfileFormValidation profileFormValidation;
    @Mock
    RestErrorAdapter mRestErrorAdapter;

    @Mock
    AuthenticationView mAuthenticationView;

    // Domain mock components
    @Mock
    ProfileForm mForm;
    @Mock
    Profile mProfile;
    @Mock
    AppCredentials mCredentials;

    AuthenticationPresenter presenterUnderTest;

    @Mock
    SaveProfileUseCase mSaveProfileUseCase;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        presenterUnderTest = new AuthenticationPresenter(
                RuntimeEnvironment.application,
                mSaveProfileUseCase,
                profileFormValidation,
                mRestErrorAdapter
        );
        presenterUnderTest.setView(mAuthenticationView);

        when(mForm.getProfile()).thenReturn(mProfile);
        when(mForm.getCredentials()).thenReturn(mCredentials);
        when(mForm.getServerUrl()).thenReturn("http://localhost");
    }

    @Test
    public void testSaveProfile() throws Exception {
        presenterUnderTest.saveProfile(mForm);
        verify(mAuthenticationView).showLoading();
        verify(mSaveProfileUseCase).execute(any(ProfileForm.class), any(Subscriber.class));
    }

    @Test
    public void testPresenterHandlesDuplicateAliasCase() {
        presenterUnderTest.handleProfileSaveFailure(new DuplicateProfileException("any profile"));
        verify(mAuthenticationView).hideLoading();
        verify(mAuthenticationView).showAliasDuplicateError();
    }

    @Test
    public void testPresenterHandlesAliasReservedCase() throws Exception {
        presenterUnderTest.handleProfileSaveFailure(new ProfileReservedException());
        verify(mAuthenticationView).hideLoading();
        verify(mAuthenticationView).showAliasReservedError();
    }

    @Test
    public void testPresenterHandlesAliasMissing() throws Exception {
        doThrow(new AliasMissingException()).when(profileFormValidation).validate(mForm);
        presenterUnderTest.saveProfile(mForm);
        verify(mAuthenticationView).showAliasRequiredError();
    }

    @Test
    public void testPresenterHandlesServerUrlInvalidFormat() throws Exception {
        doThrow(new ServerUrlFormatException()).when(profileFormValidation).validate(mForm);
        presenterUnderTest.saveProfile(mForm);
        verify(mAuthenticationView).showServerUrlFormatError();
    }

    @Test
    public void testPresenterHandlesServerUrlMissing() throws Exception {
        doThrow(new ServerUrlMissingException()).when(profileFormValidation).validate(mForm);
        presenterUnderTest.saveProfile(mForm);
        verify(mAuthenticationView).showServerUrlRequiredError();
    }

    @Test
    public void testPresenterHandlesUsernameMissing() throws Exception {
        doThrow(new UsernameMissingException()).when(profileFormValidation).validate(mForm);
        presenterUnderTest.saveProfile(mForm);
        verify(mAuthenticationView).showUsernameRequiredError();
    }

    @Test
    public void testPresenterHandlesPasswordMissing() throws Exception {
        doThrow(new PasswordMissingException()).when(profileFormValidation).validate(mForm);
        presenterUnderTest.saveProfile(mForm);
        verify(mAuthenticationView).showPasswordRequiredError();
    }

    @Test
    public void testPresenterHandlesSeverVersionNotSupported() throws Exception {
        presenterUnderTest.handleProfileSaveFailure(new ServerVersionNotSupportedException("5.0"));
        verify(mAuthenticationView).hideLoading();
        verify(mAuthenticationView).showServerVersionNotSupported();
    }

    @Test
    public void testPresenterHandlesSuccessProfileSaveEvent() throws Exception {
        presenterUnderTest.handleProfileSaveSuccess();
        verify(mAuthenticationView).navigateToApp();
    }

    @Test
    public void testPresenterHandlesCompleteProfileSaveEvent() throws Exception {
        presenterUnderTest.handleProfileComplete();
        verify(mAuthenticationView).hideLoading();
    }

    @Test
    public void testPresenterHandlesRestErrors() throws Exception {
        when(mRestErrorAdapter.transform(any(RestStatusException.class))).thenReturn("error");
        presenterUnderTest.handleProfileSaveFailure(new RestStatusException("message", null, RestErrorCodes.UNDEFINED_ERROR));
        verify(mAuthenticationView).showError("error");
    }

    @Test
    public void testPresenterUnsubscribesDuringDestroy() {
        presenterUnderTest.destroy();
        verify(mSaveProfileUseCase).unsubscribe();
    }
}