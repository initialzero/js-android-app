/*
* Copyright (C) 2012 Jaspersoft Corporation. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.test.acceptance.library;

import android.widget.GridView;
import android.widget.ListView;

import com.google.android.apps.common.testing.testrunner.ActivityLifecycleMonitorRegistry;
import com.google.android.apps.common.testing.ui.espresso.Espresso;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.repository.LibraryActivity_;
import com.jaspersoft.android.jaspermobile.activities.repository.support.RepositoryPref_;
import com.jaspersoft.android.jaspermobile.activities.repository.support.ViewType;
import com.jaspersoft.android.jaspermobile.test.ProtoActivityInstrumentation;
import com.jaspersoft.android.jaspermobile.test.utils.TestResources;
import com.jaspersoft.android.jaspermobile.util.JsXmlSpiceServiceWrapper;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.JsServerProfile;
import com.jaspersoft.android.sdk.client.async.JsXmlSpiceService;
import com.jaspersoft.android.sdk.client.async.request.cacheable.GetResourceLookupsRequest;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookupsList;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.request.SpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isAssignableFrom;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.hasTotalCount;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.swipeUp;
import static org.mockito.Mockito.when;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class LibraryPage2Test extends ProtoActivityInstrumentation<LibraryActivity_> {
    private static final int LIMIT = 40;

    @Mock
    JsServerProfile mockServerProfile;
    @Mock
    JsRestClient mockRestClient;
    @Mock
    SpiceManager mockSpiceService;
    @Mock
    JsXmlSpiceServiceWrapper mockJsXmlSpiceServiceWrapper;

    final MockedSpiceManager mMockedSpiceManager = new MockedSpiceManager(JsXmlSpiceService.class);
    private RepositoryPref_ repositoryPref;
    private ResourceLookupsList firstLookUp, secondLookUp;
    private ResourceFragmentInjector injector;

    public LibraryPage2Test() {
        super(LibraryActivity_.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);

        repositoryPref = new RepositoryPref_(getInstrumentation().getContext());
        firstLookUp = TestResources.get().fromXML(ResourceLookupsList.class, "library_0_40");
        secondLookUp = TestResources.get().fromXML(ResourceLookupsList.class, "library_40_40");

        firstLookUp.setTotalCount(
                firstLookUp.getResourceLookups().size() + secondLookUp.getResourceLookups().size()
        );

        registerTestModule(new TestModule());
        when(mockRestClient.getServerProfile()).thenReturn(mockServerProfile);
        when(mockServerProfile.getUsernameWithOrgId()).thenReturn(USERNAME);
        when(mockServerProfile.getPassword()).thenReturn(PASSWORD);
        when(mockJsXmlSpiceServiceWrapper.getSpiceManager()).thenReturn(mMockedSpiceManager);

        ResourcesFragmentIdlingResource resourceFragmentInjector =
                new ResourcesFragmentIdlingResource();
        Espresso.registerIdlingResources(resourceFragmentInjector);
        injector = new ResourceFragmentInjector(resourceFragmentInjector);
        ActivityLifecycleMonitorRegistry.getInstance()
                .addLifecycleCallback(injector);
    }

    @Override
    protected void tearDown() throws Exception {
        ActivityLifecycleMonitorRegistry.getInstance()
                .removeLifecycleCallback(injector);
        repositoryPref = null;
        super.tearDown();
    }

    public void testInitialLoadOfGrid() {
        forcePreview(ViewType.GRID);
        startActivityUnderTest();
        onView(withId(android.R.id.list)).check(matches(isAssignableFrom(GridView.class)));
    }

    public void testSwitcher() throws InterruptedException {
        forcePreview(ViewType.LIST);
        startActivityUnderTest();

        onView(withId(android.R.id.list)).check(matches(isAssignableFrom(ListView.class)));
        rotate();
        onView(withId(android.R.id.list)).check(matches(isAssignableFrom(ListView.class)));

        onView(withId(R.id.switchLayout)).perform(click());

        onView(withId(android.R.id.list)).check(matches(isAssignableFrom(GridView.class)));
        rotate();
        onView(withId(android.R.id.list)).check(matches(isAssignableFrom(GridView.class)));
    }

    public void testInitialLoadOfList() {
        forcePreview(ViewType.LIST);
        startActivityUnderTest();
        onView(withId(android.R.id.list)).check(matches(isAssignableFrom(ListView.class)));
    }

    public void testScrollTo() throws InterruptedException {
        startActivityUnderTest();
        for (int i = 0; i < 3; i++) {
            onView(withId(android.R.id.list)).perform(swipeUp());
        }
        onView(withId(android.R.id.list)).check(hasTotalCount(firstLookUp.getTotalCount()));
    }


    private void forcePreview(ViewType viewType) {
        repositoryPref.viewType().put(viewType.toString());
    }

    @Override
    public String getPageName() {
        return "library";
    }

    public class MockedSpiceManager extends SpiceManager {
        public MockedSpiceManager(Class<? extends SpiceService> spiceServiceClass) {
            super(spiceServiceClass);
        }

        public <T> void execute(final SpiceRequest<T> request, final Object requestCacheKey,
                                final long cacheExpiryDuration, final RequestListener<T> requestListener) {
            if (request instanceof GetResourceLookupsRequest) {
                int offset = ((GetResourceLookupsRequest) request).getSearchCriteria().getOffset();
                if (offset == LIMIT) {
                    requestListener.onRequestSuccess((T) secondLookUp);
                } else {
                    requestListener.onRequestSuccess((T) firstLookUp);
                }
            }
        }
    }

    public class TestModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(JsRestClient.class).toInstance(mockRestClient);
            bind(JsXmlSpiceServiceWrapper.class).toInstance(mockJsXmlSpiceServiceWrapper);
            bindConstant().annotatedWith(Names.named("LIMIT")).to(LIMIT);
            bindConstant().annotatedWith(Names.named("THRESHOLD")).to(5);
        }
    }
}
