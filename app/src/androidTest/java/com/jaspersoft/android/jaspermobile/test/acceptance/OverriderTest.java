/*
 * Copyright © 2014 TIBCO Software, Inc. All rights reserved.
 *  http://community.jaspersoft.com/project/jaspermobile-android
 *
 *  Unless you have purchased a commercial license agreement from Jaspersoft,
 *  the following license terms apply:
 *
 *  This program is part of Jaspersoft Mobile for Android.
 *
 *  Jaspersoft Mobile is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Jaspersoft Mobile is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Jaspersoft Mobile for Android. If not, see
 *  <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.test.acceptance;

import com.jaspersoft.android.jaspermobile.TestActivity_;
import com.jaspersoft.android.jaspermobile.test.ProtoActivityInstrumentation;
import com.jaspersoft.android.jaspermobile.test.acceptance.hacked.HackedRestTemplate;
import com.jaspersoft.android.jaspermobile.test.acceptance.hacked.JasperRobolectric;
import com.jaspersoft.android.jaspermobile.test.utils.ApiMatcher;
import com.jaspersoft.android.jaspermobile.test.utils.CommonTestModule;
import com.jaspersoft.android.jaspermobile.test.utils.HttpResponseUtil;
import com.jaspersoft.android.sdk.client.JsRestClient;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;

public class OverriderTest extends ProtoActivityInstrumentation<TestActivity_> {

    public OverriderTest() {
        super(TestActivity_.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        registerTestModule(new TestModule());
        setDefaultCurrentProfile();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        unregisterTestModule();
    }

    public void testAssist() {
        JasperRobolectric.addHttpResponseRule(
                ApiMatcher.SERVER_INFO,
                HttpResponseUtil.get().xmlType("server_info"));

        startActivityUnderTest();
        onView(withId(android.R.id.button1)).perform(click());
        onView(withId(android.R.id.text1)).check(matches(withText("Success")));
    }

    private class TestModule extends CommonTestModule {
        @Override
        protected void semanticConfigure() {
            bind(JsRestClient.class).toInstance(new JsRestClient(new HackedRestTemplate(true)));
        }
    }

}
