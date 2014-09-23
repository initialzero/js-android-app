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

import android.content.ContentResolver;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.favorites.FavoritesActivity_;
import com.jaspersoft.android.jaspermobile.db.model.Favorites;
import com.jaspersoft.android.jaspermobile.db.provider.JasperMobileProvider;
import com.jaspersoft.android.jaspermobile.test.ProtoActivityInstrumentation;
import com.jaspersoft.android.jaspermobile.test.utils.CommonTestModule;
import com.jaspersoft.android.jaspermobile.util.ProfileHelper;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.JsServerProfile;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.jaspersoft.android.jaspermobile.test.utils.TestServerProfileUtils.createDefaultProfile;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.hasTotalCount;
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
    private long profileId;

    public FavoritesActivityTest() {
        super(FavoritesActivity_.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        profileId = createDefaultProfile(getInstrumentation().getContext().getContentResolver());
        MockitoAnnotations.initMocks(this);
        when(mockJsRestClient.getServerProfile()).thenReturn(jsServerProfile);
        when(jsServerProfile.getId()).thenReturn(profileId);
        when(jsServerProfile.getUsername()).thenReturn(ProfileHelper.DEFAULT_USERNAME);
        when(jsServerProfile.getOrganization()).thenReturn(ProfileHelper.DEFAULT_ORGANIZATION);
        registerTestModule(new TestModule());
    }

    @Override
    protected void tearDown() throws Exception {
        unregisterTestModule();
        super.tearDown();
    }

    public void testInitialLoad() {
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

    private class TestModule extends CommonTestModule {
        @Override
        protected void semanticConfigure() {
            bind(JsRestClient.class).toInstance(mockJsRestClient);
        }
    }

}
