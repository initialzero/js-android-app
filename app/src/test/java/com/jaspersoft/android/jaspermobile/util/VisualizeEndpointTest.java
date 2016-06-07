/*
 * Copyright © 2016 TIBCO Software,Inc.All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software:you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation,either version 3of the License,or
 * (at your option)any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android.If not,see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author Tom Koptel
 * @since 2.1
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 21)
public class VisualizeEndpointTest {

    private static final String MOCK_URL = "http://mobiledemo2.jaspersoft.com/jasperserver-pro/";
    private static final String OPTIMIZED = "http://mobiledemo2.jaspersoft.com/jasperserver-pro/client/visualize.js?_opt=true&baseUrl=http%3A%2F%2Fmobiledemo2.jaspersoft.com%2Fjasperserver-pro";
    private static final String NOT_OPTIMIZED = "http://mobiledemo2.jaspersoft.com/jasperserver-pro/client/visualize.js?_opt=false&baseUrl=http%3A%2F%2Fmobiledemo2.jaspersoft.com%2Fjasperserver-pro";
    private static final String WITH_CONTROLS = "http://mobiledemo2.jaspersoft.com/jasperserver-pro/client/visualize.js?_opt=false&baseUrl=http%3A%2F%2Fmobiledemo2.jaspersoft.com%2Fjasperserver-pro&_showInputControls=true";

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAcceptNullBaseUrl() {
        VisualizeEndpoint.forBaseUrl(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAcceptEmptyBaseUrl() {
        VisualizeEndpoint.forBaseUrl("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAcceptInvalidBaseUrl() {
        VisualizeEndpoint.forBaseUrl("some_invalid_url");
    }

    @Test
    public void shouldAcceptLocalhostAsBaseUrl() {
        VisualizeEndpoint.forBaseUrl("http://localhost:554/my-server");
    }

    @Test
    public void shouldCreateOptimizedVersion() {
        VisualizeEndpoint endpoint = VisualizeEndpoint.forBaseUrl(MOCK_URL).setOptimized(true).build();
        String url = endpoint.createUri();
        assertThat(url, is(OPTIMIZED));
    }

    @Test
    public void shouldCreateOptimizedVersionUsingSyntaticSugar() {
        VisualizeEndpoint endpoint = VisualizeEndpoint.forBaseUrl(MOCK_URL).optimized().build();
        String url = endpoint.createUri();
        assertThat(url, is(OPTIMIZED));
    }

    @Test
    public void shouldCreateNotOptimizedVersion() {
        VisualizeEndpoint endpoint = VisualizeEndpoint.forBaseUrl(MOCK_URL).setOptimized(false).build();
        String url = endpoint.createUri();
        assertThat(url, is(NOT_OPTIMIZED));
    }

    @Test
    public void shouldCreateOptimizedVersionByDefault() {
        VisualizeEndpoint endpoint = VisualizeEndpoint.forBaseUrl(MOCK_URL).build();
        String url = endpoint.createUri();
        assertThat(url, is(NOT_OPTIMIZED));
    }

    @Test
    public void shouldCreateWithShowControlsOption() {
        VisualizeEndpoint endpoint = VisualizeEndpoint.forBaseUrl(MOCK_URL).showControls().build();
        String url = endpoint.createUri();
        assertThat(url, is(WITH_CONTROLS));
    }
}
