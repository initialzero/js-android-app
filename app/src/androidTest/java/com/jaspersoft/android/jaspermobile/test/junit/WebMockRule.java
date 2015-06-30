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

package com.jaspersoft.android.jaspermobile.test.junit;

import android.app.Application;
import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;

import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.jaspersoft.android.jaspermobile.test.utils.CommonTestModule;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import org.junit.Before;
import org.junit.rules.ExternalResource;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import roboguice.RoboGuice;
import timber.log.Timber;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class WebMockRule extends ExternalResource {
    private Instrumentation instrumentation;
    private MockWebServer server;
    private String endpoint;

    private Logger logger = Logger.getLogger(WebMockRule.class.getName());

    @Before
    public void before() throws Throwable {
        super.before();

        // Create a MockWebServer. These are lean enough that you can create a new
        // instance for every unit test.
        server = new MockWebServer();
        try {
            server.play();
        } catch (IOException e) {
            logger.log(Level.INFO, "Failed to start MockWebServer");
            throw new RuntimeException(e);
        }

        Application application = (Application) fetchInstrumentation().getTargetContext().getApplicationContext();
        RoboGuice.overrideApplicationInjector(application, new MyTestModule(server.getPort()));
    }

    @Override
    protected void after() {
        super.after();
        try {
            server.shutdown();
        } catch (IOException e) {
            logger.log(Level.INFO, "Failed to shutdown MockWebServer", e);
        }
        RoboGuice.Util.reset();
    }

    public MockWebServer get() {
        return server;
    }

    private Instrumentation fetchInstrumentation() {
        Instrumentation result = instrumentation;
        return result != null ? result
                : (instrumentation = InstrumentationRegistry.getInstrumentation());
    }

    /**
     * Get the {@link Instrumentation} instance for this test.
     */
    public final Instrumentation instrumentation() {
        return instrumentation;
    }

    public String getEndpoint() {
        return endpoint;
    }

    private class MyTestModule extends CommonTestModule {
        private final int mPort;

        public MyTestModule(int port) {
            mPort = port;
        }

        @Override
        protected void configure() {
            commonConfigurations();

            bind(JsRestClient.class).in(Singleton.class);
            Timber.plant(new Timber.DebugTree());
            endpoint = "http://localhost:" + mPort;

            bindConstant().annotatedWith(Names.named("DEMO_ENDPOINT")).to(endpoint);
        }
    }
}
