/*
 * Copyright (C) 2012-2014 Jaspersoft Corporation. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.test.acceptance.favorites;

import android.app.Application;
import android.content.Intent;
import android.database.Cursor;

import com.google.inject.Singleton;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.favorites.FavoritesActivity_;
import com.jaspersoft.android.jaspermobile.activities.repository.LibraryActivity_;
import com.jaspersoft.android.jaspermobile.test.ProtoActivityInstrumentation;
import com.jaspersoft.android.jaspermobile.test.utils.CommonTestModule;
import com.jaspersoft.android.jaspermobile.test.utils.SmartMockedSpiceManager;
import com.jaspersoft.android.jaspermobile.test.utils.TestResources;
import com.jaspersoft.android.jaspermobile.util.JsXmlSpiceServiceWrapper;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.JsServerProfile;
import com.jaspersoft.android.sdk.client.async.JsXmlSpiceService;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlsList;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookupsList;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.pressBack;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static com.jaspersoft.android.jaspermobile.test.utils.DatabaseUtils.deleteAllFavorites;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.mockito.Mockito.when;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class FavoritesPageTest extends ProtoActivityInstrumentation<FavoritesActivity_> {

    @Mock
    JsRestClient mockJsRestClient;
    @Mock
    JsServerProfile jsServerProfile;
    @Mock
    JsXmlSpiceServiceWrapper xmlSpiceServiceWrapper;

    private Application mApplication;
    private SmartMockedSpiceManager mMockedSpiceManager;
    private ResourceLookupsList onlyDashboard;
    private ResourceLookupsList onlyReport;

    public FavoritesPageTest() {
        super(FavoritesActivity_.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        MockitoAnnotations.initMocks(this);

        onlyDashboard = TestResources.get().fromXML(ResourceLookupsList.class, "only_dashboard");
        onlyReport = TestResources.get().fromXML(ResourceLookupsList.class, "only_report");

        mApplication = (Application) this.getInstrumentation()
                .getTargetContext().getApplicationContext();
        mMockedSpiceManager = SmartMockedSpiceManager.createMockedManager(JsXmlSpiceService.class);

        when(xmlSpiceServiceWrapper.getSpiceManager()).thenReturn(mMockedSpiceManager);

        registerTestModule(new TestModule());
        setDefaultCurrentProfile();

        deleteAllFavorites(mApplication.getContentResolver());
    }

    @Override
    protected void tearDown() throws Exception {
        unregisterTestModule();
        super.tearDown();
    }

    public void testAddToFavoriteFromDashboardView() throws InterruptedException {
        mMockedSpiceManager.addCachedResponse(onlyDashboard);
        startActivityUnderTest();

        // Force only dashboards
        Intent intent = LibraryActivity_.intent(mApplication)
                .flags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .get();
        getInstrumentation().startActivitySync(intent);
        getInstrumentation().waitForIdleSync();

        // Select dashboard
        onData(is(instanceOf(ResourceLookup.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(1).perform(click());

        // Add to favorite
        onView(withId(R.id.favoriteAction)).perform(click());
        pressBack();
        pressBack();

        onData(is(instanceOf(Cursor.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).perform(click());

        // Remove from favorite
        onView(withId(R.id.favoriteAction)).perform(click());
        pressBack();
        onView(withId(android.R.id.empty)).check(matches(withText(R.string.f_empty_list_msg)));
    }

    public void testAddToFavoriteFromReportView() throws InterruptedException {
        mMockedSpiceManager.addCachedResponse(onlyReport);
        mMockedSpiceManager.addNetworkResponse(new InputControlsList());
        mMockedSpiceManager.addNetworkResponse(new InputControlsList());
        startActivityUnderTest();

        // Force only reports
        Intent intent = LibraryActivity_.intent(mApplication)
                .flags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .get();
        getInstrumentation().startActivitySync(intent);
        getInstrumentation().waitForIdleSync();

        // Select report
        onData(is(instanceOf(ResourceLookup.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).perform(click());

        // Add to favorite
        onView(withId(R.id.favoriteAction)).perform(click());
        pressBack();
        pressBack();

        onData(is(instanceOf(Cursor.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).perform(click());

        // Remove from favorite
        onView(withId(R.id.favoriteAction)).perform(click());
        pressBack();
        onView(withId(android.R.id.empty)).check(matches(withText(R.string.f_empty_list_msg)));
    }

    private class TestModule extends CommonTestModule {
        @Override
        protected void semanticConfigure() {
            bind(JsRestClient.class).in(Singleton.class);
            bind(JsXmlSpiceServiceWrapper.class).toInstance(xmlSpiceServiceWrapper);
        }
    }

}
