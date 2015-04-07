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
import com.jaspersoft.android.jaspermobile.activities.navigation.NavigationActivity_;
import com.jaspersoft.android.jaspermobile.test.junit.ActivityRule;
import com.jaspersoft.android.jaspermobile.test.junit.WebMockRule;
import com.jaspersoft.android.jaspermobile.test.utils.AccountUtil;
import com.jaspersoft.android.jaspermobile.test.utils.TestResource;
import com.jaspersoft.android.jaspermobile.test.utils.pref.PreferenceApiAdapter;
import com.jaspersoft.android.retrofit.sdk.account.AccountServerData;
import com.jaspersoft.android.retrofit.sdk.util.JasperSettings;
import com.squareup.okhttp.mockwebserver.MockResponse;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.hasErrorText;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;

/**
 * @author Tom Koptel
 * @since 2.0
 */
@RunWith(AndroidJUnit4.class)
public class StartUpActivityTest {
    @Rule
    public WebMockRule webMockRule = new WebMockRule();
    @Rule
    public final ActivityRule<NavigationActivity_> activityRule =
            ActivityRule.create(NavigationActivity_.class);

    @Before
    public void before() {
        AccountUtil.get(activityRule.getApplicationContext()).removeAllAccounts();
        PreferenceApiAdapter.init(activityRule.getApplicationContext())
                .setInAppAnimationEnabled(false);
        assertThat(webMockRule.get(), notNullValue());
    }

    @Test
    public void testAddAccountAction() {
        mockHttpResponses();
        activityRule.saveStart();

        onView(withId(R.id.aliasEdit)).perform(typeText(AccountServerData.Demo.ALIAS));
        onView(withId(R.id.serverUrlEdit)).perform(typeText(webMockRule.getEndpoint()));
        onView(withId(R.id.organizationEdit)).perform(typeText(AccountServerData.Demo.ORGANIZATION));
        onView(withId(R.id.usernameEdit)).perform(scrollTo());
        onView(withId(R.id.usernameEdit)).perform(typeText(AccountServerData.Demo.USERNAME));
        onView(withId(R.id.passwordEdit)).perform(scrollTo());
        onView(withId(R.id.passwordEdit)).perform(typeText(AccountServerData.Demo.PASSWORD));

        onView(withId(R.id.addAccount)).perform(scrollTo());
        onView(withId(R.id.addAccount)).perform(click());

        onView(allOf(
                withParent(withId(R.id.tb_navigation)),
                withText(R.string.h_library_label)
        )).check(matches(isDisplayed()));
    }

    // TODO broken fix it
    @Ignore
    public void testTryDemoAction() {
        mockHttpResponses();
        activityRule.saveStart();

        onView(withId(R.id.usernameEdit)).perform(scrollTo());
        onView(withId(R.id.passwordEdit)).perform(scrollTo());

        onView(withId(R.id.tryDemo)).perform(scrollTo());
        onView(withId(R.id.tryDemo)).perform(click());

        onView(allOf(
                withParent(withId(R.id.tb_navigation)),
                withText(R.string.h_library_label)
        )).check(matches(isDisplayed()));
    }

    private void mockHttpResponses() {
        MockResponse authResponse = new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .addHeader("Set-Cookie", "JSESSIONID=4202A2DF42507EDEC7A66A1348C62195; Path=/jasperserver-pro/; HttpOnly")
                .addHeader("Set-Cookie", "userLocale=en_US;Expires=Thu, 15-Jan-2015 12:15:36 GMT;HttpOnly")
                .throttleBody(Integer.MAX_VALUE, 1, TimeUnit.MILLISECONDS)
                .setBody("{}");
        MockResponse mobileDemoServerRespone = authResponse.clone()
                .setBody(TestResource.getJson().rawData("mobile_demo"));
        MockResponse resources = new MockResponse()
                .addHeader("Content-Type", "application/xml; charset=utf-8")
                .setBody(TestResource.get(TestResource.DataFormat.XML).rawData("all_resources"));

        webMockRule.get().enqueue(authResponse);
        webMockRule.get().enqueue(mobileDemoServerRespone);
        webMockRule.get().enqueue(resources);
    }

