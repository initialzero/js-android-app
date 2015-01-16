/*
 * Copyright © 2014 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of Jaspersoft Mobile for Android.
 *
 * Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.test.acceptance.auth;

import android.support.test.runner.AndroidJUnit4;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.StartUpActivity_;
import com.jaspersoft.android.jaspermobile.test.junit.ActivityRule;
import com.jaspersoft.android.jaspermobile.test.junit.WebMockRule;
import com.jaspersoft.android.jaspermobile.test.utils.TestResource;
import com.jaspersoft.android.retrofit.sdk.account.AccountManagerUtil;
import com.jaspersoft.android.retrofit.sdk.account.AccountServerData;
import com.squareup.okhttp.mockwebserver.MockResponse;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Tom Koptel
 * @since 2.0
 */
@RunWith(AndroidJUnit4.class)
public class StartUpActivityTest {
    @Rule
    public WebMockRule webMockRule = new WebMockRule();
    @Rule
    public final ActivityRule<StartUpActivity_> activityRule =
            ActivityRule.create(StartUpActivity_.class);

    private MockResponse authResponse;
    private MockResponse mobileDemoServerRespone;

    @Before
    public void before() {
        AccountManagerUtil.get(activityRule.getApplicationContext())
                .removeAccounts()
                .subscribe();

        authResponse = new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .addHeader("Set-Cookie", "JSESSIONID=4202A2DF42507EDEC7A66A1348C62195; Path=/jasperserver-pro/; HttpOnly")
                .addHeader("Set-Cookie", "userLocale=en_US;Expires=Thu, 15-Jan-2015 12:15:36 GMT;HttpOnly")
                .throttleBody(Integer.MAX_VALUE, 1, TimeUnit.MILLISECONDS)
                .setBody("{}");
        mobileDemoServerRespone = authResponse.clone()
                .setBody(TestResource.getJson().rawData("mobile_demo"));
    }

    @Test
    public void testPreconditions() {
        assertThat(webMockRule.get(), notNullValue());
        assertThat(activityRule.get(), notNullValue());
        assertThat(activityRule.instrumentation(), notNullValue());
    }

    @Test
    public void testIdealTryDemoAction() {
        webMockRule.get().enqueue(authResponse);
        webMockRule.get().enqueue(mobileDemoServerRespone);

        onView(withId(R.id.tryDemo)).perform(click());
        onView(withText(R.string.app_label)).check(matches(isDisplayed()));
    }

    @Test
    public void testIdealLoginAction() {
        onView(withId(R.id.username)).perform(typeText(AccountServerData.Demo.USERNAME));
        onView(withId(R.id.password)).perform(typeText(AccountServerData.Demo.PASSWORD));
        onView(withId(R.id.logIn)).perform(click());
        onView(withText(R.string.app_label)).check(matches(isDisplayed()));
    }
}
