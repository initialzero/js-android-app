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

package com.jaspersoft.android.jaspermobile.ui.model.visualize;

import android.webkit.WebView;

import com.jaspersoft.android.jaspermobile.domain.AppCredentials;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.spy;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 21)
public class SimpleVisualizeComponentTest {

    @Mock
    VisualizeEvents mVisualizeEvents;

    private SimpleVisualizeComponent mSimpleVisualizeComponent;
    private WebView webView;

    @Before
    public void setUp() throws Exception {
        webView = spy(new WebView(RuntimeEnvironment.application));
        mSimpleVisualizeComponent = new SimpleVisualizeComponent(webView, mVisualizeEvents);
    }

    @Test
    public void testRun() throws Exception {
        AppCredentials credentials = AppCredentials.builder()
                .setOrganization("org")
                .setPassword("1234")
                .setUsername("user")
                .create();
        VisualizeExecOptions options = new VisualizeExecOptions.Builder()
                .setUri("/my/uri").setParams("{}")
                .setAppCredentials(credentials)
                .setDiagonal(10)
                .build();
        mSimpleVisualizeComponent.run(options);
        verify(webView).loadUrl("javascript:MobileReport.configure({ \"auth\": {\"username\": \"user\",\"password\": \"1234\",\"organization\": \"org\"}, \"diagonal\": 10.0 }).run({\"uri\": \"/my/uri\",\"params\": {}})");
    }

    @Test
    public void testLoadPage() throws Exception {
        mSimpleVisualizeComponent.loadPage("1");
        verify(webView).loadUrl("javascript:MobileReport.selectPage(1)");
    }

    @Test
    public void testUpdate() throws Exception {
        mSimpleVisualizeComponent.update("{}");
        verify(webView).loadUrl("javascript:MobileReport.applyReportParams({})");
    }

    @Test
    public void testRefresh() throws Exception {
        mSimpleVisualizeComponent.refresh();
        verify(webView).loadUrl("javascript:MobileReport.refresh()");
    }
}