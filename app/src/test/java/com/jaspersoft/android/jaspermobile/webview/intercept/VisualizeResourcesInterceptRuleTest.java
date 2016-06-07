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

package com.jaspersoft.android.jaspermobile.webview.intercept;

import android.os.Build;

import com.jaspersoft.android.jaspermobile.webview.WebRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

/**
 * @author Tom Koptel
 * @since 2.5
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class VisualizeResourcesInterceptRuleTest {
    private static final String[] WHITE_LIST = new String[]{
            "http://192.168.88.55:8088/jasperserver-pro-62/rest_v2/bundles?expanded=true",
            "http://192.168.88.55:8088/jasperserver-pro-62/rest_v2/settings/dateTimeSettings",
            "http://192.168.88.55:8088/jasperserver-pro-62/scripts/auth/loginSuccess.json"
    };

    @Mock
    WebRequest resourceRequest;
    private VisualizeResourcesInterceptRule ruleUnderTest;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        ruleUnderTest = VisualizeResourcesInterceptRule.getInstance();
    }

    @Test
    public void should_intercept_for_api_higher_than_lollipop() throws Exception {
        givenAndroidOfVersion(22);

        thenShouldInterceptRequests();
    }

    @Test
    public void should_intercept_for_api_equal_lollipop() throws Exception {
        givenAndroidOfVersion(21);

        thenShouldInterceptRequests();
    }

    @Test
    public void should_not_intercept_for_api_lower_than_lollipop() throws Exception {
        givenAndroidOfVersion(14);

        thenShouldNotInterceptRequests();
    }

    private void givenAndroidOfVersion(int version) {
        ReflectionHelpers.setStaticField(Build.VERSION.class, "SDK_INT", version);
    }

    private void thenShouldInterceptRequests() {
        interceptRequests(true);
    }

    private void thenShouldNotInterceptRequests() {
        interceptRequests(false);
    }

    private void interceptRequests(boolean flag) {
        for (String url : WHITE_LIST) {
            when(resourceRequest.getUrl()).thenReturn(url);
            assertThat("Should not intercept url: " + url, ruleUnderTest.shouldIntercept(resourceRequest), is(flag));
        }
    }
}