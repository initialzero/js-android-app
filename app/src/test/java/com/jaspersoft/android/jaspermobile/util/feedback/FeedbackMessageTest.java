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

package com.jaspersoft.android.jaspermobile.util.feedback;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.jaspersoft.android.jaspermobile.BuildConfig;
import com.jaspersoft.android.jaspermobile.util.server.ServerInfoProvider;
import com.jaspersoft.android.sdk.service.data.server.ServerVersion;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.res.builder.RobolectricPackageManager;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

/**
 * @author Tom Koptel
 * @since 2.1
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(sdk = 21, constants = BuildConfig.class)
public class FeedbackMessageTest {
    @Mock
    ServerInfoProvider serverInfoProvider;
    Message feedbackMessage;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        feedbackMessage = new Message(RuntimeEnvironment.application, serverInfoProvider);
    }

    @Test
    public void shouldCreateValidFeedbackMessage() {
        mockPackageManager();

        when(serverInfoProvider.getVersion()).thenReturn(ServerVersion.v6_1);
        when(serverInfoProvider.isProEdition()).thenReturn(false);

        String message = feedbackMessage.create();

        assertThat(message, is(notNullValue()));
    }

    @Test
    public void shouldGenerateAppVersionInfo() {
        mockPackageManager();
        String message = feedbackMessage.generateAppVersionInfo();
        assertThat(message, containsString("Version code: 20100000"));
    }

    @Test
    public void shouldGenerateNullForAppVersionInfo() {
        Application context = RuntimeEnvironment.application;
        RobolectricPackageManager packageManager = (RobolectricPackageManager) shadowOf(context).getPackageManager();
        packageManager.removePackage(context.getPackageName());

        String message = feedbackMessage.generateAppVersionInfo();
        assertThat(message, is(nullValue()));
    }

    @Test
    public void shouldGenerateJrsVersionInfo() {
        when(serverInfoProvider.getVersion()).thenReturn(ServerVersion.v6_1);
        String message = feedbackMessage.generateServerVersion();
        assertThat(message, containsString("JRS version: 6.1"));
    }

    @Test
    public void shouldGenerateJrsEditionInfo() {
        when(serverInfoProvider.isProEdition()).thenReturn(false);
        String message = feedbackMessage.generateServerEdition();
        assertThat(message, containsString("JRS edition: CE"));
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void mockPackageManager() {
        Application context = RuntimeEnvironment.application;
        RobolectricPackageManager packageManager = (RobolectricPackageManager) shadowOf(context).getPackageManager();
        try {
            PackageInfo info = packageManager.getPackageInfo(context.getPackageName(), 0);
            info.versionCode = 20100000;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
