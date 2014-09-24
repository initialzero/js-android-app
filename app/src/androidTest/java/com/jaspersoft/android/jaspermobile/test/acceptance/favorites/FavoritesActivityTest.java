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
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;

import com.google.android.apps.common.testing.ui.espresso.NoMatchingViewException;
import com.google.inject.Singleton;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.favorites.FavoritesActivity_;
import com.jaspersoft.android.jaspermobile.activities.repository.LibraryActivity_;
import com.jaspersoft.android.jaspermobile.activities.repository.support.RepositoryPref_;
import com.jaspersoft.android.jaspermobile.db.model.Favorites;
import com.jaspersoft.android.jaspermobile.db.provider.JasperMobileProvider;
import com.jaspersoft.android.jaspermobile.test.ProtoActivityInstrumentation;
import com.jaspersoft.android.jaspermobile.test.utils.CommonTestModule;
import com.jaspersoft.android.jaspermobile.test.utils.SmartMockedSpiceManager;
import com.jaspersoft.android.jaspermobile.util.JsXmlSpiceServiceWrapper;
import com.jaspersoft.android.jaspermobile.util.ProfileHelper;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.JsServerProfile;
import com.jaspersoft.android.sdk.client.async.JsXmlSpiceService;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.pressBack;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static com.jaspersoft.android.jaspermobile.test.utils.DatabaseUtils.deleteAllFavorites;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.hasTotalCount;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.onOverflowView;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.mockito.Mockito.when;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class FavoritesActivityTest extends ProtoActivityInstrumentation<FavoritesActivity_> {

    @Mock
    JsRestClient mockJsRestClient;
    @Mock
    JsServerProfile jsServerProfile;
    @Mock
    JsXmlSpiceServiceWrapper xmlSpiceServiceWrapper;

    private long profileId;
    private RepositoryPref_ repositoryPref;
    private Application mApplication;
    private SmartMockedSpiceManager mMockedSpiceManager;

    public FavoritesActivityTest() {
        super(FavoritesActivity_.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);

        mApplication = (Application) this.getInstrumentation()
                .getTargetContext().getApplicationContext();
        repositoryPref = new RepositoryPref_(mApplication);
        mMockedSpiceManager = SmartMockedSpiceManager.createHybridManager(JsXmlSpiceService.class);
        mMockedSpiceManager.behaveInRealMode();

        when(xmlSpiceServiceWrapper.getSpiceManager()).thenReturn(mMockedSpiceManager);

        registerTestModule(new TestModule());
        setDefaultCurrentProfile();

        deleteAllFavorites(mApplication.getContentResolver());
    }

    @Override
    protected void tearDown() throws Exception {
        mMockedSpiceManager.removeLifeCycleListener();
        unregisterTestModule();
        super.tearDown();
    }

    public void ignoreInitialLoad() {
        ContentResolver contentResolver = getInstrumentation().getContext().getContentResolver();
        Favorites favorites = new Favorites();
        favorites.setServerProfilesId(profileId);
        favorites.setUsername(ProfileHelper.DEFAULT_USERNAME);
        favorites.setOrganization(ProfileHelper.DEFAULT_ORGANIZATION);
        favorites.setWstype(ResourceLookup.ResourceType.dashboard.toString());
        contentResolver.insert(JasperMobileProvider.FAVORITES_CONTENT_URI, favorites.getContentValues());
        startActivityUnderTest();

        onView(withId(android.R.id.list)).check(hasTotalCount(1));

        rotate();
        onView(withId(android.R.id.list)).check(hasTotalCount(1));

        onView(withId(R.id.switchLayout)).perform(click());

        rotate();
        onView(withId(android.R.id.list)).check(hasTotalCount(1));
    }

    public void testAddToFavoriteFromReportView() {
        startActivityUnderTest();

        // Force only reports
        Intent intent = LibraryActivity_.intent(mApplication)
                .flags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .get();
        getInstrumentation().startActivitySync(intent);
        clickFilterMenuItem();
        onOverflowView(getActivity(), withText(R.string.s_fd_option_reports)).perform(click());
        onOverflowView(getActivity(), withText(android.R.string.ok)).perform(click());

        // Select report
        onData(is(instanceOf(ResourceLookup.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(1).perform(click());

        // Add to favorite
        onView(withId(R.id.favoriteAction)).perform(click());
        pressBack();
        pressBack();

        // Assert report to be on favorites
        onView(withId(android.R.id.list)).check(hasTotalCount(1));
        onData(is(instanceOf(Cursor.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).perform(click());

        // Remove from favorite
        onView(withId(R.id.favoriteAction)).perform(click());
        pressBack();
        onView(withId(android.R.id.empty)).check(matches(withText(R.string.f_empty_list_msg)));
    }

    private void clickFilterMenuItem() {
        try {
            onView(withId(R.id.filter)).perform(click());
        } catch (NoMatchingViewException ex) {
            openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
            try {
                onOverflowView(getCurrentActivity(), withText(R.string.s_ab_filter_by)).perform(click());
            } catch (Throwable throwable) {
                new RuntimeException(throwable);
            }
        }
    }

    private class TestModule extends CommonTestModule {
        @Override
        protected void semanticConfigure() {
            bind(JsRestClient.class).in(Singleton.class);
            bind(JsXmlSpiceServiceWrapper.class).toInstance(xmlSpiceServiceWrapper);
        }
    }

}