    @Test
    public void testEmptyAliasNotAcceptable() {
        activityRule.saveStart();
        onView(withId(R.id.aliasEdit)).perform(typeText("  "));
        onView(withId(R.id.serverUrlEdit)).perform(scrollTo());
        onView(withId(R.id.serverUrlEdit)).perform(typeText(webMockRule.getEndpoint()));
        onView(withId(R.id.usernameEdit)).perform(scrollTo());
        onView(withId(R.id.usernameEdit)).perform(typeText(AccountServerData.Demo.USERNAME));
        onView(withId(R.id.passwordEdit)).perform(scrollTo());
        onView(withId(R.id.passwordEdit)).perform(typeText(AccountServerData.Demo.PASSWORD));

        onView(withId(R.id.addAccount)).perform(scrollTo());
        onView(withId(R.id.addAccount)).perform(click());
        onView(withId(R.id.aliasEdit)).check(matches(hasErrorText(R.string.sp_error_field_required)));

        onView(withId(R.id.aliasEdit)).perform(scrollTo());
        onView(withId(R.id.aliasEdit)).perform(clearText());
        onView(withId(R.id.usernameEdit)).perform(scrollTo());
        onView(withId(R.id.passwordEdit)).perform(scrollTo());
        onView(withId(R.id.addAccount)).perform(scrollTo());
        onView(withId(R.id.addAccount)).perform(click());
        onView(withId(R.id.aliasEdit)).check(matches(hasErrorText(R.string.sp_error_field_required)));
    }

    @Test
    public void testEmptyUsernameNotAcceptable() {
        activityRule.saveStart();
        onView(withId(R.id.aliasEdit)).perform(typeText(AccountServerData.Demo.ALIAS));
        onView(withId(R.id.serverUrlEdit)).perform(scrollTo());
        onView(withId(R.id.serverUrlEdit)).perform(typeText(webMockRule.getEndpoint()));
        onView(withId(R.id.usernameEdit)).perform(scrollTo());
        onView(withId(R.id.usernameEdit)).perform(typeText("  "));
        onView(withId(R.id.passwordEdit)).perform(scrollTo());
        onView(withId(R.id.passwordEdit)).perform(typeText(AccountServerData.Demo.PASSWORD));

        onView(withId(R.id.addAccount)).perform(scrollTo());
        onView(withId(R.id.addAccount)).perform(click());
        onView(withId(R.id.usernameEdit)).check(matches(hasErrorText(R.string.sp_error_field_required)));

        onView(withId(R.id.usernameEdit)).perform(scrollTo());
        onView(withId(R.id.usernameEdit)).perform(clearText());
        onView(withId(R.id.addAccount)).perform(scrollTo());
        onView(withId(R.id.addAccount)).perform(click());
        onView(withId(R.id.usernameEdit)).check(matches(hasErrorText(R.string.sp_error_field_required)));
    }

    @Test
    public void testEmptyPasswordNotAcceptable() {
        activityRule.saveStart();
        onView(withId(R.id.aliasEdit)).perform(typeText(AccountServerData.Demo.ALIAS));
        onView(withId(R.id.serverUrlEdit)).perform(scrollTo());
        onView(withId(R.id.serverUrlEdit)).perform(typeText(webMockRule.getEndpoint()));
        onView(withId(R.id.usernameEdit)).perform(scrollTo());
        onView(withId(R.id.usernameEdit)).perform(typeText(AccountServerData.Demo.USERNAME));
        onView(withId(R.id.passwordEdit)).perform(scrollTo());
        onView(withId(R.id.passwordEdit)).perform(typeText("  "));

        onView(withId(R.id.addAccount)).perform(scrollTo());
        onView(withId(R.id.addAccount)).perform(click());
        onView(withId(R.id.passwordEdit)).check(matches(hasErrorText(R.string.sp_error_field_required)));

        onView(withId(R.id.aliasEdit)).perform(scrollTo());
        onView(withId(R.id.usernameEdit)).perform(scrollTo());
        onView(withId(R.id.passwordEdit)).perform(scrollTo());
        onView(withId(R.id.passwordEdit)).perform(clearText());
        onView(withId(R.id.addAccount)).perform(scrollTo());
        onView(withId(R.id.addAccount)).perform(click());
        onView(withId(R.id.passwordEdit)).check(matches(hasErrorText(R.string.sp_error_field_required)));
    }

