/*
 * Copyright © 2014 TIBCO Software, Inc. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.test.acceptance;

import android.widget.GridView;
import android.widget.ListView;

import com.google.android.apps.common.testing.ui.espresso.NoMatchingViewException;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.repository.RepositoryActivity_;
import com.jaspersoft.android.jaspermobile.activities.repository.support.RepositoryPref_;
import com.jaspersoft.android.jaspermobile.activities.repository.support.ViewType;
import com.jaspersoft.android.jaspermobile.test.ProtoActivityInstrumentation;
import com.jaspersoft.android.jaspermobile.test.acceptance.hacked.HackedJsRestClient;
import com.jaspersoft.android.jaspermobile.test.acceptance.hacked.JasperRobolectric;
import com.jaspersoft.android.jaspermobile.test.utils.ApiMatcher;
import com.jaspersoft.android.jaspermobile.test.utils.CommonTestModule;
import com.jaspersoft.android.jaspermobile.test.utils.HttpResponseUtil;
import com.jaspersoft.android.jaspermobile.test.utils.SmartMockedSpiceManager;
import com.jaspersoft.android.jaspermobile.test.utils.TestResources;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.oxm.report.FolderDataResponse;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookupsList;
import com.jaspersoft.android.sdk.client.oxm.server.ServerInfo;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.pressBack;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.pressImeActionButton;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.typeText;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isAssignableFrom;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.onOverflowView;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.LongListMatchers.withAdaptedData;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.LongListMatchers.withItemContent;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class RepositoryPageTest extends ProtoActivityInstrumentation<RepositoryActivity_> {

    private static final String REPORTS_QUERY = "Reports";

    private RepositoryPref_ repositoryPref;
    private SmartMockedSpiceManager mMockedSpiceManager;

    private ResourceLookupsList rootRepositories;
    private ResourceLookupsList levelRepositories;
    private FolderDataResponse rootFolder;
    private ServerInfo serverInfo;

    public RepositoryPageTest() {
        super(RepositoryActivity_.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mMockedSpiceManager = SmartMockedSpiceManager.getInstance();
        repositoryPref = new RepositoryPref_(getInstrumentation().getContext());

        rootRepositories = TestResources.get().fromXML(ResourceLookupsList.class, "root_repositories");
        levelRepositories = TestResources.get().fromXML(ResourceLookupsList.class, "level_repositories");
        rootFolder = TestResources.get().fromXML(FolderDataResponse.class, "root_folder");
        serverInfo = TestResources.get().fromXML(ServerInfo.class, "server_info");

        registerTestModule(new TestModule());
        setDefaultCurrentProfile();

        JasperRobolectric.addHttpResponseRule(
                ApiMatcher.SERVER_INFO,
                HttpResponseUtil.get().xmlType("server_info"));
        JasperRobolectric.addHttpResponseRule(
                ApiMatcher.SERVER_INFO,
                HttpResponseUtil.get().xmlType("root_folder"));
        JasperRobolectric.addHttpResponseRule(
                ApiMatcher.SERVER_INFO,
                HttpResponseUtil.get().xmlType("root_repositories"));
    }

    @Override
    protected void tearDown() throws Exception {
        unregisterTestModule();
        getActivity().finish();
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

        mMockedSpiceManager.addNetworkResponse(serverInfo);
        mMockedSpiceManager.addCachedResponse(levelRepositories);
        rotate();
        onView(withId(android.R.id.list)).check(matches(isAssignableFrom(ListView.class)));

        mMockedSpiceManager.addNetworkResponse(serverInfo);
        mMockedSpiceManager.addCachedResponse(levelRepositories);
        onView(withId(R.id.switchLayout)).perform(click());
        onView(withId(android.R.id.list)).check(matches(isAssignableFrom(GridView.class)));

        mMockedSpiceManager.addNetworkResponse(serverInfo);
        mMockedSpiceManager.addCachedResponse(levelRepositories);
        rotate();
        onView(withId(android.R.id.list)).check(matches(isAssignableFrom(GridView.class)));
    }

    public void testInitialLoadOfList() {
        forcePreview(ViewType.LIST);
        startActivityUnderTest();
        onView(withId(android.R.id.list)).check(matches(isAssignableFrom(ListView.class)));
    }

    public void testRepoClickCase() throws InterruptedException {
        forcePreview(ViewType.LIST);
        startActivityUnderTest();

        String firstRootRepoLabel = rootRepositories.getResourceLookups().get(0).getLabel();

        mMockedSpiceManager.addNetworkResponse(serverInfo);
        mMockedSpiceManager.addCachedResponse(rootRepositories);
        onData(is(instanceOf(ResourceLookup.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).perform(click());
        onView(withId(getActionBarTitleId())).check(matches(withText(firstRootRepoLabel)));

        mMockedSpiceManager.addNetworkResponse(serverInfo);
        mMockedSpiceManager.addCachedResponse(rootRepositories);
        pressBack();
        onView(withId(getActionBarTitleId())).check(matches(withText(R.string.h_repository_label)));
    }

    public void testRepoBackstackPersistance() throws InterruptedException {
        forcePreview(ViewType.LIST);
        startActivityUnderTest();

        String rootLevelRepoLabel = rootRepositories.getResourceLookups().get(0).getLabel();
        String firstLevelRepoLabel = levelRepositories.getResourceLookups().get(0).getLabel();

        mMockedSpiceManager.addNetworkResponse(serverInfo);
        mMockedSpiceManager.addCachedResponse(levelRepositories);
        onData(is(instanceOf(ResourceLookup.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0).perform(click());
        onView(withId(android.R.id.list)).check(matches(not(withAdaptedData(withItemContent(firstLevelRepoLabel)))));

        mMockedSpiceManager.addNetworkResponse(serverInfo);
        mMockedSpiceManager.addCachedResponse(levelRepositories);
        rotate();
        onView(withId(android.R.id.list)).check(matches(not(withAdaptedData(withItemContent(firstLevelRepoLabel)))));

        mMockedSpiceManager.addNetworkResponse(serverInfo);
        mMockedSpiceManager.addCachedResponse(rootRepositories);
        pressBack();
        onView(withId(android.R.id.list)).check(matches(not(withAdaptedData(withItemContent(rootLevelRepoLabel)))));

        mMockedSpiceManager.addNetworkResponse(serverInfo);
        mMockedSpiceManager.addCachedResponse(rootRepositories);
        rotate();
        onView(withId(android.R.id.list)).check(matches(not(withAdaptedData(withItemContent(rootLevelRepoLabel)))));
    }

    public void testSearchInRepository() {
        startActivityUnderTest();

        try {
            onView(withId(R.id.search)).perform(click());
        } catch (NoMatchingViewException ex) {
            openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
            onOverflowView(getActivity(), withText(android.R.string.search_go)).perform(click());
        }
        onView(withId(getSearcFieldId())).perform(typeText(REPORTS_QUERY));

        mMockedSpiceManager.addNetworkResponse(serverInfo);
        mMockedSpiceManager.addCachedResponse(levelRepositories);
        onView(withId(getSearcFieldId())).perform(pressImeActionButton());

        onView(withText(getActivity().getString(R.string.search_result_format, REPORTS_QUERY)))
                .check(matches(isDisplayed()));
    }

    private void forcePreview(ViewType viewType) {
        repositoryPref.viewType().put(viewType.toString());
    }

    public class TestModule extends CommonTestModule {
        @Override
        protected void semanticConfigure() {
            bind(JsRestClient.class).toInstance(HackedJsRestClient.get());
        }
    }
}
