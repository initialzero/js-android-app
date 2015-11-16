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

import com.jaspersoft.android.jaspermobile.domain.BaseCredentials;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.interactor.SaveProfile;
import com.jaspersoft.android.jaspermobile.presentation.mapper.CredentialsDataMapper;
import com.jaspersoft.android.jaspermobile.presentation.mapper.ProfileDataMapper;
import com.jaspersoft.android.jaspermobile.presentation.model.CredentialsModel;
import com.jaspersoft.android.jaspermobile.presentation.model.ProfileModel;
import com.jaspersoft.android.jaspermobile.presentation.view.AuthenticationView;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import rx.Subscriber;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class AuthenticationPresenterTest {

    // Presentation mock components
    @Mock
    CredentialsDataMapper mCredentialsDataMapper;
    @Mock
    ProfileDataMapper mProfileDataMapper;
    @Mock
    ProfileModel uiProfile;
    @Mock
    CredentialsModel uiCredentials;
    @Mock
    AuthenticationView mAuthenticationView;

    // Domain mock components
    @Mock
    SaveProfile mSaveProfile;
    @Mock
    Profile domainProfile;
    @Mock
    BaseCredentials domainCredentials;

    AuthenticationPresenter presenterUnderTest;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        presenterUnderTest = new AuthenticationPresenter(mSaveProfile, mProfileDataMapper, mCredentialsDataMapper);
        presenterUnderTest.setView(mAuthenticationView);

        when(uiProfile.getCredentials()).thenReturn(uiCredentials);
        when(mProfileDataMapper.transform(any(ProfileModel.class))).thenReturn(domainProfile);
        when(mCredentialsDataMapper.transform(any(CredentialsModel.class))).thenReturn(domainCredentials);
    }

    @Test
    public void testSaveProfile() throws Exception {
        when(uiProfile.getBaseUrl()).thenReturn("http://localhost");

        presenterUnderTest.saveProfile(uiProfile);

        verify(mAuthenticationView).hideRetry();
        verify(mAuthenticationView).showLoading();

        verify(mProfileDataMapper).transform(uiProfile);
        verify(mCredentialsDataMapper).transform(uiCredentials);
        verify(mSaveProfile).execute(eq("http://localhost"), eq(domainProfile), eq(domainCredentials), any(Subscriber.class));
    }
}