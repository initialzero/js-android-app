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

package com.jaspersoft.android.jaspermobile.test.junit;

import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.google.inject.name.Names;
import com.google.inject.util.Modules;
import com.jaspersoft.android.jaspermobile.test.utils.CommonTestModule;
import com.jaspersoft.android.retrofit.sdk.rest.JsRestClient2;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import org.junit.Before;
import org.junit.rules.ExternalResource;

import java.io.IOException;

import retrofit.ErrorHandler;
import retrofit.RetrofitError;
import roboguice.RoboGuice;
import timber.log.Timber;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class WebMockRule extends ExternalResource {
    private Instrumentation instrumentation;
    private MockWebServer server;

    @Before
    public void before() throws Throwable {
        super.before();
        // Create a MockWebServer. These are lean enough that you can create a new
        // instance for every unit test.
        server = new MockWebServer();
        try {
            server.play();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Application application = (Application) fetchInstrumentation().getTargetContext().getApplicationContext();
        RoboGuice.setBaseApplicationInjector(application,
                RoboGuice.DEFAULT_STAGE,
                Modules.override(RoboGuice.newDefaultRoboModule(application))
                        .with(new MyTestModule(application, server.getPort())));
    }

    @Override
    protected void after() {
        super.after();
        RoboGuice.util.reset();
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

    private static class MyTestModule extends CommonTestModule {
        private final int mPort;
        private final Context mContext;

        public MyTestModule(Context context, int port) {
            mPort = port;
            mContext = context;
        }

        @Override
        protected void semanticConfigure() {
            Timber.plant(new Timber.DebugTree());
            JsRestClient2 restClient = JsRestClient2.configure(mContext)
                    .setErrorHandler(new ErrorHandler() {
                        @Override
                        public Throwable handleError(RetrofitError cause) {
                            Timber.tag("WEB_MOCK_RULE");
                            Timber.e("Rest client received exception", cause);
                            return cause;
                        }
                    })
                    .setEndpoint("http://localhost:" + mPort)
                    .build();
            bind(JsRestClient2.class)
                    .annotatedWith(Names.named("JASPER_DEMO"))
                    .toInstance(restClient);
        }
    }
}
