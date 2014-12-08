/*
 * Copyright (C) 2012 Jaspersoft Corporation. All rights reserved.
 * http://community.jaspersoft.com/project/mobile-sdk-android
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of Jaspersoft Mobile SDK for Android.
 *
 * Jaspersoft Mobile SDK is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Jaspersoft Mobile SDK is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Jaspersoft Mobile SDK for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile;


import com.jaspersoft.android.jaspermobile.test.support.UnitTestSpecification;

import org.junit.Test;

import static junit.framework.Assert.fail;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


public class BuildConfigTest extends UnitTestSpecification {

    @Test
    public void shouldHaveCorrectConfiguration() {
        if ("debug".equals(BuildConfig.BUILD_TYPE)) {
            assertThat(BuildConfig.DEBUG, is(true));
        } else if ("release".equals(BuildConfig.BUILD_TYPE)) {
            assertThat(BuildConfig.DEBUG, is(false));
        } else {
            fail("build type configuration not tested or supported?");
        }
        new BuildConfig(); // dummy coverage, should be an interface or something else
    }
}
