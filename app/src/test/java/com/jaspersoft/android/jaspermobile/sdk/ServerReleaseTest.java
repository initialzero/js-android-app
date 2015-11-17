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

package com.jaspersoft.android.jaspermobile.sdk;

import com.jaspersoft.android.retrofit.sdk.server.ServerRelease;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author Tom Koptel
 * @since 2.0
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ServerReleaseTest {
    @Test
    public void shouldParseSemanticVersioning() {
        Map<String, ServerRelease> doubleMap = new HashMap<String, ServerRelease>();
        doubleMap.put("5.0.0", ServerRelease.EMERALD);
        doubleMap.put("5.2.0", ServerRelease.EMERALD_MR1);
        doubleMap.put("5.5.0", ServerRelease.EMERALD_MR2);
        doubleMap.put("5.6.0", ServerRelease.EMERALD_MR3);
        doubleMap.put("6.0", ServerRelease.AMBER);
        doubleMap.put("20.0", ServerRelease.UNKNOWN);

        for (Map.Entry<String, ServerRelease> entry : doubleMap.entrySet()) {
            assertThat(ServerRelease.parseVersion(entry.getKey()), is(entry.getValue())) ;
        }
    }

    @Test
    public void shouldParseNonSemanticVersioning() {
        String[] nonSemanticOne = {"5.6.0 Preview", "5.6.0-BETA"};
        for (String nonSemanticVersion : nonSemanticOne) {
            assertThat(ServerRelease.parseVersion(nonSemanticVersion), is(ServerRelease.EMERALD_MR3));
        }
    }
}