    @Test
    public void testEmptyForServerUrlNotAcceptable() {
        activityRule.saveStart();
        onView(withId(R.id.aliasEdit)).perform(typeText(AccountServerData.Demo.ALIAS));
        onView(withId(R.id.serverUrlEdit)).perform(scrollTo());
        onView(withId(R.id.serverUrlEdit)).perform(typeText("  "));
        onView(withId(R.id.usernameEdit)).perform(scrollTo());
        onView(withId(R.id.usernameEdit)).perform(typeText(AccountServerData.Demo.USERNAME));
        onView(withId(R.id.passwordEdit)).perform(scrollTo());
        onView(withId(R.id.passwordEdit)).perform(typeText(AccountServerData.Demo.PASSWORD));

        onView(withId(R.id.addAccount)).perform(scrollTo());
        onView(withId(R.id.addAccount)).perform(click());
        onView(withId(R.id.serverUrlEdit)).check(matches(hasErrorText(R.string.sp_error_url_not_valid)));

        onView(withId(R.id.serverUrlEdit)).perform(clearText());
        onView(withId(R.id.addAccount)).perform(scrollTo());
        onView(withId(R.id.addAccount)).perform(click());
        onView(withId(R.id.serverUrlEdit)).check(matches(hasErrorText(R.string.sp_error_field_required)));
    }

    @Test
    public void testServerUrlShouldBeValidUrl() {
        activityRule.saveStart();
        onView(withId(R.id.aliasEdit)).perform(typeText(AccountServerData.Demo.ALIAS));
        onView(withId(R.id.serverUrlEdit)).perform(scrollTo());
        onView(withId(R.id.serverUrlEdit)).perform(typeText("invalid url"));
        onView(withId(R.id.usernameEdit)).perform(scrollTo());
        onView(withId(R.id.usernameEdit)).perform(typeText(AccountServerData.Demo.USERNAME));
        onView(withId(R.id.passwordEdit)).perform(scrollTo());
        onView(withId(R.id.passwordEdit)).perform(typeText(AccountServerData.Demo.PASSWORD));

        onView(withId(R.id.addAccount)).perform(scrollTo());
        onView(withId(R.id.addAccount)).perform(click());
        onView(withId(R.id.serverUrlEdit)).check(matches(hasErrorText(R.string.sp_error_url_not_valid)));
    }


    @Test
    public void testAliasHasReservedValue() {
        activityRule.saveStart();
        onView(withId(R.id.aliasEdit)).perform(typeText(JasperSettings.RESERVED_ACCOUNT_NAME));
        onView(withId(R.id.serverUrlEdit)).perform(scrollTo());
        onView(withId(R.id.serverUrlEdit)).perform(typeText(AccountServerData.Demo.SERVER_URL));
        onView(withId(R.id.usernameEdit)).perform(scrollTo());
        onView(withId(R.id.usernameEdit)).perform(typeText(AccountServerData.Demo.USERNAME));
        onView(withId(R.id.passwordEdit)).perform(scrollTo());
        onView(withId(R.id.passwordEdit)).perform(typeText(AccountServerData.Demo.PASSWORD));

        onView(withId(R.id.addAccount)).perform(scrollTo());
        onView(withId(R.id.addAccount)).perform(click());
        onView(withId(R.id.aliasEdit)).check(matches(hasErrorText(R.string.sp_error_reserved_alias)));
    }
}
