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

package com.jaspersoft.android.jaspermobile.database;

import android.net.Uri;

import com.jaspersoft.android.jaspermobile.db.MobileDbProvider;
import com.jaspersoft.android.jaspermobile.db.model.ServerProfiles;
import com.jaspersoft.android.jaspermobile.test.support.DatabaseSpecification;

import org.junit.Test;

import static com.jaspersoft.android.jaspermobile.util.JsAssertions.assertNewUri;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class ServersProfileTable extends DatabaseSpecification {
    @Test
    public void testAliasFieldShouldBeUniqueness() {
        ServerProfiles profile = new ServerProfiles();
        profile.setAlias("alias");
        profile.setServerUrl("http://example.com");
        Uri newUri = getContentResolver().insert(
                MobileDbProvider.SERVER_PROFILES_CONTENT_URI, profile.getContentValues());
        assertNewUri(newUri);

        profile = new ServerProfiles();
        profile.setAlias("alias");
        profile.setServerUrl("http://example/1.com");
        newUri = getContentResolver().insert(
                MobileDbProvider.SERVER_PROFILES_CONTENT_URI, profile.getContentValues());
        assertThat(newUri, nullValue());
    }

    @Test
    public void testAliasFieldShouldNotBeNull() {
        ServerProfiles profile = new ServerProfiles();
        profile.setAlias(null);
        profile.setServerUrl("http://example.com");
        Uri newUri = getContentResolver().insert(
                MobileDbProvider.SERVER_PROFILES_CONTENT_URI, profile.getContentValues());
        assertThat(newUri, nullValue());
    }

    @Test
    public void testServerUrlShouldNotBeNull() {
        ServerProfiles profile = new ServerProfiles();
        profile.setAlias("alias");
        profile.setServerUrl(null);
        Uri newUri = getContentResolver().insert(
                MobileDbProvider.SERVER_PROFILES_CONTENT_URI, profile.getContentValues());
        assertThat(newUri, nullValue());
    }
}
