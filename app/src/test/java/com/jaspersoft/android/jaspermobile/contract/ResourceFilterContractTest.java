/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.contract;

import com.jaspersoft.android.jaspermobile.activities.repository.support.ResourceFilterFactory;
import com.jaspersoft.android.jaspermobile.test.support.CustomRobolectricTestRunner;
import com.jaspersoft.android.retrofit.sdk.server.ServerRelease;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author Tom Koptel
 * @since 2.0
 */
@RunWith(CustomRobolectricTestRunner.class)
@Config(
        manifest = "app/src/main/AndroidManifest.xml",
        emulateSdk = 18
)
public class ResourceFilterContractTest {

    @Test
    public void testFiltersForLibraryForEmeraldConfiguration() {
        ServerRelease release = ServerRelease.EMERALD;
        ResourceFilterFactory factory = ResourceFilterFactory.create(release);

        List<String> types = new ArrayList<String>(){{
            add(ResourceLookup.ResourceType.reportUnit.toString());
            add(ResourceLookup.ResourceType.dashboard.toString());
        }};
        assertThat(types, is(factory.createFiltersForLibrary()));
    }

    @Test
    public void testFiltersForRepositoryForEmeraldConfiguration() {
        ServerRelease release = ServerRelease.EMERALD;
        ResourceFilterFactory factory = ResourceFilterFactory.create(release);

        List<String> types = new ArrayList<String>(){{
            add(ResourceLookup.ResourceType.folder.toString());
            add(ResourceLookup.ResourceType.reportUnit.toString());
            add(ResourceLookup.ResourceType.dashboard.toString());
        }};
        assertThat(types, is(factory.createFiltersForRepository()));
    }

    @Test
    public void testOnlyDashboardFiltersForEmeraldConfiguration() {
        ServerRelease release = ServerRelease.EMERALD;
        ResourceFilterFactory factory = ResourceFilterFactory.create(release);

        List<String> types = new ArrayList<String>(){{
            add(ResourceLookup.ResourceType.dashboard.toString());
        }};
        assertThat(types, is(factory.createOnlyDashboardFilters()));
    }

    @Test
    public void testOnlyReportFiltersForEmeraldConfiguration() {
        ServerRelease release = ServerRelease.EMERALD;
        ResourceFilterFactory factory = ResourceFilterFactory.create(release);

        List<String> types = new ArrayList<String>(){{
            add(ResourceLookup.ResourceType.reportUnit.toString());
        }};
        assertThat(types, is(factory.createOnlyReportFilters()));
    }

    @Test
    public void testOnlyReportFiltersForAmberConfiguration() {
        ServerRelease release = ServerRelease.AMBER;
        ResourceFilterFactory factory = ResourceFilterFactory.create(release);

        List<String> types = new ArrayList<String>(){{
            add(ResourceLookup.ResourceType.reportUnit.toString());
        }};
        assertThat(types, is(factory.createOnlyReportFilters()));
    }

    @Test
    public void testOnlyDashboardFiltersForAmberConfiguration() {
        ServerRelease release = ServerRelease.AMBER;
        ResourceFilterFactory factory = ResourceFilterFactory.create(release);

        List<String> types = new ArrayList<String>(){{
            add(ResourceLookup.ResourceType.legacyDashboard.toString());
            add(ResourceLookup.ResourceType.dashboard.toString());
        }};
        assertThat(types, is(factory.createOnlyDashboardFilters()));
    }

    @Test
    public void testFiltersForRepositoryForAmberConfiguration() {
        ServerRelease release = ServerRelease.AMBER;
        ResourceFilterFactory factory = ResourceFilterFactory.create(release);

        List<String> types = new ArrayList<String>(){{
            add(ResourceLookup.ResourceType.folder.toString());
            add(ResourceLookup.ResourceType.reportUnit.toString());
            add(ResourceLookup.ResourceType.legacyDashboard.toString());
            add(ResourceLookup.ResourceType.dashboard.toString());
        }};
        assertThat(types, is(factory.createFiltersForRepository()));
    }

    @Test
    public void testFiltersForLibraryForAmberConfiguration() {
        ServerRelease release = ServerRelease.AMBER;
        ResourceFilterFactory factory = ResourceFilterFactory.create(release);

        List<String> types = new ArrayList<String>(){{
            add(ResourceLookup.ResourceType.reportUnit.toString());
            add(ResourceLookup.ResourceType.legacyDashboard.toString());
            add(ResourceLookup.ResourceType.dashboard.toString());
        }};
        assertThat(types, is(factory.createFiltersForLibrary()));
    }

}
